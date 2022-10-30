package de.jeisfeld.dsmessenger.entity;

/**
 * The reply policy.
 */
public enum ReplyPolicy {
	/**
	 * Allow unlimited responses.
	 */
	UNLIMITED(false, true),
	/**
	 * Allow only acknowledgement.
	 */
	ONLY_ACKNOWLEDGE(true, false),
	/**
	 * Allow one response.
	 */
	ONE_RESPONSE(false, true),
	/**
	 * Allow acknowledgement, followed by response.
	 */
	ACKNOWLEDGE_AND_RESPONSE(true, true);
	/**
	 * Flag indicating if an acknowledgement is expected.
	 */
	private final boolean expectsAcknowledgement;
	/**
	 * Flag indicating if a response is expected.
	 */
	private final boolean expectsResponse;

	/**
	 * Constructor.
	 *
	 * @param expectsAcknowledgement Flag indicating if an acknowledgement is expected.
	 * @param expectsResponse        Flag indicating if a response is expectec.
	 */
	ReplyPolicy(final boolean expectsAcknowledgement, final boolean expectsResponse) {
		this.expectsAcknowledgement = expectsAcknowledgement;
		this.expectsResponse = expectsResponse;
	}

	/**
	 * Get a messageType from its ordinal value.
	 *
	 * @param ordinal The ordinal value.
	 * @return The message type.
	 */
	public static ReplyPolicy fromOrdinal(final int ordinal) {
		for (ReplyPolicy replyPolicy : values()) {
			if (replyPolicy.ordinal() == ordinal) {
				return replyPolicy;
			}
		}
		return ONLY_ACKNOWLEDGE;
	}

	public boolean isExpectsAcknowledgement() {
		return expectsAcknowledgement;
	}

	public boolean isExpectsResponse() {
		return expectsResponse;
	}
}
