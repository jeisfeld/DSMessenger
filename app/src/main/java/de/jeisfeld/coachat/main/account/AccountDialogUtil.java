package de.jeisfeld.coachat.main.account;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.databinding.DialogAcceptInvitationBinding;
import de.jeisfeld.coachat.databinding.DialogChangePasswordBinding;
import de.jeisfeld.coachat.databinding.DialogCreateAccountBinding;
import de.jeisfeld.coachat.databinding.DialogCreateInvitationBinding;
import de.jeisfeld.coachat.databinding.DialogEditContactBinding;
import de.jeisfeld.coachat.databinding.DialogEditConversationBinding;
import de.jeisfeld.coachat.databinding.DialogEditDeviceBinding;
import de.jeisfeld.coachat.databinding.DialogLoginBinding;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Contact.AiPolicy;
import de.jeisfeld.coachat.entity.Contact.ContactStatus;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.entity.ConversationFlags;
import de.jeisfeld.coachat.entity.Device;
import de.jeisfeld.coachat.entity.ReplyPolicy;
import de.jeisfeld.coachat.entity.SlavePermissions;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.MainActivity;
import de.jeisfeld.coachat.main.account.AccountFragment.ActionType;
import de.jeisfeld.coachat.main.message.EditConversationParentFragment;
import de.jeisfeld.coachat.message.MessageDisplayStrategy;
import de.jeisfeld.coachat.message.MessageDisplayStrategy.MessageDisplayType;
import de.jeisfeld.coachat.util.DropdownHandler;
import de.jeisfeld.coachat.util.PreferenceUtil;

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
	 * Display dialog for accept invitation.
	 *
	 * @param accountFragment The triggering fragment.
	 */
	public static void displayAcceptInvitationDialog(final AccountFragment accountFragment) {
		AcceptInvitationDialogFragment fragment = new AcceptInvitationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("fromActivity", false);
		fragment.setArguments(bundle);
		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for accept invitation.
	 *
	 * @param activity       The triggering activity.
	 * @param connectionCode The connection code.
	 */
	public static void displayAcceptInvitationDialog(final MainActivity activity, final String connectionCode) {
		AcceptInvitationDialogFragment fragment = new AcceptInvitationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("fromActivity", true);
		if (connectionCode != null) {
			bundle.putString("connectionCode", connectionCode);
		}
		fragment.setArguments(bundle);
		try {
			fragment.show(activity.getSupportFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for edit contact.
	 *
	 * @param accountFragment The triggering fragment.
	 * @param contact         The contact.
	 */
	public static void displayEditContactDialog(final AccountFragment accountFragment, final Contact contact) {
		EditContactDialogFragment fragment = new EditContactDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("contact", contact);
		fragment.setArguments(bundle);
		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for edit device.
	 *
	 * @param accountFragment The triggering fragment.
	 * @param device          The device
	 */
	public static void displayEditDeviceDialog(final AccountFragment accountFragment, final Device device) {
		EditDeviceDialogFragment fragment = new EditDeviceDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("device", device);
		fragment.setArguments(bundle);
		try {
			fragment.show(accountFragment.getChildFragmentManager(), fragment.getClass().toString());
		}
		catch (IllegalStateException e) {
			// May appear if activity is not active any more - ignore.
		}
	}

	/**
	 * Display dialog for edit conversation.
	 *
	 * @param triggeringFragment The triggering fragment.
	 * @param conversation       The conversation
	 * @param contact            The contact
	 */
	public static void displayEditConversationDialog(final EditConversationParentFragment triggeringFragment, final Conversation conversation,
													 final Contact contact) {
		EditConversationDialogFragment fragment = new EditConversationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("conversation", conversation);
		bundle.putSerializable("contact", contact);
		fragment.setArguments(bundle);
		try {
			fragment.show(((Fragment) triggeringFragment).getChildFragmentManager(), fragment.getClass().toString());
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

		protected final DialogLoginBinding getBinding() {
			return binding;
		}

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
						binding.editTextUsername.getText().toString().trim(), binding.editTextPassword.getText().toString(),
						binding.editTextDeviceName.getText() == null ? "" : binding.editTextDeviceName.getText().toString());
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
						binding.editTextNewPassword.getText().toString());
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

			if (MainActivity.isDsUser()) {
				binding.radioButtonSub.setText(R.string.text_sub);
				binding.radioButtonDom.setText(R.string.text_dom);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_create_invitation).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonCreateInvitation.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextContactName.getText() == null || binding.editTextContactName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_contactname);
					return;
				}
				String myName = binding.editTextMyName.getText() == null ? null : binding.editTextMyName.getText().toString().trim();
				((AccountFragment) requireParentFragment()).handleCreateInvitationDialogResponse(this, binding.radioButtonSub.isChecked(),
						myName, binding.editTextContactName.getText().toString().trim());
			});

			return builder.create();
		}
	}

	/**
	 * Fragment to accept an invitation dialog.
	 */
	public static class AcceptInvitationDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogAcceptInvitationBinding binding;
		/**
		 * The relation id.
		 */
		private int relationId;
		/**
		 * The contact id.
		 */
		private int contactId;

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
			binding = DialogAcceptInvitationBinding.inflate(getLayoutInflater());

			assert getArguments() != null;
			final String initialConnectionCode = getArguments().getString("connectionCode");
			if (initialConnectionCode != null) {
				binding.editTextConnectionCode.setText(initialConnectionCode);
				binding.editTextConnectionCode.setEnabled(false);
			}
			if (MainActivity.isDsUser()) {
				binding.radioButtonSub.setText(R.string.text_sub);
				binding.radioButtonDom.setText(R.string.text_dom);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.button_accept_invitation).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonReviewInvitation.setOnClickListener(v -> {
				if (binding.editTextConnectionCode.getText() == null || binding.editTextConnectionCode.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_connectioncode);
					return;
				}
				String connectionCode = binding.editTextConnectionCode.getText().toString().trim();
				if (connectionCode.length() != 24 || !connectionCode.startsWith("m") && !connectionCode.startsWith("s")) { // MAGIC_NUMBER
					displayError(R.string.error_invalid_connectioncode);
					return;
				}
				new HttpSender(getContext()).sendMessage("db/usermanagement/queryconnectioncode.php", (response, responseData) -> {
					if (responseData.isSuccess()) {
						relationId = (int) responseData.getData().get("relationId");
						String myName = (String) responseData.getData().get("myname");
						String contactName = (String) responseData.getData().get("contactname");
						contactId = (int) responseData.getData().get("contactId");
						boolean isSlave = (Boolean) responseData.getData().get("isSlave");
						getActivity().runOnUiThread(() -> {
							binding.editTextMyName.setText(myName);
							binding.editTextContactName.setText(contactName);
							if (isSlave) {
								binding.radioButtonSub.setChecked(true);
								binding.editTextMyName.setEnabled(false);
								if (binding.editTextContactName.getText() != null
										&& binding.editTextContactName.getText().toString().trim().length() > 0) {
									binding.editTextContactName.setEnabled(false);
								}
							}
							else {
								binding.radioButtonDom.setChecked(true);
							}
							binding.tableMyAccount.setVisibility(View.VISIBLE);
							binding.buttonReviewInvitation.setVisibility(View.INVISIBLE);
							binding.buttonAcceptInvitation.setVisibility(View.VISIBLE);
						});
					}
					else {
						getActivity().runOnUiThread(() -> displayError(responseData.getMappedErrorMessage(getContext())));
					}
				}, "connectioncode", connectionCode);
			});

			binding.buttonAcceptInvitation.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextMyName.getText() == null || binding.editTextMyName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_ownname);
					return;
				}
				if (binding.editTextContactName.getText() == null || binding.editTextContactName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_contactname);
					return;
				}
				String myName = binding.editTextMyName.getText().toString().trim();
				String contactName = binding.editTextContactName.getText().toString().trim();
				boolean amSlave = binding.radioButtonSub.isChecked();
				String connectionCode = binding.editTextConnectionCode.getText().toString().trim();

				new HttpSender(getContext()).sendMessage("db/usermanagement/acceptinvitation.php", (response, responseData) -> {
							if (responseData.isSuccess()) {
								dismiss();
								// TODO: fill slave permissions and AI Policy.
								Contact contact = new Contact(relationId, contactName, myName, contactId, !amSlave, null, null,
										ContactStatus.CONNECTED, null, AiPolicy.NONE, null, null, null, null);
								ContactRegistry.getInstance().addOrUpdate(contact);

								AccountFragment.sendBroadcast(getContext(), ActionType.CONTACTS_UPDATED);
							}
							else {
								requireActivity().runOnUiThread(() -> displayError(responseData.getMappedErrorMessage(getContext())));
							}
						}, "isSlave", amSlave ? "" : "1", "myname", myName, "contactname", contactName,
						"contactId", Integer.toString(contactId), "connectioncode", connectionCode,
						"relationId", Integer.toString(relationId));
			});

			return builder.create();
		}
	}

	/**
	 * Fragment to edit contact dialog.
	 */
	public static class EditContactDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogEditContactBinding binding;

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
			binding = DialogEditContactBinding.inflate(getLayoutInflater());

			assert getArguments() != null;
			final Contact contact = (Contact) getArguments().getSerializable("contact");

			binding.layoutContactName.setVisibility(contact.getMyPermissions().isEditRelation() ? View.VISIBLE : View.GONE);
			binding.layoutSlavePermissions.setVisibility(contact.getMyPermissions().isEditSlavePermissions() ? View.VISIBLE : View.GONE);

			binding.editTextMyName.setText(contact.getMyName());
			binding.editTextContactName.setText(contact.getName());
			binding.editTextAiMessageSuffix.setText(contact.getAiMessageSuffix());
			binding.checkboxAiTimeout.setOnCheckedChangeListener((buttonView, isChecked) ->
					binding.layoutAiTimeoutValue.setVisibility(isChecked ? View.VISIBLE : View.GONE));
			binding.checkboxAiTimeout.setChecked(contact.getAiTimeout() != null);
			binding.layoutAiSettings.setVisibility(contact.getAiPolicy() == AiPolicy.NONE ? View.GONE : View.VISIBLE);

			binding.checkboxEditSlavePermissions.setChecked(contact.getSlavePermissions().isEditSlavePermissions());
			binding.checkboxEditRelation.setChecked(contact.getSlavePermissions().isEditRelation());
			binding.checkboxManageConversations.setChecked(contact.getSlavePermissions().isManageConversations());
			final DropdownHandler<String> dropdownHandlerDefaultReplyPolicy = DropdownHandler.fromResource(getContext(),
					binding.dropdownDefaultReplyPolicy, R.array.array_reply_policies,
					contact.getSlavePermissions().getDefaultReplyPolicy().ordinal());
			int timerUnitSelection = splitTimerValue(contact);
			final DropdownHandler<String> dropdownHandlerTimeUnit = DropdownHandler.fromResource(getContext(),
					binding.dropdownAiTimeoutUnit, R.array.array_timer_unit_names, timerUnitSelection);

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.title_dialog_edit_contact).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonUpdateContact.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (contact.getStatus() == ContactStatus.CONNECTED
						&& (binding.editTextMyName.getText() == null || binding.editTextMyName.getText().toString().trim().length() == 0)) {
					displayError(R.string.error_missing_ownname);
					return;
				}
				if (binding.editTextContactName.getText() == null || binding.editTextContactName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_contactname);
					return;
				}
				String myName = binding.editTextMyName.getText().toString().trim();
				String contactName = binding.editTextContactName.getText().toString().trim();
				String messageSuffix = binding.editTextAiMessageSuffix.getText().toString().trim();

				SlavePermissions newSlavePermissions = new SlavePermissions(binding.checkboxEditSlavePermissions.isChecked(),
						binding.checkboxEditRelation.isChecked(), binding.checkboxManageConversations.isChecked(),
						ReplyPolicy.fromOrdinal(dropdownHandlerDefaultReplyPolicy.getSelectedPosition()));

				Long aiTimeout = null;
				if (binding.checkboxAiTimeout.isChecked()) {
					int aiTimeoutValue;
					try {
						aiTimeoutValue = Integer.parseInt(binding.editTextAiTimeoutValue.getText().toString());
					}
					catch (NumberFormatException e) {
						aiTimeoutValue = 1;
					}
					aiTimeout = 1000 * aiTimeoutValue *
							(long) (getResources().getIntArray(R.array.array_timer_unit_values))[dropdownHandlerTimeUnit.getSelectedPosition()];
				}

				Contact newContact = new Contact(contact.getRelationId(), contactName, myName, contact.getContactId(), contact.isSlave(),
						contact.getConnectionCode(), newSlavePermissions, contact.getStatus(), contact.getAiRelationId(), contact.getAiPolicy(),
						contact.getAiUsername(), contact.getAiAddPrimingText(), messageSuffix, aiTimeout);
				((AccountFragment) requireParentFragment()).handleEditContactDialogResponse(this, newContact);

			});

			return builder.create();
		}

		private int splitTimerValue(final Contact contact) {
			if (contact.getAiTimeout() == null || contact.getAiTimeout() <= 0) {
				binding.editTextAiTimeoutValue.setText("1");
				return 2;
			}

			long timerValueSeconds = contact.getAiTimeout() / 1000;
			int[] timerUnitValues = getResources().getIntArray(R.array.array_timer_unit_values);

			for (int i = timerUnitValues.length - 1; i >= 0; i--) {
				int timerUnitValue = timerUnitValues[i];
				if (timerValueSeconds % timerUnitValue == 0) {
					binding.editTextAiTimeoutValue.setText(Long.toString(timerValueSeconds / timerUnitValue));
					return i;
				}
			}
			return 1;
		}
	}


	/**
	 * Fragment to edit device dialog.
	 */
	public static class EditDeviceDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogEditDeviceBinding binding;

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
			binding = DialogEditDeviceBinding.inflate(getLayoutInflater());

			assert getArguments() != null;
			final Device device = (Device) getArguments().getSerializable("device");

			binding.editTextDeviceName.setText(device.getName());

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.title_dialog_edit_device).setView(binding.getRoot());

			final DropdownHandler<String> dropdownHandlerMessageDisplayTypeNormal = DropdownHandler.fromResource(getContext(),
					binding.dropdownMessageDisplayTypeNormal, R.array.array_message_display_types,
					device.getDisplayStrategyNormal().getMessageDisplayType().ordinal());
			binding.checkboxVibrateNormal.setChecked(device.getDisplayStrategyNormal().isVibrate());
			binding.checkboxRepeatVibrationNormal.setChecked(device.getDisplayStrategyNormal().isVibrationRepeated());
			binding.checkboxRepeatVibrationNormal.setVisibility(device.getDisplayStrategyNormal().isVibrate() ? View.VISIBLE : View.GONE);
			binding.checkboxDisplayOnLockScreenNormal.setChecked(device.getDisplayStrategyNormal().isDisplayOnLockScreen());
			binding.checkboxKeepScreenOnNormal.setChecked(device.getDisplayStrategyNormal().isKeepScreenOn());
			binding.checkboxLockMessageNormal.setChecked(device.getDisplayStrategyNormal().isLockMessage());
			final DropdownHandler<String> dropdownHandlerVibrationStyleNormal = DropdownHandler.fromResource(getContext(),
					binding.dropdownVibrationStyleNormal, R.array.array_vibrate_pattern_names,
					device.getDisplayStrategyNormal().getVibrationPattern());
			binding.layoutVibrationStyleNormal.setVisibility(device.getDisplayStrategyNormal().isVibrate() ? View.VISIBLE : View.GONE);

			final DropdownHandler<String> dropdownHandlerMessageDisplayTypeUrgent = DropdownHandler.fromResource(getContext(),
					binding.dropdownMessageDisplayTypeUrgent, R.array.array_message_display_types,
					device.getDisplayStrategyUrgent().getMessageDisplayType().ordinal());
			binding.checkboxVibrateUrgent.setChecked(device.getDisplayStrategyUrgent().isVibrate());
			binding.checkboxRepeatVibrationUrgent.setChecked(device.getDisplayStrategyUrgent().isVibrationRepeated());
			binding.checkboxRepeatVibrationUrgent.setVisibility(device.getDisplayStrategyUrgent().isVibrate() ? View.VISIBLE : View.GONE);
			binding.checkboxDisplayOnLockScreenUrgent.setChecked(device.getDisplayStrategyUrgent().isDisplayOnLockScreen());
			binding.checkboxKeepScreenOnUrgent.setChecked(device.getDisplayStrategyUrgent().isKeepScreenOn());
			binding.checkboxLockMessageUrgent.setChecked(device.getDisplayStrategyUrgent().isLockMessage());
			final DropdownHandler<String> dropdownHandlerVibrationStyleUrgent = DropdownHandler.fromResource(getContext(),
					binding.dropdownVibrationStyleUrgent, R.array.array_vibrate_pattern_names,
					device.getDisplayStrategyUrgent().getVibrationPattern());
			binding.layoutVibrationStyleUrgent.setVisibility(device.getDisplayStrategyUrgent().isVibrate() ? View.VISIBLE : View.GONE);

			binding.checkboxVibrateNormal.setOnCheckedChangeListener((buttonView, isChecked) -> {
				binding.checkboxRepeatVibrationNormal.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				binding.layoutVibrationStyleNormal.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			});
			binding.checkboxVibrateUrgent.setOnCheckedChangeListener((buttonView, isChecked) -> {
				binding.checkboxRepeatVibrationUrgent.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				binding.layoutVibrationStyleUrgent.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			});

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonUpdateDevice.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextDeviceName.getText() == null || binding.editTextDeviceName.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_devicename);
					return;
				}
				String deviceName = binding.editTextDeviceName.getText().toString().trim();
				Device newDevice = new Device(device.getId(), deviceName, device.isMuted(),
						new MessageDisplayStrategy(MessageDisplayType.fromOrdinal(dropdownHandlerMessageDisplayTypeNormal.getSelectedPosition()),
								binding.checkboxDisplayOnLockScreenNormal.isChecked(), binding.checkboxVibrateNormal.isChecked(),
								binding.checkboxRepeatVibrationNormal.isChecked(), dropdownHandlerVibrationStyleNormal.getSelectedPosition(),
								binding.checkboxKeepScreenOnNormal.isChecked(), binding.checkboxLockMessageNormal.isChecked()),
						new MessageDisplayStrategy(MessageDisplayType.fromOrdinal(dropdownHandlerMessageDisplayTypeUrgent.getSelectedPosition()),
								binding.checkboxDisplayOnLockScreenUrgent.isChecked(), binding.checkboxVibrateUrgent.isChecked(),
								binding.checkboxRepeatVibrationUrgent.isChecked(), dropdownHandlerVibrationStyleUrgent.getSelectedPosition(),
								binding.checkboxKeepScreenOnUrgent.isChecked(), binding.checkboxLockMessageUrgent.isChecked()),
						device.isThis());
				((AccountFragment) requireParentFragment()).handleEditDeviceDialogResponse(this, newDevice);
			});

			return builder.create();
		}

	}

	/**
	 * Fragment to edit conversation dialog.
	 */
	public static class EditConversationDialogFragment extends DialogFragment {
		/**
		 * The binding of the view.
		 */
		private DialogEditConversationBinding binding;

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
			binding = DialogEditConversationBinding.inflate(getLayoutInflater());

			assert getArguments() != null;
			final Conversation conversation = (Conversation) getArguments().getSerializable("conversation");
			final Contact contact = (Contact) getArguments().getSerializable("contact");

			binding.editTextSubject.setText(conversation.getSubject());
			final DropdownHandler<String> dropdownHandlerReplyPolicy = DropdownHandler.fromResource(getContext(),
					binding.dropdownReplyPolicy, R.array.array_reply_policies,
					conversation.getConversationFlags().getReplyPolicy().ordinal());
			binding.checkboxArchive.setChecked(conversation.isArchived());

			boolean allowReplyPolicy = contact.isSlave() || contact.getSlavePermissions().isEditSlavePermissions();
			binding.layoutReplyPolicy.setVisibility(allowReplyPolicy ? View.VISIBLE : View.GONE);

			AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
			builder.setTitle(R.string.title_dialog_edit_conversation).setView(binding.getRoot());

			binding.buttonCancel.setOnClickListener(v -> dismiss());

			binding.buttonUpdateConversation.setOnClickListener(v -> {
				binding.textViewErrorMessage.setVisibility(View.INVISIBLE);
				if (binding.editTextSubject.getText() == null || binding.editTextSubject.getText().toString().trim().length() == 0) {
					displayError(R.string.error_missing_subject);
					return;
				}
				String subject = binding.editTextSubject.getText().toString().trim();
				boolean archived = binding.checkboxArchive.isChecked();

				ReplyPolicy replyPolicy = ReplyPolicy.fromOrdinal(dropdownHandlerReplyPolicy.getSelectedPosition());
				Conversation newConversation = new Conversation(conversation.getRelationId(), subject,
						conversation.getConversationId().toString(), conversation.getLastTimestamp(),
						new ConversationFlags(replyPolicy, replyPolicy.isExpectsAcknowledgement(),
								replyPolicy.isExpectsResponse() && !replyPolicy.isExpectsAcknowledgement()).toString(),
						null, archived);

				((EditConversationParentFragment) requireParentFragment()).handleEditConversationDialogResponse(this, contact, newConversation);
			});

			return builder.create();
		}

	}
}
