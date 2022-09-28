package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * The details of a text message.
 */
public class RandomimageMessageDetails extends MessageDetails {
	/**
	 * The origin from where the randomimage should be displayed.
	 */
	private static final String NAME_RANDOMIMAGE_ORIGIN = "randomImageOrigin";
	/**
	 * The parameter name for notification name.
	 */
	private static final String NAME_NOTIFICATION_NAME = "notificationName";
	/**
	 * The parameter name for notification name.
	 */
	private static final String NAME_WIDGET_NAME = "widgetName";

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
	 * @param randomImageOrigin the random image origin.
	 * @param notificationName  The notification name.
	 * @param widgetName        The widget name.
	 */
	public RandomimageMessageDetails(final RandomImageOrigin randomImageOrigin, final String notificationName, final String widgetName) {
		super(MessageType.RANDOMIMAGE);
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
	 * @param message The remote message.
	 * @return The message details.
	 */
	public static RandomimageMessageDetails fromRemoteMessage(final RemoteMessage message) {
		Map<String, String> data = message.getData();

		RandomImageOrigin randomImageOrigin = RandomImageOrigin.fromName(data.get(NAME_RANDOMIMAGE_ORIGIN));
		String notificationName = data.get(NAME_NOTIFICATION_NAME);
		String widgetName = data.get(NAME_WIDGET_NAME);
		return new RandomimageMessageDetails(randomImageOrigin, notificationName, widgetName);
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
