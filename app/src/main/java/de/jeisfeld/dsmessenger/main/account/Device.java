package de.jeisfeld.dsmessenger.main.account;

/**
 * Class holding information for one device.
 */
public class Device {
	/**
	 * The device id.
	 */
	final int id;
	/**
	 * The device name.
	 */
	final String name;

	/**
	 * Constructor.
	 *
	 * @param id   The device id.
	 * @param name The device name.
	 */
	public Device(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
