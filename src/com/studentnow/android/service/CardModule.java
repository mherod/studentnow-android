package com.studentnow.android.service;

import java.util.Iterator;
import java.util.List;

import org.studentnow.ECard;

import android.util.Log;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.MyCard;
import com.studentnow.android.util.ConnectionDetector;

public class CardModule implements ServiceModule {

	private final String TAG = LiveService.class.getName();

	private final LiveService mLiveService;
	private final ConnectionDetector mConnectionDetector;

	private boolean requestCardViewUpdate = false;

	public CardModule(LiveService liveService) {
		mLiveService = liveService;
		mConnectionDetector = new ConnectionDetector(liveService);
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

		maintainLocalCards();

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

	public void maintainLocalCards() {
		int removedCount = 0;
		Iterator<ECard> iter = mLiveService.getCards().iterator();
		long time = System.currentTimeMillis();
		while (iter.hasNext()) {
			ECard ecard = iter.next();
			if (ecard.doesExpire() && ecard.getTimeExpires() < time) {
				iter.remove();
				removedCount++;
				continue;
			}
		}
		if (removedCount > 0) {
			Log.i(TAG, "Removed " + removedCount + " expired cards");
		}
	}

	public boolean renderCardsView(CardUI cardsView) {
		List<ECard> cards = mLiveService.getCards();
		if (cards == null || cards.size() == 0) {
			if (mConnectionDetector.isConnectedOnline()) {
				// No cards available with connection so let's trigger a refresh
				return false;
			} else {
				// No cards and no internet - we need the user to get online
				cardsView.setSwipeable(false);
				MyCard myCard = new MyCard(
						"No internet connection",
						"Student Now has been unable to connect to the cloud and so is unable to display your latest information");
				cardsView.addCard(myCard);
				return true;
			}
		}

		long time = System.currentTimeMillis();

		cardsView.clearCards();
		cardsView.setSwipeable(false);

		for (ECard ecard : mLiveService.getCards()) {
			if (ecard.getTimeDisplays() > time) {
				continue;
			}
			MyCard myCard = new MyCard(ecard);
			myCard.setSwipeable(ecard.isSwipable());
			cardsView.addCard(myCard);
		}

		cardsView.refresh();

		return true;
	}
}
