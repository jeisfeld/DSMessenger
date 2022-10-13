package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.dsmessenger.main.account.Contact;

/**
 * The details of a text message.
 */
public class TextMessageDetails extends MessageDetails {
	/**
	 * The message text.
	 */
	private final String messageText;

	/**
	 * Generate message details.
	 *
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param priority    The message priority.
	 * @param contact     The contact who sent the message.
	 * @param messageText The message text.
	 */
	public TextMessageDetails(final UUID messageId, final Instant messageTime, final MessagePriority priority, final Contact contact,
							  final String messageText) {
		super(MessageType.TEXT, messageId, messageTime, priority, contact);
		this.messageText = messageText;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message     The remote message.
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param priority    The message priority.
	 * @param contact     The contact.
	 * @return The message details.
	 */
	public static TextMessageDetails fromRemoteMessage(final RemoteMessage message, final UUID messageId, final Instant messageTime,
													   final MessagePriority priority, final Contact contact) {
		Map<String, String> data = message.getData();
		String messageText = data.get("messageText");
		return new TextMessageDetails(messageId, messageTime, priority, contact, messageText);
	}

	public final String getMessageText() {
		return messageText;
	}
}
