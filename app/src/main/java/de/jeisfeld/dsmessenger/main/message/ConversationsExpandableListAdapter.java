package de.jeisfeld.dsmessenger.main.message;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.entity.Contact;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;

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
		return contacts.get(groupPosition).getConversations().size();
	}

	@Override
	public final List<Conversation> getGroup(final int groupPosition) {
		return contacts.get(groupPosition).getConversations();
	}

	@Override
	public final Object getChild(final int groupPosition, final int childPosition) {
		return contacts.get(groupPosition).getConversations().get(childPosition);
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
		TextView view = (TextView) convertView;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) fragment.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert layoutInflater != null;
			view = (TextView) layoutInflater.inflate(R.layout.list_view_conversation, parent, false);
		}
		Contact contact = contacts.get(groupPosition);
		Conversation conversation = contact.getConversations().get(childPosition);
		view.setText(conversation.getSubject());

		view.setOnClickListener(v -> {
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

	@Override
	public final boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}

}
