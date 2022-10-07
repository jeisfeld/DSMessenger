package de.jeisfeld.dsmessenger.message;

import com.google.firebase.messaging.RemoteMessage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.jeisfeld.dsmessenger.util.DateUtil;

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
	 * @param messageId   The message id.
	 * @param messageTime The message time.
	 * @param adminType   The admin type
	 * @param data        The data
	 */
	public AdminMessageDetails(final String messageId, final Instant messageTime, final AdminType adminType, final Map<String, String> data) {
		super(MessageType.ADMIN, messageId, messageTime);
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
		Instant messageTime = DateUtil.jsonDateToInstant(retrievedData.get("messageTime"));
		String messageId = retrievedData.get("messageId");
		Map<String, String> data = new HashMap<>(retrievedData);
		data.remove("messageId");
		data.remove("messageTime");
		data.remove("messageType");
		data.remove("adminType");
		return new AdminMessageDetails(messageId, messageTime, adminType, data);
	}

	public final AdminType getAdminType() {
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
	public String getValue(final String key) {
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
		INVITATION_ACCEPTED,
		/**
		 * Contact deleted.
		 */
		CONTACT_DELETED,
		/**
		 * Contact updated.
		 */
		CONTACT_UPDATED;

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
