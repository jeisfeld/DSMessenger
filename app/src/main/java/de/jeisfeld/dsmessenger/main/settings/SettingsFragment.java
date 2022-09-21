package de.jeisfeld.dsmessenger.main.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.jeisfeld.dsmessenger.R;

/**
 * Fragment for settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	ActivityResultLauncher<Intent> permissionResultLauncher;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		permissionResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (Settings.canDrawOverlays(getActivity())) {
						((CheckBoxPreference) Objects.requireNonNull(findPreference(getString(R.string.key_pref_screen_control)))).setChecked(true);
					}
				});
	}

	@Override
	public final void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		configureBatteryOptimizationButton();

		findPreference(getString(R.string.key_pref_night_mode)).setOnPreferenceChangeListener((preference, newValue) -> {
			AppCompatDelegate.setDefaultNightMode(Integer.parseInt((String) newValue));
			return true;
		});

		findPreference(getString(R.string.key_pref_screen_control)).setOnPreferenceChangeListener((preference, newValue) -> {
			if (Boolean.TRUE.equals(newValue)) {
				if (!Settings.canDrawOverlays(getActivity())) {
					Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
							Uri.parse("package:" + getActivity().getPackageName()));
					permissionResultLauncher.launch(permissionIntent);
					return false;
				}
			}
			return true;
		});
	}

	/**
	 * Configure the button for battery optimization.
	 */
	private void configureBatteryOptimizationButton() {
		Preference batteryOptimizationPreference = findPreference(getString(R.string.key_pref_dummy_setting_battery_optimizations));
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
}
