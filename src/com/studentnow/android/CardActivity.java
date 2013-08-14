package com.studentnow.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.service.CardViewBuildModule;
import com.studentnow.android.service.CourseSelectionModule;
import com.studentnow.android.service.LiveService;
import com.studentnow.android.service.TimetableSyncModule;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CardActivity extends Activity implements Runnable {

	// private final String TAG = CardActivity.class.getName();

	private View mContentView;
	private CardUI mCardsView;
	private ProgressBar mLoadingView;
	private int mShortAnimationDuration = 700;

	private Thread thread = new Thread(this);

	private LiveServiceLink serviceLink = null;

	private boolean updateCardsFlag = false;

	private void crossfade(final View from, final View to) {
		to.setAlpha(0f);
		to.setVisibility(View.VISIBLE);
		to.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);
		from.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						from.setVisibility(View.GONE);
					}
				});
	}

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

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == 10) {
			finish();
		}
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
			openSetupWizard();
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.action_refresh:
			((TimetableSyncModule) getLiveService().getServiceModule(
					TimetableSyncModule.class)).requestUpdate();
			toast("Refreshing...");
			return true;

		case R.id.action_credits:
			startActivity(new Intent(this, CreditActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void openSetupWizard() {
		startActivityForResult(new Intent(this, SetupActivity.class), 10);
	}

	private boolean updateCardsView() {
		LiveService l;
		if ((l = getLiveService()) == null) {
			return false;
		}
		CardViewBuildModule cvbm = (CardViewBuildModule) l
				.getServiceModule(CardViewBuildModule.class);
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
			CourseSelectionModule csm = (CourseSelectionModule) l
					.getServiceModule(CourseSelectionModule.class);
			if (csm.isCourseSelected()) {
				updateCardsFlag = !updateCardsView();
			} else {
				openSetupWizard();
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
				crossfade(mLoadingView, mContentView);
			}
		}

	};

	final Runnable showProgress = new Runnable() {
		@Override
		public void run() {
			if (!isLoadingView()) {
				crossfade(mContentView, mLoadingView);
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
		//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
		
		Crouton.makeText(this, s, Style.INFO).show();		
	}

}
