package de.jeisfeld.coachat.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.util.PreferenceUtil;

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
	 * The slave permissions.
	 */
	private final SlavePermissions slavePermissions;
	/**
	 * The contact status.
	 */
	private final ContactStatus status;

	/**
	 * Constructor without id.
	 *
	 * @param relationId       The relation id
	 * @param name             The name
	 * @param myName           My name for the contact
	 * @param contactId        The contactId
	 * @param isSlave          The flag indicating if it is slave or master
	 * @param connectionCode   The connection code
	 * @param slavePermissions The slave permissions.
	 * @param status           The contact status
	 */
	public Contact(final int relationId, final String name, final String myName, final int contactId,
				   final boolean isSlave, final String connectionCode, final SlavePermissions slavePermissions, final ContactStatus status) {
		this.relationId = relationId;
		this.name = name;
		this.myName = myName;
		this.contactId = contactId;
		this.isSlave = isSlave;
		this.connectionCode = connectionCode;
		this.slavePermissions = slavePermissions;
		this.status = status;
	}

	/**
	 * Retrieve a contact from storage via id.
	 *
	 * @param relationId The id.
	 */
	public Contact(final int relationId) {
		this.relationId = relationId;
		name = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_name, relationId);
		myName = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_my_name, relationId);
		contactId = PreferenceUtil.getIndexedSharedPreferenceInt(R.string.key_contact_contact_id, relationId, -1);
		isSlave = PreferenceUtil.getIndexedSharedPreferenceBoolean(R.string.key_contact_is_slave, relationId, false);
		connectionCode = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_connection_code, relationId);
		slavePermissions =
				SlavePermissions.fromString(PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_slave_permissions, relationId));
		String statusString = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_status, relationId);
		status = statusString == null ? ContactStatus.INVITED : ContactStatus.valueOf(statusString);
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

	public final SlavePermissions getSlavePermissions() {
		return slavePermissions;
	}

	public final ContactStatus getStatus() {
		return status;
	}

	/**
	 * Get my permissions for this contact.
	 *
	 * @return My permissions for this contact.
	 */
	public SlavePermissions getMyPermissions() {
		if (isSlave() || getStatus() == ContactStatus.INVITED) {
			return SlavePermissions.ALL_PERMISSIONS;
		}
		else {
			return getSlavePermissions();
		}
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
				+ ", isSlave=" + isSlave + ", connectionCode='" + connectionCode + "', slavePermissions=" + slavePermissions
				+ ", status=" + status + '}';
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
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_slave_permissions, getRelationId(), getSlavePermissions().toString());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_status, getRelationId(), getStatus().name());
	}

	/**
	 * Get the conversations of this contact.
	 *
	 * @return The conversations.
	 */
	public List<Conversation> getConversations() {
		ConversationDao conversationDao = Application.getAppDatabase().getConversationDao();
		List<Conversation> result = new ArrayList<>(conversationDao.getConversationsByRelationId(getRelationId()));
		if (getMyPermissions().isManageConversations()) {
			result.add(Conversation.createNewConversation(this));
		}
		return result;
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
