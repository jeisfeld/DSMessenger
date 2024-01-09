package de.jeisfeld.coachat.main.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.coachat.databinding.FragmentConversationBinding;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.account.AccountDialogUtil.EditConversationDialogFragment;
import de.jeisfeld.coachat.main.account.ContactRegistry;
import de.jeisfeld.coachat.message.AdminMessageDetails.AdminType;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;

/**
 * Fragment for sending messages.
 */
public class ConversationsFragment extends Fragment implements EditConversationParentFragment {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.coachat.main.message.ConversationsFragment";
	/**
	 * The view binding.
	 */
	private FragmentConversationBinding binding;
	/**
	 * The adapter for the display.
	 */
	private ConversationsExpandableListAdapter adapter;
	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				ConversationsFragment.ActionType actionType = (ConversationsFragment.ActionType) intent.getSerializableExtra("actionType");
				switch (actionType) {
				case CONVERSATION_EDITED:
				case CONVERSATION_DELETED:
				case CONVERSATION_ADDED:
				case CONTACTS_UPDATED:
					adapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
			}
		}
	};
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context      The context.
	 * @param actionType   The action type.
	 * @param conversation The conversation.
	 * @param parameters   The parameters.
	 */
	public static void sendBroadcast(final Context context, final ConversationsFragment.ActionType actionType, final Conversation conversation,
									 final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		if (conversation != null) {
			bundle.putSerializable("conversation", conversation);
		}
		int i = 0;
		while (i < parameters.length - 1) {
			String key = parameters[i++];
			String value = parameters[i++];
			bundle.putString(key, value);
		}
		intent.putExtras(bundle);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentConversationBinding.inflate(inflater, container, false);
		adapter = new ConversationsExpandableListAdapter(this);
		binding.listViewConversations.setAdapter(adapter);
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			binding.listViewConversations.expandGroup(i);
		}
		binding.imageViewRefreshContacts.setOnClickListener(
				v -> ContactRegistry.getInstance().refreshContacts(getContext(),
						() -> ContactRegistry.getInstance().refreshConversations(getContext(), () -> {
							if (adapter != null) {
								getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
							}
						})));
		binding.toggleShowArchived.setOnCheckedChangeListener((buttonView, isChecked) -> {
			adapter.setArchived(binding.toggleShowArchived.isChecked());
			getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
		});
		return binding.getRoot();
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@Override
	public final void onAttach(@NonNull final Context context) {
		super.onAttach(context);
		broadcastManager = LocalBroadcastManager.getInstance(context);
		IntentFilter actionReceiver = new IntentFilter();
		actionReceiver.addAction(BROADCAST_ACTION);
		broadcastManager.registerReceiver(localBroadcastReceiver, actionReceiver);
	}

	@Override
	public final void onDetach() {
		super.onDetach();
		broadcastManager.unregisterReceiver(localBroadcastReceiver);
	}

	/**
	 * Handle the response of edit conversation dialog.
	 *
	 * @param dialog       The dialog.
	 * @param contact      The contact.
	 * @param conversation The new conversation data.
	 */
	public void handleEditConversationDialogResponse(final EditConversationDialogFragment dialog, final Contact contact,
													 final Conversation conversation) {
		conversation.update();
		adapter.notifyDataSetChanged();
		Activity activity = getActivity();
		if (activity != null) {
			new HttpSender(activity).sendMessage("db/conversation/editconversation.php", contact, UUID.randomUUID(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.CONVERSATION_EDITED.name(),
					"conversationId", conversation.getConversationId().toString(), "subject", conversation.getSubject(),
					"archived", Boolean.toString(conversation.isArchived()),
					"conversationFlags", conversation.getConversationFlags().toString());
		}
		dialog.dismiss();
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Inform about conversation edited.
		 */
		CONVERSATION_EDITED,
		/**
		 * Inform about conversation deleted.
		 */
		CONVERSATION_DELETED,
		/**
		 * Inform about conversation added.
		 */
		CONVERSATION_ADDED,
		/**
		 * Inform about contact changed.
		 */
		CONTACTS_UPDATED
	}
}
