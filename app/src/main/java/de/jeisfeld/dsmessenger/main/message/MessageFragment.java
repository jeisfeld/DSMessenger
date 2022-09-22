package de.jeisfeld.dsmessenger.main.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.dsmessenger.databinding.FragmentMessageBinding;

public class MessageFragment extends Fragment {

	private FragmentMessageBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		MessageViewModel messageViewModel =
				new ViewModelProvider(this).get(MessageViewModel.class);

		binding = FragmentMessageBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		final EditText editTextMessageText = binding.editTextMessageText;
		messageViewModel.getMessageText().observe(getViewLifecycleOwner(), editTextMessageText::setText);
		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}