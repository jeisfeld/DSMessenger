package de.jeisfeld.coachat.main.account;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.databinding.FragmentAccountBinding;
import de.jeisfeld.coachat.databinding.ListViewContactBinding;
import de.jeisfeld.coachat.databinding.ListViewDeviceMultiBinding;
import de.jeisfeld.coachat.databinding.ListViewDeviceSingleBinding;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Contact.AiPolicy;
import de.jeisfeld.coachat.entity.Contact.ContactStatus;
import de.jeisfeld.coachat.entity.Device;
import de.jeisfeld.coachat.entity.SlavePermissions;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.MainActivity;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.ChangePasswordDialogFragment;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.CreateAccountDialogFragment;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.CreateInvitationDialogFragment;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.EditContactDialogFragment;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.EditDeviceDialogFragment;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.LoginDialogFragment;
import de.jeisfeld.coachat.service.AlarmReceiver;
import de.jeisfeld.coachat.util.DialogUtil;
import de.jeisfeld.coachat.util.PreferenceUtil;

/**
 * The fragment for account administration.
 */
public class AccountFragment extends Fragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.coachat.account.AccountFragment";
	/**
	 * The view binding.
	 */
	private FragmentAccountBinding binding;

	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				ActionType actionType = (ActionType) intent.getSerializableExtra("actionType");
				switch (actionType) {
				case CONTACTS_UPDATED:
					if (binding != null) {
						refreshDisplayedContactList();
					}
					break;
				case DEVICES_CHANGED:
					if (binding != null) {
						updateDeviceInfo();
					}
					break;
				case DEVICE_LOGGED_OUT: {
					if (binding != null) {
						doLogoutUpdates();
					}
					break;
				}
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
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final String... parameters) {
		Intent intent = new Intent(BROADCAST_ACTION);
		Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
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
	 * Check if user is logged in.
	 *
	 * @return true if logged in.
	 */
	public static boolean isLoggedIn() {
		return PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1) >= 0;
	}

	/**
	 * Remove the stored device information.
	 */
	public static void removeStoredDeviceInfo() {
		PreferenceUtil.removeSharedPreference(R.string.key_pref_device_id);
		PreferenceUtil.removeSharedPreference(R.string.key_pref_device_name);
		PreferenceUtil.removeSharedPreference(R.string.key_pref_device_muted);
		PreferenceUtil.removeSharedPreference(R.string.key_pref_device_display_strategy_normal);
		PreferenceUtil.removeSharedPreference(R.string.key_pref_device_display_strategy_urgent);
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
	 * Add a contact to the view.
	 *
	 * @param contact The contact to be added.
	 */
	private void addContactToView(final Contact contact) {
		LinearLayout layout = contact.isSlave() ? binding.layoutMySubs : binding.layoutMyDoms;
		layout.setVisibility(View.VISIBLE);
		ListViewContactBinding childBinding = ListViewContactBinding.inflate(getLayoutInflater());
		childBinding.textViewContactName.setText(contact.getName());
		switch (contact.getStatus()) {
		case INVITED:
			childBinding.imageViewPending.setVisibility(View.VISIBLE);
			childBinding.imageViewPending.setOnClickListener(v -> sendInvitationUrl(contact.getConnectionCode()));
			break;
		case CONNECTED:
		default:
			childBinding.imageViewConfirmed.setVisibility(View.VISIBLE);
			break;
		}

		if (contact.getMyPermissions().isEditRelation()) {
			childBinding.buttonDelete.setVisibility(View.VISIBLE);
			childBinding.buttonDelete.setOnClickListener(v -> DialogUtil.displayConfirmationMessage(getActivity(),
					dialog -> new HttpSender(getContext()).sendMessage("db/usermanagement/deletecontact.php", (response, responseData) -> {
								if (responseData.isSuccess()) {
									ContactRegistry.getInstance().refreshContacts(getContext(), () -> {
										Activity activity = getActivity();
										if (activity != null) {
											activity.runOnUiThread(this::refreshDisplayedContactList);
										}
									});
								}
								else {
									Log.e(Application.TAG, "Failed to delete contact: " + responseData.getErrorMessage());
								}
							}, "isSlave", contact.isSlave() ? "1" : "", "relationId", Integer.toString(contact.getRelationId()),
							"isConnected", contact.getStatus() == ContactStatus.CONNECTED ? "1" : ""),
					R.string.title_dialog_confirm_deletion, R.string.button_cancel, R.string.button_delete_contact,
					R.string.dialog_confirm_delete_contact, contact.getName()));
		}
		else {
			childBinding.buttonDelete.setVisibility(View.GONE);
		}

		if (contact.getMyPermissions().isEditRelation() || contact.getMyPermissions().isEditSlavePermissions()) {
			childBinding.buttonEdit.setVisibility(View.VISIBLE);
			childBinding.buttonEdit.setOnClickListener(v -> AccountDialogUtil.displayEditContactDialog(this, contact));
		}
		else {
			childBinding.buttonEdit.setVisibility(View.GONE);
		}

		layout.addView(childBinding.getRoot());

		Activity activity = getActivity();
		if (activity != null) {
			((MainActivity) activity).updateNavigationDrawer();
		}
	}

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater,
								   final ViewGroup container, final Bundle savedInstanceState) {

		binding = FragmentAccountBinding.inflate(inflater, container, false);
		String username = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_username);

		if (username == null) {
			binding.tableRowUsername.setVisibility(View.GONE);
			binding.tableRowButtonsLogout.setVisibility(View.GONE);
			binding.tableRowButtonsLogin.setVisibility(View.VISIBLE);
			binding.buttonCreateInvitation.setVisibility(View.GONE);
			binding.buttonAcceptInvitation.setVisibility(View.GONE);
		}
		else {
			binding.tableRowUsername.setVisibility(View.VISIBLE);
			binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
			binding.tableRowButtonsLogin.setVisibility(View.GONE);
			binding.textViewUsername.setText(username);
			binding.buttonCreateInvitation.setVisibility(View.VISIBLE);
			binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
		}

		configureMyAccountButtons();
		configureContactButtons();

		if (MainActivity.isDsUser()) {
			binding.textMyDoms.setText(R.string.text_my_doms);
			binding.textMySubs.setText(R.string.text_my_subs);
		}


		for (Contact contact : ContactRegistry.getInstance().getContacts()) {
			addContactToView(contact);
		}

		if (isLoggedIn()) {
			displaySingleDeviceInfo(Device.getThisDevice());
		}
		updateDeviceInfo();

		return binding.getRoot();
	}

	/**
	 * Configure the buttons for my account.
	 */
	private void configureMyAccountButtons() {
		binding.buttonCreateAccount.setOnClickListener(v -> AccountDialogUtil.displayCreateAccountDialog(this));

		binding.buttonLogin.setOnClickListener(v -> AccountDialogUtil.displayLoginDialog(this));

		binding.buttonChangePassword.setOnClickListener(v -> AccountDialogUtil.displayChangePasswordDialog(this));

		binding.buttonLogout.setOnClickListener(v -> new HttpSender(getContext()).sendMessage("db/usermanagement/logout.php",
				(response, responseData) -> {
					if (responseData != null && responseData.isSuccess()) {
						PreferenceUtil.removeSharedPreference(R.string.key_pref_username);
						PreferenceUtil.removeSharedPreference(R.string.key_pref_password);
						removeStoredDeviceInfo();
						ContactRegistry.getInstance().cleanContacts();
						doLogoutUpdates();
					}
				}, "deviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1)),
				"clientDeviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1))));
	}

	/**
	 * Do GUI updates on logout.
	 */
	private void doLogoutUpdates() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(() -> {
				refreshDisplayedContactList();

				binding.tableRowButtonsLogin.setVisibility(View.VISIBLE);
				binding.tableRowButtonsLogout.setVisibility(View.GONE);
				binding.tableRowUsername.setVisibility(View.GONE);
				binding.textViewUsername.setText("");
				binding.buttonCreateInvitation.setVisibility(View.GONE);
				binding.buttonAcceptInvitation.setVisibility(View.GONE);
				binding.layoutMyDevices.setVisibility(View.GONE);
				PreferenceUtil.removeSharedPreference(R.string.key_last_conversation_timestamp);
			});
		}
	}

	/**
	 * Clean the displayed contacts in a list.
	 *
	 * @param linearLayout The list.
	 */
	private void cleanupContactsFromList(final LinearLayout linearLayout) {
		linearLayout.setVisibility(View.GONE);
		View headline = linearLayout.getChildAt(0);
		linearLayout.removeAllViews();
		linearLayout.addView(headline);

		Activity activity = getActivity();
		if (activity != null) {
			((MainActivity) activity).updateNavigationDrawer();
		}
	}

	/**
	 * Update the device information from the DB.
	 */
	private void updateDeviceInfo() {
		if (!isLoggedIn()) {
			Activity activity = getActivity();
			if (activity != null) {
				binding.layoutMyDevices.setVisibility(View.GONE);
			}
		}
		else {
			new Thread(() -> new HttpSender(getContext()).sendMessage("db/usermanagement/querydevices.php", (response, responseData) -> {
				if (responseData.isSuccess()) {
					List<Device> devices = (List<Device>) responseData.getData().get("devices");

					// Update stored deviceId. This helps when switching DBs during testing.
					for (Device device : devices) {
						if (device.isThis()) {
							int storedDeviceId = PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1);
							if (storedDeviceId != device.getId()) {
								Log.w(Application.TAG, "Updated deviceId from " + storedDeviceId + " to " + device.getId());
								PreferenceUtil.setSharedPreferenceInt(R.string.key_pref_device_id, device.getId());
							}
						}
					}
					for (Device device : devices) {
						if (device.getId() == PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1)) {
							PreferenceUtil.setSharedPreferenceString(R.string.key_pref_device_name, device.getName());
							PreferenceUtil.setSharedPreferenceBoolean(R.string.key_pref_device_muted, device.isMuted());
							PreferenceUtil.setSharedPreferenceString(
									R.string.key_pref_device_display_strategy_normal, device.getDisplayStrategyNormal().toString());
							PreferenceUtil.setSharedPreferenceString(
									R.string.key_pref_device_display_strategy_urgent, device.getDisplayStrategyUrgent().toString());
						}
					}

					final int numberOfUnmutedDevices = (int) devices.stream().filter(device -> !device.isMuted()).count();

					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(() -> {
							if (devices.size() > 1) {
								binding.layoutMyDevices.setVisibility(View.VISIBLE);
								binding.textMyDevices.setVisibility(View.VISIBLE);
								// Remove device list
								View headline = binding.layoutMyDevices.getChildAt(0);
								binding.layoutMyDevices.removeAllViews();
								binding.layoutMyDevices.addView(headline);
								// Add device list
								devices.sort((d1, d2) -> Boolean.compare(d2.isThis(), d1.isThis()));
								for (Device device : devices) {
									addMultiDeviceToView(device, numberOfUnmutedDevices > 1);
								}
							}
							else if (devices.size() == 1) {
								displaySingleDeviceInfo(devices.get(0));
							}
							else {
								binding.layoutMyDevices.setVisibility(View.GONE);
							}
						});
					}
				}
				else {
					Log.e(Application.TAG, "Failed to retrieve device data: " + responseData.getErrorMessage());
				}
			}, "clientToken", PreferenceUtil.getSharedPreferenceString(R.string.key_pref_messaging_token))).start();
		}
	}

	/**
	 * Display device info of a single device.
	 *
	 * @param device The device.
	 */
	private void displaySingleDeviceInfo(final Device device) {
		binding.layoutMyDevices.setVisibility(View.VISIBLE);
		binding.textMyDevices.setVisibility(View.GONE);
		// Remove device list
		View headline = binding.layoutMyDevices.getChildAt(0);
		binding.layoutMyDevices.removeAllViews();
		binding.layoutMyDevices.addView(headline);

		addSingleDeviceToView(device);
	}

	/**
	 * Add a device to the view, for multiple devices.
	 *
	 * @param device The device to be added.
	 * @param allowMuting Flag indicating if muting is allowed
	 */
	private void addMultiDeviceToView(final Device device, final boolean allowMuting) {
		ListViewDeviceMultiBinding childBinding = ListViewDeviceMultiBinding.inflate(getLayoutInflater());
		childBinding.textViewDeviceName.setText(device.getName());

		if (device.isThis()) {
			childBinding.buttonDelete.setVisibility(View.INVISIBLE);
		}
		else {
			childBinding.buttonDelete.setVisibility(View.VISIBLE);
			childBinding.buttonDelete.setOnClickListener(v -> DialogUtil.displayConfirmationMessage(getActivity(),
					dialog -> new HttpSender(getContext()).sendMessage("db/usermanagement/logout.php", (response, responseData) -> {
								if (responseData.isSuccess()) {
									updateDeviceInfo();
								}
								else {
									Log.e(Application.TAG, "Failed to disconnect device: " + responseData.getErrorMessage());
								}
							}, "deviceId", Integer.toString(device.getId()),
							"clientDeviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1))),
					R.string.title_dialog_confirm_deletion, R.string.button_cancel, R.string.button_disconnect_device,
					R.string.dialog_confirm_disconnect_device, device.getName()));
		}

		childBinding.buttonEnablement.setImageResource(device.isMuted() ? R.drawable.ic_icon_message_disabled : R.drawable.ic_icon_message_enabled);
		childBinding.buttonEnablement
				.setOnClickListener(v -> new HttpSender(getContext()).sendMessage("db/usermanagement/updatedevice.php", (response, responseData) -> {
							if (responseData.isSuccess()) {
								updateDeviceInfo();
							}
						}, "deviceName", device.getName(), "deviceId", Integer.toString(device.getId()), "muted", device.isMuted() ? "" : "1",
						"displayStrategyNormal", device.getDisplayStrategyNormal().toString(),
						"displayStrategyUrgent", device.getDisplayStrategyUrgent().toString(),
						"clientDeviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1))));
		childBinding.buttonEnablement.setEnabled(device.isMuted() || allowMuting);

		childBinding.buttonEdit.setOnClickListener(v -> AccountDialogUtil.displayEditDeviceDialog(this, device));

		binding.layoutMyDevices.addView(childBinding.getRoot());
	}

	/**
	 * Add a device to the view, for single device.
	 *
	 * @param device The device to be added.
	 */
	private void addSingleDeviceToView(final Device device) {
		ListViewDeviceSingleBinding childBinding = ListViewDeviceSingleBinding.inflate(getLayoutInflater());
		childBinding.textViewDeviceName.setText(device.getName());

		childBinding.buttonEdit.setOnClickListener(v -> AccountDialogUtil.displayEditDeviceDialog(this, device));

		binding.layoutMyDevices.addView(childBinding.getRoot());
	}

	/**
	 * Refresh the lists of contacts in display.
	 */
	public void refreshDisplayedContactList() {
		cleanupContactsFromList(binding.layoutMyDoms);
		cleanupContactsFromList(binding.layoutMySubs);
		for (Contact contact : ContactRegistry.getInstance().getContacts()) {
			addContactToView(contact);
		}
	}

	/**
	 * Configure the buttons for invitations.
	 */
	private void configureContactButtons() {
		binding.buttonCreateInvitation.setOnClickListener(v -> AccountDialogUtil.displayCreateInvitationDialog(this));
		binding.buttonAcceptInvitation.setOnClickListener(v -> AccountDialogUtil.displayAcceptInvitationDialog(this));

		binding.imageViewRefreshContacts.setOnClickListener(
				v -> ContactRegistry.getInstance().refreshContacts(getContext(), () -> {
					Activity activity = getActivity();
					if (activity != null) {
						activity.runOnUiThread(this::refreshDisplayedContactList);
					}
				}));
	}

	/**
	 * Handle the response of create account dialog.
	 *
	 * @param dialog The dialog.
	 * @param username The username.
	 * @param password The password.
	 */
	protected void handleCreateAccountDialogResponse(final CreateAccountDialogFragment dialog, final String username, final String password) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/createuser.php", false, null, null, (response, responseData) -> {
					if (responseData.isSuccess()) {
						dialog.dismiss();
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
						PreferenceUtil.setSharedPreferenceInt(R.string.key_pref_device_id, (int) responseData.getData().get("deviceId"));
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_device_name, (String) responseData.getData().get("deviceName"));
						PreferenceUtil.setSharedPreferenceBoolean(R.string.key_pref_device_muted, (boolean) responseData.getData().get("muted"));
						PreferenceUtil.setSharedPreferenceString(
								R.string.key_pref_device_display_strategy_normal, (String) responseData.getData().get("displayStrategyNormal"));
						PreferenceUtil.setSharedPreferenceString(
								R.string.key_pref_device_display_strategy_urgent, (String) responseData.getData().get("displayStrategyUrgent"));

						final Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> {
								binding.tableRowButtonsLogin.setVisibility(View.GONE);
								binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
								binding.tableRowUsername.setVisibility(View.VISIBLE);
								binding.textViewUsername.setText(username);
								binding.buttonCreateInvitation.setVisibility(View.VISIBLE);
								binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
								displaySingleDeviceInfo(Device.getThisDevice());
							});
						}

						ContactRegistry.getInstance().refreshContacts(getContext(), () -> {
							if (activity != null) {
								activity.runOnUiThread(() -> {
									refreshDisplayedContactList();
									((MainActivity) activity).navigateTo(R.id.nav_conversations);
								});
							}
						});
					}
					else {
						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
						}
					}
				}, "username", username, "password", password,
				"token", PreferenceUtil.getSharedPreferenceString(R.string.key_pref_messaging_token));
	}

	/**
	 * Handle the response of login dialog.
	 *
	 * @param dialog     The dialog.
	 * @param username   The username.
	 * @param password   The password.
	 * @param deviceName The device name.
	 */
	protected void handleLoginDialogResponse(final LoginDialogFragment dialog, final String username, final String password,
											 final String deviceName) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/login.php", false, null, null, (response, responseData) -> {
					if (responseData.isSuccess()) {
						dialog.dismiss();
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
						PreferenceUtil.setSharedPreferenceInt(R.string.key_pref_device_id, (int) responseData.getData().get("deviceId"));
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_device_name, (String) responseData.getData().get("deviceName"));
						PreferenceUtil.setSharedPreferenceBoolean(R.string.key_pref_device_muted, (boolean) responseData.getData().get("muted"));
						PreferenceUtil.setSharedPreferenceString(
								R.string.key_pref_device_display_strategy_normal, (String) responseData.getData().get("displayStrategyNormal"));
						PreferenceUtil.setSharedPreferenceString(
								R.string.key_pref_device_display_strategy_urgent, (String) responseData.getData().get("displayStrategyUrgent"));

						final Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> {
								binding.tableRowButtonsLogin.setVisibility(View.GONE);
								binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
								binding.tableRowUsername.setVisibility(View.VISIBLE);
								binding.textViewUsername.setText(username);
								binding.buttonCreateInvitation.setVisibility(View.VISIBLE);
								binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
								displaySingleDeviceInfo(Device.getThisDevice());
							});
						}
						ContactRegistry.getInstance().refreshContacts(getContext(), () -> {
							if (activity != null) {
								activity.runOnUiThread(this::refreshDisplayedContactList);
							}
						});
						updateDeviceInfo();
					}
					else {
						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> {
								dialog.displayError(responseData.getMappedErrorMessage(getContext()));
								if (responseData.getErrorCode() == 115) { // MAGIC_NUMBER user is logged in on another device.
									dialog.getBinding().layoutDeviceName.setVisibility(View.VISIBLE);
								}
							});
						}
					}
				}, "username", username, "password", password, "deviceName", deviceName,
				"token", PreferenceUtil.getSharedPreferenceString(R.string.key_pref_messaging_token));
	}

	/**
	 * Handle the response of change password dialog.
	 *
	 * @param dialog The dialog.
	 * @param newPassword The new password.
	 */
	protected void handleChangePasswordDialogResponse(final ChangePasswordDialogFragment dialog, final String newPassword) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/changepassword.php", (response, responseData) -> {
			if (responseData.isSuccess()) {
				PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, newPassword);
				dialog.dismiss();
			}
			else {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
				}
			}
		}, "newPassword", newPassword, "clientDeviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1)));
	}

	/**
	 * Handle the response of create invitation dialog.
	 *
	 * @param dialog      The dialog.
	 * @param amSlave     Flag indicating if my role is slave.
	 * @param myName      My name.
	 * @param contactName The contact name.
	 */
	protected void handleCreateInvitationDialogResponse(final CreateInvitationDialogFragment dialog, final boolean amSlave,
														final String myName, final String contactName) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/createinvitation.php", (response, responseData) -> {
			if (responseData.isSuccess()) {
				dialog.dismiss();

				String connectionCode = (String) responseData.getData().get("connectionCode");
				int relationId = (int) responseData.getData().get("relationId");

				Contact contact = new Contact(relationId, contactName, myName, -1, !amSlave, connectionCode, SlavePermissions.DEFAULT_SLAVE_PERMISSIONS,
						ContactStatus.INVITED, null, AiPolicy.NONE, null, null, null, null);
				ContactRegistry.getInstance().addOrUpdate(contact);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> addContactToView(contact));
				}
				sendInvitationUrl(connectionCode);
			}
			else {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
				}
			}
		}, "is_slave", amSlave ? "1" : "", "myname", myName, "contactname", contactName);
	}

	/**
	 * Send an invitation URL.
	 *
	 * @param connectionCode The connection code.
	 */
	private void sendInvitationUrl(final String connectionCode) {
		Intent messageIntent = new Intent(Intent.ACTION_SEND);
		messageIntent.setType("text/plain");
		messageIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_subject_invitation));
		messageIntent.putExtra(Intent.EXTRA_TEXT, "https://coachat.de/connect?code=" + connectionCode);
		startActivity(Intent.createChooser(messageIntent, null));
	}

	/**
	 * Handle the response of edit contact dialog.
	 *
	 * @param dialog The dialog.
	 * @param contact The new contact data.
	 */
	protected void handleEditContactDialogResponse(final EditContactDialogFragment dialog, final Contact contact) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/updatecontact.php", contact, null, (response, responseData) -> {
			if (responseData.isSuccess()) {
				dialog.dismiss();
				ContactRegistry.getInstance().addOrUpdate(contact);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(this::refreshDisplayedContactList);
				}
				AlarmReceiver.setAlarm(getContext(), contact);
			}
			else {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
				}
			}
				}, "myName", contact.getMyName(), "contactName", contact.getName(), "slavePermissions", contact.getSlavePermissions().toString(),
				"aiRelationId", contact.getAiRelationId() == null ? "" : Integer.toString(contact.getAiRelationId()),
				"aiPolicy", Integer.toString(contact.getAiPolicy().ordinal()), "aiMessageSuffix", contact.getAiMessageSuffix());
	}

	/**
	 * Handle the response of edit device dialog.
	 *
	 * @param dialog The dialog.
	 * @param device The new device data.
	 */
	protected void handleEditDeviceDialogResponse(final EditDeviceDialogFragment dialog, final Device device) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/updatedevice.php", (response, responseData) -> {
					if (responseData.isSuccess()) {
						dialog.dismiss();
						updateDeviceInfo();
					}
					else {
						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
						}
					}
				}, "deviceName", device.getName(), "deviceId", Integer.toString(device.getId()), "muted", device.isMuted() ? "1" : "",
				"displayStrategyNormal", device.getDisplayStrategyNormal().toString(),
				"displayStrategyUrgent", device.getDisplayStrategyUrgent().toString(),
				"clientDeviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1)));
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Inform about contacts changed.
		 */
		CONTACTS_UPDATED,
		/**
		 * Inform about devices changed.
		 */
		DEVICES_CHANGED,
		/**
		 * This device has been logged out.
		 */
		DEVICE_LOGGED_OUT
	}
}
