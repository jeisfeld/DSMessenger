package de.jeisfeld.coachat.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * The flags on a conversation.
 */
public class ConversationFlags implements Serializable {
	/**
	 * Default slave permissions.
	 */
	public static final ConversationFlags DEFAULT_CONVERSATION_FLAGS = new ConversationFlags(ReplyPolicy.UNLIMITED, false, true);
	/**
	 * The reply policy.
	 */
	private final ReplyPolicy replyPolicy;
	/**
	 * Flag indicating if acknowledgement from slave is expected.
	 */
	private final boolean expectingAcknowledgement;
	/**
	 * Flag indicating if response from slave is expected.
	 */
	private final boolean expectingResponse;

	/**
	 * Constructor.
	 *
	 * @param replyPolicy              The reply policy.
	 * @param expectingAcknowledgement Flag indicating if acknowledgement from slave is expected.
	 * @param expectingResponse        Flag indicating if response from slave is expected.
	 */
	public ConversationFlags(final ReplyPolicy replyPolicy, final boolean expectingAcknowledgement, final boolean expectingResponse) {
		this.replyPolicy = replyPolicy;
		this.expectingAcknowledgement = expectingAcknowledgement;
		this.expectingResponse = expectingResponse;
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

		boolean expectingAcknowledgement = charToBoolean(conversationFlags.charAt(1));
		boolean expectingResponse = charToBoolean(conversationFlags.charAt(2));
		return new ConversationFlags(replyPolicy, expectingAcknowledgement, expectingResponse);
	}

	/**
	 * Get char representation of a boolean.
	 *
	 * @param b The boolean
	 * @return The char representation
	 */
	private static char booleanToChar(final boolean b) {
		return b ? '1' : '0';
	}

	/**
	 * Restore boolean from char representation.
	 *
	 * @param c The char representation
	 * @return The boolean
	 */
	private static boolean charToBoolean(final char c) {
		return c == '1';
	}

	/**
	 * Convert conversation flags into String.
	 *
	 * @return The string representation.
	 */
	@NonNull
	@Override
	public String toString() {
		return "" + getReplyPolicy().ordinal() + booleanToChar(expectingAcknowledgement) + booleanToChar(expectingResponse);
	}

	public final ReplyPolicy getReplyPolicy() {
		return replyPolicy;
	}

	public final boolean isExpectingAcknowledgement() {
		return expectingAcknowledgement;
	}

	public final boolean isExpectingResponse() {
		return expectingResponse;
	}
}
