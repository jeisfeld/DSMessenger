package de.jeisfeld.dsmessenger.main.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentAccountBinding;
import de.jeisfeld.dsmessenger.databinding.ListViewContactBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.ChangePasswordDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.CreateAccountDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.CreateInvitationDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.LoginDialogFragment;
import de.jeisfeld.dsmessenger.main.account.Contact.ContactStatus;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * The fragment for account administration.
 */
public class AccountFragment extends Fragment {
	/**
	 * The view binding.
	 */
	private FragmentAccountBinding binding;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater,
								   final ViewGroup container, final Bundle savedInstanceState) {

		binding = FragmentAccountBinding.inflate(inflater, container, false);
		String username = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_username);

		if (username == null) {
			binding.tableRowUsername.setVisibility(View.GONE);
			binding.tableRowButtonsLogout.setVisibility(View.GONE);
			binding.tableRowButtonsLogin.setVisibility(View.VISIBLE);
		}
		else {
			binding.tableRowUsername.setVisibility(View.VISIBLE);
			binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
			binding.tableRowButtonsLogin.setVisibility(View.GONE);
			binding.textViewUsername.setText(username);
		}

		configureMyAccountButtons();
		configureInvitationButtons();

		for (Contact contact : ContactRegistry.getInstance().getContacts()) {
			addContactToView(contact);
		}

		return binding.getRoot();
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
			break;
		case CONNECTED:
		default:
			childBinding.imageViewConfirmed.setVisibility(View.VISIBLE);
			break;
		}
		layout.addView(childBinding.getRoot());
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	/**
	 * Configure the buttons for my account.
	 */
	private void configureMyAccountButtons() {
		binding.buttonCreateAccount.setOnClickListener(v -> AccountDialogUtil.displayCreateAccountDialog(this));

		binding.buttonLogin.setOnClickListener(v -> AccountDialogUtil.displayLoginDialog(this));

		binding.buttonChangePassword.setOnClickListener(v -> AccountDialogUtil.displayChangePasswordDialog(this));

		binding.buttonLogout.setOnClickListener(v -> new HttpSender().sendMessage("db/usermanagement/logout.php", (response, responseData) -> {
			if (responseData != null && responseData.isSuccess()) {
				requireActivity().runOnUiThread(() -> {
					ContactRegistry.getInstance().cleanContacts();
					cleanDisplayedContacts();

					PreferenceUtil.removeSharedPreference(R.string.key_pref_username);
					PreferenceUtil.removeSharedPreference(R.string.key_pref_password);
					binding.tableRowButtonsLogin.setVisibility(View.VISIBLE);
					binding.tableRowButtonsLogout.setVisibility(View.GONE);
					binding.tableRowUsername.setVisibility(View.GONE);
					binding.textViewUsername.setText("");
				});
			}
		}));
	}

	/**
	 * Clean the displayed contacts in a list.
	 *
	 * @param linearLayout The list.
	 */
	private void cleanDisplayedContacts(LinearLayout linearLayout) {
		linearLayout.setVisibility(View.GONE);
		int contactCount = linearLayout.getChildCount();
		// Remove all views except the headline.
		for (int i = 1; i < contactCount; i++) {
			linearLayout.removeView(linearLayout.getChildAt(i));
		}
	}

	/**
	 * Clean all displayed contacts.
	 */
	private void cleanDisplayedContacts() {
		cleanDisplayedContacts(binding.layoutMyDoms);
		cleanDisplayedContacts(binding.layoutMySubs);
	}

	/**
	 * Configure the buttons for invitations.
	 */
	private void configureInvitationButtons() {
		binding.buttonCreateInvitation.setOnClickListener(v -> AccountDialogUtil.displayCreateInvitationDialog(this));
	}

	/**
	 * Handle the response of create account dialog.
	 *
	 * @param dialog   The dialog.
	 * @param username The username.
	 * @param password The password.
	 */
	protected void handleCreateAccountDialogResponse(final CreateAccountDialogFragment dialog, final String username, final String password) {
		new HttpSender().sendMessage("db/usermanagement/createuser.php", false, (response, responseData) -> {
					if (responseData == null) {
						Log.e(Application.TAG, "Error in server communication: " + response);
						requireActivity().runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
					}
					else if (responseData.isSuccess()) {
						dialog.dismiss();
						requireActivity().runOnUiThread(() -> {
							binding.tableRowButtonsLogin.setVisibility(View.GONE);
							binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
							binding.tableRowUsername.setVisibility(View.VISIBLE);
							binding.textViewUsername.setText(username);
							PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
							PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
						});
					}
					else {
						requireActivity().runOnUiThread(() -> dialog.displayError(responseData.getErrorMessage()));
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
		new HttpSender().sendMessage("db/usermanagement/login.php", false, (response, responseData) -> {
					if (responseData == null) {
						Log.e(Application.TAG, "Error in server communication: " + response);
						requireActivity().runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
					}
					else if (responseData.isSuccess()) {
						dialog.dismiss();
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_username, username);
						PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, password);
						requireActivity().runOnUiThread(() -> {
							binding.tableRowButtonsLogin.setVisibility(View.GONE);
							binding.tableRowButtonsLogout.setVisibility(View.VISIBLE);
							binding.tableRowUsername.setVisibility(View.VISIBLE);
							binding.textViewUsername.setText(username);
						});
						ContactRegistry.getInstance().refreshContacts(() -> getActivity().runOnUiThread(() -> {
							cleanDisplayedContacts();
							for (Contact contact : ContactRegistry.getInstance().getContacts()) {
								addContactToView(contact);
							}
						}));
					}
					else {
						requireActivity().runOnUiThread(() -> dialog.displayError(responseData.getErrorMessage()));
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
		new HttpSender().sendMessage("db/usermanagement/changepassword.php", (response, responseData) -> {
			if (responseData == null) {
				Log.e(Application.TAG, "Error in server communication: " + response);
				requireActivity().runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
			}
			else if (responseData.isSuccess()) {
				PreferenceUtil.setSharedPreferenceString(R.string.key_pref_password, newPassword);
				dialog.dismiss();
			}
			else {
				requireActivity().runOnUiThread(() -> dialog.displayError(responseData.getErrorMessage()));
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
		new HttpSender().sendMessage("db/usermanagement/createinvitation.php", (response, responseData) -> {
			if (responseData == null) {
				Log.e(Application.TAG, "Error in server communication: " + response);
				requireActivity().runOnUiThread(() -> dialog.displayError(R.string.error_technical_error));
			}
			else if (responseData.isSuccess()) {
				dialog.dismiss();

				String connectionCode = (String) responseData.getData().get("connectionCode");
				int relationId = (int) responseData.getData().get("relationId");

				Contact contact = new Contact(relationId, contactName, !amSlave, connectionCode, ContactStatus.INVITED);
				ContactRegistry.getInstance().addOrUpdate(contact);
				requireActivity().runOnUiThread(() -> addContactToView(contact));
			}
			else {
				requireActivity().runOnUiThread(() -> dialog.displayError(responseData.getErrorMessage()));
			}
		}, "is_slave", amSlave ? "1" : "", "myname", myName, "contactname", contactName);
	}
}
