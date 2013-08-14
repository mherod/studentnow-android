package com.studentnow.android.service;

import org.studentnow.Course;
import org.studentnow.Session;
import org.studentnow.Time;
import org.studentnow.Timetable;
import org.studentnow._;

import android.util.Log;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.MyCard;
import com.studentnow.android.R;
import com.studentnow.android.StringUtils;

public class CardViewBuildModule implements ServiceModule {

	private final String TAG = LiveService.class.getName();

	private final LiveService mLiveService;

	private boolean requestCardViewUpdate = false;

	public CardViewBuildModule(LiveService liveService) {
		this.mLiveService = liveService;
	}

	@Override
	public void load() {
	}

	@Override
	public void schedule() {
		requestUpdate();

	}

	@Override
	public void cancel() {
	}

	public void requestUpdate() {
		requestCardViewUpdate = true;
	}

	@Override
	public void cycle() {

		if (requestCardViewUpdate) {
			requestCardViewUpdate = false;

			((NotificationModule) mLiveService
					.getServiceModule(NotificationModule.class))
					.requestCardsRefresh();

		}

	}

	@Override
	public boolean save() {
		return true;
	}

	public boolean renderCardsView(CardUI cardsView) {
		cardsView.clearCards();
		cardsView.setSwipeable(false);

		LiveService l = mLiveService;
		CourseSelectionModule csm = (CourseSelectionModule) l
				.getServiceModule(CourseSelectionModule.class);

		Course c = csm.getCourse();
		Timetable tt = l.getTimetable();

		if (c == null || tt == null) {
			return false;
		}
		switch (tt.refreshStatus().getStatus()) {

		case _.STATUS_PROGRAMME_ENDED:
			cardsView.addCard(new MyCard(l.getString(R.string.week) + " "
					+ tt.getWeek(), l.getString(R.string.card_over_desc)));
			cardsView.addCard(new MyCard("Your programme",
					"Your current programme selection for this year is "
							+ c.getName()));
			cardsView.addCard(new MyCard(l
					.getString(R.string.card_examreminder_title), l
					.getString(R.string.card_examreminder_desc)));
			break;

		case _.STATUS_WEEK_ENDED:
			cardsView.addCard(new MyCard(l.getString(R.string.week) + " "
					+ tt.getWeek(), l.getString(R.string.card_weekover_desc)));
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

			cardsView.addCard(new MyCard(l.getString(R.string.week) + " "
					+ tt.getWeek(), l.getString(R.string.week_later) + " "
					+ continues + "."));

			int k = 0;
			String upcoming = "";
			for (Session session : tt.getSessions()) {
				if (session.hasPassed())
					continue;
				if (k++ == 3)
					break;
				Log.d(TAG, session.get(_.FIELD_DAY));
				upcoming += (session.get(_.FIELD_DAY) + " "
						+ session.get(_.FIELD_TIME_START) + " - "
						+ session.get(_.FIELD_MODULE) + _.nl);
			}
			if (nextSession != null && upcoming.length() > 0) {
				// getString(R.string.next)
				String continuesTitle = StringUtils.capitalize(continues)
						+ " from " + nextSession.get(_.FIELD_TIME_START);
				cardsView.addCard(new MyCard(continuesTitle, upcoming));
			}
			break;

		case _.STATUS_LIVE:
			cardsView.addCard(new MyCard(l.getString(R.string.week) + " "
					+ tt.getWeek(), l.getString(R.string.week_desc)));

			Time lastTime = null;
			MyCard newCard;
			for (Session session : tt.getSessions()) {
				if (session.hasPassed())
					continue;

				String travel = "";
				if (session.isWithinTravel()) {
					if (session.isSet(_.FIELD_TRAVEL_DURATION)) {
						int mins = session.getInt(_.FIELD_TRAVEL_DURATION) / 60;
						travel += ",\ntravel time ~ " + mins + " mins";
					}
				}

				newCard = new MyCard(session.get(_.FIELD_TYPE) + " at "
						+ session.get(_.FIELD_TIME_START) + travel,
						session.get(_.FIELD_MODULE) + " in "
								+ session.get(_.FIELD_ROOM_NAME));
				
				if (lastTime != null && session.getStartTime().compareTo(lastTime) == 0) {
					cardsView.addCardToLastStack(newCard);					
				} else {
					cardsView.addCard(newCard);					
				}
				lastTime = session.getStartTime();
			}
			break;

		default:
			cardsView.addCard(new MyCard(l
					.getString(R.string.card_timetableerror_title), l
					.getString(R.string.card_timetableerror_desc)));
			break;

		}

		cardsView.setSwipeable(true);
		cardsView.refresh();

		return true;
	}
}
