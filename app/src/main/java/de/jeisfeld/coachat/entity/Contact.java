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
	 * The AI Relation Id
	 */
	private final Integer aiRelationId;
	/**
	 * The AI Policy.
	 */
	private final AiPolicy aiPolicy;
	/**
	 * The AI Username.
	 */
	private final String aiUsername;
	/**
	 * The AI Additional Priming Text.
	 */
	private final String aiAddPrimingText;
	/**
	 * The AI Message Suffix.
	 */
	private final String aiMessageSuffix;

	/**
	 * A timeout after which AI is triggered.
	 */
	private final Long aiTimeout;

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
	 * @param aiRelationId     The AI relation id
	 * @param aiPolicy         The AI Policy
	 * @param aiUsername       The AI Username
	 * @param aiAddPrimingText The AI additional priming text
	 * @param aiMessageSuffix  The AI message suffix
	 * @param aiTimeout        The AI timeout
	 */
	public Contact(final int relationId, final String name, final String myName, final int contactId,
				   final boolean isSlave, final String connectionCode, final SlavePermissions slavePermissions, final ContactStatus status,
				   final Integer aiRelationId, final AiPolicy aiPolicy, final String aiUsername, final String aiAddPrimingText,
				   final String aiMessageSuffix, final Long aiTimeout) {
		this.relationId = relationId;
		this.name = name;
		this.myName = myName;
		this.contactId = contactId;
		this.isSlave = isSlave;
		this.connectionCode = connectionCode;
		this.slavePermissions = slavePermissions;
		this.status = status;
		this.aiRelationId = aiRelationId;
		this.aiPolicy = aiPolicy;
		this.aiUsername = aiUsername;
		this.aiAddPrimingText = aiAddPrimingText;
		this.aiMessageSuffix = aiMessageSuffix;
		this.aiTimeout = aiTimeout;
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
		int storedAiRelationId = PreferenceUtil.getIndexedSharedPreferenceInt(R.string.key_contact_ai_relation_id, relationId, -1);
		aiRelationId = storedAiRelationId == -1 ? null : storedAiRelationId;
		aiPolicy = AiPolicy.fromOrdinal(PreferenceUtil.getIndexedSharedPreferenceInt(R.string.key_contact_ai_policy, relationId, 0));
		aiUsername = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_ai_username, relationId);
		aiAddPrimingText = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_ai_add_priming_text, relationId);
		aiMessageSuffix = PreferenceUtil.getIndexedSharedPreferenceString(R.string.key_contact_ai_message_suffix, relationId);
		long storedAiTimeout = PreferenceUtil.getIndexedSharedPreferenceLong(R.string.key_contact_ai_timeout, relationId, -1);
		aiTimeout = storedAiTimeout == -1 ? null : storedAiTimeout;
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

	public Integer getAiRelationId() {
		return aiRelationId;
	}

	public AiPolicy getAiPolicy() {
		return aiPolicy == null ? AiPolicy.NONE : aiPolicy;
	}

	public String getAiUsername() {
		return aiUsername;
	}

	public String getAiAddPrimingText() {
		return aiAddPrimingText;
	}

	public String getAiMessageSuffix() {
		return aiMessageSuffix;
	}

	public Long getAiTimeout() {
		return aiTimeout;
	}

	/**
	 * Get the time to be used for an alarm.
	 *
	 * @return The time to be used for an alarm.
	 */
	public Long getAlarmTime() {
		return getAiTimeout() == null ? null
				: Application.getAppDatabase().getConversationDao().getLastTimestampForContact(getRelationId()) + getAiTimeout();
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
				+ ", status=" + status + ", aiPolicy=" + aiPolicy + ", aiUsername=" + aiUsername + ", aiTimeout=" + aiTimeout + '}';
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
		PreferenceUtil.setIndexedSharedPreferenceInt(R.string.key_contact_ai_policy, getRelationId(), getAiPolicy().ordinal());
		if (getAiRelationId() != null) {
			PreferenceUtil.setIndexedSharedPreferenceInt(R.string.key_contact_ai_relation_id, getAiRelationId(), getContactId());
		}
		else {
			PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_ai_relation_id, getRelationId());
		}
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_ai_username, getRelationId(), getAiUsername());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_ai_add_priming_text, getRelationId(), getAiAddPrimingText());
		PreferenceUtil.setIndexedSharedPreferenceString(R.string.key_contact_ai_message_suffix, getRelationId(), getAiMessageSuffix());
		if (getAiTimeout() != null) {
			PreferenceUtil.setIndexedSharedPreferenceLong(R.string.key_contact_ai_timeout, getRelationId(), getAiTimeout());
		}
		else {
			PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_ai_timeout, getRelationId());
		}
	}

	/**
	 * Get the conversations of this contact.
	 *
	 * @param archived Flag indicating if archived conversations should be included.
	 * @return The conversations.
	 */
	public List<Conversation> getConversations(final boolean archived) {
		ConversationDao conversationDao = Application.getAppDatabase().getConversationDao();
		List<Conversation> result = archived ? new ArrayList<>(conversationDao.getConversationsByRelationId(getRelationId()))
				: new ArrayList<>(conversationDao.getUnarchivedConversationsByRelationId(getRelationId()));
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


	/**
	 * AI Policies
	 */
	public enum AiPolicy {
		/**
		 * No use of AI.
		 */
		NONE,
		/**
		 * Manual use of AI.
		 */
		MANUAL,
		/**
		 * Automatic use of AI.
		 */
		AUTOMATIC,
		/**
		 * Automatic use of AI, no message forwarding.
		 */
		AUTOMATIC_NOMESSAGE,
		/**
		 * Manual use of AI in near-realtime.
		 */
		MANUAL_LIVE;

		/**
		 * Get a AiPolicy from its ordinal value.
		 *
		 * @param ordinal The ordinal value.
		 * @return The AI Policy.
		 */
		public static AiPolicy fromOrdinal(final int ordinal) {
			for (AiPolicy messageDisplayType : values()) {
				if (messageDisplayType.ordinal() == ordinal) {
					return messageDisplayType;
				}
			}
			return NONE;
		}
	}

}
