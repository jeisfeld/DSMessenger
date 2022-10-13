package de.jeisfeld.dsmessenger.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.lang.ref.WeakReference;

import de.jeisfeld.dsmessenger.R;

/**
 * Class for managing dropdowns.
 *
 * @param <T> The class for dropdown objects.
 */
public class DropdownHandler<T> {
	/**
	 * The values.
	 */
	private final T[] values;
	/**
	 * The view.
	 */
	private final WeakReference<AutoCompleteTextView> viewReference;

	/**
	 * Constructor.
	 *
	 * @param context The context.
	 * @param view    The view for the dropdown.
	 * @param values  The values of the dropdown.
	 */
	public DropdownHandler(final Context context, final AutoCompleteTextView view, final T[] values) {
		this.values = values;
		view.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_item, values));
		this.viewReference = new WeakReference<>(view);
	}

	/**
	 * Get dropdown handler from array resource.
	 *
	 * @param context         The context.
	 * @param view            The view for the dropdown.
	 * @param resource        The array resource with the dropdown values.
	 * @param initialPosition The initial position;
	 */
	public static DropdownHandler<String> fromResource(final Context context, final AutoCompleteTextView view, int resource, int initialPosition) {
		DropdownHandler<String> result = new DropdownHandler<>(context, view, context.getResources().getStringArray(resource));
		result.selectEntry(initialPosition);
		return result;
	}

	/**
	 * Select entry for a specific position.
	 *
	 * @param position The position.
	 */
	public void selectEntry(int position) {
		AutoCompleteTextView view = viewReference.get();
		if (view != null) {
			view.setText(values[position].toString(), false);
		}
	}

	/**
	 * Get the selected position of the dropdown.
	 *
	 * @return The selected position.
	 */
	public int getSelectedPosition() {
		AutoCompleteTextView view = viewReference.get();
		if (view == null) {
			return -1;
		}
		else {
			for (int i = 0; i < values.length; i++) {
				if (values[i].toString().equals(view.getText().toString())) {
					return i;
				}
			}
		}
		return -1;
	}

	public T getSelectedItem() {
		int i = getSelectedPosition();
		if (i < 0) {
			return null;
		}
		else {
			return values[i];
		}
	}
}
