package de.jeisfeld.dsmessenger.main.randomimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentRandomimageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;

/**
 * Fragment for triggering Randomimage notificactions.
 */
public class RandomimageFragment extends Fragment {
	/**
	 * The view binding.
	 */
	private FragmentRandomimageBinding binding;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater,
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
	public void onResume() {
		super.onResume();
		List<Contact> contactList = ContactRegistry.getInstance().getConnectedContacts();

		ArrayAdapter<Contact> dataAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, contactList);
		binding.spinnerContact.setAdapter(dataAdapter);

		if (contactList.size() == 0) {
			binding.tableRowOrigin.setVisibility(View.GONE);
			binding.tableRowNotificationName.setVisibility(View.GONE);
			binding.tableRowWidgetName.setVisibility(View.GONE);
			binding.buttonSend.setVisibility(View.GONE);
			binding.spinnerContact.setVisibility(View.GONE);
		}
		else if (contactList.size() == 1) {
			binding.tableRowOrigin.setVisibility(View.VISIBLE);
			binding.tableRowNotificationName.setVisibility(View.VISIBLE);
			binding.tableRowWidgetName.setVisibility(View.VISIBLE);
			binding.buttonSend.setVisibility(View.VISIBLE);
			binding.spinnerContact.setVisibility(View.GONE);
		}
		else {
			binding.tableRowOrigin.setVisibility(View.VISIBLE);
			binding.tableRowNotificationName.setVisibility(View.VISIBLE);
			binding.tableRowWidgetName.setVisibility(View.VISIBLE);
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
		String randomImageOrigin =
				binding.radioGroupOrigin.getCheckedRadioButtonId() == binding.radioOriginWidget.getId() ? "WIDGET" : "NOTIFICATION";
		String notificationName = binding.spinnerNotificationName.getSelectedItem().toString();
		String widgetName = binding.spinnerWidgetName.getSelectedItem().toString();
		Contact contact = (Contact) binding.spinnerContact.getSelectedItem();
		UUID messageId = UUID.randomUUID();

		new HttpSender(getContext()).sendMessage("firebase/displayrandomimage.php", contact, messageId, null,
				"messageType", "RANDOMIMAGE", "randomImageOrigin", randomImageOrigin,
				"notificationName", notificationName, "widgetName", widgetName);
	}

}
