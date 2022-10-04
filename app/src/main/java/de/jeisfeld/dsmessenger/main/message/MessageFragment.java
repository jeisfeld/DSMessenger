package de.jeisfeld.dsmessenger.main.message;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;

/**
 * Fragment for sending messages.
 */
public class MessageFragment extends Fragment {
	/**
	 * The view binding.
	 */
	private FragmentMessageBinding binding;

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

	/**
	 * Send the message.
	 */
	private void sendMessage() {
		binding.textMessageResponse.setText(R.string.text_sending_message);

		String messageText = binding.editTextMessageText.getText().toString();
		boolean vibrate = binding.checkboxVibrate.isChecked();
		boolean repeatVibration = vibrate && binding.checkboxRepeatVibration.isChecked();
		int vibrationPattern = binding.spinnerVibrationPattern.getSelectedItemPosition();
		boolean displayOnLockScreen = binding.checkboxDisplayOnLockScreen.isChecked();
		boolean lockMessage = binding.checkboxLockMessage.isChecked();
		boolean keepScreenOn = binding.checkboxKeepScreenOn.isChecked();
		Contact contact = (Contact) binding.spinnerContact.getSelectedItem();

		new HttpSender(getContext()).sendMessage("firebase/sendmessage.php", contact, (response, responseData) -> {
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
				"messageType", "TEXT", "messageText", messageText, "vibrate", Boolean.toString(vibrate),
				"vibrationRepeated", Boolean.toString(repeatVibration), "vibrationPattern", Integer.toString(vibrationPattern),
				"displayOnLockScreen", Boolean.toString(displayOnLockScreen), "lockMessage", Boolean.toString(lockMessage),
				"keepScreenOn", Boolean.toString(keepScreenOn));
	}

}
