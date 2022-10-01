package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The details of a text message.
 */
public class AdminMessageDetails extends MessageDetails {
	/**
	 * The admin message type.
	 */
	private final AdminType adminType;
	/**
	 * The data.
	 */
	private final Map<String, String> data;

	/**
	 * Generate message details.
	 *
	 * @param adminType The admin type
	 * @param data      The data
	 */
	public AdminMessageDetails(final AdminType adminType, final Map<String, String> data) {
		super(MessageType.ADMIN);
		this.adminType = adminType;
		this.data = data;
	}

	/**
	 * Extract messageDetails from remote message.
	 *
	 * @param message The remote message.
	 * @return The message details.
	 */
	public static AdminMessageDetails fromRemoteMessage(final RemoteMessage message) {
		Map<String, String> retrievedData = message.getData();
		AdminType adminType = AdminType.fromName(retrievedData.get("adminType"));
		Map<String, String> data = new HashMap<>(retrievedData);
		data.remove("messageType");
		data.remove("adminType");
		return new AdminMessageDetails(adminType, data);
	}

	public AdminType getAdminType() {
		return adminType;
	}

	/**
	 * Get the available data keys.
	 *
	 * @return The data keys.
	 */
	public Set<String> getKeys() {
		return data.keySet();
	}

	/**
	 * Get the data value for a key.
	 *
	 * @param key The key.
	 * @return The value.
	 */
	public String getValue(String key) {
		return data.get(key);
	}

	/**
	 * Type of admin message.
	 */
	public enum AdminType {
		/**
		 * Unknown.
		 */
		UNKNOWN,
		/**
		 * Invitation accepted.
		 */
		INVITATION_ACCEPTED;

		private static AdminType fromName(final String name) {
			if (name == null) {
				return UNKNOWN;
			}
			try {
				return AdminType.valueOf(name);
			}
			catch (IllegalArgumentException e) {
				return UNKNOWN;
			}
		}
	}

}
