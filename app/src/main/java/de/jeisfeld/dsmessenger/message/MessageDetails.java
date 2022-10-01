package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * The details of a message.
 */
public class MessageDetails implements Serializable {
	/**
	 * The message type.
	 */
	private final MessageType type;

	public MessageDetails(final MessageType type) {
		this.type = type;
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
			return new MessageDetails(MessageType.UNKNOWN);
		}
	}

	public final MessageType getType() {
		return type;
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
