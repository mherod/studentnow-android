package com.studentnow.android.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.studentnow.ECard;
import org.studentnow.api.Cards;
import org.studentnow.gd.Location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;
import com.studentnow.android.CourseSelectActivity;
import com.studentnow.android.MyCard;
import com.studentnow.android.MyImageCard;
import com.studentnow.android.MyMapCard;
import com.studentnow.android.R;
import com.studentnow.android.__;
import com.studentnow.android.util.ConnectionDetector;

public class CardModule extends ServiceModule {

	private final String TAG = LiveService.class.getName();

	private LiveService mLiveService;
	private UserSyncModule mUserSyncModule;
	private LocationModule mLocationModule;

	private boolean requestCardViewUpdate = false;

	private List<ECard> cards = new ArrayList<ECard>();
	private static HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

	public CardModule(LiveService liveService) {
		mLiveService = liveService;
	}

	@Override
	public void linkModules() {
		mUserSyncModule = (UserSyncModule) mLiveService
				.getServiceModule(UserSyncModule.class);
		mLocationModule = (LocationModule) mLiveService
				.getServiceModule(LocationModule.class);
	}

	@Override
	public void schedule() {
		requestUpdate();
	}

	@Override
	public void cycle() {
		maintainLocalCards();
		prepareBitmaps();
		processRequests();
	}

	public void requestUpdate() {
		requestCardViewUpdate = true;
	}

	public List<ECard> getCards() {
		return cards;
	}

	private void maintainLocalCards() {
		List<ECard> cards = getCards();
		int removedCount = Cards.maintainLocalCards(cards);
		if (removedCount > 0) {
			Log.i(TAG, "Removed " + removedCount + " expired cards");
		}
	}

	private void prepareBitmaps() {
		List<ECard> cards = getCards();
		if (cards == null || cards.size() == 0) {
			return;
		}
		for (ECard ecard : cards) {
			if (ecard.hasMapCoords()) {
				String coords = ecard.getMapCoords();
				if (!bitmaps.containsKey(coords)) {
					Bitmap b = getGoogleMapThumbnail(coords);
					bitmaps.put(coords, b);
				}
			}
			if (ecard.hasImageSrc()) {
				String url = ecard.getImageSrc();
				if (!bitmaps.containsKey(url)) {
					URL url2 = null;
					try {
						url2 = new URL(url);
					} catch (Exception e) {
						continue;
					}
					Bitmap b = bitmapFromURL(url2);
					bitmaps.put(url, b);
				}
			}
		}
	}

	private boolean preparedBitmaps() {
		List<ECard> cards = getCards();
		if (cards == null || cards.size() == 0) {
			return true;
		}
		for (ECard ecard : cards) {
			if (ecard.hasMapCoords()) {
				String coords = ecard.getMapCoords();
				if (!bitmaps.containsKey(coords)) {
					return false;
				}
			}
			if (ecard.hasImageSrc()) {
				String url = ecard.getImageSrc();
				if (!bitmaps.containsKey(url)) {
					return false;
				}
			}
		}
		return true;
	}

	private void processRequests() {
		if (requestCardViewUpdate) {
			requestCardViewUpdate = false;
			updateActivityCards(mLiveService);
		}
	}

	public boolean renderCardsView(final Context context, CardUI cardsView) {
		List<ECard> cards = getCards();
		if (cards == null || cards.size() == 0) {
			if (!ConnectionDetector.hasNetwork(context)) {
				// No cards and no Internet - we need the user to get online
				cardsView.setSwipeable(false);
				MyCard myCard = new MyCard(
						mLiveService.getString(R.string.card_offline_title),
						mLiveService.getString(R.string.card_offline_content));
				cardsView.addCard(myCard);
				return true;
			} else if (mUserSyncModule.cardSuppressionPeriod.isSuppressed()) {
				// No cards and updates are currently suppressed means we are
				// getting errors
				cardsView.setSwipeable(false);
				MyCard myCard = new MyCard(
						mLiveService.getString(R.string.card_error_title),
						mLiveService.getString(R.string.card_error_content));
				cardsView.addCard(myCard);
				return true;
			}
			return false;
		}
		if (!preparedBitmaps()) {
			return false;
		}
		cardsView.clearCards();
		cardsView.setSwipeable(false);
		long time = System.currentTimeMillis();
		for (final ECard ecard : cards) {
			if (ecard.getTimeDisplays() > time) {
				continue;
			}
			Card card = null;
			if (ecard.hasImageSrc()) {
				card = new MyImageCard(ecard.getTitle(), ecard.getDesc(),
						bitmaps.get(ecard.getImageSrc()));
			} else if (ecard.hasMapCoords()) {
				card = new MyMapCard(ecard.getTitle(), ecard.getDesc(),
						bitmaps.get(ecard.getMapCoords()));
			} else {
				card = new MyCard(ecard);
			}
			if (ecard.isType(ECard.TRAVEL) && ecard.hasMapCoords()) {
				card.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Location loc = getLastLocation();
						if (loc == null) {
							return;
						}
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW, Uri
										.parse("http://"
												+ "maps.google.com/maps"
												+ "?saddr=" + loc.getString()
												+ "&daddr="
												+ ecard.getMapCoords()));
						intent.setClassName("com.google.android.apps.maps",
								"com.google.android.maps.MapsActivity");
						context.startActivity(intent);
					}
				});
			} else if (ecard.isType(ECard.SELECT_COURSE)) {
				card.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(context,
								CourseSelectActivity.class));
					}
				});
			}
			card.setSwipeable(ecard.isSwipable());
			cardsView.addCard(card);
		}
		cardsView.refresh();
		return true;
	}

	private Location getLastLocation() {
		try {
			LocationCache mLocationCache = mLocationModule.getLocationCache();
			return mLocationCache.getLastLocation().getLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public static void updateActivityCards(Context context) {
		context.sendBroadcast(new Intent(__.INTENT_CARD_UPDATE));
	}

	public static Bitmap getGoogleMapThumbnail(String... coords) {
		try {
			String urlString = "https://maps.googleapis.com/maps/api/staticmap?";
			for (String marker : coords) {
				urlString += "markers=color:red|" + marker + "&";
			}
			urlString += "zoom=17&size=600x350" + "&sensor=false";
			return bitmapFromURL(new URL(urlString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap bitmapFromURL(URL url) {
		Bitmap bmp = null;
		InputStream in = null;
		try {
			in = url.openStream();
			bmp = BitmapFactory.decodeStream(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}
}
