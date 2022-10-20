package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.dsmessenger.entity.Contact;

/**
 * The details of a LUT message.
 */
public class LutMessageDetails extends MessageDetails {
	/**
	 * The LUT message type.
	 */
	private final LutMessageType lutMessageType;
	/**
	 * The pulse duration.
	 */
	private final Long duration;

	/**
	 * Generate message details.
	 *
	 * @param lutMessageType The LUT message type.
	 * @param duration       The pulse duration.
	 */
	public LutMessageDetails(final UUID messageId, final Instant messageTime, final MessagePriority priority, final Contact contact,
							 final LutMessageType lutMessageType, final Long duration) {
		super(MessageType.LUT, messageId, messageTime, priority, contact);
		this.lutMessageType = lutMessageType;
		this.duration = duration;
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
	public static LutMessageDetails fromRemoteMessage(final RemoteMessage message, final UUID messageId, final Instant messageTime,
													  final MessagePriority priority, final Contact contact) {
		Map<String, String> data = message.getData();

		LutMessageType lutMessageType = LutMessageType.fromName(data.get("lutMessageType"));
		String durationString = data.get("duration");
		Long duration = durationString == null ? null : Long.parseLong(durationString);
		return new LutMessageDetails(messageId, messageTime, priority, contact, lutMessageType, duration);
	}

	public LutMessageType getLutMessageType() {
		return lutMessageType;
	}

	public Long getDuration() {
		return duration;
	}

	/**
	 * Message type for LuT.
	 */
	public enum LutMessageType {
		/**
		 * Single pulse.
		 */
		PULSE,
		/**
		 * Signal on.
		 */
		ON,
		/**
		 * Signal off.
		 */
		OFF;

		private static LutMessageType fromName(final String name) {
			if (name == null) {
				return OFF;
			}
			try {
				return LutMessageType.valueOf(name);
			}
			catch (IllegalArgumentException e) {
				return OFF;
			}
		}
	}
}