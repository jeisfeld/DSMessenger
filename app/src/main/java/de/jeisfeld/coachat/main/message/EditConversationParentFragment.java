package de.jeisfeld.coachat.main.message;

import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.EditConversationDialogFragment;

/**
 * A fragment from which edit conversation dialog can be started.
 */
public interface EditConversationParentFragment {
	/**
	 * Handle the response of edit conversation dialog.
	 *
	 * @param dialog       The dialog.
	 * @param contact      The contact.
	 * @param conversation The new conversation data.
	 */
	public void handleEditConversationDialogResponse(final EditConversationDialogFragment dialog, final Contact contact,
													 final Conversation conversation);
}
