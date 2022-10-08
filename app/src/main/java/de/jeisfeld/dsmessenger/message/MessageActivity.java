package de.jeisfeld.dsmessenger.message;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.jeisfeld.dsmessenger.databinding.ActivityMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails.AdminType;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;

/**
 * Activity to display messages.
 */
public class MessageActivity extends AppCompatActivity {
	/**
	 * The resource key for the message.
	 */
	private static final String STRING_EXTRA_MESSAGE_DETAILS = "de.jeisfeld.dsmessenger.MESSAGE_DETAILS";
	/**
	 * String for storing message in instance state.
	 */
	private static final String STRING_MESSAGE = "MESSAGE";
	/**
	 * The contact currently on top.
	 */
	private static Contact currentTopContact = null;
	/**
	 * The binding of the activity.
	 */
	private ActivityMessageBinding binding;
	/**
	 * The message text.
	 */
	private String messageText;
	/**
	 * The message vibration.
	 */
	private MessageVibration messageVibration = null;

	/**
	 * Static helper method to create an intent for this activity.
	 *
	 * @param context            The context in which this activity is started.
	 * @param textMessageDetails The details of the message to be displayed
	 * @return the intent.
	 */
	public static Intent createIntent(final Context context, final TextMessageDetails textMessageDetails) {
		Intent intent = new Intent(context, MessageActivity.class);
		intent.putExtra(STRING_EXTRA_MESSAGE_DETAILS, textMessageDetails);
		if (textMessageDetails.getContact() != null && currentTopContact != null
				&& textMessageDetails.getContact().getRelationId() != currentTopContact.getRelationId()) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		else {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		return intent;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMessageBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if (savedInstanceState != null && savedInstanceState.getCharSequence(STRING_MESSAGE) != null) {
			messageText = savedInstanceState.getString(STRING_MESSAGE);
		}
		else {
			messageText = extractMessageText(getIntent());
		}

		handleIntentData(getIntent());
	}

	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		if (messageText.length() > 0 && messageText.charAt(messageText.length() - 1) != '\n') {
			messageText += "\n";
		}
		messageText += extractMessageText(intent);
		handleIntentData(intent);
		binding.buttonAcknowledge.setVisibility(View.VISIBLE);
	}

	@Override
	protected final void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STRING_MESSAGE, messageText);
	}

	/**
	 * Extract the message text form an intent.
	 *
	 * @param intent The intent.
	 * @return The message text.
	 */
	private String extractMessageText(final Intent intent) {
		return ((TextMessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS)).getMessageText();
	}

	/**
	 * Handle the data of a new intent.
	 *
	 * @param intent The intent.
	 */
	private void handleIntentData(final Intent intent) {
		binding.textviewMessage.setText(messageText);

		TextMessageDetails textMessageDetails = (TextMessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS);
		MessageActivity.currentTopContact = textMessageDetails.getContact();

		if (textMessageDetails.isVibrate()) {
			messageVibration = new MessageVibration(this);
			messageVibration.vibrate(textMessageDetails);
		}
		if (textMessageDetails.isDisplayOnLockScreen()) {
			displayOnLockScreen();
		}
		if (textMessageDetails.isLockMessage()) {
			startLockTask();
		}
		if (textMessageDetails.isKeepScreenOn()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		sendConfirmation(AdminType.MESSAGE_RECEIVED, textMessageDetails.getMessageId(), textMessageDetails.getContact());

		binding.buttonAcknowledge.setOnClickListener(v -> {
			if (messageVibration != null) {
				messageVibration.cancelVibration();
			}
			sendConfirmation(AdminType.MESSAGE_ACKNOWLEDGED, textMessageDetails.getMessageId(), textMessageDetails.getContact());
			binding.buttonAcknowledge.setVisibility(View.INVISIBLE);
		});
	}

	/**
	 * Send confirmation for the message.
	 *
	 * @param adminType The type of confirmation.
	 * @param messageId The messageId.
	 */
	private void sendConfirmation(AdminType adminType, UUID messageId, Contact contact) {
		new HttpSender(this).sendMessage(contact, messageId, null,
				"messageType", MessageType.ADMIN.name(), "adminType", adminType.name());
	}

	/**
	 * Ensure that the message is displayed on top of lock screen.
	 */
	private void displayOnLockScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setTurnScreenOn(true);
			setShowWhenLocked(true);
		}
		else {
			Window window = getWindow();
			window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		}
	}

}
