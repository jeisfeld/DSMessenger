package de.jeisfeld.dsmessenger.main.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.util.DialogUtil;

/**
 * Fragment for settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public final void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		configureBatteryOptimizationButton();
		configureScreenControlButton();

		findPreference(getString(R.string.key_pref_night_mode)).setOnPreferenceChangeListener((preference, newValue) -> {
			AppCompatDelegate.setDefaultNightMode(Integer.parseInt((String) newValue));
			return true;
		});
	}

	/**
	 * Configure the button for battery optimization.
	 */
	private void configureBatteryOptimizationButton() {
		Preference batteryOptimizationPreference = findPreference(getString(R.string.key_pref_dummy_battery_optimizations));
		assert batteryOptimizationPreference != null;
		batteryOptimizationPreference.setOnPreferenceClickListener(preference -> {
			PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
			Intent intent = new Intent();
			if (pm.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
				intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
			}
			else {
				intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + getContext().getPackageName()));
			}
			startActivity(intent);
			return true;
		});
	}

	/**
	 * Configure the button for screen control.
	 */
	private void configureScreenControlButton() {
		Preference screenControlPreference = findPreference(getString(R.string.key_pref_dummy_screen_control));
		assert screenControlPreference != null;
		screenControlPreference.setOnPreferenceClickListener(preference -> {
			final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
			if (Settings.canDrawOverlays(getActivity())) {
				startActivity(intent);
				return true;
			}
			else {
				DialogUtil.displayConfirmationMessage(getActivity(), dialog -> startActivity(intent), R.string.title_dialog_info,
						null, R.string.button_ok, R.string.dialog_screen_control_permission, getString(R.string.app_name));
				return true;
			}
		});
	}
}
