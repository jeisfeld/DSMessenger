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
	@ColumnInfo(name = "conversationId")
	@NonNull
	private final String conversationIdString;

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
	 * @param relationId              The relationId of the contact.
	 * @param subject                 The subject of the conversation.
	 * @param conversationIdString    The unique id of the conversation.
	 * @param lastTimestamp           The last timestamp of this conversation.
	 * @param conversationFlagsString The conversation flags.
	 */
	protected Conversation(final int relationId, final String subject, @NonNull final String conversationIdString, final long lastTimestamp,
						   final String conversationFlagsString) {
		this.relationId = relationId;
		this.subject = subject;
		this.conversationIdString = conversationIdString;
		this.lastTimestamp = lastTimestamp;
		this.conversationFlagsString = conversationFlagsString;
		isStored = true;
	}

	/**
	 * Constructor.
	 *
	 * @param relationId        The relationId of the contact.
	 * @param subject           The subject of the conversation.
	 * @param conversationId    The unique id of the conversation.
	 * @param lastTimestamp     The last timestamp of this conversation.
	 * @param conversationFlags The conversation flags.
	 */
	@Ignore
	public Conversation(final int relationId, final String subject, @NonNull final UUID conversationId, final long lastTimestamp,
						final ConversationFlags conversationFlags) {
		this(relationId, subject, conversationId.toString(), lastTimestamp, conversationFlags.toString());
	}

	/**
	 * Create a new conversation.
	 *
	 * @param contact The contact.
	 * @return the conversation.
	 */
	public static Conversation createNewConversation(final Contact contact) {
		Conversation result = new Conversation(contact.getRelationId(), Application.getResourceString(R.string.text_new_conversation_name),
				UUID.randomUUID(), System.currentTimeMillis(),
				new ConversationFlags(contact.getSlavePermissions().getDefaultReplyPolicy(), false, false));
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
		ReplyPolicy defaultReplyPolicy = textMessageDetails.getContact().getMyPermissions().getDefaultReplyPolicy();
		Conversation result = new Conversation(textMessageDetails.getContact().getRelationId(), textMessageDetails.getMessageText(),
				textMessageDetails.getConversationId(), textMessageDetails.getTimestamp(),
				new ConversationFlags(defaultReplyPolicy, defaultReplyPolicy.isExpectsAcknowledgement(),
						defaultReplyPolicy.isExpectsResponse() && !defaultReplyPolicy.isExpectsAcknowledgement()));
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

	/**
	 * Update flags with acknowledgement.
	 */
	public void updateWithAcknowledgement() {
		ReplyPolicy replyPolicy = getConversationFlags().getReplyPolicy();
		ConversationFlags newConversationFlags = new ConversationFlags(replyPolicy, false, replyPolicy.isExpectsResponse());
		setConversationFlags(newConversationFlags);
		update();
	}

	/**
	 * Update flags with response.
	 */
	public void updateWithResponse() {
		ReplyPolicy replyPolicy = getConversationFlags().getReplyPolicy();
		ConversationFlags newConversationFlags = new ConversationFlags(replyPolicy, false, replyPolicy == ReplyPolicy.UNLIMITED);
		setConversationFlags(newConversationFlags);
		update();
	}

	/**
	 * Update flags with new message.
	 */
	public void updateWithNewMessage() {
		ReplyPolicy replyPolicy = getConversationFlags().getReplyPolicy();
		ConversationFlags newConversationFlags = new ConversationFlags(replyPolicy, replyPolicy.isExpectsAcknowledgement(),
				replyPolicy.isExpectsResponse() && !replyPolicy.isExpectsAcknowledgement());
		setConversationFlags(newConversationFlags);
		update();
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
	protected final String getConversationIdString() {
		return conversationIdString;
	}

	public final UUID getConversationId() {
		return UUID.fromString(getConversationIdString());
	}

	public final long getLastTimestamp() {
		return lastTimestamp;
	}

	public final void setLastTimestamp(final long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	protected final String getConversationFlagsString() {
		return conversationFlagsString;
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
	public void setConversationFlags(final ConversationFlags conversationFlags) {
		conversationFlagsString = conversationFlags.toString();
	}

	public final boolean isStored() {
		return isStored;
	}
}
