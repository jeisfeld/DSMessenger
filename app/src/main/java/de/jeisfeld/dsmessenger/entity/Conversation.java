package de.jeisfeld.dsmessenger.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A conversation with a contact.
 */
public class Conversation implements Serializable {
	/**
	 * The relationId of the contact.
	 */
	private final int relationId;
	/**
	 * The subject of the conversation.
	 */
	private final String subject;
	/**
	 * The unique id of the conversation.
	 */
	private final UUID conversationId;
	/**
	 * The list of messages of the conversation.
	 */
	private final List<MessageInfo> messages = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param relationId     The relationId of the contact.
	 * @param subject        The subject of the conversation.
	 * @param conversationId The unique id of the conversation.
	 */
	public Conversation(int relationId, String subject, UUID conversationId) {
		this.relationId = relationId;
		this.subject = subject;
		this.conversationId = conversationId;
	}

	public int getRelationId() {
		return relationId;
	}

	public String getSubject() {
		return subject;
	}

	public UUID getConversationId() {
		return conversationId;
	}

	public List<MessageInfo> getMessages() {
		return messages;
	}
}
