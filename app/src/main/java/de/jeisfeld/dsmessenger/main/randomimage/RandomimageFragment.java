package de.jeisfeld.dsmessenger.main.randomimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.dsmessenger.databinding.FragmentRandomimageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;

public class RandomimageFragment extends Fragment {

	private FragmentRandomimageBinding binding;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentRandomimageBinding.inflate(inflater, container, false);

		binding.radioGroupOrigin.setOnCheckedChangeListener((group, checkedId) -> {
			if (checkedId == binding.radioOriginWidget.getId()) {
				binding.tableRowNotificationName.setVisibility(View.GONE);
				binding.tableRowWidgetName.setVisibility(View.VISIBLE);
			}
			else {
				binding.tableRowNotificationName.setVisibility(View.VISIBLE);
				binding.tableRowWidgetName.setVisibility(View.GONE);
			}
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
		String randomImageOrigin = binding.radioGroupOrigin.getCheckedRadioButtonId() ==
				binding.radioOriginWidget.getId() ? "WIDGET" : "NOTIFICATION";
		String notificationName = binding.spinnerNotificationName.getSelectedItem().toString();
		String widgetName = binding.spinnerWidgetName.getSelectedItem().toString();

		new HttpSender().sendMessage("device", device, "messageType", "RANDOMIMAGE", "randomImageOrigin", randomImageOrigin,
				"notificationName", notificationName, "widgetName", widgetName);
	}

}