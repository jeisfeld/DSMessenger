package de.jeisfeld.dsmessenger.main.account;

import java.util.List;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Class holding contact data.
 */
public class Contact {
	/**
	 * The id of the relation in DB.
	 */
	private final int relationId;
	/**
	 * The contact name.
	 */
	private final String name;
	/**
	 * Flag indicating if the contact is slave or master.
	 */
	private final boolean isSlave;
	/**
	 * The connection code.
	 */
	private final String connectionCode;
	/**
	 * The contact status.
	 */
	private final ContactStatus status;

	/**
	 * Constructor without id.
	 *
	 * @param relationId     The relation id
	 * @param name           The name
	 * @param isSlave        The flag indicating if it is slave or master
	 * @param connectionCode The connection code
	 * @param status         The contact status
	 */
	public Contact(final int relationId, final String name, final boolean isSlave, final String connectionCode, final ContactStatus status) {
		this.name = name;
		this.isSlave = isSlave;
		this.relationId = relationId;
		this.connectionCode = connectionCode;
		this.status = status;
	}

	/**
	 * Retrieve a contact from storage via id.
	 *
	 * @param relationId The id.
	 */
	protected Contact(final int relationId) {
		this.relationId = relationId;
		name = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_name, relationId);
		isSlave = PreferenceUtil.getIndexedSharedPreferenceBoolean(R.string.key_contact_is_slave, relationId, false);
		connectionCode = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_connection_code, relationId);
		status = ContactStatus.valueOf(PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_status, relationId));
	}

	public int getRelationId() {
		return relationId;
	}

	public String getName() {
		return name;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public String getConnectionCode() {
		return connectionCode;
	}

	public ContactStatus getStatus() {
		return status;
	}

	@NonNull
	@Override
	public String toString() {
		return "Contact{" +
				"relationId=" + relationId + ", name='" + name + '\'' + ", isSlave=" + isSlave +
				", connectionCode='" + connectionCode + '\'' + ", status=" + status + '}';
	}

	/**
	 * Store this contact.
	 */
	public void store() {
		List<Integer> contactIds = PreferenceUtil.getSharedPreferenceIntList(R.string.key_contact_ids);
		if (!contactIds.contains(relationId)) {
			contactIds.add((Integer) relationId);
		}
		PreferenceUtil.setSharedPreferenceIntList(R.string.key_contact_ids, contactIds);
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_name, getRelationId(), getName());
		PreferenceUtil.setIndexedSharedPreferenceBoolean(R.string.key_contact_is_slave, getRelationId(), isSlave());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_connection_code, getRelationId(), getConnectionCode());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_status, getRelationId(), getStatus().name());
	}

	/**
	 * The status of a contact.
	 */
	public enum ContactStatus {
		/**
		 * Invited.
		 */
		INVITED,
		/**
		 * Connected.
		 */
		CONNECTED
	}

}
