package de.jeisfeld.dsmessenger.main.lut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.dsmessenger.databinding.FragmentLutBinding;

/**
 * Fragment for LuT controlling.
 */
public class LutFragment extends Fragment {
	/**
	 * The view binding.
	 */
	private FragmentLutBinding binding;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater,
								   final ViewGroup container, final Bundle savedInstanceState) {

		binding = FragmentLutBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public final void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
