package de.jeisfeld.dsmessenger.main.randomimage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.dsmessenger.databinding.FragmentRandomimageBinding;

public class RandomimageFragment extends Fragment {

	private FragmentRandomimageBinding binding;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		RandomimageViewModel randomimageViewModel =
				new ViewModelProvider(this).get(RandomimageViewModel.class);

		binding = FragmentRandomimageBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		final TextView textView = binding.textGallery;
		randomimageViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}