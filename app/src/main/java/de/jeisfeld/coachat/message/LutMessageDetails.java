package de.jeisfeld.coachat.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import de.jeisfeld.coachat.entity.Contact;

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
	 * The channel
	 */
	private final Integer channel;
	/**
	 * The power
	 */
	private final Integer power;
	/**
	 * The power factor.
	 */
	private final Double powerFactor;
	/**
	 * The frequency.
	 */
	private final Integer frequency;
	/**
	 * The wave name.
	 */
	private final String wave;

	/**
	 * Generate message details.
	 *
	 * @param messageId      The message id.
	 * @param messageTime    The message time.
	 * @param priority       The message priority.
	 * @param contact        The contact who sent the message
	 * @param lutMessageType The LUT message type.
	 * @param duration       The pulse duration.
	 * @param channel        The channel.
	 * @param power          The power.
	 * @param powerFactor    The power factor.
	 */
	public LutMessageDetails(final UUID messageId, final Instant messageTime, final MessagePriority priority, final Contact contact,
							 final LutMessageType lutMessageType, final Long duration, final Integer channel,
							 final Integer power, final Double powerFactor, final Integer frequency, final String wave) {
		super(MessageType.LUT, messageId, messageTime, priority, contact);
		this.lutMessageType = lutMessageType;
		this.duration = duration;
		this.channel = channel;
		this.power = power;
		this.powerFactor = powerFactor;
		this.frequency = frequency;
		this.wave = wave;
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
		String channelString = data.get("channel");
		Integer channel = channelString == null ? null : Integer.parseInt(channelString);
		String powerString = data.get("power");
		Integer power = powerString == null ? null : Integer.parseInt(powerString);
		String powerFactorString = data.get("powerFactor");
		Double powerFactor = powerFactorString == null ? null : Double.parseDouble(powerFactorString);
		String frequencyString = data.get("frequency");
		Integer frequency = frequencyString == null ? null : Integer.parseInt(frequencyString);
		String wave = data.get("wave");
		return new LutMessageDetails(messageId, messageTime, priority, contact, lutMessageType, duration, channel,
				power, powerFactor, frequency, wave);
	}

	public final LutMessageType getLutMessageType() {
		return lutMessageType;
	}

	public final Long getDuration() {
		return duration;
	}

	public final Integer getChannel() {
		return channel;
	}

	public final Integer getPower() {
		return power;
	}

	public final Double getPowerFactor() {
		return powerFactor;
	}

	public final Integer getFrequency() {
		return frequency;
	}

	public final String getWave() {
		return wave;
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
