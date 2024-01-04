package de.jeisfeld.coachat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.util.Log;

import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;
import de.jeisfeld.coachat.entity.AppDatabase;
import de.jeisfeld.coachat.main.MainActivity;
import de.jeisfeld.coachat.util.PreferenceUtil;

/**
 * Utility class to retrieve base application resources.
 */
public class Application extends android.app.Application {
	/**
	 * The notification channel id.
	 */
	public static final String NOTIFICATION_CHANNEL_ID = "Coachat Messages";

	/**
	 * A utility field to store a context statically.
	 */
	@SuppressLint("StaticFieldLeak")
	private static Context mContext;
	/**
	 * The default tag for logging.
	 */
	public static final String TAG = "Coachat.JE";
	/**
	 * The default locale.
	 */
	@SuppressLint("ConstantLocale")
	private static final Locale DEFAULT_LOCALE = Locale.getDefault();
	/**
	 * The application database.
	 */
	private static AppDatabase appDatabase;

	/**
	 * Retrieve the application database.
	 *
	 * @return The (statically storec) application database
	 */
	public static AppDatabase getAppDatabase() {
		return Application.appDatabase;
	}

	/**
	 * Retrieve the application context.
	 *
	 * @return The (statically stored) application context
	 */
	public static Context getAppContext() {
		return Application.mContext;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		Application.mContext = getApplicationContext();
		Application.mContext = Application.createContextWrapperForLocale(getApplicationContext());

		AppCompatDelegate.setDefaultNightMode(
				PreferenceUtil.getSharedPreferenceIntString(R.string.key_pref_night_mode, R.string.pref_default_night_mode));

		appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "coachat")
				.addMigrations(AppDatabase.MIGRATION_2_3)
				.allowMainThreadQueries().build();

		createNotificationChannel();
	}

	/**
	 * Get a resource string.
	 *
	 * @param resourceId the id of the resource.
	 * @param args       arguments for the formatting
	 * @return the value of the String resource.
	 */
	public static String getResourceString(final int resourceId, final Object... args) {
		return Application.getAppContext().getResources().getString(resourceId, args);
	}

	/**
	 * Retrieve the version number of the app.
	 *
	 * @return the app version.
	 */
	public static int getVersion() {
		PackageInfo pInfo;
		try {
			pInfo = Application.getAppContext().getPackageManager().getPackageInfo(Application.getAppContext().getPackageName(), 0);
			return pInfo.versionCode;
		}
		catch (NameNotFoundException e) {
			Log.e(Application.TAG, "Did not find application version", e);
			return 0;
		}
	}

	/**
	 * Get the configured application locale.
	 *
	 * @return The configured application locale.
	 */
	private static Locale getApplicationLocale() {
		String languageString = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_language);
		if (languageString == null || languageString.length() == 0) {
			languageString = "0";
			PreferenceUtil.setSharedPreferenceString(R.string.key_pref_language, "0");
		}

		int languageSetting = Integer.parseInt(languageString);
		switch (languageSetting) {
		case 0:
			return Application.DEFAULT_LOCALE;
		case 1:
			return Locale.ENGLISH;
		case 2:
			return Locale.GERMAN;
		default:
			return Application.DEFAULT_LOCALE;
		}
	}

	/**
	 * Create a ContextWrapper, wrapping the context with a specific locale.
	 *
	 * @param context The original context.
	 * @return The context wrapper.
	 */
	public static ContextWrapper createContextWrapperForLocale(final Context context) {
		Resources res = context.getResources();
		Configuration configuration = res.getConfiguration();
		Locale newLocale = Application.getApplicationLocale();
		configuration.setLocale(newLocale);

		LocaleList localeList = new LocaleList(newLocale);
		LocaleList.setDefault(localeList);
		configuration.setLocales(localeList);

		return new ContextWrapper(context.createConfigurationContext(configuration));
	}

	/**
	 * Start the app programmatically.
	 *
	 * @param triggeringActivity triggeringActivity the triggering activity.
	 */
	public static void startApplication(final Activity triggeringActivity) {
		Intent intent = new Intent(triggeringActivity, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		triggeringActivity.startActivity(intent);
	}

	private void createNotificationChannel() {
		CharSequence name = getString(R.string.notification_channel_name);
		String description = getString(R.string.notification_channel_description);
		int importance = NotificationManager.IMPORTANCE_HIGH;
		NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
		channel.setDescription(description);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);
	}
}
