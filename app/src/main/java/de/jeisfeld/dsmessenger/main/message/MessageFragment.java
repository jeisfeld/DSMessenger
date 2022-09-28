package de.jeisfeld.dsmessenger.main.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;

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
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	/**
	 * Send the message.
	 */
	private void sendMessage() {
		String device = binding.radioDeviceTablet.isChecked() ? "tablet" : "handy";
		String messageText = binding.editTextMessageText.getText().toString();
		boolean vibrate = binding.checkboxVibrate.isChecked();
		boolean repeatVibration = vibrate && binding.checkboxRepeatVibration.isChecked();
		int vibrationPattern = binding.spinnerVibrationPattern.getSelectedItemPosition();
		boolean displayOnLockScreen = binding.checkboxDisplayOnLockScreen.isChecked();
		boolean lockMessage = binding.checkboxLockMessage.isChecked();
		boolean keepScreenOn = binding.checkboxKeepScreenOn.isChecked();

		new HttpSender().sendMessage("index.php", false, null,
				"device", device, "messageType", "TEXT", "messageText", messageText, "vibrate", Boolean.toString(vibrate),
				"vibrationRepeated", Boolean.toString(repeatVibration), "vibrationPattern", Integer.toString(vibrationPattern),
				"displayOnLockScreen", Boolean.toString(displayOnLockScreen), "lockMessage", Boolean.toString(lockMessage),
				"keepScreenOn", Boolean.toString(keepScreenOn));
	}

}
