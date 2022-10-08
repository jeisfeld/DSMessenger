package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.dsmessenger.main.account.Contact;

/**
 * The details of a text message.
 */
public class RandomimageMessageDetails extends MessageDetails {
	/**
	 * The random image origin.
	 */
	private final RandomImageOrigin randomImageOrigin;
	/**
	 * The notification name.
	 */
	private final String notificationName;
	/**
	 * The widget name.
	 */
	private final String widgetName;

	/**
	 * Generate message details.
	 *
	 * @param messageId         The message id.
	 * @param messageTime       The message time.
	 * @param contact           The contact who sent the message.
	 * @param randomImageOrigin the random image origin.
	 * @param notificationName  The notification name.
	 * @param widgetName        The widget name.
	 */
	public RandomimageMessageDetails(final UUID messageId, final Instant messageTime, final Contact contact,
									 final RandomImageOrigin randomImageOrigin, final String notificationName, final String widgetName) {
		super(MessageType.RANDOMIMAGE, messageId, messageTime, contact);
		this.randomImageOrigin = randomImageOrigin;
		this.notificationName = notificationName;
		this.widgetName = widgetName;
	}

	public final RandomImageOrigin getRandomImageOrigin() {
		return randomImageOrigin;
	}

	public final String getNotificationName() {
		return notificationName;
	}

	public final String getWidgetName() {
		return widgetName;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message     The remote message.
	 * @param messageTime The message time.
	 * @param messageId   The message id.
	 * @param contact     The contact.
	 * @return The message details.
	 */
	public static RandomimageMessageDetails fromRemoteMessage(final RemoteMessage message, UUID messageId, Instant messageTime, Contact contact) {
		Map<String, String> data = message.getData();

		RandomImageOrigin randomImageOrigin = RandomImageOrigin.fromName(data.get("randomImageOrigin"));
		String notificationName = data.get("notificationName");
		String widgetName = data.get("widgetName");
		return new RandomimageMessageDetails(messageId, messageTime, contact, randomImageOrigin, notificationName, widgetName);
	}

	/**
	 * Random Image Origins.
	 */
	public enum RandomImageOrigin {
		/**
		 * Unknown.
		 */
		UNKNOWN,
		/**
		 * Notification.
		 */
		NOTIFICATION,
		/**
		 * Widget.
		 */
		WIDGET;

		private static RandomImageOrigin fromName(final String name) {
			if (name == null) {
				return UNKNOWN;
			}
			try {
				return RandomImageOrigin.valueOf(name);
			}
			catch (IllegalArgumentException e) {
				return UNKNOWN;
			}
		}
	}
}
