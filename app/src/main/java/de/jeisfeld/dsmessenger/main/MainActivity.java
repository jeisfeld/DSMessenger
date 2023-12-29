package de.jeisfeld.dsmessenger.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.databinding.ActivityMainBinding;
import de.jeisfeld.dsmessenger.main.account.AccountDialogUtil;
import de.jeisfeld.dsmessenger.main.account.AccountFragment;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.service.FirebaseDsMessagingService;
import de.jeisfeld.dsmessenger.util.DialogUtil;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * The main activity of the app.
 */
public class MainActivity extends AppCompatActivity {
	/**
	 * The configuration of the app bar.
	 */
	private AppBarConfiguration mAppBarConfiguration;
	/**
	 * The id of the navigation start.
	 */
	private int navigationStartId = R.id.nav_conversations;

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
				R.id.nav_conversations, R.id.nav_randomimage, R.id.nav_lut, R.id.nav_account, R.id.nav_settings)
				.setOpenableLayout(drawer)
				.build();

		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		logMessagingToken();
		handleAppLink();

		ContactRegistry.getInstance().refreshContacts(this, () -> ContactRegistry.getInstance().refreshConversations(this, null));
	}

	/**
	 * Find out if user is logged in and connected.
	 *
	 * @return true if logged in and connected.
	 */
	private boolean isConnected() {
		return AccountFragment.isLoggedIn()
				&& ContactRegistry.getInstance().getConnectedContacts().size() > 0;
	}

	@Override
	protected final void onResume() {
		super.onResume();
		if (isConnected()) {
			if (navigationStartId == R.id.nav_account) {
				NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
				NavOptions navOptions = new NavOptions.Builder()
						.setPopUpTo(R.id.nav_account, true)
						.build();
				navController.navigate(R.id.nav_conversations, null, navOptions);
				navigationStartId = R.id.nav_conversations;
			}
			updateNavigationDrawer();
		}
		else {
			if (navigationStartId == R.id.nav_conversations) {
				NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
				NavOptions navOptions = new NavOptions.Builder()
						.setPopUpTo(R.id.nav_conversations, true)
						.build();
				navController.navigate(R.id.nav_account, null, navOptions);
				navigationStartId = R.id.nav_account;
			}
			updateNavigationDrawer();
		}
	}

	/**
	 * Enable/disable navigation drawer elements based on contact status.
	 */
	public void updateNavigationDrawer() {
		if (isConnected()) {
			NavigationView navigationView = findViewById(R.id.nav_view);
			Menu menuNav = navigationView.getMenu();
			menuNav.findItem(R.id.nav_conversations).setEnabled(true);
			menuNav.findItem(R.id.nav_randomimage).setEnabled(true);
			menuNav.findItem(R.id.nav_lut).setEnabled(true);
		}
		else {
			NavigationView navigationView = findViewById(R.id.nav_view);
			Menu menuNav = navigationView.getMenu();
			menuNav.findItem(R.id.nav_conversations).setEnabled(false);
			menuNav.findItem(R.id.nav_randomimage).setEnabled(false);
			menuNav.findItem(R.id.nav_lut).setEnabled(false);
		}
	}

	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		handleAppLink();
	}

	@Override
	protected final void attachBaseContext(final Context newBase) {
		super.attachBaseContext(Application.createContextWrapperForLocale(newBase));
	}

	/**
	 * Handle a link sent to the app.
	 */
	public void handleAppLink() {
		if ("android.intent.action.VIEW".equals(getIntent().getAction())) {
			Uri uri = getIntent().getData();
			if (uri != null && "/dsmessenger/connect".equals(uri.getPath())) {
				String connectionCode = uri.getQueryParameter("code");
				if (connectionCode != null && connectionCode.length() == 24) { // MAGIC_NUMBER
					if (!AccountFragment.isLoggedIn()) {
						DialogUtil.displayInfoMessage(this, R.string.title_dialog_info, R.string.button_ok, R.string.dialog_login_before_connect);
					}
					else {
						NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
						navController.navigate(R.id.nav_account);
						AccountDialogUtil.displayAcceptInvitationDialog(this, connectionCode);
					}
				}
			}
		}
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
						FirebaseDsMessagingService.updateToken(this, token);
					}

				});
	}

}
