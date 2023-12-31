package de.jeisfeld.dsmessenger.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * The permissions of slave.
 */
public class SlavePermissions implements Serializable {
	/**
	 * Default slave permissions.
	 */
	public static final SlavePermissions DEFAULT_SLAVE_PERMISSIONS = new SlavePermissions(false, false, true, ReplyPolicy.UNLIMITED);
	/**
	 * All permissions (used for master).
	 */
	public static final SlavePermissions ALL_PERMISSIONS = new SlavePermissions(true, true, true, ReplyPolicy.UNLIMITED);
	/**
	 * Flag indicating if slave can edit the permissions.
	 */
	private final boolean editSlavePermissions;
	/**
	 * Flag indicating if slave can edit or delete the relation.
	 */
	private final boolean editRelation;
	/**
	 * Flag indicating if slave can manage conversations.
	 */
	private final boolean manageConversations;
	/**
	 * The default reply policy.
	 */
	private final ReplyPolicy defaultReplyPolicy;

	/**
	 * Constructor.
	 *
	 * @param editSlavePermissions Flag indicating if slave can edit the permissions.
	 * @param editRelation         Flag indicating if slave can edit or delete the relation.
	 * @param manageConversations  Flag indicating if slave can manage conversations.
	 * @param defaultReplyPolicy   The default reply policy.
	 */
	public SlavePermissions(final boolean editSlavePermissions, final boolean editRelation, final boolean manageConversations,
							final ReplyPolicy defaultReplyPolicy) {
		this.editSlavePermissions = editSlavePermissions;
		this.editRelation = editRelation;
		this.manageConversations = manageConversations;
		this.defaultReplyPolicy = defaultReplyPolicy;
	}

	/**
	 * Extract slave permissions from String.
	 *
	 * @param slavePermissions The permissions as String
	 * @return The slave permissions.
	 */
	public static SlavePermissions fromString(final String slavePermissions) {
		if (slavePermissions == null || slavePermissions.length() == 0) {
			return DEFAULT_SLAVE_PERMISSIONS;
		}
		boolean editSlavePermissions = charToBoolean(slavePermissions.charAt(0));
		boolean editRelation = charToBoolean(slavePermissions.charAt(1));
		boolean manageConversations = charToBoolean(slavePermissions.charAt(2));
		ReplyPolicy defaultReplyPolicy = ReplyPolicy.fromOrdinal(Integer.parseInt(Character.toString(slavePermissions.charAt(3)))); // MAGIC_NUMBER
		return new SlavePermissions(editSlavePermissions, editRelation, manageConversations, defaultReplyPolicy);
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
	 * Convert slave permissions into String.
	 *
	 * @return The string representation.
	 */
	@NonNull
	@Override
	public String toString() {
		return "" + booleanToChar(isEditSlavePermissions())
				+ booleanToChar(isEditRelation())
				+ booleanToChar(isManageConversations())
				+ getDefaultReplyPolicy().ordinal();
	}

	public final boolean isEditSlavePermissions() {
		return editSlavePermissions;
	}

	public final boolean isEditRelation() {
		return editRelation;
	}

	public final boolean isManageConversations() {
		return manageConversations;
	}

	public final ReplyPolicy getDefaultReplyPolicy() {
		return defaultReplyPolicy;
	}
}
