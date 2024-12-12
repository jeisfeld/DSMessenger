package de.jeisfeld.coachat.main.message;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.databinding.FragmentMessageBinding;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Contact.AiPolicy;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.entity.Message;
import de.jeisfeld.coachat.entity.ReplyPolicy;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.MainActivity;
import de.jeisfeld.coachat.main.account.AccountDialogUtil;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.EditConversationDialogFragment;
import de.jeisfeld.coachat.message.AdminMessageDetails.AdminType;
import de.jeisfeld.coachat.message.MessageDetails.MessagePriority;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;
import de.jeisfeld.coachat.util.DateUtil;
import de.jeisfeld.coachat.util.DialogUtil;
import io.noties.markwon.Markwon;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;

/**
 * Fragment for sending messages.
 */
public class MessageFragment extends Fragment implements EditConversationParentFragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.coachat.main.message.MessageFragment";
	/**
	 * An instance for markup handling.
	 */
	private Markwon markwon;
	/**
	 * The view binding.
	 */
	private FragmentMessageBinding binding;
	/**
	 * The contact for this conversation.
	 */
	private Contact contact;
	/**
	 * The list of displayed messages.
	 */
	private final List<Message> messageList = new ArrayList<>();
	/**
	 * The conversation.
	 */
	private Conversation conversation;
	/**
	 * Messages to be deleted for retry.
	 */
	private final List<Message> deleteMessages = new ArrayList<>();
	/**
	 * The array adapter for the list of displayed messages.
	 */
	private ArrayAdapter<Message> arrayAdapter;
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;
	/**
	 * The TTS engine used for speaking messages.
	 */
	private TextToSpeech textToSpeech = null;

	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				ActionType actionType = (ActionType) intent.getSerializableExtra("actionType");
				Activity activity = getActivity();
				switch (actionType) {
				case MESSAGE_RECEIVED:
				case MESSAGE_ACKNOWLEDGED:
				case MESSAGE_SENT:
				case MESSAGE_DELETED:
				case TEXT_RESPONSE:
					Conversation receivedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (receivedConversation != null && receivedConversation.getConversationId().equals(conversation.getConversationId())
							&& activity != null) {
						conversation = receivedConversation;
						refreshMessageList();
						if (conversation.getPreparedMessage() != null && !conversation.getPreparedMessage().isEmpty() && contact.isSlave()) {
							String oldEditText = binding.editTextMessageText.getText().toString();
							binding.editTextMessageText.setText(oldEditText.trim().isEmpty() ? conversation.getPreparedMessage() :
									oldEditText + "\n" + conversation.getPreparedMessage());
						}
					}
					break;
				case CONVERSATION_EDITED:
					Conversation editedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (editedConversation != null && editedConversation.getConversationId().equals(conversation.getConversationId())) {
						binding.textSubject.setText(getString(R.string.text_subject, editedConversation.getSubject()));
					}
					break;
				case CONVERSATION_DELETED:
					Conversation deletedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (deletedConversation != null && deletedConversation.getConversationId().equals(conversation.getConversationId())
							&& activity != null) {
						NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
						navController.popBackStack();
					}
					break;
				case DEVICE_LOGGED_OUT:
					binding.listViewMessages.setVisibility(View.GONE);
					binding.buttonSend.setVisibility(View.GONE);
					if (activity != null) {
						NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
						navController.popBackStack();
						navController.navigate(R.id.nav_account);
						((MainActivity) activity).updateNavigationDrawer();
					}
					break;
				case PONG:
					Contact contactPont = (Contact) intent.getSerializableExtra("contact");
					if (contact.getRelationId() == contactPont.getRelationId()) {
						binding.imageViewConnectionStatus.setImageResource(R.drawable.ic_icon_connected);
					}
					break;
				default:
					break;
				}
			}
		}
	};

	/**
	 * The Activity result launcher for audio permission.
	 */
	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new RequestPermission(), isGranted -> {
			});

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context      The context.
	 * @param actionType   The action type.
	 * @param messageId    The messageId.
	 * @param contact      The contact.
	 * @param conversation The conversation.
	 * @param message      The message.
	 * @param parameters   The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final Contact contact,
									 final Conversation conversation, final Message message, final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		bundle.putSerializable("messageId", messageId);
		if (contact != null) {
			bundle.putSerializable("contact", contact);
		}
		if (message != null) {
			bundle.putSerializable("message", message);
		}
		if (conversation != null) {
			bundle.putSerializable("conversation", conversation);
		}
		int i = 0;
		while (i < parameters.length - 1) {
			String key = parameters[i++];
			String value = parameters[i++];
			bundle.putString(key, value);
		}
		intent.putExtras(bundle);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * Remove trailing parameters such as [3ok] from messageText.
	 *
	 * @param messageText The message of the text.
	 * @return The messageText where trailing parameters have been removed if applicable.
	 */
	private static String removeTrailingParametersFromMessage(String messageText) {
		if (messageText.endsWith("]")) {
			Pattern pattern = Pattern.compile("^(.*)\\s*\\[([a-zA-Z0-9@]+)]$", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(messageText);
			if (matcher.find()) {
				return matcher.group(1);
			}
			else {
				return messageText;
			}
		}
		else {
			return messageText;
		}
	}

	/**
	 * Set the listener for the "Record Voice" button
	 */
	private void setRecordVoiceButtonListener() {
		binding.imageButtonRecordVoice.setOnClickListener(new OnClickListener() {
			/**
			 * The speech recognizer.
			 */
			private final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
			/**
			 * The old text.
			 */
			private String oldText = "";
			/**
			 * Flag indicating if recording is happening.
			 */
			private boolean isRecording = false;

			@Override
			public void onClick(View v) {
				if (isRecording) {
					isRecording = false;
					speechRecognizer.stopListening();
				}
				else {
					if (ContextCompat.checkSelfPermission(MessageFragment.this.getContext(), permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
						oldText = binding.editTextMessageText.getText().toString();

						Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
						//speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");

						speechRecognizer.setRecognitionListener(new RecognitionListener() {
							@Override
							public void onReadyForSpeech(Bundle params) {
								getActivity().runOnUiThread(() -> {
									binding.imageButtonRecordVoice.setImageResource(R.drawable.ic_icon_microphone_red);
									binding.editTextMessageText.setEnabled(false);
								});
							}

							@Override
							public void onBeginningOfSpeech() {
							}

							@Override
							public void onRmsChanged(float rmsdB) {
							}

							@Override
							public void onBufferReceived(byte[] buffer) {
							}

							@Override
							public void onEndOfSpeech() {
							}

							@Override
							public void onError(int error) {
								getActivity().runOnUiThread(() -> {
									binding.imageButtonRecordVoice.setImageResource(R.drawable.ic_icon_microphone);
									binding.editTextMessageText.setEnabled(true);
								});
							}

							@Override
							public void onResults(Bundle results) {
								isRecording = false;
								ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
								if (matches != null && !matches.isEmpty()) {
									String foundText = matches.get(0);
									String recognizedText;
									if (foundText != null && !foundText.isEmpty()) {
										recognizedText = foundText.substring(0, 1).toUpperCase() + foundText.substring(1);
									}
									else {
										recognizedText = "";
									}
									getActivity().runOnUiThread(() -> {
										binding.imageButtonRecordVoice.setImageResource(R.drawable.ic_icon_microphone);
										binding.editTextMessageText.setEnabled(true);
										if ("Löschen".equals(recognizedText)) {
											binding.editTextMessageText.setText("");
										}
										else {
											if (oldText.trim().isEmpty()) {
												binding.editTextMessageText.setText(recognizedText);
											}
											else {
												binding.editTextMessageText.setText(oldText.trim() + "\n" + recognizedText);
											}
										}
									});
								}
							}

							@Override
							public void onPartialResults(Bundle partialResults) {
							}

							@Override
							public void onEvent(int eventType, Bundle params) {
							}
						});

						speechRecognizer.startListening(speechRecognizerIntent);
						isRecording = true;
					}
					else {
						requestPermissionLauncher.launch(permission.RECORD_AUDIO);
					}
				}
			}
		});
	}

	/**
	 * Set button visibility depending on conversation flags.
	 */
	private void setButtonVisibility() {
		boolean enableAcknowledgement = !contact.isSlave() && conversation.getConversationFlags().isExpectingAcknowledgement();
		boolean enableSend = contact.isSlave() || conversation.getConversationFlags().isExpectingResponse()
				|| conversation.getConversationFlags().getReplyPolicy() == ReplyPolicy.UNLIMITED;
		boolean enablePrioSend = (contact.isSlave() || conversation.getConversationFlags().getReplyPolicy() == ReplyPolicy.UNLIMITED)
				&& contact.getAiPolicy() != AiPolicy.AUTOMATIC_NOMESSAGE;
		boolean enableEdit = contact.getMyPermissions().isManageConversations();
		binding.buttonAcknowledge.setVisibility(enableAcknowledgement ? View.VISIBLE : View.GONE);
		binding.buttonSend.setVisibility(enableSend ? View.VISIBLE : enableAcknowledgement ? View.INVISIBLE : View.GONE);
		binding.buttonSendWithPriority.setVisibility(enablePrioSend ? View.VISIBLE : View.INVISIBLE);
		binding.layoutTextInput.setVisibility(enableSend ? View.VISIBLE : View.GONE);
		binding.imageButtonRecordVoice.setVisibility(enableSend && isSpeechRecognitionActivityAvailable() ? View.VISIBLE : View.GONE);
		binding.buttonEdit.setVisibility(enableEdit && conversation.isStored() ? View.VISIBLE : View.GONE);
		binding.imageButtonRefreshPreparedMessage.setVisibility(
				enableSend && contact.isSlave() && (contact.getAiPolicy() == AiPolicy.MANUAL || contact.getAiPolicy() == AiPolicy.MANUAL_LIVE)
						? View.VISIBLE : View.GONE);
	}

	/**
	 * Refresh the message list.
	 */
	private void refreshMessageList() {
		new Thread(() -> {
			List<Message> newMessageList =
					Application.getAppDatabase().getMessageDao().getMessagesByConversationId(conversation.getConversationId());
			deleteMessages.clear();
			synchronized (messageList) {
				messageList.clear();
				messageList.addAll(newMessageList);
			}
			Activity activity = getActivity();
			if (activity != null) {
				activity.runOnUiThread(() -> {
					setButtonVisibility();
					arrayAdapter.notifyDataSetChanged();
				});
			}
		}).start();
	}

	@Override
	public final void onResume() {
		super.onResume();
		if (MainActivity.isDsUser()) {
			pingContact();
		}
	}

	@Override
	public final void onStop() {
		super.onStop();
		if (conversation != null && contact != null && contact.isSlave()) {
			conversation.setPreparedMessage(binding.editTextMessageText.getText().toString());
			conversation.update();
			new HttpSender(getActivity()).sendMessage("db/conversation/editconversation.php", contact, UUID.randomUUID(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.MESSAGE_PREPARED.name(),
					"conversationId", conversation.getConversationId().toString(),
					"preparedMessage", binding.editTextMessageText.getText().toString());
		}
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			textToSpeech = null;
		}
	}

	@Override
	public final void onAttach(@NonNull final Context context) {
		super.onAttach(context);
		broadcastManager = LocalBroadcastManager.getInstance(context);
		IntentFilter actionReceiver = new IntentFilter();
		actionReceiver.addAction(BROADCAST_ACTION);
		broadcastManager.registerReceiver(localBroadcastReceiver, actionReceiver);
	}

	@Override
	public final void onDetach() {
		super.onDetach();
		broadcastManager.unregisterReceiver(localBroadcastReceiver);
	}

	/**
	 * Ping the current selected contact.
	 */
	private void pingContact() {
		binding.imageViewConnectionStatus.setImageResource(R.drawable.ic_icon_connection_uncertain);
		UUID messageId = UUID.randomUUID();

		new HttpSender(getContext()).sendMessage(contact, messageId, (response, responseData) -> {
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							if (responseData == null || !responseData.isSuccess()) {
								binding.imageViewConnectionStatus.setImageResource(R.drawable.ic_icon_connection_gone);
							}
						});
					}
				},
				"messageType", MessageType.ADMIN.name(), "adminType", AdminType.PING.name());
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentMessageBinding.inflate(inflater, container, false);
		markwon = Markwon.builder(getContext())
				.usePlugin(SoftBreakAddsNewLinePlugin.create())
				.build();

		binding.buttonSend.setOnClickListener(v -> sendMessage(MessagePriority.NORMAL));
		binding.buttonSendWithPriority.setOnClickListener(v -> sendMessage(MessagePriority.HIGH));
		binding.buttonAcknowledge.setOnClickListener(v -> {
			Activity activity = getActivity();
			if (activity != null) {
				conversation.updateWithAcknowledgement();
				setButtonVisibility();
				Application.getAppDatabase().getMessageDao().acknowledgeMessages(
						messageList.stream().filter(msg -> !msg.isOwn()).map(msg -> msg.getMessageId().toString()).toArray(String[]::new));
				refreshMessageList();

				new HttpSender(activity).sendMessage("db/conversation/updatemessagestatus.php",
						contact, messageList.get(messageList.size() - 1).getMessageId(), null,
						"messageType", MessageType.ADMIN.name(), "adminType", AdminType.MESSAGE_ACKNOWLEDGED.name(),
						"conversationId", conversation.getConversationId().toString(), "messageIds",
						messageList.stream().filter(msg -> !msg.isOwn())
								.map(message -> message.getMessageId().toString()).collect(Collectors.joining(",")));
			}
		});
		binding.buttonEdit.setOnClickListener(v -> AccountDialogUtil.displayEditConversationDialog(this, conversation, contact));

		binding.imageViewRefreshMessages.setOnClickListener(
				v -> new HttpSender(getContext()).sendMessage("db/conversation/querymessages.php", contact, null, (response, responseData) -> {
					if (responseData.isSuccess()) {
						List<Message> messages = (List<Message>) responseData.getData().get("messages");
						if (messages == null) {
							return;
						}
						Application.getAppDatabase().getMessageDao().deleteMessagesByConversationId(conversation.getConversationId().toString());
						Application.getAppDatabase().getMessageDao().insert(messages);
						refreshMessageList();
					}
					else {
						Log.e(Application.TAG, "Failed to retrieve message data: " + responseData.getErrorMessage());
					}
				}, "conversationId", conversation.getConversationId().toString()));

		binding.imageButtonRefreshPreparedMessage.setOnClickListener(v -> {
			binding.buttonSend.setEnabled(false);
			binding.buttonSendWithPriority.setEnabled(false);
			binding.editTextMessageText.setEnabled(false);
			new HttpSender(getContext()).sendMessage("db/conversation/recreatepreparedmessage.php", contact, null,
					(response, responseData) -> getActivity().runOnUiThread(() -> {
						binding.buttonSend.setEnabled(true);
						binding.buttonSendWithPriority.setEnabled(true);
						binding.editTextMessageText.setEnabled(true);
						if (responseData.isSuccess()) {
							binding.editTextMessageText.setText((String) responseData.getData().get("preparedMessage"));
						}
						else {
							Log.e(Application.TAG, "Failed to retrieve message data: " + responseData.getErrorMessage());
						}
					}), "conversationId", conversation.getConversationId().toString());
		});

		setRecordVoiceButtonListener();

		assert getArguments() != null;
		contact = (Contact) getArguments().getSerializable("contact");
		conversation = (Conversation) getArguments().getSerializable("conversation");
		binding.textSendMessage.setText(getString(R.string.text_send_message_to, contact.getName()));
		binding.textSubject.setText(getString(R.string.text_subject, conversation.getSubject()));
		if (contact.isSlave()) {
			binding.editTextMessageText.setText(conversation.getPreparedMessage());
		}
		binding.imageViewConnectionStatus.setVisibility(MainActivity.isDsUser() ? View.VISIBLE : View.GONE);

		arrayAdapter = new ArrayAdapter<Message>(requireContext(), R.layout.list_view_message, R.id.textViewMessage, messageList) {
			@NonNull
			@Override
			public View getView(final int position, final @Nullable View convertView, final @NonNull ViewGroup parent) {
				final View view = super.getView(position, convertView, parent);

				Message message = messageList.get(position);
				TextView textViewMessage = view.findViewById(R.id.textViewMessage);
				markwon.setMarkdown(textViewMessage, message.getMessageText());

				if (message.isOwn()) {
					view.findViewById(R.id.spaceRight).setVisibility(View.GONE);
					view.findViewById(R.id.spaceLeft).setVisibility(View.VISIBLE);
					textViewMessage.setBackgroundResource(R.drawable.background_message_own);
				}
				else {
					view.findViewById(R.id.spaceRight).setVisibility(View.VISIBLE);
					view.findViewById(R.id.spaceLeft).setVisibility(View.GONE);
					textViewMessage.setBackgroundResource(R.drawable.background_message_contact);
				}

				((TextView) view.findViewById(R.id.textViewMessageTime)).setText(DateUtil.formatTimestamp(message.getTimestamp(), false));

				ImageView imageViewMessageStatus = view.findViewById(R.id.imageViewMessageStatus);
				switch (message.getStatus()) {
				case MESSAGE_SENT:
					imageViewMessageStatus.setImageResource(R.drawable.ic_icon_message_sent);
					break;
				case MESSAGE_RECEIVED:
					imageViewMessageStatus.setImageResource(R.drawable.ic_icon_message_received);
					break;
				case MESSAGE_ACKNOWLEDGED:
					imageViewMessageStatus.setImageResource(R.drawable.ic_icon_message_acknowledged);
					break;
				default:
					imageViewMessageStatus.setImageResource(0);
					break;
				}

				if (position == messageList.size() - 1 && !contact.isSlave() && deleteMessages.isEmpty() &&
						(contact.getAiPolicy() == AiPolicy.AUTOMATIC || contact.getAiPolicy() == AiPolicy.AUTOMATIC_NOMESSAGE)
						&& messageList.size() >= 2
						&& !messageList.get(messageList.size() - 1).isOwn() && messageList.get(messageList.size() - 2).isOwn()) {
					ImageView buttonRefreshMessage = view.findViewById(R.id.imageButtonRefreshMessage);
					buttonRefreshMessage.setVisibility(View.VISIBLE);
					buttonRefreshMessage.setOnClickListener(v -> {
						buttonRefreshMessage.setVisibility(View.GONE);
						Message lastAiMessage = messageList.get(messageList.size() - 1);
						Message lastOwnMessage = messageList.get(messageList.size() - 2);
						messageList.remove(lastAiMessage);
						messageList.remove(lastOwnMessage);
						deleteMessages.add(lastAiMessage);
						deleteMessages.add(lastOwnMessage);
						arrayAdapter.notifyDataSetChanged();
						binding.listViewMessages.setSelection(messageList.size() - 1);
						binding.listViewMessages.smoothScrollToPosition(messageList.size() - 1);
						binding.editTextMessageText.setText(lastOwnMessage.getMessageText());
					});
				}
				else {
					view.findViewById(R.id.imageButtonRefreshMessage).setVisibility(View.GONE);
				}

				ImageView buttonSpeak = view.findViewById(R.id.imageButtonSpeak);
				ImageView buttonStopSpeaking = view.findViewById(R.id.imageButtonStopSpeaking);
				buttonSpeak.setVisibility(View.VISIBLE);
				buttonStopSpeaking.setVisibility(View.GONE);
				buttonSpeak.setOnClickListener(v -> {
					if (textToSpeech != null) {
						textToSpeech.stop();
						textToSpeech.shutdown();
						textToSpeech = null;
					}
					buttonSpeak.setVisibility(View.GONE);
					buttonStopSpeaking.setVisibility(View.VISIBLE);
					textToSpeech = new TextToSpeech(getActivity(), status -> {
						if (status == TextToSpeech.SUCCESS) {
							textToSpeech.speak(message.getMessageText(), TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
						}
					});
				});
				buttonStopSpeaking.setOnClickListener(v -> {
					if (textToSpeech != null) {
						textToSpeech.stop();
						textToSpeech.shutdown();
						textToSpeech = null;
					}
					buttonSpeak.setVisibility(View.VISIBLE);
					buttonStopSpeaking.setVisibility(View.GONE);
				});

				return view;
			}
		};
		binding.listViewMessages.setAdapter(arrayAdapter);

		refreshMessageList();

		return binding.getRoot();
	}

	/**
	 * Send the message.
	 *
	 * @param priority The message priority.
	 */
	private void sendMessage(final MessagePriority priority) {
		String messageText = binding.editTextMessageText.getText().toString();
		UUID messageId = UUID.randomUUID();
		long timestamp = System.currentTimeMillis();
		binding.buttonSend.setEnabled(false);

		new HttpSender(getContext()).sendMessage("db/conversation/sendmessage.php", contact, messageId, (response, responseData) -> {
					Message message = new Message(removeTrailingParametersFromMessage(binding.editTextMessageText.getText().toString().trim()),
							true, messageId, conversation.getConversationId(), timestamp, MessageStatus.MESSAGE_SENT);
					Application.getAppDatabase().getMessageDao().acknowledgeMessages(
							messageList.stream().filter(msg -> !msg.isOwn()).map(msg -> msg.getMessageId().toString()).toArray(String[]::new));
					refreshMessageList();
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							binding.buttonSend.setEnabled(true);
							if (responseData != null && responseData.isSuccess()) {
								if (message.getMessageText() != null && !message.getMessageText().isEmpty()) {
									conversation.setPreparedMessage("");

									conversation.insertIfNew(message.getMessageText());
									if (!contact.isSlave()) {
										conversation.updateWithResponse();
										setButtonVisibility();
									}
									addMessage(message);
								}
								binding.editTextMessageText.setText("");
							}
							else {
								binding.buttonSend.setEnabled(true);
								DialogUtil.displayToast(getContext(), R.string.toast_error_when_sending, responseData.getMappedErrorMessage(getContext()));
							}
						});
					}
				},
				"messageType", !contact.isSlave() && conversation.getConversationFlags().getReplyPolicy() != ReplyPolicy.UNLIMITED
						? MessageType.TEXT_RESPONSE.name()
						: MessageType.TEXT.name(),
				"messageText", messageText, "priority", priority.name(),
				"conversationId", conversation.getConversationId().toString(), "timestamp", Long.toString(timestamp),
				"conversationFlags", conversation.getConversationFlags().toString(), "messageIds",
				messageList.stream().filter(msg -> !msg.isOwn()).map(message -> message.getMessageId().toString()).collect(Collectors.joining(",")),
				"lastAiMessageId", deleteMessages.size() == 2 ? deleteMessages.get(0).getMessageId().toString() : "",
				"lastOwnMessageId", deleteMessages.size() == 2 ? deleteMessages.get(1).getMessageId().toString() : "");
		deleteMessages.clear();
	}

	/**
	 * Add a message to the displayed list.
	 *
	 * @param message The message to be added.
	 */
	private void addMessage(final Message message) {
		message.store(conversation);
		if (message.getConversationId().equals(conversation.getConversationId())) {
			synchronized (messageList) {
				messageList.add(message);
			}
			arrayAdapter.notifyDataSetChanged();
			binding.listViewMessages.setSelection(messageList.size() - 1);
			binding.listViewMessages.smoothScrollToPosition(messageList.size() - 1);
		}
	}

	/**
	 * Handle the response of edit conversation dialog.
	 *
	 * @param dialog       The dialog.
	 * @param contact      The contact.
	 * @param conversation The new conversation data.
	 */
	@Override
	public void handleEditConversationDialogResponse(final EditConversationDialogFragment dialog, final Contact contact,
													 final Conversation conversation) {
		conversation.update();
		this.conversation = conversation;
		binding.textSubject.setText(conversation.getSubject());
		Activity activity = getActivity();
		if (activity != null) {
			new HttpSender(activity).sendMessage("db/conversation/editconversation.php", contact, UUID.randomUUID(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.CONVERSATION_EDITED.name(),
					"conversationId", conversation.getConversationId().toString(), "subject", conversation.getSubject(),
					"archived", Boolean.toString(conversation.isArchived()),
					"conversationFlags", conversation.getConversationFlags().toString());
		}
		dialog.dismiss();
	}

	private boolean isSpeechRecognitionActivityAvailable() {
		try {
			PackageManager pm = getContext().getPackageManager();
			return !pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).isEmpty();
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Error when retrieving speech recognition activities", e);
			return false;
		}
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Inform about message received.
		 */
		MESSAGE_RECEIVED,
		/**
		 * Inform about message acknowledged.
		 */
		MESSAGE_ACKNOWLEDGED,
		/**
		 * Message deleted.
		 */
		MESSAGE_DELETED,
		/**
		 * Conversation edited.
		 */
		CONVERSATION_EDITED,
		/**
		 * Conversation deleted.
		 */
		CONVERSATION_DELETED,
		/**
		 * Text message as acknowledgement.
		 */
		TEXT_RESPONSE,
		/**
		 * This device has been logged out.
		 */
		DEVICE_LOGGED_OUT,
		/**
		 * Response to Ping.
		 */
		PONG,
		/**
		 * Message sent from other device.
		 */
		MESSAGE_SENT
	}

	/**
	 * The status of a message.
	 */
	public enum MessageStatus {
		/**
		 * Sent.
		 */
		MESSAGE_SENT,
		/**
		 * Received.
		 */
		MESSAGE_RECEIVED,
		/**
		 * Acknowledged.
		 */
		MESSAGE_ACKNOWLEDGED;

		public static MessageStatus fromOrdinal(final int ordinal) {
			for (MessageStatus messageStatus : MessageStatus.values()) {
				if (messageStatus.ordinal() == ordinal) {
					return messageStatus;
				}
			}
			return MESSAGE_SENT;
		}
	}

}
