package de.jeisfeld.dsmessenger.message;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.jeisfeld.dsmessenger.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {
	/**
	 * The resource key for the message.
	 */
	private static final String STRING_EXTRA_MESSAGE_DETAILS = "de.jeisfeld.dsmessenger.MESSAGE_DETAILS";
	/**
	 * String for storing message in instance state.
	 */
	private static final String STRING_MESSAGE="MESSAGE";

	/**
	 * The binding of the activity.
	 */
	ActivityMessageBinding binding;
	/**
	 * The message text.
	 */
	CharSequence messageText;
	/**
	 * The message vibration.
	 */
	MessageVibration messageVibration = null;

	/**
	 * Static helper method to create an intent for this activity.
	 *
	 * @param context        The context in which this activity is started.
	 * @param messageDetails The details of the message to be displayed
	 * @return the intent.
	 */
	public static Intent createIntent(final Context context, final MessageDetails messageDetails) {
		Intent intent = new Intent(context, MessageActivity.class);
		intent.putExtra(STRING_EXTRA_MESSAGE_DETAILS, messageDetails);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMessageBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if(savedInstanceState != null && savedInstanceState.getCharSequence(STRING_MESSAGE) != null) {
			messageText = savedInstanceState.getCharSequence(STRING_MESSAGE);
		}
		else {
			messageText = extractMessageText(getIntent());
		}

		handleIntentData(getIntent());

		binding.buttonAcknowledge.setOnClickListener(v -> {
			if (messageVibration != null) {
				messageVibration.cancelVibration();
			}
			binding.buttonAcknowledge.setVisibility(View.INVISIBLE);
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (messageText.length() > 0 && messageText.charAt(messageText.length() - 1) != '\n') {
			messageText += "\n";
		}
		messageText += extractMessageText(intent);
		handleIntentData(intent);
		binding.buttonAcknowledge.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence(STRING_MESSAGE, messageText);
	}

	/**
	 * Extract the message text form an intent.
	 * @param intent The intent.
	 * @return The message text.
	 */
	private String extractMessageText(final Intent intent) {
		return ((MessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS)).getMessageText();
	}

	/**
	 * Handle the data of a new intent.
	 * @param intent The intent.
	 */
	private void handleIntentData(Intent intent) {
		binding.textviewMessage.setText(messageText);

		MessageDetails messageDetails = (MessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS);
		if (messageDetails.isVibrate()) {
			messageVibration = new MessageVibration(this);
			messageVibration.vibrate(messageDetails);
		}
		if (messageDetails.isDisplayOnLockScreen()) {
			displayOnLockScreen();
		}
		if (messageDetails.isLockMessage()) {
			startLockTask();
		}
		if (messageDetails.isKeepScreenOn()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
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