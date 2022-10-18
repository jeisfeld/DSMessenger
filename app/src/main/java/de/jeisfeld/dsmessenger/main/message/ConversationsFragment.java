package de.jeisfeld.dsmessenger.main.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.databinding.FragmentConversationBinding;

/**
 * Fragment for sending messages.
 */
public class ConversationsFragment extends Fragment {
	/**
	 * The view binding.
	 */
	private FragmentConversationBinding binding;

	/**
	 * The threadId.
	 */
	private UUID conversationId;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		binding = FragmentConversationBinding.inflate(inflater, container, false);
		ConversationsExpandableListAdapter adapter = new ConversationsExpandableListAdapter(this);
		binding.listViewConversations.setAdapter(adapter);
		for (int i = 0; i < adapter.getGroupCount(); i++) {
			binding.listViewConversations.expandGroup(i);
		}

		return binding.getRoot();
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
