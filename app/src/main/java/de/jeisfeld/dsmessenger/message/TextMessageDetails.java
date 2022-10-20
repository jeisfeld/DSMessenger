package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.dsmessenger.entity.Contact;

/**
 * The details of a text message.
 */
public class TextMessageDetails extends MessageDetails {
	/**
	 * The message text.
	 */
	private final String messageText;
	/**
	 * The conversationId.
	 */
	private final UUID conversationId;
	/**
	 * The timestamp.
	 */
	private final long timestamp;

	/**
	 * Generate message details.
	 *
	 * @param messageType The message type.
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param priority    The message priority.
	 * @param contact     The contact who sent the message.
	 * @param messageText The message text.
	 * @param timestamp   The timestamp.
	 */
	public TextMessageDetails(final MessageType messageType, final UUID messageId, final Instant messageTime, final MessagePriority priority,
							  final Contact contact, final String messageText, final UUID conversationId, final long timestamp) {
		super(messageType, messageId, messageTime, priority, contact);
		this.messageText = messageText;
		this.conversationId = conversationId;
		this.timestamp = timestamp;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message     The remote message.
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param priority    The message priority.
	 * @param contact     The contact.
	 * @param messageType The message type.
	 * @return The message details.
	 */
	public static TextMessageDetails fromRemoteMessage(final RemoteMessage message, final UUID messageId, final Instant messageTime,
													   final MessagePriority priority, final Contact contact, final MessageType messageType) {
		Map<String, String> data = message.getData();
		String messageText = data.get("messageText");
		UUID conversationId = UUID.fromString(data.get("conversationId"));
		long timestamp = Long.parseLong(data.get("timestamp"));
		return new TextMessageDetails(messageType, messageId, messageTime, priority, contact, messageText, conversationId, timestamp);
	}

	public final String getMessageText() {
		return messageText;
	}

	public UUID getConversationId() {
		return conversationId;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
