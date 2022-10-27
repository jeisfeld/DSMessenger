package de.jeisfeld.dsmessenger.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.main.account.SlavePermissions.ReplyPolicy;

/**
 * The flags on a conversation.
 */
public class ConversationFlags implements Serializable {
	/**
	 * Default slave permissions.
	 */
	public static final ConversationFlags DEFAULT_CONVERSATION_FLAGS = new ConversationFlags(ReplyPolicy.UNLIMITED);
	/**
	 * The reply policy.
	 */
	private final ReplyPolicy replyPolicy;

	/**
	 * Constructor.
	 *
	 * @param replyPolicy The reply policy.
	 */
	public ConversationFlags(final ReplyPolicy replyPolicy) {
		this.replyPolicy = replyPolicy;
	}

	/**
	 * Extract conversation flags from String.
	 *
	 * @param conversationFlags The flags as String
	 * @return The flags.
	 */
	public static ConversationFlags fromString(final String conversationFlags) {
		if (conversationFlags == null || conversationFlags.length() == 0) {
			return DEFAULT_CONVERSATION_FLAGS;
		}
		ReplyPolicy replyPolicy = ReplyPolicy.fromOrdinal(Integer.parseInt(Character.toString(conversationFlags.charAt(0))));
		return new ConversationFlags(replyPolicy);
	}

	/**
	 * Convert conversation flags into String.
	 *
	 * @return The string representation.
	 */
	@NonNull
	@Override
	public String toString() {
		return "" + getReplyPolicy().ordinal();
	}

	public ReplyPolicy getReplyPolicy() {
		return replyPolicy;
	}

}
