package org.herod.studentnow;

import org.herod.studentnow.service.CourseSelectionModule;
import org.herod.studentnow.service.LiveService;
import org.herod.studentnow.service.TimetableSyncModule;
import org.studentnow.Course;
import org.studentnow.Session;
import org.studentnow.Timetable;
import org.studentnow._;

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

public class CardActivity extends Activity implements Runnable {

	private View mContentView;
	private CardUI mCards;
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
		mCards = (CardUI) findViewById(R.id.cards);

		serviceLink = new LiveServiceLink();
	}

	@Override
	public void onResume() {
		super.onResume();

		serviceLink.start(this);

		IntentFilter cardUpdateFilter = new IntentFilter(__.Intent_CardUpdate);
		registerReceiver(cardUpdateReceiver, cardUpdateFilter);

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
		Intent intent;
		switch (item.getItemId()) {

		case R.id.action_setup:
			openSetupWizard();
			return true;

		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;

		case R.id.action_refresh:
			((TimetableSyncModule) getLiveService().getServiceModule(
					TimetableSyncModule.class)).requestTimetableUpdate();
			toast("Refreshing...");
			return true;

		case R.id.action_credits:
			intent = new Intent(this, CreditActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
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

	private void openSetupWizard() {
		Intent intent = new Intent(this, SetupActivity.class);
		startActivityForResult(intent, 10);
	}

	private void updateCardsView() {
		boolean cards = false;

		mCards.clearCards();
		mCards.setSwipeable(false);

		LiveService l;
		if ((l = getLiveService()) != null) {
			CourseSelectionModule csm = (CourseSelectionModule) l
					.getServiceModule(CourseSelectionModule.class);
			
			Course c = csm.getCourse();
			Timetable tt = l.getTimetable();

			if (c == null || tt == null) {
				cards = false;
			} else {

				cards = true;

				switch (tt.getStatus()) {

				case _.STATUS_PROGRAMME_ENDED:
					mCards.addCard(new MyCard(getString(R.string.week) + " "
							+ tt.getWeek(), getString(R.string.card_over_desc)));
					mCards.addCard(new MyCard("Your programme",
							"Your current programme selection for this year is "
									+ c.getName()));
					mCards.addCard(new MyCard(
							getString(R.string.card_examreminder_title),
							getString(R.string.card_examreminder_desc)));
					break;

				case _.STATUS_WEEK_ENDED:
					mCards.addCard(new MyCard(getString(R.string.week) + " "
							+ tt.getWeek(),
							getString(R.string.card_weekover_desc)));
					break;

				case _.STATUS_DAY_ENDED:
					Session nextSession = tt.getNextSession();
					String continues = "next";
					if (nextSession == null) {
						continues = "later this week";
					} else if (nextSession.isTomorrow()) {
						continues = "tomorrow";
					} else {
						continues = "on " + nextSession.get(_.FIELD_DAY);
					}

					mCards.addCard(new MyCard(getString(R.string.week) + " "
							+ tt.getWeek(), getString(R.string.week_later)
							+ " " + continues + "."));

					int k = 0;
					String upcoming = "";
					for (Session session : tt.getSessions()) {
						if (session.hasPassed())
							continue;
						if (k++ == 3)
							break;
						upcoming += session.get(_.FIELD_DAY) + " "
								+ session.get(_.FIELD_TIME_START) + " - "
								+ session.get(_.FIELD_MODULE) + _.nl;
					}
					if (nextSession != null && upcoming.length() > 0) {
						// getString(R.string.next)
						String continuesTitle = StringUtils
								.capitalize(continues)
								+ " from "
								+ nextSession.get(_.FIELD_TIME_START);
						mCards.addCard(new MyCard(continuesTitle, upcoming));
					}
					break;

				case _.STATUS_LIVE:
					mCards.addCard(new MyCard(getString(R.string.week) + " "
							+ tt.getWeek(), getString(R.string.week_desc)));

					MyCard newCard;
					for (Session session : tt.getSessions()) {
						if (session.hasPassed())
							continue;

						String travel = "";
						if (session.isWithinTravel()) {
							if (session.isSet("travel-duration")) {
								int mins = session.getInt("travel-duration") / 60;
								travel += ",\ntravel time ~ " + mins + " mins";
							}
						} else {
							travel += ",\nyou may not arrive in time";
						}

						newCard = new MyCard(session.get(_.FIELD_TYPE) + " at "
								+ session.get(_.FIELD_TIME_START) + travel,
								session.get(_.FIELD_MODULE) + " in "
										+ session.get(_.FIELD_ROOM_NAME));

						mCards.addCard(newCard);

					}
					break;

				default:
					mCards.addCard(new MyCard(
							getString(R.string.card_timetableerror_title),
							getString(R.string.card_timetableerror_desc)));
					break;

				}
			}
		}

		runOnUiThread(cards ? showCards : showProgress);

		mCards.setSwipeable(true);
		mCards.refresh();

		updateCardsFlag = !cards;

	}

	private LiveService getLiveService() {
		return serviceLink.getLiveService();
	}

	private Runnable updateCardsRunnable = new Runnable() {

		@Override
		public void run() {
			LiveService l;
			if ((l = getLiveService()) == null) {
				updateCardsFlag = true;
			} else {
				CourseSelectionModule csm = (CourseSelectionModule) l
						.getServiceModule(CourseSelectionModule.class);
				if (csm.isCourseSelected()) {
					updateCardsView();
				} else {
					openSetupWizard();
				}
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
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
