package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * The details of a message.
 */
public class MessageDetails implements Serializable {
	/**
	 * The parameter name for message type.
	 */
	private static final String NAME_MESSAGE_TYPE = "messageType";

	/**
	 * The message type.
	 */
	private final MessageType type;

	public MessageType getType() {
		return type;
	}

	public MessageDetails(MessageType type) {
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
		MessageType messageType = MessageType.fromName(data.get(NAME_MESSAGE_TYPE));

		switch(messageType) {
		case TEXT:
			return TextMessageDetails.fromRemoteMessage(message);
		case RANDOMIMAGE:
			return RandomimageMessageDetails.fromRemoteMessage(message);
		case LUT:
		case UNKNOWN:
		default:
			return new MessageDetails(MessageType.UNKNOWN);
		}
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
		 * Randomimage notification.
		 */
		RANDOMIMAGE,
		/**
		 * Lob und Tadel notification.
		 */
		LUT;

		private static MessageType fromName(String name) {
			try {
				return MessageType.valueOf(name);
			}
			catch (IllegalArgumentException | NullPointerException e) {
				return UNKNOWN;
			}
		}
	}
}
