package de.jeisfeld.dsmessenger.main.account;

import java.io.Serializable;

import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.message.MessageDisplayStrategy;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Class holding information for one device.
 */
public class Device implements Serializable {
	/**
	 * The device id.
	 */
	final int id;
	/**
	 * The device name.
	 */
	final String name;
	/**
	 * The muted flag.
	 */
	final boolean muted;
	/**
	 * The display strategy for normal messages.
	 */
	final MessageDisplayStrategy displayStrategyNormal;
	/**
	 * The display strategy for urgent messages.
	 */
	final MessageDisplayStrategy displayStrategyUrgent;
	/**
	 * Flag indicating if this is current device.
	 */
	final boolean isThis;

	/**
	 * Constructor.
	 *
	 * @param id                    The device id.
	 * @param name                  The device name.
	 * @param muted                 The muted flag.
	 * @param displayStrategyNormal The display strategy for normal messages.
	 * @param displayStrategyUrgent The display strategy for urgent messages.
	 * @param isThis                The flag indicating if this is current device.
	 */
	public Device(final int id, final String name, final boolean muted,
				  final MessageDisplayStrategy displayStrategyNormal, final MessageDisplayStrategy displayStrategyUrgent, final boolean isThis) {
		this.id = id;
		this.name = name;
		this.muted = muted;
		this.displayStrategyNormal = displayStrategyNormal;
		this.displayStrategyUrgent = displayStrategyUrgent;
		this.isThis = isThis;
	}

	/**
	 * Get the information of the current device.
	 *
	 * @return The current device information.
	 */
	public static Device getThisDevice() {
		int id = PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1);
		String name = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_name);
		boolean muted = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_pref_device_muted);
		MessageDisplayStrategy displayStrategyNormal = MessageDisplayStrategy.fromString(
				PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_display_strategy_normal));
		MessageDisplayStrategy displayStrategyUrgent = MessageDisplayStrategy.fromString(
				PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_display_strategy_urgent));
		return new Device(id, name, muted, displayStrategyNormal, displayStrategyUrgent, true);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isMuted() {
		return muted;
	}

	public MessageDisplayStrategy getDisplayStrategyNormal() {
		return displayStrategyNormal;
	}

	public MessageDisplayStrategy getDisplayStrategyUrgent() {
		return displayStrategyUrgent;
	}

	public boolean isThis() {
		return isThis;
	}
}
