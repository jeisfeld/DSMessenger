package de.jeisfeld.coachat.main.message;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.account.AccountDialogUtil;
import de.jeisfeld.coachat.main.account.ContactRegistry;
import de.jeisfeld.coachat.message.AdminMessageDetails.AdminType;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;
import de.jeisfeld.coachat.service.FirebaseDsMessagingService;
import de.jeisfeld.coachat.util.DialogUtil;

/**
 * Adapter for the expandable list of conversations.
 */
public class ConversationsExpandableListAdapter extends BaseExpandableListAdapter {
	/**
	 * The triggering fragment.
	 */
	private final ConversationsFragment fragment;
	/**
	 * The list of connected contacts.
	 */
	private final List<Contact> contacts;
	/**
	 * Flag indicating if archived conversations are included.
	 */
	private boolean archived = false;

	/**
	 * Constructor.
	 *
	 * @param fragment The triggering fragment.
	 */
	protected ConversationsExpandableListAdapter(final ConversationsFragment fragment) {
		this.fragment = fragment;
		this.contacts = ContactRegistry.getInstance().getConnectedContacts();
	}

	@Override
	public final int getGroupCount() {
		return contacts.size();
	}

	@Override
	public final int getChildrenCount(final int groupPosition) {
		return contacts.get(groupPosition).getConversations(archived).size();
	}

	@Override
	public final List<Conversation> getGroup(final int groupPosition) {
		return contacts.get(groupPosition).getConversations(archived);
	}

	@Override
	public final Object getChild(final int groupPosition, final int childPosition) {
		return contacts.get(groupPosition).getConversations(archived).get(childPosition);
	}

	@Override
	public final long getGroupId(final int groupPosition) {
		return groupPosition;
	}

	@Override
	public final long getChildId(final int groupPosition, final int childPosition) {
		return childPosition;
	}

	@Override
	public final boolean hasStableIds() {
		return false;
	}

	@Override
	public final View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
		TextView view = (TextView) convertView;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) fragment.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert layoutInflater != null;
			view = (TextView) layoutInflater.inflate(R.layout.list_view_contact_conversations, parent, false);
		}
		if (groupPosition >= getGroupCount()) {
			// Sometimes, after deletion outdated groups are called.
			return view;
		}
		view.setText(contacts.get(groupPosition).getName());

		return view;
	}

	@Override
	public final void notifyDataSetChanged() {
		contacts.clear();
		contacts.addAll(ContactRegistry.getInstance().getConnectedContacts());
		super.notifyDataSetChanged();
	}

	@Override
	public final View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView,
								   final ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) fragment.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert layoutInflater != null;
			view = layoutInflater.inflate(R.layout.list_view_conversation, parent, false);
		}
		Contact contact = contacts.get(groupPosition);
		Conversation conversation = contact.getConversations(archived).get(childPosition);

		TextView textViewSubject = view.findViewById(R.id.textViewSubject);
		textViewSubject.setText(conversation.getSubject());

		prepareConversationButtons(view, contact, conversation);

		textViewSubject.setOnClickListener(v -> {
			Activity activity = fragment.getActivity();
			if (activity != null) {
				NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
				Bundle bundle = new Bundle();
				bundle.putSerializable("contact", contact);
				bundle.putSerializable("conversation", conversation);
				navController.navigate(R.id.nav_conversations_to_message, bundle);
			}
		});

		return view;
	}

	/**
	 * Prepare the buttons of the conversation.
	 *
	 * @param view         the conversation view.
	 * @param contact      the contact.
	 * @param conversation the conversation.
	 */
	private void prepareConversationButtons(final View view, final Contact contact, final Conversation conversation) {
		View buttonDelete = view.findViewById(R.id.button_delete);
		View buttonEdit = view.findViewById(R.id.button_edit);
		if (conversation.isStored() && contact.getMyPermissions().isManageConversations()) {
			buttonDelete.setVisibility(View.VISIBLE);
			buttonEdit.setVisibility(View.VISIBLE);
			buttonEdit.setOnClickListener(v -> {
				FragmentActivity activity = fragment.getActivity();
				if (activity != null) {
					AccountDialogUtil.displayEditConversationDialog(fragment, conversation, contact);
				}
			});
			buttonDelete.setOnClickListener(v -> {
				FragmentActivity activity = fragment.getActivity();
				if (conversation.isStored() && activity != null) {
					DialogUtil.displayConfirmationMessage(activity, dialog -> {
								UUID conversationId = conversation.getConversationId();
								Application.getAppDatabase().getConversationDao().delete(conversation);
								notifyDataSetChanged();
								new HttpSender(activity).sendMessage("db/conversation/deleteconversation.php", contact, UUID.randomUUID(), null,
										"messageType", MessageType.ADMIN.name(), "adminType", AdminType.CONVERSATION_DELETED.name(),
										"conversationId", conversationId.toString());
								FirebaseDsMessagingService.cancelNotification(fragment.getContext(), conversation.getRelationId());
							},
							R.string.title_dialog_confirm_deletion, R.string.button_cancel, R.string.button_delete_conversation,
							R.string.dialog_confirm_delete_conversation, conversation.getSubject());
				}
			});
		}
		else {
			buttonDelete.setVisibility(View.GONE);
			buttonEdit.setVisibility(View.GONE);
		}
	}


	@Override
	public final boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
