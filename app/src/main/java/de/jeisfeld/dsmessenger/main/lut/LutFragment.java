package de.jeisfeld.dsmessenger.main.lut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.dsmessenger.databinding.FragmentLutBinding;

public class LutFragment extends Fragment {

	private FragmentLutBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		LutViewModel lutViewModel =
				new ViewModelProvider(this).get(LutViewModel.class);

		binding = FragmentLutBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		final TextView textView = binding.textSlideshow;
		lutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}