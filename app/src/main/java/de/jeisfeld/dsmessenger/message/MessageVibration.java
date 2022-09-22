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
			{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
			{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
					100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 6000}
	};
	/**
	 * The vibration pattern.
	 */
	private static final int[][] VIBRATION_AMPLITUDES = {
			{255, 0, 150, 0, 150, 0, 150, 0, 255, 0},
			{255, 245, 235, 225, 215, 204, 194, 184, 174, 164, 153, 143, 123, 113, 103, 92, 82, 72, 62, 52, 41, 31, 21, 11, 0},
			{10, 30, 50, 70, 100, 130, 160, 190, 220, 255, 220, 190, 160, 130, 100, 70, 50, 30, 10, 0},
			{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
					31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
					61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
					91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
					111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130,
					131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150,
					151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170,
					171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190,
					191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210,
					211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230,
					231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250,
					251, 252, 253, 254, 255, 210, 170, 130, 90, 60, 30, 0}
	};
	/**
	 * The repetition points of vibration patterns.
	 */
	private static final int[] VIBRATION_REPEAT_POINTS = {2, 0, 0, 0};

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
	 * @param textMessageDetails The message details.
	 */
	public void vibrate(final TextMessageDetails textMessageDetails) {
		int vibrationPattern = textMessageDetails.getVibrationPattern();
		if (vibrationPattern < 0 || vibrationPattern >= VIBRATION_PATTERNS.length) {
			vibrationPattern = 0;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERNS[vibrationPattern], VIBRATION_AMPLITUDES[vibrationPattern],
							textMessageDetails.isVibrationRepeated() ? VIBRATION_REPEAT_POINTS[vibrationPattern] : -1),
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
