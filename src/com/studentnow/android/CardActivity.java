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
import com.studentnow.android.service.CardsBuildModule;
import com.studentnow.android.service.LiveService;
import com.studentnow.android.service.UserSyncModule;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CardActivity extends Activity implements Runnable {

	private final String TAG = CardActivity.class.getName();

	private View mContentView;
	private CardUI mCardsView;
	private ProgressBar mLoadingView;

	private Thread thread = new Thread(this);

	private LiveServiceLink serviceLink = null;

	private boolean updateCardsFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cards);

		mLoadingView = (ProgressBar) findViewById(R.id.loading_spinner);
		mContentView = findViewById(R.id.content);
		mCardsView = (CardUI) findViewById(R.id.cards);

		serviceLink = new LiveServiceLink();
	}

	@Override
	public void onResume() {
		super.onResume();
		serviceLink.start(this);
		registerReceiver(cardUpdateReceiver, new IntentFilter(
				__.Intent_CardUpdate));
		updateCardsFlag = true;
		try {
			thread.start();
		} catch (RuntimeException re) {
		}
	}

	@Override
	protected void onPause() {
		unregisterReceiver(cardUpdateReceiver);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		serviceLink.stop(this);
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
			// openSetup();
			startActivity(new Intent(this, CourseSelectActivity.class));
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.action_refresh:
			((UserSyncModule) getLiveService().getServiceModule(
					UserSyncModule.class)).requestUpdate();
			toast("Refreshing...");
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
		LiveService l;
		if ((l = getLiveService()) == null) {
			return false;
		}
		CardsBuildModule cvbm = (CardsBuildModule) l
				.getServiceModule(CardsBuildModule.class);
		boolean cards = cvbm.renderCardsView(mCardsView);
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
			LiveService l;
			if ((l = getLiveService()) == null) {
				updateCardsFlag = true;
				return;
			}
			AccountModule am = (AccountModule) l
					.getServiceModule(AccountModule.class);
			if (!am.hasAuthResponse() /* || !am.isCourseSelected() */) {
				openSetup();
			} else {
				updateCardsFlag = !updateCardsView();
			}
		}
	};

	private boolean isLoadingView() {
		return mLoadingView.getVisibility() == View.VISIBLE
				&& mContentView.getVisibility() == View.GONE;
	}

	final Runnable showCards = new Runnable() {
		@Override
		public void run() {
			if (isLoadingView()) {
				ViewHelpers.crossfade(mLoadingView, mContentView);
			}
		}

	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			if (!isLoadingView()) {
				ViewHelpers.crossfade(mContentView, mLoadingView);
			}
		}
	};

	private BroadcastReceiver cardUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			toast("Updating cards...");
			updateCardsFlag = true;
		}
	};

	private void toast(String s) {
		// Toast.makeText(getApplicationContext(), s,
		// Toast.LENGTH_SHORT).show();

		Crouton.makeText(this, s, Style.INFO).show();
	}

}
