package de.jeisfeld.coachat.message;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * The strategy how to display a message.
 */
public class MessageDisplayStrategy implements Serializable {
	/**
	 * Dummy display strategy to be used if not logged in.
	 */
	public static final MessageDisplayStrategy DUMMY_DISPLAY_STRATEGY = new MessageDisplayStrategy(MessageDisplayType.NOTIFICATION,
			false, false, false, 0, false, false);

	/**
	 * The message display type.
	 */
	private final MessageDisplayType messageDisplayType;
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
	 * @param messageDisplayType  The message type.
	 * @param displayOnLockScreen The display on lock screen flag.
	 * @param vibrate             The vibration flag.
	 * @param vibrationRepeated   The vibration repetition flag.
	 * @param vibrationPattern    The vibration pattern.
	 * @param keepScreenOn        The Keep screen on flag.
	 * @param lockMessage         The lock message flag.
	 */
	public MessageDisplayStrategy(final MessageDisplayType messageDisplayType, final boolean displayOnLockScreen, final boolean vibrate,
								  final boolean vibrationRepeated, final int vibrationPattern,
								  final boolean keepScreenOn, final boolean lockMessage) {
		this.messageDisplayType = messageDisplayType;
		this.displayOnLockScreen = displayOnLockScreen;
		this.vibrate = vibrate;
		this.vibrationRepeated = vibrationRepeated;
		this.vibrationPattern = vibrationPattern;
		this.keepScreenOn = keepScreenOn;
		this.lockMessage = lockMessage;
	}

	/**
	 * Extract message display strategy from String.
	 *
	 * @param strategy The strategy as String
	 * @return The message display strategy.
	 */
	public static MessageDisplayStrategy fromString(final String strategy) {
		MessageDisplayType messageDisplayType = MessageDisplayType.fromOrdinal(Integer.parseInt(Character.toString(strategy.charAt(0))));
		boolean displayOnLockScreen = charToBoolean(strategy.charAt(1));
		boolean keepScreenOn = charToBoolean(strategy.charAt(2));
		boolean lockMessage = charToBoolean(strategy.charAt(3)); // MAGIC_NUMBER
		boolean vibrate = charToBoolean(strategy.charAt(4)); // MAGIC_NUMBER
		boolean vibrationRequired = charToBoolean(strategy.charAt(5)); // MAGIC_NUMBER
		int vibrationPattern = Integer.parseInt(Character.toString(strategy.charAt(6))); // MAGIC_NUMBER
		return new MessageDisplayStrategy(messageDisplayType, displayOnLockScreen, vibrate, vibrationRequired, vibrationPattern, keepScreenOn,
				lockMessage);
	}

	/**
	 * Get char representation of a boolean.
	 *
	 * @param b The boolean
	 * @return The char representation
	 */
	private static char booleanToChar(final boolean b) {
		return b ? '1' : '0';
	}

	/**
	 * Restore boolean from char representation.
	 *
	 * @param c The char representation
	 * @return The boolean
	 */
	private static boolean charToBoolean(final char c) {
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
		return getMessageDisplayType().ordinal()
				+ String.valueOf(booleanToChar(isDisplayOnLockScreen()))
				+ booleanToChar(isKeepScreenOn())
				+ booleanToChar(isLockMessage())
				+ booleanToChar(isVibrate())
				+ booleanToChar(isVibrationRepeated())
				+ getVibrationPattern();
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

	public final MessageDisplayType getMessageDisplayType() {
		return messageDisplayType;
	}

	/**
	 * The type of message display.
	 */
	public enum MessageDisplayType {
		/**
		 * Full screen action popup.
		 */
		ACTION,
		/**
		 * A notification.
		 */
		NOTIFICATION;

		/**
		 * Get a messageType from its ordinal value.
		 *
		 * @param ordinal The ordinal value.
		 * @return The message type.
		 */
		public static MessageDisplayType fromOrdinal(final int ordinal) {
			for (MessageDisplayType messageDisplayType : values()) {
				if (messageDisplayType.ordinal() == ordinal) {
					return messageDisplayType;
				}
			}
			return ACTION;
		}
	}
}
