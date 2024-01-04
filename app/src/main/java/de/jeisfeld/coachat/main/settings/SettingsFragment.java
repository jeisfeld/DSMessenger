package de.jeisfeld.coachat.main.settings;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.util.DialogUtil;

/**
 * Fragment for settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	/**
	 * A launcher for requesting notification permission
	 */
	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new RequestPermission(), isGranted -> {
				if (isGranted) {
					Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
					intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
					intent.putExtra(Settings.EXTRA_CHANNEL_ID, Application.NOTIFICATION_CHANNEL_ID);
					startActivity(intent);
				}
			});

	@Override
	public final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public final void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		configureBatteryOptimizationButton();
		configureScreenControlButton();
		configureNotificationSettingsButton();

		findPreference(getString(R.string.key_pref_night_mode)).setOnPreferenceChangeListener((preference, newValue) -> {
			AppCompatDelegate.setDefaultNightMode(Integer.parseInt((String) newValue));
			return true;
		});

		findPreference(getString(R.string.key_pref_language)).setOnPreferenceChangeListener((preference, newValue) -> {
			Application.startApplication(getActivity());
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
	 * Configure the button for notification settings.
	 */
	private void configureNotificationSettingsButton() {
		Preference notificationSettingsPreference = findPreference(getString(R.string.key_pref_dummy_notification_settings));
		assert notificationSettingsPreference != null;
		notificationSettingsPreference.setOnPreferenceClickListener(preference -> {
			if (ContextCompat.checkSelfPermission(
					getContext(), permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
				intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
				intent.putExtra(Settings.EXTRA_CHANNEL_ID, Application.NOTIFICATION_CHANNEL_ID);
				startActivity(intent);
			}
			else {
				requestPermissionLauncher.launch(permission.POST_NOTIFICATIONS);
			}
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
