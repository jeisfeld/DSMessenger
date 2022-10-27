package de.jeisfeld.dsmessenger.entity;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.main.message.MessageFragment.MessageStatus;

import static androidx.room.ForeignKey.CASCADE;

/**
 * The data object for storing a message.
 */
@Entity(tableName = "message", foreignKeys = {
		@ForeignKey(onDelete = CASCADE, entity = Conversation.class, parentColumns = "conversationId", childColumns = "conversationId")},
		indices = {@Index("conversationId")})
public class Message {
	/**
	 * The message text.
	 */
	@ColumnInfo(name = "messageText")
	private final String messageText;
	/**
	 * Indicator if it is own message or contact's message.
	 */
	@ColumnInfo(name = "isOwn")
	private final boolean isOwn;
	/**
	 * The messageId.
	 */
	@PrimaryKey
	@ColumnInfo(name = "messageId")
	@NonNull
	private final String messageIdString;
	/**
	 * The conversationId.
	 */
	@ColumnInfo(name = "conversationId")
	@NonNull
	private final String conversationIdString;
	/**
	 * The timestamp of the message.
	 */
	@ColumnInfo(name = "timestamp")
	private final long timestamp;
	/**
	 * The message status.
	 */
	@ColumnInfo(name = "status")
	private MessageStatus status;

	/**
	 * Constructor.
	 *
	 * @param messageText          The message text.
	 * @param isOwn                Indicator if it is own message or contact's message.
	 * @param messageIdString      The messageId.
	 * @param conversationIdString The conversationId.
	 * @param timestamp            The timestamp of the message.
	 * @param status               The message status
	 */
	protected Message(final String messageText, final boolean isOwn, @NonNull final String messageIdString, @NonNull final String conversationIdString,
					  final long timestamp, final MessageStatus status) {
		this.messageText = messageText;
		this.isOwn = isOwn;
		this.messageIdString = messageIdString;
		this.conversationIdString = conversationIdString;
		this.timestamp = timestamp;
		this.status = status;
	}

	/**
	 * Constructor.
	 *
	 * @param messageText    The message text.
	 * @param isOwn          Indicator if it is own message or contact's message.
	 * @param messageId      The messageId.
	 * @param conversationId The conversationId.
	 * @param timestamp      The timestamp of the message.
	 * @param status         The message status
	 */
	@Ignore
	public Message(final String messageText, final boolean isOwn, final UUID messageId, final UUID conversationId,
				   final long timestamp, final MessageStatus status) {
		this(messageText, isOwn, messageId.toString(), conversationId.toString(), timestamp, status);
	}

	/**
	 * Store this message.
	 *
	 * @param conversation Conversation where timestamp should be updated.
	 */
	public void store(final Conversation conversation) {
		Application.getAppDatabase().getMessageDao().insert(this);
		if (conversation != null) {
			conversation.setLastTimestamp(getTimestamp());
			conversation.update();
		}
	}

	/**
	 * Update the message in DB.
	 */
	public void update() {
		Application.getAppDatabase().getMessageDao().update(this);
	}

	public final String getMessageText() {
		return messageText;
	}

	public final boolean isOwn() {
		return isOwn;
	}

	@NonNull
	protected final String getMessageIdString() {
		return messageIdString;
	}

	public final UUID getMessageId() {
		return UUID.fromString(getMessageIdString());
	}

	@NonNull
	protected final String getConversationIdString() {
		return conversationIdString;
	}

	public final UUID getConversationId() {
		return UUID.fromString(getConversationIdString());
	}

	public final long getTimestamp() {
		return timestamp;
	}

	public final MessageStatus getStatus() {
		return status;
	}

	public void setStatus(MessageStatus status) {
		this.status = status;
	}
}
