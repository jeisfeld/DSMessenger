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

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;

/**
 * Fragment for sending messages.
 */
public class MessageFragment extends Fragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.dsmessenger.account.MessageFragment";
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
	 * @param context    The context.
	 * @param actionType The action type.
	 * @param messageId  The messageId.
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final String... parameters) {
		Intent intent = new Intent(BROADCAST_ACTION);
		Bundle bundle = new Bundle();
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

		binding.checkboxVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
			binding.tableRowRepeatVibration.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			binding.tableRowVibrationPattern.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		});

		binding.buttonSend.setOnClickListener(v -> sendMessage());

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		List<Contact> contactList = ContactRegistry.getInstance().getConnectedContacts();

		ArrayAdapter<Contact> dataAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, contactList);
		binding.spinnerContact.setAdapter(dataAdapter);

		if (contactList.size() == 0) {
			binding.scrollViewSendMessage.setVisibility(View.GONE);
			binding.buttonSend.setVisibility(View.GONE);
			binding.spinnerContact.setVisibility(View.GONE);
		}
		else if (contactList.size() == 1) {
			binding.scrollViewSendMessage.setVisibility(View.VISIBLE);
			binding.buttonSend.setVisibility(View.VISIBLE);
			binding.spinnerContact.setVisibility(View.GONE);
		}
		else {
			binding.scrollViewSendMessage.setVisibility(View.VISIBLE);
			binding.buttonSend.setVisibility(View.VISIBLE);
			binding.spinnerContact.setVisibility(View.VISIBLE);
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
	 */
	private void sendMessage() {
		String messageText = binding.editTextMessageText.getText().toString();
		boolean vibrate = binding.checkboxVibrate.isChecked();
		boolean repeatVibration = vibrate && binding.checkboxRepeatVibration.isChecked();
		int vibrationPattern = binding.spinnerVibrationPattern.getSelectedItemPosition();
		boolean displayOnLockScreen = binding.checkboxDisplayOnLockScreen.isChecked();
		boolean lockMessage = binding.checkboxLockMessage.isChecked();
		boolean keepScreenOn = binding.checkboxKeepScreenOn.isChecked();
		Contact contact = (Contact) binding.spinnerContact.getSelectedItem();
		UUID messageId = UUID.randomUUID();
		lastMessageId = messageId;
		binding.textMessageResponse.setText(R.string.text_sending_message);

		new HttpSender(getContext()).sendMessage(contact, messageId, (response, responseData) -> {
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							if (responseData == null || !responseData.isSuccess()) {
								binding.textMessageResponse.setText(responseData == null ? response : responseData.getMappedErrorMessage(getContext()));
							}
							else {
								binding.textMessageResponse.setText(R.string.text_message_sent);
							}
						});
					}
				},
				"messageType", MessageType.TEXT.name(), "messageText", messageText, "vibrate", Boolean.toString(vibrate),
				"vibrationRepeated", Boolean.toString(repeatVibration), "vibrationPattern", Integer.toString(vibrationPattern),
				"displayOnLockScreen", Boolean.toString(displayOnLockScreen), "lockMessage", Boolean.toString(lockMessage),
				"keepScreenOn", Boolean.toString(keepScreenOn));
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
		MESSAGE_ACKNOWLEDGED
	}
}
