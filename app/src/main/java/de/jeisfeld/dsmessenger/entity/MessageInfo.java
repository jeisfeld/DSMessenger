package de.jeisfeld.dsmessenger.entity;

import de.jeisfeld.dsmessenger.main.message.MessageFragment.MessageStatus;

public class MessageInfo {
	/**
	 * The message text.
	 */
	private final String messageText;
	/**
	 * Indicator if it is own message or contact's message.
	 */
	private final boolean isOwn;
	/**
	 * The message status.
	 */
	private final MessageStatus status;

	/**
	 * Constructor.
	 *
	 * @param messageText The message text.
	 * @param isOwn       Indicator if it is own message or contact's message.
	 * @param status      The message status
	 */
	public MessageInfo(String messageText, boolean isOwn, MessageStatus status) {
		this.messageText = messageText;
		this.isOwn = isOwn;
		this.status = status;
	}

	public String getMessageText() {
		return messageText;
	}

	public boolean isOwn() {
		return isOwn;
	}

	public MessageStatus getStatus() {
		return status;
	}
}
