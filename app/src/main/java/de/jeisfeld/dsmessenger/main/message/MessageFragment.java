package de.jeisfeld.dsmessenger.main.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.entity.Contact;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.entity.Message;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.MainActivity;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails.AdminType;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessagePriority;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;
import de.jeisfeld.dsmessenger.message.TextMessageDetails;

/**
 * Fragment for sending messages.
 */
public class MessageFragment extends Fragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.dsmessenger.main.message.MessageFragment";
	/**
	 * The view binding.
	 */
	private FragmentMessageBinding binding;
	/**
	 * The last sent messageId.
	 */
	private UUID lastMessageId;
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
	 * The array adapter for the list of displayed messages.
	 */
	private ArrayAdapter<Message> arrayAdapter;
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;
	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				ActionType actionType = (ActionType) intent.getSerializableExtra("actionType");
				switch (actionType) {
				case MESSAGE_RECEIVED:
				case MESSAGE_ACKNOWLEDGED:
					UUID messageId = (UUID) intent.getSerializableExtra("messageId");
					if (messageId.equals(lastMessageId)) {
						binding.textMessageResponse.setText(
								actionType == ActionType.MESSAGE_RECEIVED ? R.string.text_message_received : R.string.text_message_acknowledged);
					}
					break;
				case TEXT_ACKNOWLEDGE:
					TextMessageDetails textMessageDetails = (TextMessageDetails) intent.getSerializableExtra("textMessageDetails");
					Message message = new Message(textMessageDetails.getMessageText(), false, textMessageDetails.getMessageId(),
							textMessageDetails.getConversationId(), textMessageDetails.getTimestamp(), MessageStatus.MESSAGE_RECEIVED);
					addMessage(message);
					break;
				case DEVICE_LOGGED_OUT:
					binding.listViewMessages.setVisibility(View.GONE);
					binding.buttonSend.setVisibility(View.GONE);
					Activity activity = getActivity();
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
	 * Send a broadcast to this fragment.
	 *
	 * @param context    The context.
	 * @param actionType The action type.
	 * @param messageId  The messageId.
	 * @param contact    The contact.
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final Contact contact,
									 final TextMessageDetails textMessageDetails, final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		bundle.putSerializable("messageId", messageId);
		if (contact != null) {
			bundle.putSerializable("contact", contact);
		}
		if (textMessageDetails != null) {
			bundle.putSerializable("textMessageDetails", textMessageDetails);
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

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentMessageBinding.inflate(inflater, container, false);

		binding.buttonSend.setOnClickListener(v -> sendMessage(MessagePriority.NORMAL));
		binding.buttonSendWithPriority.setOnClickListener(v -> sendMessage(MessagePriority.HIGH));

		assert getArguments() != null;
		contact = (Contact) getArguments().getSerializable("contact");
		conversation = (Conversation) getArguments().getSerializable("conversation");
		binding.textSendMessage.setText(getString(R.string.text_send_message_to, contact.getName()));

		arrayAdapter = new ArrayAdapter<Message>(requireContext(), R.layout.list_view_message, R.id.textViewMessageOwn, messageList) {
			@NonNull
			@Override
			public View getView(final int position, final @Nullable View convertView, final @NonNull ViewGroup parent) {
				final View view = super.getView(position, convertView, parent);

				Message message = messageList.get(position);

				if (message.isOwn()) {
					view.findViewById(R.id.textViewMessageOwn).setVisibility(View.VISIBLE);
					view.findViewById(R.id.textViewMessageContact).setVisibility(View.GONE);
					((TextView) view.findViewById(R.id.textViewMessageOwn)).setText(message.getMessageText());
				}
				else {
					view.findViewById(R.id.textViewMessageOwn).setVisibility(View.GONE);
					view.findViewById(R.id.textViewMessageContact).setVisibility(View.VISIBLE);
					((TextView) view.findViewById(R.id.textViewMessageContact)).setText(message.getMessageText());
				}

				return view;
			}
		};
		binding.listViewMessages.setAdapter(arrayAdapter);

		new Thread(() -> {
			List<Message> newMessageList =
					Application.getAppDatabase().getMessageDao().getMessagesByConversationId(conversation.getConversationId());
			messageList.clear();
			messageList.addAll(newMessageList);
			arrayAdapter.notifyDataSetChanged();
			Activity activity = getActivity();
			if (activity != null) {
				activity.runOnUiThread(() -> binding.listViewMessages.setSelection(messageList.size() - 1));
			}
		}).start();

		return binding.getRoot();
	}

	@Override
	public final void onResume() {
		super.onResume();
		pingContact();
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
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


	/**
	 * Send the message.
	 *
	 * @param priority The message priority.
	 */
	private void sendMessage(final MessagePriority priority) {
		String messageText = binding.editTextMessageText.getText().toString();
		UUID messageId = UUID.randomUUID();
		lastMessageId = messageId;
		long timestamp = System.currentTimeMillis();
		binding.textMessageResponse.setText(R.string.text_sending_message);

		new HttpSender(getContext()).sendMessage(contact, messageId, (response, responseData) -> {
					Message message = new Message(binding.editTextMessageText.getText().toString(), true, messageId,
							conversation.getConversationUuid(), timestamp, MessageStatus.MESSAGE_SENT);
					conversation.storeIfNew(message.getMessageText());
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							if (responseData == null || !responseData.isSuccess()) {
								binding.textMessageResponse.setText(responseData == null ? response : responseData.getMappedErrorMessage(getContext()));
							}
							else {
								binding.textMessageResponse.setText(R.string.text_message_sent);
								if (message.getMessageText() != null && message.getMessageText().length() > 0) {
									addMessage(message);
								}
								binding.editTextMessageText.setText("");
							}
						});
					}
				},
				"messageType", MessageType.TEXT.name(), "messageText", messageText, "priority", priority.name(),
				"conversationId", conversation.getConversationId(), "timestamp", Long.toString(timestamp));
	}

	/**
	 * Add a message to the displayed list.
	 *
	 * @param message The message to be added.
	 */
	private void addMessage(Message message) {
		message.store();
		if (message.getConversationId().equals(conversation.getConversationId())) {
			messageList.add(message);
			arrayAdapter.notifyDataSetChanged();
			binding.listViewMessages.setSelection(messageList.size() - 1);
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
		 * Text message as acknowledgement.
		 */
		TEXT_ACKNOWLEDGE,
		/**
		 * This device has been logged out.
		 */
		DEVICE_LOGGED_OUT,
		/**
		 * Response to Ping.
		 */
		PONG
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
		MESSAGE_ACKNOWLEDGED
	}

}
