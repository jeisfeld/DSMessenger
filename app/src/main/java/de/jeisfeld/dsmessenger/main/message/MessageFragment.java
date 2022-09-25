package de.jeisfeld.dsmessenger.main.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;

public class MessageFragment extends Fragment {

	private FragmentMessageBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMessageBinding.inflate(inflater, container, false);

		binding.checkboxVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
			binding.tableRowRepeatVibration.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			binding.tableRowVibrationPattern.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		});

		binding.buttonSend.setOnClickListener(v -> sendMessage());

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
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

		new HttpSender().sendMessage(null,
				"device", device, "messageType", "TEXT", "messageText", messageText, "vibrate", Boolean.toString(vibrate),
				"vibrationRepeated", Boolean.toString(repeatVibration), "vibrationPattern", Integer.toString(vibrationPattern),
				"displayOnLockScreen", Boolean.toString(displayOnLockScreen), "lockMessage", Boolean.toString(lockMessage),
				"keepScreenOn", Boolean.toString(keepScreenOn));
	}

}