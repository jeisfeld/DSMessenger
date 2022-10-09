package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.util.DateUtil;

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
	private final UUID messageId;
	/**
	 * The messageTime.
	 */
	private final Instant messageTime;
	/**
	 * The contact who sent the message.
	 */
	private final Contact contact;

	/**
	 * Constructor.
	 *
	 * @param type        The message type.
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param contact     The contact who sent the message.
	 */
	public MessageDetails(final MessageType type, final UUID messageId, final Instant messageTime, final Contact contact) {
		this.type = type;
		this.messageId = messageId;
		this.messageTime = messageTime;
		this.contact = contact;
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
		Instant messageTime = DateUtil.jsonDateToInstant(data.get("messageTime"));
		UUID messageId = null;
		String messageIdString = data.get("messageId");
		if (messageIdString != null) {
			messageId = UUID.fromString(messageIdString);
		}
		String relationId = data.get("relationId");
		Contact contact = null;
		if (relationId != null) {
			contact = ContactRegistry.getInstance().getContact(Integer.parseInt(relationId));
		}

		switch (messageType) {
		case TEXT:
			return TextMessageDetails.fromRemoteMessage(message, messageId, messageTime, contact);
		case RANDOMIMAGE:
			return RandomimageMessageDetails.fromRemoteMessage(message, messageId, messageTime, contact);
		case ADMIN:
			return AdminMessageDetails.fromRemoteMessage(message, messageId, messageTime, contact);
		case LUT:
		case UNKNOWN:
		default:
			return new MessageDetails(MessageType.UNKNOWN, messageId, messageTime, contact);
		}
	}

	public final MessageType getType() {
		return type;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public Instant getMessageTime() {
		return messageTime;
	}

	public Contact getContact() {
		return contact;
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
