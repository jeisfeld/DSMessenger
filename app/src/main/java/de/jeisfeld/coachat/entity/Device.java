package de.jeisfeld.coachat.entity;

import java.io.Serializable;

import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.main.account.AccountFragment;
import de.jeisfeld.coachat.message.MessageDisplayStrategy;
import de.jeisfeld.coachat.util.PreferenceUtil;

/**
 * Class holding information for one device.
 */
public class Device implements Serializable {
	/**
	 * The device id.
	 */
	private final int id;
	/**
	 * The device name.
	 */
	private final String name;
	/**
	 * The muted flag.
	 */
	private final boolean muted;
	/**
	 * The display strategy for normal messages.
	 */
	private final MessageDisplayStrategy displayStrategyNormal;
	/**
	 * The display strategy for urgent messages.
	 */
	private final MessageDisplayStrategy displayStrategyUrgent;
	/**
	 * Flag indicating if this is current device.
	 */
	private final boolean isThis;

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
		if (AccountFragment.isLoggedIn()) {
			int id = PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1);
			String name = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_name);
			boolean muted = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_pref_device_muted);
			MessageDisplayStrategy displayStrategyNormal = MessageDisplayStrategy.fromString(
					PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_display_strategy_normal));
			MessageDisplayStrategy displayStrategyUrgent = MessageDisplayStrategy.fromString(
					PreferenceUtil.getSharedPreferenceString(R.string.key_pref_device_display_strategy_urgent));
			return new Device(id, name, muted, displayStrategyNormal, displayStrategyUrgent, true);
		}
		else {
			return new Device(-1, "None", true, MessageDisplayStrategy.DUMMY_DISPLAY_STRATEGY,
					MessageDisplayStrategy.DUMMY_DISPLAY_STRATEGY, true);
		}
	}

	public final int getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final boolean isMuted() {
		return muted;
	}

	public final MessageDisplayStrategy getDisplayStrategyNormal() {
		return displayStrategyNormal;
	}

	public final MessageDisplayStrategy getDisplayStrategyUrgent() {
		return displayStrategyUrgent;
	}

	public final boolean isThis() {
		return isThis;
	}
}
