package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * The details of a text message.
 */
public class TextMessageDetails extends MessageDetails {
	/**
	 * The parameter name for message text.
	 */
	private static final String NAME_MESSAGE_TEXT = "messageText";
	/**
	 * The parameter name for display on lock screen flag.
	 */
	private static final String NAME_DISPLAY_ON_LOCK_SCREEN = "displayOnLockScreen";
	/**
	 * The parameter name for vibration flag.
	 */
	private static final String NAME_VIBRATE = "vibrate";
	/**
	 * The parameter name for vibration repetition flag.
	 */
	private static final String NAME_VIBRATION_REPEATED = "vibrationRepeated";
	/**
	 * The parameter name for vibration pattern flag.
	 */
	private static final String NAME_VIBRATION_PATTERN = "vibrationPattern";
	/**
	 * The parameter name for the lock message flag.
	 */
	private static final String LOCK_MESSAGE = "lockMessage";
	/**
	 * The parameter name for the keep screen on flag.
	 */
	private static final String KEEP_SCREEN_ON = "keepScreenOn";

	/**
	 * The message text.
	 */
	private final String messageText;
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
	 * @param messageText         The message text.
	 * @param displayOnLockScreen The display on lock screen flag.
	 * @param vibrate             The vibration flag.
	 * @param vibrationRepeated   The vibration repetition flag.
	 * @param vibrationPattern    The vibration pattern.
	 * @param lockMessage         The lock message flag.
	 * @param keepScreenOn        The Keep screen on flag.
	 */
	public TextMessageDetails(final String messageText, final boolean displayOnLockScreen, final boolean vibrate, final boolean vibrationRepeated,
							  final int vibrationPattern, final boolean lockMessage, final boolean keepScreenOn) {
		super(MessageType.TEXT);
		this.messageText = messageText;
		this.displayOnLockScreen = displayOnLockScreen;
		this.vibrate = vibrate;
		this.vibrationRepeated = vibrationRepeated;
		this.vibrationPattern = vibrationPattern;
		this.lockMessage = lockMessage;
		this.keepScreenOn = keepScreenOn;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message The remote message.
	 * @return The message details.
	 */
	public static TextMessageDetails fromRemoteMessage(final RemoteMessage message) {
		Map<String, String> data = message.getData();

		String messageText = data.get(NAME_MESSAGE_TEXT);
		boolean displayOnLockScreen = Boolean.parseBoolean(data.get(NAME_DISPLAY_ON_LOCK_SCREEN));
		boolean vibrate = Boolean.parseBoolean(data.get(NAME_VIBRATE));
		boolean vibrationRepated = Boolean.parseBoolean(data.get(NAME_VIBRATION_REPEATED));
		int vibrationPattern = 0;
		try {
			if (data.get(NAME_VIBRATION_PATTERN) != null) {
				vibrationPattern = Integer.parseInt(data.get(NAME_VIBRATION_PATTERN));
			}
		}
		catch (NumberFormatException e) {
			// ignore
		}
		boolean lockMessage = Boolean.parseBoolean(data.get(LOCK_MESSAGE));
		boolean keepScreenOn = Boolean.parseBoolean(data.get(KEEP_SCREEN_ON));
		return new TextMessageDetails(messageText, displayOnLockScreen, vibrate, vibrationRepated, vibrationPattern, lockMessage, keepScreenOn);
	}

	public final String getMessageText() {
		return messageText;
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
