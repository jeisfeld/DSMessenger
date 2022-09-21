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
	 * Generate message details.
	 *
	 * @param messageText         The message text.
	 * @param displayOnLockScreen The display on lock screen flag.
	 * @param vibrate             The vibration flag.
	 */
	public MessageDetails(String messageText, boolean displayOnLockScreen, boolean vibrate) {
		this.messageText = messageText;
		this.displayOnLockScreen = displayOnLockScreen;
		this.vibrate = vibrate;
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
		return new MessageDetails(messageText, displayOnLockScreen, vibrate);
	}

}
