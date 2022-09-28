package de.jeisfeld.dsmessenger.main.account;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.DialogChangePasswordBinding;
import de.jeisfeld.dsmessenger.databinding.DialogCreateAccountBinding;
import de.jeisfeld.dsmessenger.databinding.DialogCreateInvitationBinding;
import de.jeisfeld.dsmessenger.databinding.DialogLoginBinding;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Helper class to show standard dialogs.
 */
public final class AccountDialogUtil {
	/**
	 * The min length of passwords.
	 */
	private static final int MIN_PASSWORD_LENGTH = 8;

	/**
	 * Hide default constructor.
	 */
	private AccountDialogUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Display dialog for account creation.
	 *
	 * @param accountFragment The triggering fragment.
	 */
	public static void displayCreateAccountDialog(final AccountFragment accountFragment) {
		CreateAccountDialogFragment fragment = new CreateAccountDialogFragment();

		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for login.
	 *
	 * @param accountFragment The triggering fragment.
	 */
	public static void displayLoginDialog(final AccountFragment accountFragment) {
		LoginDialogFragment fragment = new LoginDialogFragment();

		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for change password.
	 *
	 * @param accountFragment The triggering fragment.
	 */
	public static void displayChangePasswordDialog(final AccountFragment accountFragment) {
		ChangePasswordDialogFragment fragment = new ChangePasswordDialogFragment();

		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for create invitation.
	 *
	 * @param accountFragment The triggering fragment.
	 */
	public static void displayCreateInvitationDialog(final AccountFragment accountFragment) {
		CreateInvitationDialogFragment fragment = new CreateInvitationDialogFragment();

		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Fragment to create an account.
	 */
	public static class CreateAccountDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogCreateAccountBinding binding;

		/**
		 * Display an error in the dialog.
		 *
		 * @param resource The text resource.
		 */
		public void displayError(final int resource) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(resource);
		}

		/**
		 * Display an error in the dialog.
		 *
		 * @param message The error message.
		 */
		public void displayError(final String message) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(message);
		}

		@NonNull
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			binding = DialogCreateAccountBinding.inflate(getLayoutInflater());

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_create_account).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonCreateAccount.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextUsername.getText() == null || binding.editTextUsername.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_username);
					return;
				}
				if (binding.editTextPassword.getText() == null || binding.editTextRepeatPassword.getText() == null
						|| !binding.editTextRepeatPassword.getText().toString().equals(binding.editTextPassword.getText().toString())) {
					displayError(R.string.error_passwords_do_not_match);
					return;
				}
				if (binding.editTextPassword.getText() == null || binding.editTextPassword.getText().length() < MIN_PASSWORD_LENGTH) {
					displayError(R.string.error_password_too_short);
					return;
				}
				((AccountFragment) requireParentFragment()).handleCreateAccountDialogResponse(this,
						binding.editTextUsername.getText().toString().trim(), binding.editTextPassword.getText().toString());
			});

			return builder.create();
		}
	}

	/**
	 * Fragment to login.
	 */
	public static class LoginDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogLoginBinding binding;

		/**
		 * Display an error in the dialog.
		 *
		 * @param resource The text resource.
		 */
		public void displayError(final int resource) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(resource);
		}

		/**
		 * Display an error in the dialog.
		 *
		 * @param message The error message.
		 */
		public void displayError(final String message) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(message);
		}

		@NonNull
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			binding = DialogLoginBinding.inflate(getLayoutInflater());

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_login).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonLogin.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextUsername.getText() == null || binding.editTextUsername.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_username);
					return;
				}
				if (binding.editTextPassword.getText() == null || binding.editTextPassword.getText().length() < MIN_PASSWORD_LENGTH) {
					displayError(R.string.error_password_too_short);
					return;
				}
				((AccountFragment) requireParentFragment()).handleLoginDialogResponse(this,
						binding.editTextUsername.getText().toString().trim(), binding.editTextPassword.getText().toString());
			});

			return builder.create();
		}
	}

	/**
	 * Fragment to change a password.
	 */
	public static class ChangePasswordDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogChangePasswordBinding binding;

		/**
		 * Display an error in the dialog.
		 *
		 * @param resource The text resource.
		 */
		public void displayError(final int resource) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(resource);
		}

		/**
		 * Display an error in the dialog.
		 *
		 * @param message The error message.
		 */
		public void displayError(final String message) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(message);
		}

		@NonNull
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			binding = DialogChangePasswordBinding.inflate(getLayoutInflater());

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_change_password).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonChangePassword.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextOldPassword.getText() == null || !binding.editTextOldPassword.getText().toString().equals(
						PreferenceUtil.getSharedPreferenceString(R.string.key_pref_password))) {
					displayError(R.string.error_wrong_old_password);
					return;
				}
				if (binding.editTextNewPassword.getText() == null || binding.editTextRepeatPassword.getText() == null
						|| !binding.editTextRepeatPassword.getText().toString().equals(binding.editTextNewPassword.getText().toString())) {
					displayError(R.string.error_passwords_do_not_match);
					return;
				}
				if (binding.editTextNewPassword.getText() == null || binding.editTextNewPassword.getText().length() < MIN_PASSWORD_LENGTH) {
					displayError(R.string.error_password_too_short);
					return;
				}
				((AccountFragment) requireParentFragment()).handleChangePasswordDialogResponse(this,
						binding.editTextOldPassword.getText().toString().trim(), binding.editTextNewPassword.getText().toString());
			});

			return builder.create();
		}
	}

	/**
	 * Fragment to create an invitation dialog.
	 */
	public static class CreateInvitationDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogCreateInvitationBinding binding;

		/**
		 * Display an error in the dialog.
		 *
		 * @param resource The text resource.
		 */
		public void displayError(final int resource) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(resource);
		}

		/**
		 * Display an error in the dialog.
		 *
		 * @param message The error message.
		 */
		public void displayError(final String message) {
			binding.textViewErrorMessage.setVisibility(View.VISIBLE);
			binding.textViewErrorMessage.setText(message);
		}

		@NonNull
		@Override
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			binding = DialogCreateInvitationBinding.inflate(getLayoutInflater());

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_create_invitation).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonCreateInvitation.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextContactName.getText() == null || binding.editTextContactName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_contactname);
					return;
				}
				String myName = binding.editTextMyName.getText() == null ? null : binding.editTextMyName.getText().toString();
				((AccountFragment) requireParentFragment()).handleCreateInvitationDialogResponse(this, binding.radioButtonSub.isChecked(),
						myName, binding.editTextContactName.getText().toString().trim());
			});

			return builder.create();
		}
	}
}
