package de.jeisfeld.dsmessenger.message;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * Class for handling vibration from messages.
 */
public class MessageVibration {
	/**
	 * The vibration pattern.
	 */
	private static final long[][] VIBRATION_PATTERNS = {
			{800, 400, 100, 200, 100, 200, 250, 300, 1000, 400},
			{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 1000},
			{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}
	};
	/**
	 * The vibration pattern.
	 */
	private static final int[][] VIBRATION_AMPLITUDES = {
			{255, 0, 150, 0, 150, 0, 150, 0, 255, 0},
			{255, 245, 235, 225, 215, 204, 194, 184, 174, 164, 153, 143, 123, 113, 103, 92, 82, 72, 62, 52, 41, 31, 21, 11, 0},
			{10, 30, 50, 70, 100, 130, 160, 190, 220, 255, 220, 190, 160, 130, 100, 70, 50, 30, 10, 0}
	};
	/**
	 * The repetition points of vibration patterns.
	 */
	private static final int[] VIBRATION_REPEAT_POINTS = {2, 0, 0};

	/**
	 * The vibrator.
	 */
	private final Vibrator vibrator;

	/**
	 * Constructor.
	 *
	 * @param context The context.
	 */
	public MessageVibration(final Context context) {
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}


	/**
	 * Vibrate when displaying the message.
	 *
	 * @param messageDetails The message details.
	 */
	public void vibrate(final MessageDetails messageDetails) {
		int vibrationPattern = messageDetails.getVibrationPattern();
		if (vibrationPattern < 0 || vibrationPattern >= VIBRATION_PATTERNS.length) {
			vibrationPattern = 0;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERNS[vibrationPattern], VIBRATION_AMPLITUDES[vibrationPattern],
							messageDetails.isVibrationRepeated() ? VIBRATION_REPEAT_POINTS[vibrationPattern] : -1),
					new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build());
		}
		else {
			vibrator.vibrate(VIBRATION_PATTERNS[vibrationPattern], -1);
		}
	}

	/**
	 * Cancel the vibration.
	 */
	public void cancelVibration() {
		vibrator.cancel();
	}
}
