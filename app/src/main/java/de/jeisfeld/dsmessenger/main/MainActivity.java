package de.jeisfeld.dsmessenger.main;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.ActivityMainBinding;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.service.FirebaseDsMessagingService;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * The main activity of the app.
 */
public class MainActivity extends AppCompatActivity {
	/**
	 * The configuration of the app bar.
	 */
	private AppBarConfiguration mAppBarConfiguration;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		de.jeisfeld.dsmessenger.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.appBarMain.toolbar);
		DrawerLayout drawer = binding.drawerLayout;
		NavigationView navigationView = binding.navView;
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.nav_account, R.id.nav_message, R.id.nav_randomimage, R.id.nav_lut, R.id.nav_settings)
				.setOpenableLayout(drawer)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		logMessagingToken();

		ContactRegistry.getInstance().refreshContacts(null);
	}

	@Override
	public final boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}

	/**
	 * Log the messaging token.
	 */
	private void logMessagingToken() {
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w(Application.TAG, "Fetching FCM registration token failed", task.getException());
						return;
					}

					// Get new FCM registration token
					String token = task.getResult();
					if (token == null) {
						Log.w(Application.TAG, "Messaging token is null");
						return;
					}

					String oldToken = PreferenceUtil.getSharedPreferenceString(R.string.key_pref_messaging_token);
					if (token.equals(oldToken)) {
						Log.d(Application.TAG, "Unchanged messaging token: " + token);
					}
					else {
						Log.i(Application.TAG, "Got new messaging token: " + token);
						FirebaseDsMessagingService.updateToken(token);
					}

				});
	}

}
