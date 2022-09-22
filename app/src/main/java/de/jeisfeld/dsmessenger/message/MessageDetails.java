package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * The details of a message.
 */
public class MessageDetails implements Serializable {
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
	private String messageText;
	/**
	 * Flag indicating if the message should be displayed on top of lock screen.
	 */
	private boolean displayOnLockScreen;
	/**
	 * Flag indicating if there should be vibration on display.
	 */
	private boolean vibrate;
	/**
	 * Flag indicating if the message should be locked.
	 */
	private boolean lockMessage;
	/**
	 * Flag indicating if screen should be kept on.
	 */
	private boolean keepScreenOn;

	/**
	 * Generate message details.
	 *
	 * @param messageText         The message text.
	 * @param displayOnLockScreen The display on lock screen flag.
	 * @param vibrate             The vibration flag.
	 * @param lockMessage         The lock message flag.
	 * @param keepScreenOn        The Keep screen on flag.
	 */
	public MessageDetails(String messageText, boolean displayOnLockScreen, boolean vibrate, boolean lockMessage, boolean keepScreenOn) {
		this.messageText = messageText;
		this.displayOnLockScreen = displayOnLockScreen;
		this.vibrate = vibrate;
		this.lockMessage = lockMessage;
		this.keepScreenOn = keepScreenOn;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public boolean isDisplayOnLockScreen() {
		return displayOnLockScreen;
	}

	public void setDisplayOnLockScreen(boolean displayOnLockScreen) {
		this.displayOnLockScreen = displayOnLockScreen;
	}

	public boolean isVibrate() {
		return vibrate;
	}

	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}

	public boolean isLockMessage() {
		return lockMessage;
	}

	public void setLockMessage(boolean lockMessage) {
		this.lockMessage = lockMessage;
	}

	public boolean isKeepScreenOn() {
		return keepScreenOn;
	}

	public void setKeepScreenOn(boolean keepScreenOn) {
		this.keepScreenOn = keepScreenOn;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message The remote message.
	 * @return The message details.
	 */
	public static MessageDetails fromRemoteMessage(final RemoteMessage message) {
		Map<String, String> data = message.getData();

		String messageText = data.get(NAME_MESSAGE_TEXT);
		boolean displayOnLockScreen = Boolean.parseBoolean(data.get(NAME_DISPLAY_ON_LOCK_SCREEN));
		boolean vibrate = Boolean.parseBoolean(data.get(NAME_VIBRATE));
		boolean lockMessage = Boolean.parseBoolean(data.get(LOCK_MESSAGE));
		boolean keepScreenOn = Boolean.parseBoolean(data.get(KEEP_SCREEN_ON));
		return new MessageDetails(messageText, displayOnLockScreen, vibrate, lockMessage, keepScreenOn);
	}

}
