package de.jeisfeld.dsmessenger.main.account;

import java.io.Serializable;

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
	 * Flag indicating if this is current device.
	 */
	final boolean isThis;

	/**
	 * Constructor.
	 *
	 * @param id     The device id.
	 * @param name   The device name.
	 * @param muted  The muted flag.
	 * @param isThis The flag indicating if this is current device.
	 */
	public Device(final int id, final String name, final boolean muted, final boolean isThis) {
		this.id = id;
		this.name = name;
		this.muted = muted;
		this.isThis = isThis;
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

	public boolean isThis() {
		return isThis;
	}
}
