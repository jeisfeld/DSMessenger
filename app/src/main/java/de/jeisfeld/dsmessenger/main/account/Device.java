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
	 * Flag indicating if this is current device.
	 */
	final boolean isThis;

	/**
	 * Constructor.
	 *
	 * @param id     The device id.
	 * @param name   The device name.
	 * @param isThis The flag indicating if this is current device.
	 */
	public Device(final int id, final String name, final boolean isThis) {
		this.id = id;
		this.name = name;
		this.isThis = isThis;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isThis() {
		return isThis;
	}
}
