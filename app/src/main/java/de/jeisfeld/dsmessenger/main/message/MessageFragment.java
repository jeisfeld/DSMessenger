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
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.MainActivity;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessagePriority;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;
import de.jeisfeld.dsmessenger.util.DropdownHandler;
import de.jeisfeld.dsmessenger.util.Logger;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

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
				case DEVICE_LOGGED_OUT:
					binding.listViewMessages.setVisibility(View.GONE);
					binding.buttonSend.setVisibility(View.GONE);
					binding.dropdownContact.setVisibility(View.GONE);
					Activity activity = getActivity();
					if (activity != null) {
						((MainActivity) activity).updateNavigationDrawer();
					}
					break;
				default:
					break;
				}
			}
		}
	};
	/**
	 * The list of displayed messages.
	 */
	private final List<MessageInfo> messageList = new ArrayList<>();
	/**
	 * Dropdown handler for the contact.
	 */
	DropdownHandler<Contact> dropdownHandlerContact;
	/**
	 * The array adapter for the list of displayed messages.
	 */
	private ArrayAdapter<MessageInfo> arrayAdapter;
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context    The context.
	 * @param actionType The action type.
	 * @param messageId  The messageId.
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		bundle.putSerializable("messageId", messageId);
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
	public final View onCreateView(@NonNull final LayoutInflater inflater,
								   final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentMessageBinding.inflate(inflater, container, false);

		binding.buttonSend.setOnClickListener(v -> sendMessage(MessagePriority.NORMAL));
		binding.buttonSendWithPriority.setOnClickListener(v -> sendMessage(MessagePriority.HIGH));

		arrayAdapter = new ArrayAdapter<MessageInfo>(requireContext(), R.layout.list_view_message, R.id.textViewMessageOwn, messageList) {
			@NonNull
			@Override
			public View getView(final int position, final @Nullable View convertView, final @NonNull ViewGroup parent) {
				final View view = super.getView(position, convertView, parent);

				MessageInfo messageInfo = messageList.get(position);

				switch (messageInfo.viewType) {
				case MESSAGE_OWN:
					view.findViewById(R.id.textViewMessageOwn).setVisibility(View.VISIBLE);
					view.findViewById(R.id.textViewMessageContact).setVisibility(View.GONE);
					((TextView) view.findViewById(R.id.textViewMessageOwn)).setText(messageInfo.getMessageText());
					break;
				case MESSAGE_CONTACT:
					view.findViewById(R.id.textViewMessageOwn).setVisibility(View.GONE);
					view.findViewById(R.id.textViewMessageContact).setVisibility(View.VISIBLE);
					((TextView) view.findViewById(R.id.textViewMessageContact)).setText(messageInfo.getMessageText());
					break;
				}

				return view;
			}
		};
		binding.listViewMessages.setAdapter(arrayAdapter);

		return binding.getRoot();
	}

	@Override
	public final void onResume() {
		super.onResume();
		Contact[] contacts = ContactRegistry.getInstance().getConnectedContacts().toArray(new Contact[0]);

		dropdownHandlerContact = new DropdownHandler<>(getContext(), binding.dropdownContact, contacts);
		int lastContact = PreferenceUtil.getSharedPreferenceInt(R.string.key_last_contact, 0);

		if (contacts.length == 0) {
			binding.listViewMessages.setVisibility(View.GONE);
			binding.buttonSend.setVisibility(View.GONE);
			binding.layoutContact.setVisibility(View.GONE);
			binding.textSendMessage.setText(R.string.text_send_message);
		}
		else if (contacts.length == 1) {
			binding.listViewMessages.setVisibility(View.VISIBLE);
			binding.buttonSend.setVisibility(View.VISIBLE);
			binding.layoutContact.setVisibility(View.GONE);
			dropdownHandlerContact.selectEntry(0);
			binding.textSendMessage.setText(getString(R.string.text_send_message_to, contacts[0].getName()));
		}
		else {
			binding.listViewMessages.setVisibility(View.VISIBLE);
			binding.buttonSend.setVisibility(View.VISIBLE);
			binding.layoutContact.setVisibility(View.VISIBLE);
			dropdownHandlerContact.selectEntry(Math.min(lastContact, contacts.length));
			binding.textSendMessage.setText(R.string.text_send_message);
		}
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
	 * Send the message.
	 *
	 * @param priority The message priority.
	 */
	private void sendMessage(final MessagePriority priority) {
		String messageText = binding.editTextMessageText.getText().toString();
		Contact contact = dropdownHandlerContact.getSelectedItem();
		UUID messageId = UUID.randomUUID();
		lastMessageId = messageId;
		binding.textMessageResponse.setText(R.string.text_sending_message);
		PreferenceUtil.setSharedPreferenceInt(R.string.key_last_contact, dropdownHandlerContact.getSelectedPosition());

		new HttpSender(getContext()).sendMessage(contact, messageId, (response, responseData) -> {
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							if (responseData == null || !responseData.isSuccess()) {
								binding.textMessageResponse.setText(responseData == null ? response : responseData.getMappedErrorMessage(getContext()));
							}
							else {
								binding.textMessageResponse.setText(R.string.text_message_sent);
								messageList.add(new MessageInfo(binding.editTextMessageText.getText().toString(), MessageViewType.MESSAGE_OWN, MessageStatus.MESSAGE_SENT));
								Logger.log("List size: " + messageList.size());
								arrayAdapter.notifyDataSetChanged();
								binding.listViewMessages.setSelection(messageList.size() - 1);
								binding.editTextMessageText.setText("");
							}
						});
					}
				},
				"messageType", MessageType.TEXT.name(), "messageText", messageText, "priority", priority.name());
	}

	/**
	 * The view type of a message.
	 */
	public enum MessageViewType {
		/**
		 * Message of yourself.
		 */
		MESSAGE_OWN,
		/**
		 * Message of a contact.
		 */
		MESSAGE_CONTACT
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
		 * This device has been logged out.
		 */
		DEVICE_LOGGED_OUT
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

	private static class MessageInfo {
		/**
		 * The message text.
		 */
		private final String messageText;
		/**
		 * The message view type.
		 */
		private final MessageViewType viewType;
		/**
		 * The message status.
		 */
		private final MessageStatus status;

		/**
		 * Constructor.
		 *
		 * @param messageText The message text.
		 * @param viewType    The message view type
		 * @param status      The message status
		 */
		public MessageInfo(String messageText, MessageViewType viewType, MessageStatus status) {
			this.messageText = messageText;
			this.viewType = viewType;
			this.status = status;
		}

		public String getMessageText() {
			return messageText;
		}

		public MessageViewType getViewType() {
			return viewType;
		}

		public MessageStatus getStatus() {
			return status;
		}
	}
}
