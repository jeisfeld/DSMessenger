package de.jeisfeld.dsmessenger.message;

import androidx.annotation.NonNull;

/**
 * The strategy how to display a message.
 */
public class MessageDisplayStrategy {
	/**
	 * Flag indicating if the message should be displayed on top of lock screen.
	 */
	private final boolean displayOnLockScreen;
	/**
	 * Flag indicating if there should be vibration on display.
	 */
	private final boolean vibrate;
	/**
	 * Flag indicating if the vibration should be repeated.
	 */
	private final boolean vibrationRepeated;
	/**
	 * The vibration pattern.
	 */
	private final int vibrationPattern;
	/**
	 * Flag indicating if the message should be locked.
	 */
	private final boolean lockMessage;
	/**
	 * Flag indicating if screen should be kept on.
	 */
	private final boolean keepScreenOn;

	/**
	 * Generate message details.
	 *
	 * @param displayOnLockScreen The display on lock screen flag.
	 * @param vibrate             The vibration flag.
	 * @param vibrationRepeated   The vibration repetition flag.
	 * @param vibrationPattern    The vibration pattern.
	 * @param keepScreenOn        The Keep screen on flag.
	 * @param lockMessage         The lock message flag.
	 */
	public MessageDisplayStrategy(final boolean displayOnLockScreen, final boolean vibrate, final boolean vibrationRepeated, final int vibrationPattern,
								  final boolean keepScreenOn, final boolean lockMessage) {
		this.displayOnLockScreen = displayOnLockScreen;
		this.vibrate = vibrate;
		this.vibrationRepeated = vibrationRepeated;
		this.vibrationPattern = vibrationPattern;
		this.keepScreenOn = keepScreenOn;
		this.lockMessage = lockMessage;
	}

	/**
	 * Extract message display strategy from String
	 *
	 * @param strategy The strategy as String
	 * @return The message display strategy.
	 */
	public static MessageDisplayStrategy fromString(final String strategy) {
		boolean displayOnLockScreen = charToBoolean(strategy.charAt(0));
		boolean keepScreenOn = charToBoolean(strategy.charAt(1));
		boolean lockMessage = charToBoolean(strategy.charAt(2));
		boolean vibrate = charToBoolean(strategy.charAt(3));
		boolean vibrationRequired = charToBoolean(strategy.charAt(4));
		int vibrationPattern = Integer.parseInt(Character.toString(strategy.charAt(5)));
		return new MessageDisplayStrategy(displayOnLockScreen, vibrate, vibrationRequired, vibrationPattern, keepScreenOn, lockMessage);
	}

	/**
	 * Get char representation of a boolean.
	 *
	 * @param b The boolean
	 * @return The char representation
	 */
	private static char booleanToChar(boolean b) {
		return b ? '1' : '0';
	}

	/**
	 * Restore boolean from char representation.
	 *
	 * @param c The char representation
	 * @return The boolean
	 */
	private static boolean charToBoolean(char c) {
		return c == '1';
	}

	/**
	 * Convert message display strategy into String.
	 *
	 * @return The string representation.
	 */
	@NonNull
	@Override
	public String toString() {
		return String.valueOf(booleanToChar(isDisplayOnLockScreen())) +
				booleanToChar(isKeepScreenOn()) +
				booleanToChar(isLockMessage()) +
				booleanToChar(isVibrate()) +
				booleanToChar(isVibrationRepeated()) +
				getVibrationPattern();
	}

	public final boolean isDisplayOnLockScreen() {
		return displayOnLockScreen;
	}

	public final boolean isVibrate() {
		return vibrate;
	}

	public final boolean isVibrationRepeated() {
		return vibrationRepeated;
	}

	public final int getVibrationPattern() {
		return vibrationPattern;
	}

	public final boolean isLockMessage() {
		return lockMessage;
	}

	public final boolean isKeepScreenOn() {
		return keepScreenOn;
	}

}
