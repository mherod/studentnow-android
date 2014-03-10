package com.studentnow.android;

import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.service.AccountModule;
import com.studentnow.android.service.CardProviderModule;
import com.studentnow.android.service.LiveService;
import com.studentnow.android.service.UserSyncModule;
import com.studentnow.android.util.ViewHelpers;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CardActivity extends Activity implements Runnable {

	public final String TAG = CardActivity.class.getSimpleName();

	private View mContentView;
	private CardUI mCardsView;
	private ProgressBar mLoadingView;

	private Thread thread = new Thread(this);

	private MyLiveServiceLink serviceLink = null;
	private LiveService mLiveService = null;

	private boolean updateCardsFlag = false;
	private boolean isLoadingView = false;
	private boolean isForeground = false;

	private long lastBackground = 0;
	private long lastUpdate = System.currentTimeMillis();

	final HashMap<String, BroadcastReceiver> mReceivers = new HashMap<String, BroadcastReceiver>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_cards);

		mLoadingView = (ProgressBar) findViewById(R.id.loading_spinner);
		mContentView = findViewById(R.id.content);
		mCardsView = (CardUI) findViewById(R.id.cards);
		isLoadingView = mLoadingView.getVisibility() == View.VISIBLE;

		mReceivers.put(__.INTENT_CONNECT_SERVICE, connectServiceReceiver);
		mReceivers.put(__.INTENT_CLOSE_APP, closeAppReceiver);
		mReceivers.put(__.INTENT_CARD_UPDATE, cardUpdateReceiver);
		mReceivers.put(__.INTENT_ALERT, alertToastReciever);

		serviceLink = new MyLiveServiceLink(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		isForeground = true;
		serviceLink.start();

		for (String filter : mReceivers.keySet()) {
			registerReceiver(mReceivers.get(filter), new IntentFilter(filter));
		}

		updateCardsFlag = true;
		try {
			thread.start();
		} catch (RuntimeException re) {
		}
	}

	@Override
	protected void onPause() {
		isForeground = false;
		lastBackground = System.currentTimeMillis();
		serviceLink.stop();
		thread.interrupt();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		for (String filter : mReceivers.keySet()) {
			unregisterReceiver(mReceivers.get(filter));
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		return;
		// super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_card_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_setup:
			startActivity(new Intent(this, CourseSelectActivity.class));
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.action_refresh:
			Crouton.makeText(this, "Refreshing...", Style.INFO).show();

			UserSyncModule usm = ((UserSyncModule) mLiveService
					.getServiceModule(UserSyncModule.class));
			usm.requestUpdate();
			return true;

		case R.id.action_credits:
			startActivity(new Intent(this, CreditActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void openSetup() {
		startActivity(new Intent(this, SetupActivity.class));
	}

	private boolean updateCardsView() {
		LiveService l = mLiveService;
		if (l == null) {
			return false;
		}
		CardProviderModule cardProviderModule = (CardProviderModule) l
				.getServiceModule(CardProviderModule.class);
		boolean cards = cardProviderModule.renderCardsView(this, mCardsView);
		if (cards) {
			lastUpdate = System.currentTimeMillis();
		}
		runOnUiThread(cards ? showCards : showProgress);
		return cards;
	}

	@Override
	public void run() {
		Log.i(TAG, "New thread");
		do {
			long now = System.currentTimeMillis();
			if (!isForeground && (now - lastBackground) > (30 * 1000)) {
				Log.d(TAG, "Background timeout");
				finish();
				break;
			}
			if (isForeground && (now - lastUpdate) > (15 * 1000)) {
				updateCardsFlag = true;
			}
			if (updateCardsFlag) {
				updateCardsFlag = false;
				runOnUiThread(updateCardsRunnable);
			}
			try {
				Thread.sleep(250);
			} catch (Exception e) {
			}
		} while (!thread.isInterrupted());
		Log.i(TAG, "Exit thread");
	}

	private Runnable updateCardsRunnable = new Runnable() {
		@Override
		public void run() {
			LiveService l = mLiveService;
			if (l == null) {
				updateCardsFlag = true;
				return;
			}

			AccountModule am = (AccountModule) l
					.getServiceModule(AccountModule.class);
			if (isForeground && !am.hasAuthResponse()) {
				Log.i(TAG, "Login a");
				openSetup();
				return;
			}

			updateCardsFlag = !updateCardsView();
		}
	};

	private boolean isLoadingView() {
		return isLoadingView;
	}

	final Runnable showCards = new Runnable() {
		@Override
		public void run() {
			if (!isLoadingView()) {
				return;
			}
			ViewHelpers.crossfade(mLoadingView, mContentView);
			isLoadingView = false;
		}
	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			if (isLoadingView()) {
				return;
			}
			ViewHelpers.crossfade(mContentView, mLoadingView);
			isLoadingView = true;
		}
	};

	private BroadcastReceiver cardUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateCardsFlag = true;
			if (!isForeground) {
				return;
			}
			Crouton.makeText(CardActivity.this, "Cards updated", Style.CONFIRM)
					.show();
		}
	};

	private BroadcastReceiver alertToastReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isForeground) {
				return;
			}
			String alertString = intent.getStringExtra(__.EXTRA_ALERT);
			Crouton.makeText(CardActivity.this, alertString, Style.ALERT)
					.show();
		}
	};

	private BroadcastReceiver closeAppReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CardActivity.this.finish();
		}
	};

	private BroadcastReceiver connectServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			serviceLink.start();
		}
	};

	public static void showAlert(Context context, String alertString) {
		context.sendBroadcast(new Intent(__.INTENT_ALERT).putExtra(
				__.EXTRA_ALERT, alertString));
	}

	public static void finishAll(Context context) {
		context.sendBroadcast(new Intent(__.INTENT_CLOSE_APP));
	}

	private class MyLiveServiceLink extends LiveServiceLink {

		Context context = null;

		public MyLiveServiceLink(Context context) {
			super(context);
			this.context = context;
		}

		@Override
		public void onConnect() {
			mLiveService = getLiveService();
		}

		@Override
		public void onDisconnect() {
			CardActivity.finishAll(context);
		}

	}
}
