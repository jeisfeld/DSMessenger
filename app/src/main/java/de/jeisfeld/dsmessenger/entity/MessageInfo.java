package de.jeisfeld.dsmessenger.entity;

import java.util.UUID;

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
	 * The messageId.
	 */
	private final UUID messageId;
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
	public MessageInfo(String messageText, boolean isOwn, UUID messageId, MessageStatus status) {
		this.messageText = messageText;
		this.isOwn = isOwn;
		this.messageId = messageId;
		this.status = status;
	}

	public String getMessageText() {
		return messageText;
	}

	public boolean isOwn() {
		return isOwn;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public MessageStatus getStatus() {
		return status;
	}
}
