package de.jeisfeld.dsmessenger.main.account;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentAccountBinding;
import de.jeisfeld.dsmessenger.databinding.ListViewContactBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.MainActivity;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.ChangePasswordDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.CreateAccountDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.CreateInvitationDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.EditContactDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.LoginDialogFragment;
import de.jeisfeld.dsmessenger.main.account.Contact.ContactStatus;
import de.jeisfeld.dsmessenger.util.DialogUtil;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * The fragment for account administration.
 */
public class AccountFragment extends Fragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.dsmessenger.account.AccountFragment";
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
				case CONTACTS_CHANGED:
					if (binding != null) {
						refreshDisplayedContactList();
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

		for (Contact contact : ContactRegistry.getInstance().getContacts()) {
			addContactToView(contact);
		}

		return binding.getRoot();
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

		if (contact.isSlave() || contact.getStatus() == ContactStatus.INVITED) {
			childBinding.buttonDelete.setVisibility(View.VISIBLE);
			childBinding.buttonDelete.setOnClickListener(v -> DialogUtil.displayConfirmationMessage(getActivity(),
					dialog -> new HttpSender(getContext()).sendMessage("db/usermanagement/deletecontact.php", (response, responseData) -> {
								if (responseData == null) {
									Log.e(Application.TAG, "Error in server communication: " + response);
								}
								else if (responseData.isSuccess()) {
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
			childBinding.buttonEdit.setVisibility(View.VISIBLE);
			childBinding.buttonEdit.setOnClickListener(v -> AccountDialogUtil.displayEditContactDialog(this, contact));
		}
		else {
			childBinding.buttonDelete.setVisibility(View.GONE);
			childBinding.buttonEdit.setVisibility(View.GONE);
		}

		layout.addView(childBinding.getRoot());

		Activity activity = getActivity();
		if (activity != null) {
			((MainActivity) activity).updateNavigationDrawer();
		}
	}

	/**
	 * Configure the buttons for my account.
	 */
	private void configureMyAccountButtons() {
		binding.buttonCreateAccount.setOnClickListener(v -> AccountDialogUtil.displayCreateAccountDialog(this));

		binding.buttonLogin.setOnClickListener(v -> AccountDialogUtil.displayLoginDialog(this));

		binding.buttonChangePassword.setOnClickListener(v -> AccountDialogUtil.displayChangePasswordDialog(this));

		binding.buttonLogout.setOnClickListener(v -> new HttpSender(getContext()).sendMessage("db/usermanagement/logout.php", (response, responseData) -> {
			PreferenceUtil.removeSharedPreference(R.string.key_pref_username);
			PreferenceUtil.removeSharedPreference(R.string.key_pref_password);
			ContactRegistry.getInstance().cleanContacts();
			if (responseData != null && responseData.isSuccess()) {
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
					});
				}
			}
		}));
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
	 * @param dialog   The dialog.
	 * @param username The username.
	 * @param password The password.
	 */
	protected void handleCreateAccountDialogResponse(final CreateAccountDialogFragment dialog, final String username, final String password) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/createuser.php", false, null, null, (response, responseData) -> {
					if (responseData == null) {
						Log.e(Application.TAG, "Error in server communication: " + response);

						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
						}
					}
					else if (responseData.isSuccess()) {
						dialog.dismiss();

						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> {
								binding.tableRowButtonsLogin.setVisibility(View.GONE);
								binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
								binding.tableRowUsername.setVisibility(View.VISIBLE);
								binding.textViewUsername.setText(username);
								binding.buttonCreateInvitation.setVisibility(View.VISIBLE);
								binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
								PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
								PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
							});
						}
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
	 * @param dialog   The dialog.
	 * @param username The username.
	 * @param password The password.
	 */
	protected void handleLoginDialogResponse(final LoginDialogFragment dialog, final String username, final String password) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/login.php", false, null, null, (response, responseData) -> {
					if (responseData == null) {
						Log.e(Application.TAG, "Error in server communication: " + response);
						Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
						}
					}
					else if (responseData.isSuccess()) {
						dialog.dismiss();
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
						final Activity activity = getActivity();
						if (activity != null) {
							activity.runOnUiThread(() -> {
								binding.tableRowButtonsLogin.setVisibility(View.GONE);
								binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
								binding.tableRowUsername.setVisibility(View.VISIBLE);
								binding.textViewUsername.setText(username);
								binding.buttonCreateInvitation.setVisibility(View.VISIBLE);
								binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
							});
						}
						ContactRegistry.getInstance().refreshContacts(getContext(), () -> {
							if (activity != null) {
								activity.runOnUiThread(this::refreshDisplayedContactList);
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
	 * Handle the response of change password dialog.
	 *
	 * @param dialog      The dialog.
	 * @param oldPassword The old password.
	 * @param newPassword The new password.
	 */
	protected void handleChangePasswordDialogResponse(final ChangePasswordDialogFragment dialog, final String oldPassword, final String newPassword) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/changepassword.php", (response, responseData) -> {
			if (responseData == null) {
				Log.e(Application.TAG, "Error in server communication: " + response);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
				}
			}
			else if (responseData.isSuccess()) {
				PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, newPassword);
				dialog.dismiss();
			}
			else {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
				}
			}
		}, "newpassword", newPassword);
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
			if (responseData == null) {
				Log.e(Application.TAG, "Error in server communication: " + response);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
				}
			}
			else if (responseData.isSuccess()) {
				dialog.dismiss();

				String connectionCode = (String) responseData.getData().get("connectionCode");
				int relationId = (int) responseData.getData().get("relationId");

				Contact contact = new Contact(relationId, contactName, myName, -1, !amSlave, connectionCode, ContactStatus.INVITED);
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
		messageIntent.putExtra(Intent.EXTRA_TEXT, "https://jeisfeld.de/dsmessenger/connect?code=" + connectionCode);
		startActivity(Intent.createChooser(messageIntent, null));
	}

	/**
	 * Handle the response of edit contact dialog.
	 *
	 * @param dialog  The dialog.
	 * @param contact The new contact data.
	 */
	protected void handleEditContactDialogResponse(final EditContactDialogFragment dialog, final Contact contact) {
		new HttpSender(getContext()).sendMessage("db/usermanagement/updatecontact.php", contact, null, (response, responseData) -> {
			if (responseData == null) {
				Log.e(Application.TAG, "Error in server communication: " + response);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
				}
			}
			else if (responseData.isSuccess()) {
				dialog.dismiss();
				ContactRegistry.getInstance().addOrUpdate(contact);
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(this::refreshDisplayedContactList);
				}
			}
			else {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(() -> dialog.displayError(responseData.getMappedErrorMessage(getContext())));
				}
			}
		}, "myName", contact.getMyName(), "contactName", contact.getName());
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Inform about contacts changed.
		 */
		CONTACTS_CHANGED
	}
}
