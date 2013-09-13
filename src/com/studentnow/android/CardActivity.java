package com.studentnow.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.service.AccountModule;
import com.studentnow.android.service.CardModule;
import com.studentnow.android.service.LiveService;
import com.studentnow.android.service.UserSyncModule;
import com.studentnow.android.util.ViewHelpers;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CardActivity extends Activity implements Runnable {

	private final String TAG = CardActivity.class.getSimpleName();

	private View mContentView;
	private CardUI mCardsView;
	private ProgressBar mLoadingView;

	private Thread thread = new Thread(this);

	private LiveServiceLink serviceLink = null;

	private boolean updateCardsFlag = false;

	private boolean isLoadingView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cards);

		mLoadingView = (ProgressBar) findViewById(R.id.loading_spinner);
		mContentView = findViewById(R.id.content);
		mCardsView = (CardUI) findViewById(R.id.cards);

		isLoadingView = mLoadingView.getVisibility() == View.VISIBLE;

		serviceLink = new LiveServiceLink();
	}

	@Override
	public void onResume() {
		super.onResume();
		serviceLink.start(this);

		registerReceiver(cardUpdateReceiver, new IntentFilter(
				__.INTENT_CARD_UPDATE));
		registerReceiver(connectServiceReceiver, new IntentFilter(
				__.INTENT_CONNECT_SERVICE));
		registerReceiver(closeAppReceiver,
				new IntentFilter(__.INTENT_CLOSE_APP));

		updateCardsFlag = true;
		try {
			thread.start();
		} catch (RuntimeException re) {
		}
	}

	@Override
	protected void onPause() {
		serviceLink.stop(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(cardUpdateReceiver);
		unregisterReceiver(connectServiceReceiver);
		unregisterReceiver(closeAppReceiver);
		// serviceLink.stop(this);
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
			((UserSyncModule) getLiveService().getServiceModule(
					UserSyncModule.class)).requestUpdate();
			Crouton.makeText(this, "Refreshing...", Style.INFO).show();
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
		LiveService l = getLiveService();
		if (l == null) {
			return false;
		}
		CardModule cvbm = (CardModule) l.getServiceModule(CardModule.class);
		boolean cards = cvbm.renderCardsView(this, mCardsView);
		runOnUiThread(cards ? showCards : showProgress);
		return cards;
	}

	private LiveService getLiveService() {
		return serviceLink.getLiveService();
	}

	@Override
	public void run() {
		while (true) {
			if (updateCardsFlag) {
				updateCardsFlag = false;
				runOnUiThread(updateCardsRunnable);
			}
			try {
				Thread.sleep(250);
			} catch (Exception e) {
			}
		}
	}

	private Runnable updateCardsRunnable = new Runnable() {
		@Override
		public void run() {
			LiveService l = getLiveService();
			if (l == null) {
				updateCardsFlag = true;
				return;
			}
			AccountModule am = (AccountModule) l
					.getServiceModule(AccountModule.class);
			if (!am.hasAuthResponse()) {
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
			if (isLoadingView()) {
				ViewHelpers.crossfade(mLoadingView, mContentView);
				isLoadingView = false;
			}
		}
	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			if (!isLoadingView()) {
				ViewHelpers.crossfade(mContentView, mLoadingView);
				isLoadingView = true;
			}
		}
	};

	private BroadcastReceiver cardUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Crouton.makeText(CardActivity.this, "Cards updated", Style.CONFIRM)
					.show();
			updateCardsFlag = true;
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
			serviceLink.start(CardActivity.this);
		}
	};

	public static void finishAll(Context context) {
		context.sendBroadcast(new Intent(__.INTENT_CLOSE_APP));
	}
}
