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
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;
import de.jeisfeld.dsmessenger.databinding.ActivityMessageBinding;
import de.jeisfeld.dsmessenger.util.Logger;

public class MessageActivity extends AppCompatActivity {
	/**
	 * The resource key for the message.
	 */
	private static final String STRING_EXTRA_MESSAGE_DETAILS = "de.jeisfeld.dsmessenger.MESSAGE_DETAILS";
	/**
	 * The vibration pattern.
	 */
	private static final long[] VIBRATION_PATTERN = {0, 800, 400, 100, 200, 100, 200, 250, 300, 1000};

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
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		Logger.log("Created MessageActivity intent");
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.log("MessageActivity.onCreate()");

		ActivityMessageBinding binding = ActivityMessageBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		MessageDetails messageDetails = (MessageDetails) getIntent().getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS);
		binding.textviewFirst.setText(messageDetails.getMessageText());

		if (messageDetails.isVibrate()) {
			vibrate(true);
		}
		if (messageDetails.isDisplayOnLockScreen()) {
			displayOnLockScreen();
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

	/**
	 * Vibrate when displaying the message.
	 *
	 * @param forced Flag indicating if vibration should happen even if in silent mode
	 */
	private void vibrate(final boolean forced) {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (forced || am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			if (VERSION.SDK_INT >= VERSION_CODES.O) {
				vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1),
						new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build());
			}
			else {
				vibrator.vibrate(VIBRATION_PATTERN, -1);
			}
		}
	}

}