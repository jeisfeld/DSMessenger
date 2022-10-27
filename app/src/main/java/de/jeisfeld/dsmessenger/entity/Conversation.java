package de.jeisfeld.dsmessenger.entity;

import java.io.Serializable;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.message.TextMessageDetails;

/**
 * A conversation with a contact.
 */
@Entity(tableName = "conversation")
public class Conversation implements Serializable {
	/**
	 * The relationId of the contact.
	 */
	@ColumnInfo(name = "relationId")
	private final int relationId;
	/**
	 * The subject of the conversation.
	 */
	@ColumnInfo(name = "subject")
	private String subject;
	/**
	 * The unique id of the conversation.
	 */
	@PrimaryKey
	@NonNull
	private final String conversationId;

	/**
	 * The last timestamp of this conversation.
	 */
	@ColumnInfo(name = "lastTimestamp")
	private long lastTimestamp;

	/**
	 * Flags that can be set on a conversation.
	 */
	@ColumnInfo(name = "conversationFlags")
	private String conversationFlagsString;

	/**
	 * Flag indicating if the conversation is already stored.
	 */
	@Ignore
	private boolean isStored;

	/**
	 * Constructor.
	 *
	 * @param relationId     The relationId of the contact.
	 * @param subject        The subject of the conversation.
	 * @param conversationId The unique id of the conversation.
	 * @param lastTimestamp  The last timestamp of this conversation.
	 */
	public Conversation(final int relationId, final String subject, @NonNull final String conversationId, final long lastTimestamp) {
		this.relationId = relationId;
		this.subject = subject;
		this.conversationId = conversationId;
		this.lastTimestamp = lastTimestamp;
		isStored = true;
	}

	/**
	 * Create a new conversation.
	 *
	 * @param contact The contact.
	 * @return the conversation.
	 */
	public static Conversation createNewConversation(final Contact contact) {
		Conversation result = new Conversation(contact.getRelationId(), Application.getResourceString(R.string.text_new_conversation_name),
				UUID.randomUUID().toString(), System.currentTimeMillis());
		result.isStored = false;
		return result;
	}

	/**
	 * Create a new conversation from received text message.
	 *
	 * @param textMessageDetails The received text message.
	 * @return the conversation.
	 */
	public static Conversation createNewConversation(final TextMessageDetails textMessageDetails) {
		Conversation result = new Conversation(textMessageDetails.getContact().getRelationId(), textMessageDetails.getMessageText(),
				textMessageDetails.getConversationId().toString(), textMessageDetails.getTimestamp());
		result.isStored = false;
		return result;
	}

	/**
	 * Store this conversation.
	 *
	 * @param newSubject The subject for storage.
	 */
	public void insertIfNew(final String newSubject) {
		if (!isStored) {
			if (newSubject != null) {
				setSubject(newSubject);
			}
			Application.getAppDatabase().getConversationDao().insert(this);
		}
	}

	/**
	 * Update the conversation in DB.
	 */
	public void update() {
		Application.getAppDatabase().getConversationDao().update(this);
		isStored = true;
	}

	public final int getRelationId() {
		return relationId;
	}

	public final String getSubject() {
		return subject;
	}

	public final void setSubject(final String subject) {
		this.subject = subject;
	}

	@NonNull
	public final String getConversationId() {
		return conversationId;
	}

	public final UUID getConversationUuid() {
		return UUID.fromString(getConversationId());
	}

	public final long getLastTimestamp() {
		return lastTimestamp;
	}

	public final void setLastTimestamp(final long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	protected String getConversationFlagsString() {
		return conversationFlagsString;
	}

	protected void setConversationFlagsString(String conversationFlagsString) {
		this.conversationFlagsString = conversationFlagsString;
	}

	/**
	 * Get the conversation flags.
	 *
	 * @return The conversation flags.
	 */
	public ConversationFlags getConversationFlags() {
		return ConversationFlags.fromString(getConversationFlagsString());
	}

	/**
	 * Set the conversation flags.
	 *
	 * @param conversationFlags The new conversation flags.
	 */
	public void setConversationFlags(ConversationFlags conversationFlags) {
		this.conversationFlagsString = conversationFlags.toString();
	}

	public final boolean isStored() {
		return isStored;
	}
}
