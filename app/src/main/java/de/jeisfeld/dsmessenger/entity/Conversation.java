package de.jeisfeld.dsmessenger.entity;

import java.io.Serializable;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
	private final String subject;
	/**
	 * The unique id of the conversation.
	 */
	@PrimaryKey
	@NonNull
	private final String conversationId;

	/**
	 * Constructor.
	 *
	 * @param relationId     The relationId of the contact.
	 * @param subject        The subject of the conversation.
	 * @param conversationId The unique id of the conversation.
	 */
	public Conversation(int relationId, String subject, @NonNull String conversationId) {
		this.relationId = relationId;
		this.subject = subject;
		this.conversationId = conversationId;
	}

	/**
	 * Constructor.
	 *
	 * @param relationId     The relationId of the contact.
	 * @param subject        The subject of the conversation.
	 * @param conversationId The unique id of the conversation.
	 */
	@Ignore
	public Conversation(int relationId, String subject, UUID conversationId) {
		this(relationId, subject, conversationId.toString());
	}

	public int getRelationId() {
		return relationId;
	}

	public String getSubject() {
		return subject;
	}

	@NonNull
	public String getConversationId() {
		return conversationId;
	}

	public UUID getConversationUuid() {
		return UUID.fromString(getConversationId());
	}
}
