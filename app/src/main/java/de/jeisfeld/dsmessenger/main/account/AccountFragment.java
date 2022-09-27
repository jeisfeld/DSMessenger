package de.jeisfeld.dsmessenger.main.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.FragmentAccountBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.ChangePasswordDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.CreateAccountDialogFragment;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil.LoginDialogFragment;
import de.jeisfeld.dsmessenger.util.Logger;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

public class AccountFragment extends Fragment {

	private FragmentAccountBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

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

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
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
	 * Configure the buttons for invitations.
	 */
	private void configureInvitationButtons() {
		binding.buttonCreateInvitation.setOnClickListener(v -> {

		});
	}


	/**
	 * Handle the response of create account dialog.
	 */
	protected void handleCreateAccountDialogResponse(CreateAccountDialogFragment dialog, String username, String password) {
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
	 */
	protected void handleLoginDialogResponse(LoginDialogFragment dialog, String username, String password) {
		new HttpSender().sendMessage("db/usermanagement/login.php", false, (response, responseData) -> {
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
	 * Handle the response of change password dialog.
	 */
	protected void handleChangePasswordDialogResponse(ChangePasswordDialogFragment dialog, String oldPassword, String newPassword) {
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
}