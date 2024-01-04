package de.jeisfeld.coachat.main.lut;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.databinding.FragmentLutBinding;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.account.ContactRegistry;
import de.jeisfeld.coachat.main.message.MessageFragment;
import de.jeisfeld.coachat.message.AdminMessageDetails.AdminType;
import de.jeisfeld.coachat.message.LutMessageDetails.LutMessageType;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;
import de.jeisfeld.coachat.util.DropdownHandler;

/**
 * Fragment for LuT controlling.
 */
public class LutFragment extends Fragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.coachat.main.message.LutFragment";
	/**
	 * The view binding.
	 */
	private FragmentLutBinding binding;
	/**
	 * Dropdown handler for the contact.
	 */
	private DropdownHandler<Contact> dropdownHandlerContact;
	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				MessageFragment.ActionType actionType = (MessageFragment.ActionType) intent.getSerializableExtra("actionType");
				switch (actionType) {
				case PONG:
					Contact contactPont = (Contact) intent.getSerializableExtra("contact");
					if (dropdownHandlerContact != null && dropdownHandlerContact.getSelectedItem().getRelationId() == contactPont.getRelationId()) {
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
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context    The context.
	 * @param actionType The action type.
	 * @param messageId  The messageId.
	 * @param contact    The contact.
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final MessageFragment.ActionType actionType, final UUID messageId, final Contact contact,
									 final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		bundle.putSerializable("messageId", messageId);
		if (contact != null) {
			bundle.putSerializable("contact", contact);
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
	public final View onCreateView(@NonNull final LayoutInflater inflater,
								   final ViewGroup container, final Bundle savedInstanceState) {

		binding = FragmentLutBinding.inflate(inflater, container, false);

		binding.buttonTriggerLut.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 1.0));
		binding.buttonTriggerLutAdd1.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 1.01)); // MAGIC_NUMBER
		binding.buttonTriggerLutAdd2.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 1.08)); // MAGIC_NUMBER
		binding.buttonTriggerLutAdd3.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 1.3)); // MAGIC_NUMBER
		binding.buttonTriggerLutSub1.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 0.99)); // MAGIC_NUMBER
		binding.buttonTriggerLutSub2.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 0.93)); // MAGIC_NUMBER
		binding.buttonTriggerLutSub3.setOnClickListener(v -> sendMessage(LutMessageType.PULSE, 0.77)); // MAGIC_NUMBER

		binding.toggleButtonSetLut.setOnCheckedChangeListener((buttonView, isChecked) -> {
			binding.buttonTriggerLut.setEnabled(!isChecked);
			binding.buttonTriggerLutAdd1.setEnabled(!isChecked);
			binding.buttonTriggerLutAdd2.setEnabled(!isChecked);
			binding.buttonTriggerLutAdd3.setEnabled(!isChecked);
			binding.buttonTriggerLutSub1.setEnabled(!isChecked);
			binding.buttonTriggerLutSub2.setEnabled(!isChecked);
			binding.buttonTriggerLutSub3.setEnabled(!isChecked);
			sendMessage(isChecked ? LutMessageType.ON : LutMessageType.OFF, 1.0);
		});

		binding.dropdownContact.setOnItemClickListener((parent, view, position, id) -> pingContact());

		return binding.getRoot();
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@Override
	public final void onResume() {
		super.onResume();
		Contact[] contacts = ContactRegistry.getInstance().getConnectedContacts().toArray(new Contact[0]);
		dropdownHandlerContact = new DropdownHandler<>(getContext(), binding.dropdownContact, contacts);

		if (contacts.length == 0) {
			binding.buttonTriggerLut.setVisibility(View.GONE);
			binding.toggleButtonSetLut.setVisibility(View.GONE);
			binding.layoutContact.setVisibility(View.GONE);
		}
		else if (contacts.length == 1) {
			binding.buttonTriggerLut.setVisibility(View.VISIBLE);
			binding.toggleButtonSetLut.setVisibility(View.VISIBLE);
			binding.layoutContact.setVisibility(View.GONE);
			dropdownHandlerContact.selectEntry(0);
		}
		else {
			binding.buttonTriggerLut.setVisibility(View.VISIBLE);
			binding.toggleButtonSetLut.setVisibility(View.VISIBLE);
			binding.dropdownContact.setVisibility(View.VISIBLE);
			dropdownHandlerContact.selectEntry(0);
		}

		pingContact();
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
		Contact contact = dropdownHandlerContact.getSelectedItem();
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
	 * @param lutMessageType The LUT message type.
	 * @param powerFactor    A factor by which the power is multiplied.
	 */
	private void sendMessage(final LutMessageType lutMessageType, final double powerFactor) {
		new HttpSender(getContext()).sendMessage(dropdownHandlerContact.getSelectedItem(), UUID.randomUUID(), null,
				"messageType", MessageType.LUT.name(), "lutMessageType", lutMessageType.name(), "ttl", "60",
				"powerFactor", Double.toString(powerFactor));
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Response to Ping.
		 */
		PONG
	}
}
