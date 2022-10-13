package de.jeisfeld.dsmessenger.main.account;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Class holding contact data.
 */
public class Contact implements Serializable {
	/**
	 * The id of the relation in DB.
	 */
	private final int relationId;
	/**
	 * The contact name.
	 */
	private final String name;
	/**
	 * My name for the contact.
	 */
	private final String myName;
	/**
	 * The id of the contact in DB.
	 */
	private final int contactId;
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
	 * @param myName         My name for the contact
	 * @param contactId      The contactId
	 * @param isSlave        The flag indicating if it is slave or master
	 * @param connectionCode The connection code
	 * @param status         The contact status
	 */
	public Contact(final int relationId, final String name, final String myName, final int contactId,
				   final boolean isSlave, final String connectionCode, final ContactStatus status) {
		this.relationId = relationId;
		this.name = name;
		this.myName = myName;
		this.contactId = contactId;
		this.isSlave = isSlave;
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
		myName = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_my_name, relationId);
		contactId = PreferenceUtil.getIndexedSharedPreferenceInt(R.string.key_contact_contact_id, relationId, -1);
		isSlave = PreferenceUtil.getIndexedSharedPreferenceBoolean(R.string.key_contact_is_slave, relationId, false);
		connectionCode = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_connection_code, relationId);
		status = ContactStatus.valueOf(PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_status, relationId));
	}

	public final int getRelationId() {
		return relationId;
	}

	public final String getName() {
		return name;
	}

	public final String getMyName() {
		return myName;
	}

	public final int getContactId() {
		return contactId;
	}

	public final boolean isSlave() {
		return isSlave;
	}

	public final String getConnectionCode() {
		return connectionCode;
	}

	public final ContactStatus getStatus() {
		return status;
	}

	@NonNull
	@Override
	public final String toString() {
		return getName();
	}

	/**
	 * Get detailed String representation.
	 *
	 * @return A detailed String representation.
	 */
	public final String toDetailedString() {
		return "Contact{" + "relationId=" + relationId + ", name='" + name + '\'' + ", myName='" + myName + '\'' + ", contactId='" + contactId + '\''
				+ ", isSlave=" + isSlave + ", connectionCode='" + connectionCode + '\'' + ", status=" + status + '}';
	}

	/**
	 * Store this contact.
	 */
	public void store() {
		List<Integer> contactIds = PreferenceUtil.getSharedPreferenceIntList(R.string.key_contact_ids);
		if (!contactIds.contains(relationId)) {
			contactIds.add(relationId);
		}
		PreferenceUtil.setSharedPreferenceIntList(R.string.key_contact_ids, contactIds);
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_name, getRelationId(), getName());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_my_name, getRelationId(), getMyName());
		PreferenceUtil.setIndexedSharedPreferenceInt(R.string.key_contact_contact_id, getRelationId(), getContactId());
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
