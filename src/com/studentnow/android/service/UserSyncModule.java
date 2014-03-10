package com.studentnow.android.service;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.studentnow.ECard;
import org.studentnow.Static.Fields;
import org.studentnow.Static.Responses;
import org.studentnow._;
import org.studentnow.api.AuthResponse;
import org.studentnow.api.Cards;
import org.studentnow.api.CardsResponse;
import org.studentnow.api.PostUserSetting;
import org.studentnow.api.StandardResponse;
import org.studentnow.gd.Loc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.studentnow.android.CardActivity;
import com.studentnow.android.__;
import com.studentnow.android.service.LocationCache.CachedLoc;
import com.studentnow.android.util.OFiles;
import com.studentnow.android.util.SuppressionPeriod;

public class UserSyncModule extends ServiceModule {

	public final static String TAG = UserSyncModule.class.getSimpleName();

	private final static int API_V = _.API_VERSION;

	private final static String FILE_PERS = "SyncModule" + API_V + ".dat";
	private final static String FILE_CARDS = "cards" + API_V + ".dat";
	private final static String FILE_PVLS = "postfields" + API_V + ".dat";

	private final static int SYNC_INTERVAL = 1000 * 60 * 60;

	private LiveService mLiveService;
	private CardProviderModule mCardModule;
	private AccountModule mAccountModule;
	private LocationModule mLocationModule;

	private AlarmManager mAlarmManager;

	private PendingIntent partDailyIntent, fullDailyIntent;

	private UpdateHold mSyncUpdateHold;

	private boolean requestUpdate = false;
	private boolean requestCardRefresh = false;
	private boolean requestSave = false;

	protected SuppressionPeriod cardSuppressionPeriod = new SuppressionPeriod();
	protected SuppressionPeriod postSuppressionPeriod = new SuppressionPeriod();

	private HashMap<String, String> postValues = new HashMap<String, String>();

	public UserSyncModule(LiveService liveService) {
		this.mLiveService = liveService;
		this.partDailyIntent = PendingIntent.getBroadcast(liveService, 0,
				new Intent(__.INTENT_UPDATE_CARDS), 0);
		this.fullDailyIntent = PendingIntent.getBroadcast(liveService, 1,
				new Intent(__.INTENT_UPDATE_CARDS), 0);
	}

	@Override
	public void link() {
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
		mCardModule = ((CardProviderModule) mLiveService
				.getServiceModule(CardProviderModule.class));
		mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);
		mLocationModule = (LocationModule) mLiveService
				.getServiceModule(LocationModule.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		String f = OFiles.getFolder(mLiveService);
		try {
			mSyncUpdateHold = (UpdateHold) OFiles.read(f + FILE_PERS);
			Log.i(TAG, "Recovered mSyncUpdateHold");
		} catch (Exception e) {
			mSyncUpdateHold = new UpdateHold();
		}
		
		HashMap<String, String> loadPostVals = null;
		try {
			loadPostVals = (HashMap<String, String>) OFiles.read(f + FILE_PVLS);
			Log.i(TAG, "Recovered " + loadPostVals.size() + " pending postValues");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading field syncs");
		}
		if (loadPostVals != null) {
			postValues.putAll(loadPostVals);
		}
		
		List<ECard> loadCards = null;
		try {
			loadCards = (List<ECard>) OFiles.read(f + FILE_CARDS);
			Log.i(TAG, "Recovered " + loadCards.size()
					+ " cards from previous service session");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading cards");
		}
		if (loadCards != null) {
			mCardModule.getCards().clear();
			mCardModule.getCards().addAll(loadCards);
		}
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(updateReciever, new IntentFilter(
				__.INTENT_UPDATE_CARDS));

		Random randomGenerator = new Random();

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, randomGenerator.nextInt(5));
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long midnightRefreshDate = c.getTimeInMillis();
		long sixHoursRefreshDate = System.currentTimeMillis()
				+ (AlarmManager.INTERVAL_DAY / 4);

		scheduleRepeatIntentRTC(fullDailyIntent, 2, midnightRefreshDate);
		scheduleRepeatIntentRTC(partDailyIntent, 6, sixHoursRefreshDate);
	}

	public boolean scheduleRepeatIntentRTC(PendingIntent pi, int timesPerDay,
			long start) {
		if (pi == null) {
			return false;
		}
		if (timesPerDay <= 0) {
			return false;
		}
		long i = AlarmManager.INTERVAL_DAY / timesPerDay;
		if (start == 0) {
			start = System.currentTimeMillis() + 5000;
		}
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, start, i, pi);
		return true;
	}

	@Override
	public void cancel() {
		mAlarmManager.cancel(partDailyIntent);
		mAlarmManager.cancel(fullDailyIntent);
		mLiveService.unregisterReceiver(updateReciever);
	}

	@Override
	public void cycle() {
		if (!requestUpdate && mCardModule.getCards().size() == 0) {
			requestUpdate = true;
		} else if (mSyncUpdateHold.isDue(SYNC_INTERVAL)) {
			requestUpdate = true;
		}
		if (requestCardRefresh && mCardModule != null) {
			mCardModule.requestUpdate();
			requestCardRefresh = false;
		}
		if (requestSave && save()) {
			requestSave = false;
		}
	}

	@Override
	public void cycleNetwork() {
		if (mAccountModule != null && mAccountModule.hasAuthResponse()) {
			AuthResponse authResponse = mAccountModule.getAuthResponse();
			if (!postValues.isEmpty() && !postSuppressionPeriod.isSuppressed()) {
				StandardResponse sr = PostUserSetting.post(authResponse,
						postValues);
				if (sr.isOK()) {
					Log.d(TAG, "Submitted " + postValues.size() + " values");
					if (postValues.containsKey(Fields.PROGRAMME_ID)) {
						requestUpdate = true;
					}
					postValues.clear();
					postSuppressionPeriod.reset();
				} else {
					long retry = postSuppressionPeriod.suppress();
					Log.e(TAG, "/sync response: " + sr.getStatus()
							+ " - retry in " + retry);
					CardActivity.showAlert(mLiveService,
							"Account sync error - retry in " + retry / 1000
									+ "s");
				}
				requestSave = true;
			}
			if (requestUpdate && !cardSuppressionPeriod.isSuppressed()) {
				Loc loc = getLastLocation();
				CardsResponse cr = Cards.query(authResponse, loc);
				List<ECard> newCards = cr.getCards();
				if (cr.isOK()) {
					if (newCards != null && newCards.size() > 0) {
						Log.d(TAG, "Updating cards with " + newCards.size()
								+ " new");
						mCardModule.getCards().clear();
						mCardModule.getCards().addAll(newCards);

						requestUpdate = false;
						requestCardRefresh = true;
						requestSave = true;
					}
					cardSuppressionPeriod.reset();
					mSyncUpdateHold.update();
				} else if (cr.getStatus() == Responses.ERROR) {
					long retry = cardSuppressionPeriod.suppress();
					Log.e(TAG, "Error CardsResponse - retry in " + retry);
					CardActivity.showAlert(mLiveService,
							"Card update error - retry in " + retry / 1000
									+ "s");
				}
			}
		}
	}

	private CachedLoc getLastLocation() {
		try {
			LocationCache mLocationCache = mLocationModule.getLocationCache();
			return mLocationCache.getLastLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public void put(String field, String value) {
		postValues.put(field, value);
		requestSave = true;
	}

	private BroadcastReceiver updateReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			requestUpdate();
			mLocationModule.requestLocationUpdate(true);
		}
	};

	public void clearLocalData() {
		mCardModule.getCards().clear();
		postValues.clear();
		requestSave = true;
		Log.i(TAG, "Cleared local sync data");
	}

	public void requestUpdate() {
		this.requestUpdate = true;
	}

	@Override
	public boolean save() {
		String f = OFiles.getFolder(mLiveService);

		boolean pers = save(f + FILE_PERS, mSyncUpdateHold,
				mSyncUpdateHold != null);
		boolean post = save(f + FILE_PVLS, postValues, postValues.size() > 0);

		List<ECard> cards = mCardModule.getCards();
		boolean card = save(f + FILE_CARDS, cards, cards.size() > 0);

		return pers && post && card;
	}

	private boolean save(String file, Object o, boolean toSave) {
		File f = new File(file);
		if (toSave) {
			try {
				OFiles.saveObject(o, f);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				return false;
			}
			Log.i(TAG, "Saved " + o.toString() + " to " + file);
			return true;
		}
		return !f.exists() || f.delete();
	}

}
