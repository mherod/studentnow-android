package com.studentnow.android.service;

import java.util.Iterator;
import java.util.List;

import org.studentnow.ECard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.MyCard;
import com.studentnow.android.R;
import com.studentnow.android.__;
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

	@Override
	public void cycle() {
		maintainLocalCards();
		processRequests();
	}

	@Override
	public boolean save() {
		return true;
	}

	public void requestUpdate() {
		requestCardViewUpdate = true;
	}

	private void processRequests() {
		if (requestCardViewUpdate) {
			requestCardViewUpdate = false;
			updateActivityCards(mLiveService);
		}
	}

	private void maintainLocalCards() {
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
			if (!mConnectionDetector.isConnectedOnline()) {
				// No cards and no Internet - we need the user to get online
				cardsView.setSwipeable(false);
				MyCard myCard = new MyCard(
						mLiveService.getString(R.string.card_offline_title),
						mLiveService.getString(R.string.card_offline_content));
				cardsView.addCard(myCard);
				return true;
			}
			return false;
		}
		cardsView.clearCards();
		cardsView.setSwipeable(false);
		long time = System.currentTimeMillis();
		for (ECard ecard : cards) {
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

	public static void updateActivityCards(Context context) {
		context.sendBroadcast(new Intent(__.Intent_CardUpdate));
	}
}
