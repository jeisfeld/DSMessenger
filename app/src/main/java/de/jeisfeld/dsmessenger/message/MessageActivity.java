package de.jeisfeld.dsmessenger.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.ActivityMessageBinding;
import de.jeisfeld.dsmessenger.entity.Contact;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.entity.Message;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.message.ConversationsFragment;
import de.jeisfeld.dsmessenger.main.message.MessageFragment.MessageStatus;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails.AdminType;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessagePriority;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;
import de.jeisfeld.dsmessenger.util.DateUtil;

/**
 * Activity to display messages.
 */
public class MessageActivity extends AppCompatActivity {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.dsmessenger.message.MessageActivity";
	/**
	 * The resource key for the message.
	 */
	private static final String STRING_EXTRA_MESSAGE_DETAILS = "de.jeisfeld.dsmessenger.MESSAGE_DETAILS";
	/**
	 * The conversation currently on top.
	 */
	private static Conversation currentTopConversation = null;
	/**
	 * The binding of the activity.
	 */
	private ActivityMessageBinding binding;
	/**
	 * The list of displayed messages.
	 */
	private final List<Message> messageList = new ArrayList<>();
	/**
	 * The array adapter for the list of displayed messages.
	 */
	private ArrayAdapter<Message> arrayAdapter;
	/**
	 * The conversation.
	 */
	private Conversation conversation;
	/**
	 * The message vibration.
	 */
	private MessageVibration messageVibration = null;
	/**
	 * The last received messageId.
	 */
	private TextMessageDetails lastTextMessageDetails;

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
					Conversation receivedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (receivedConversation != null && receivedConversation.getConversationId().equals(conversation.getConversationId())) {
						refreshMessageList(conversation.getConversationId(), null, false);
					}
					break;
				case MESSAGE_ACKNOWLEDGED:
					UUID acknowledgetMessageId = (UUID) intent.getSerializableExtra("messageId");
					if (acknowledgetMessageId != null && lastTextMessageDetails != null && acknowledgetMessageId.equals(lastTextMessageDetails.getMessageId())) {
						cancelLastIntentEffects();
						binding.buttonAcknowledge.setVisibility(View.INVISIBLE);
					}
					break;
				case CONVERSATION_EDITED:
					Conversation editedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (editedConversation != null && conversation != null
							&& editedConversation.getConversationId().equals(conversation.getConversationId())) {
						binding.textSubject.setText(getString(R.string.text_subject, editedConversation.getSubject()));
					}
					break;
				case CONVERSATION_DELETED:
					Conversation deletedConversation = (Conversation) intent.getSerializableExtra("conversation");
					if (deletedConversation != null && conversation != null
							&& deletedConversation.getConversationId().equals(conversation.getConversationId())) {
						finish();
					}
					break;
				default:
					break;
				}
			}
		}
	};
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context      The context.
	 * @param actionType   The action type.
	 * @param messageId    The messageId.
	 * @param conversation The conversation.
	 * @param parameters   The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final Conversation conversation,
									 final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		if (messageId != null) {
			bundle.putSerializable("messageId", conversation);
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
	 * Static helper method to create an intent for this activity.
	 *
	 * @param context            The context in which this activity is started.
	 * @param textMessageDetails The details of the message to be displayed
	 * @return the intent.
	 */
	public static Intent createIntent(final Context context, final TextMessageDetails textMessageDetails) {
		Intent intent = new Intent(context, MessageActivity.class);
		intent.putExtra(STRING_EXTRA_MESSAGE_DETAILS, textMessageDetails);
		if (textMessageDetails.getConversationId() != null && currentTopConversation != null
				&& !currentTopConversation.getConversationId().equals(textMessageDetails.getConversationId())) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		else {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		return intent;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMessageBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		arrayAdapter = new ArrayAdapter<Message>(this, R.layout.list_view_message, R.id.textViewMessage, messageList) {
			@NonNull
			@Override
			public View getView(final int position, final @Nullable View convertView, final @NonNull ViewGroup parent) {
				final View view = super.getView(position, convertView, parent);

				Message message = messageList.get(position);
				TextView textViewMessage = view.findViewById(R.id.textViewMessage);
				textViewMessage.setText(message.getMessageText());

				if (message.isOwn()) {
					view.findViewById(R.id.spaceRight).setVisibility(View.VISIBLE);
					view.findViewById(R.id.spaceLeft).setVisibility(View.GONE);
					textViewMessage.setBackgroundResource(R.drawable.background_message);
				}
				else {
					view.findViewById(R.id.spaceRight).setVisibility(View.GONE);
					view.findViewById(R.id.spaceLeft).setVisibility(View.VISIBLE);
					textViewMessage.setBackgroundResource(R.drawable.background_message_contact);
				}

				((TextView) view.findViewById(R.id.textViewMessageTime)).setText(DateUtil.formatTimestamp(message.getTimestamp()));

				ImageView imageViewMessageStatus = view.findViewById(R.id.imageViewMessageStatus);
				switch (message.getStatus()) {
				case MESSAGE_SENT:
					imageViewMessageStatus.setImageResource(R.drawable.ic_icon_message_sent);
					break;
				case MESSAGE_RECEIVED:
				case MESSAGE_ACKNOWLEDGED:
					imageViewMessageStatus.setImageResource(R.drawable.ic_icon_message_received);
					break;
				default:
					imageViewMessageStatus.setImageIcon(null);
					break;
				}

				return view;
			}
		};
		binding.listViewMessages.setAdapter(arrayAdapter);

		final TextMessageDetails textMessageDetails = (TextMessageDetails) getIntent().getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS);

		refreshMessageList(textMessageDetails.getConversationId(), textMessageDetails, true);

		broadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter actionReceiver = new IntentFilter();
		actionReceiver.addAction(BROADCAST_ACTION);
		broadcastManager.registerReceiver(localBroadcastReceiver, actionReceiver);
	}

	/**
	 * Refresh the message list.
	 *
	 * @param conversationId     The conversationId.
	 * @param textMessageDetails The text message details to be handled at the end (if applicable)
	 * @param scrollDown         Flag indicating if view should scroll to last position.
	 */
	private void refreshMessageList(final UUID conversationId, final TextMessageDetails textMessageDetails, final boolean scrollDown) {
		new Thread(() -> {
			List<Message> newMessageList = Application.getAppDatabase().getMessageDao().getMessagesByConversationId(conversationId);
			messageList.clear();
			messageList.addAll(newMessageList);
			runOnUiThread(() -> {
				arrayAdapter.notifyDataSetChanged();
				if (scrollDown) {
					binding.listViewMessages.setSelection(messageList.size() - 1);
				}
				if (textMessageDetails != null) {
					handleIntentData(textMessageDetails);
				}
			});
		}).start();
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		broadcastManager.unregisterReceiver(localBroadcastReceiver);
	}

	@Override
	protected final void onStop() {
		super.onStop();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		handleIntentData((TextMessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS));
		binding.buttonAcknowledge.setVisibility(View.VISIBLE);
		binding.editTextMessageText.setVisibility(View.VISIBLE);
	}

	/**
	 * Handle the data of a new intent.
	 *
	 * @param textMessageDetails The text message details of the new intent.
	 */
	private void handleIntentData(final TextMessageDetails textMessageDetails) {
		cancelLastIntentEffects();

		binding.textMessageFrom.setText(getString(R.string.text_message_from, textMessageDetails.getContact().getName()));

		UUID conversationId = textMessageDetails.getConversationId();
		Conversation messageConversation = Application.getAppDatabase().getConversationDao().getConversationById(conversationId);
		if (messageConversation == null) {
			messageConversation = Conversation.createNewConversation(textMessageDetails);
			messageConversation.insertIfNew(textMessageDetails.getMessageText());
			ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_ADDED, messageConversation);
		}
		MessageActivity.currentTopConversation = messageConversation;
		conversation = messageConversation;
		binding.textSubject.setText(getString(R.string.text_subject, conversation.getSubject()));

		Message message = new Message(textMessageDetails.getMessageText(), false, textMessageDetails.getMessageId(),
				conversationId, textMessageDetails.getTimestamp(), MessageStatus.MESSAGE_RECEIVED);
		if (message.getMessageText() != null && message.getMessageText().length() > 0) {
			message.store(conversation);
			messageList.add(message);
			arrayAdapter.notifyDataSetChanged();
			ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_EDITED, conversation);
			binding.listViewMessages.setSelection(messageList.size() - 1);
		}

		MessageDisplayStrategy displayStrategy = textMessageDetails.getDisplayStrategy();

		if (displayStrategy.isVibrate()) {
			messageVibration = new MessageVibration(this);
			messageVibration.vibrate(displayStrategy);
		}
		if (displayStrategy.isDisplayOnLockScreen()) {
			displayOnLockScreen(true);
		}
		if (displayStrategy.isLockMessage()) {
			startLockTask();
		}
		if (displayStrategy.isKeepScreenOn()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		sendConfirmation(AdminType.MESSAGE_RECEIVED, textMessageDetails.getMessageId(), textMessageDetails.getContact());

		binding.buttonAcknowledge.setOnClickListener(v -> {
			if (messageVibration != null) {
				messageVibration.cancelVibration();
			}
			sendConfirmation(AdminType.MESSAGE_ACKNOWLEDGED, textMessageDetails.getMessageId(), textMessageDetails.getContact());
			new HttpSender(this).sendSelfMessage(textMessageDetails.getMessageId(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.MESSAGE_SELF_ACKNOWLEDGED.name());
			binding.buttonAcknowledge.setVisibility(View.GONE);
			binding.editTextMessageText.setVisibility(View.GONE);
		});

		lastTextMessageDetails = textMessageDetails;
	}

	/**
	 * Cancel the effects of the last text message (such as vibration).
	 */
	private void cancelLastIntentEffects() {
		if (lastTextMessageDetails != null) {
			MessageDisplayStrategy lastDisplayStrategy = lastTextMessageDetails.getDisplayStrategy();
			if (lastDisplayStrategy.isVibrate()) {
				if (messageVibration != null) {
					messageVibration.cancelVibration();
				}
			}
			if (lastDisplayStrategy.isDisplayOnLockScreen()) {
				displayOnLockScreen(false);
			}
			if (lastDisplayStrategy.isLockMessage()) {
				stopLockTask();
			}
			if (lastDisplayStrategy.isKeepScreenOn()) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		}
	}

	/**
	 * Send confirmation for the message.
	 *
	 * @param adminType The type of confirmation.
	 * @param messageId The messageId.
	 * @param contact   The contact.
	 */
	private void sendConfirmation(final AdminType adminType, final UUID messageId, final Contact contact) {
		String messageText = binding.editTextMessageText.getText().toString();
		if (adminType == AdminType.MESSAGE_ACKNOWLEDGED && messageText.length() > 0) {
			final long timestamp = System.currentTimeMillis();
			UUID newMessageId = UUID.randomUUID();
			new HttpSender(this).sendMessage(contact, newMessageId, (response, responseData) -> {
						Message message = new Message(messageText, true, newMessageId,
								conversation.getConversationId(), timestamp, MessageStatus.MESSAGE_SENT);
						runOnUiThread(() -> {
							if (responseData != null && responseData.isSuccess()) {
								message.store(conversation);
								messageList.add(message);
								arrayAdapter.notifyDataSetChanged();
								ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_EDITED, conversation);
								binding.listViewMessages.setSelection(messageList.size() - 1);
								binding.editTextMessageText.setText("");
							}
						});
					},
					"messageType", MessageType.TEXT_ACKNOWLEDGE.name(), "messageText", messageText,
					"priority", MessagePriority.NORMAL.name(), "conversationId", conversation.getConversationId().toString(),
					"timestamp", Long.toString(timestamp));
		}
		else {
			new HttpSender(this).sendMessage(contact, messageId, null,
					"messageType", MessageType.ADMIN.name(), "adminType", adminType.name(),
					"conversationId", conversation.getConversationId().toString());
		}
	}

	/**
	 * Ensure that the message is displayed on top of lock screen.
	 *
	 * @param displayOnLockScreen Flag indicating if the effect should be added or removed.
	 */
	private void displayOnLockScreen(final boolean displayOnLockScreen) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setTurnScreenOn(displayOnLockScreen);
			setShowWhenLocked(displayOnLockScreen);
		}
		else {
			Window window = getWindow();
			if (displayOnLockScreen) {
				window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
			}
			else {
				window.clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.clearFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
			}
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
		 * Inform about message acknowledgement sent.
		 */
		MESSAGE_ACKNOWLEDGED,
		/**
		 * Conversation edited.
		 */
		CONVERSATION_EDITED,
		/**
		 * Conversation deleted.
		 */
		CONVERSATION_DELETED
	}
}
