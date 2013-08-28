package com.studentnow.android.service;

import java.util.List;

import org.studentnow.ECard;

import com.fima.cardsui.views.CardUI;
import com.studentnow.android.MyCard;

//import com.studentnow.android.service.TravelModule.TravelInformation;

public class CardsBuildModule implements ServiceModule {

	private final String TAG = LiveService.class.getName();

	private final LiveService mLiveService;

	private boolean requestCardViewUpdate = false;

	public CardsBuildModule(LiveService liveService) {
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
		List<ECard> cards = mLiveService.getCards();
		if (cards == null || cards.size() == 0) {
			return false;
		}

		cardsView.clearCards();
		cardsView.setSwipeable(false);

		long time = System.currentTimeMillis();

		for (ECard ecard : mLiveService.getCards()) {
			if (ecard.doesExpire() && ecard.getTimeExpires() < time) {
				continue;
			}
			if (ecard.getTimeDisplays() > time) {
				continue;
			}
			MyCard myCard = new MyCard(ecard);
			myCard.setSwipeable(ecard.isSwipable());
			cardsView.addCard(myCard);
		}

		cardsView.setSwipeable(true);
		cardsView.refresh();

		return true;
	}
}
