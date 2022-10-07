package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * The details of a message.
 */
public class MessageDetails implements Serializable {
	/**
	 * The message type.
	 */
	private final MessageType type;
	/**
	 * The messageId.
	 */
	private final String messageId;
	/**
	 * The messageTime.
	 */
	private final Instant messageTime;

	/**
	 * Constructor.
	 *
	 * @param type        The message type.
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 */
	public MessageDetails(final MessageType type, final String messageId, final Instant messageTime) {
		this.type = type;
		this.messageId = messageId;
		this.messageTime = messageTime;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message The remote message.
	 * @return The message details.
	 */
	public static MessageDetails fromRemoteMessage(final RemoteMessage message) {
		Map<String, String> data = message.getData();
		MessageType messageType = MessageType.fromName(data.get("messageType"));

		switch (messageType) {
		case TEXT:
			return TextMessageDetails.fromRemoteMessage(message);
		case RANDOMIMAGE:
			return RandomimageMessageDetails.fromRemoteMessage(message);
		case ADMIN:
			return AdminMessageDetails.fromRemoteMessage(message);
		case LUT:
		case UNKNOWN:
		default:
			return new MessageDetails(MessageType.UNKNOWN, null, null);
		}
	}

	public final MessageType getType() {
		return type;
	}

	public String getMessageId() {
		return messageId;
	}

	public Instant getMessageTime() {
		return messageTime;
	}

	/**
	 * The type of messages.
	 */
	public enum MessageType {
		/**
		 * Unknown message.
		 */
		UNKNOWN,
		/**
		 * Text message.
		 */
		TEXT,
		/**
		 * Admin message.
		 */
		ADMIN,
		/**
		 * Randomimage notification.
		 */
		RANDOMIMAGE,
		/**
		 * Lob und Tadel notification.
		 */
		LUT;

		private static MessageType fromName(final String name) {
			if (name == null) {
				return UNKNOWN;
			}
			try {
				return MessageType.valueOf(name);
			}
			catch (IllegalArgumentException e) {
				return UNKNOWN;
			}
		}
	}
}
