package com.studentnow.android.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.studentnow.ECard;
import org.studentnow.Static.Responses;
import org.studentnow.api.AuthResponse;
import org.studentnow.api.Cards;
import org.studentnow.api.CardsResponse;
import org.studentnow.api.PostUserSetting;
import org.studentnow.api.StandardResponse;
import org.studentnow.gd.Location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.studentnow.android.__;
import com.studentnow.android.util.OFiles;
import com.studentnow.android.util.SuppressionPeriod;

public class UserSyncModule extends ServiceModule {

	final static String TAG = UserSyncModule.class.getSimpleName();

	private final static String FILE_CARDS = "cards.dat";
	private final static String FILE_SYNC_FIELDS = "postfields.dat";

	private LiveService mLiveService;
	private CardModule mCardModule;
	private AccountModule mAccountModule;
	private LocationModule mLocationModule;
	private NotificationModule mNotificationModule;

	private AlarmManager mAlarmManager;

	private PendingIntent partDailyIntent, fullDailyIntent;

	private boolean requestUpdate = false;
	private boolean requestCardRefresh = false;
	private boolean requestSave = false;

	protected SuppressionPeriod cardSuppressionPeriod = new SuppressionPeriod();
	protected SuppressionPeriod postSuppressionPeriod = new SuppressionPeriod();

	private HashMap<String, String> postFields = new HashMap<String, String>();

	public UserSyncModule(LiveService liveService) {
		this.mLiveService = liveService;
		this.partDailyIntent = PendingIntent.getBroadcast(liveService, 0,
				new Intent(__.INTENT_UPDATE_CARDS), 0);
		this.fullDailyIntent = PendingIntent.getBroadcast(liveService, 1,
				new Intent(__.INTENT_UPDATE_CARDS), 0);
	}

	@Override
	public void linkModules() {
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
		mCardModule = ((CardModule) mLiveService
				.getServiceModule(CardModule.class));
		mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);
		mLocationModule = (LocationModule) mLiveService
				.getServiceModule(LocationModule.class);
		mNotificationModule = ((NotificationModule) mLiveService
				.getServiceModule(NotificationModule.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		String folder = OFiles.getFolder(mLiveService);
		try {
			List<ECard> loadCards = (List<ECard>) OFiles.readObject(folder
					+ FILE_CARDS);
			mCardModule.getCards().clear();
			mCardModule.getCards().addAll(loadCards);

			Log.i(TAG, "Recovered " + loadCards.size()
					+ " cards from previous service session");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading cards");
		}
		try {
			postFields = (HashMap<String, String>) OFiles.readObject(folder
					+ FILE_SYNC_FIELDS);

			Log.i(TAG, "Recovered " + postFields.size()
					+ " waiting field syncs from previous service session");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading field syncs");
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

		mAlarmManager
				.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(),
						AlarmManager.INTERVAL_DAY, fullDailyIntent);
		mAlarmManager.setInexactRepeating(AlarmManager.RTC,
				System.currentTimeMillis() + (AlarmManager.INTERVAL_DAY / 4),
				(AlarmManager.INTERVAL_DAY / 6), partDailyIntent);

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
		}
		if (requestCardRefresh) {
			if (mNotificationModule != null) {
				mNotificationModule.requestCardsRefresh();
			}
			requestCardRefresh = false;
		}
		if (requestSave && save()) {
			requestSave = false;
		}
	}

	@Override
	public void networkOperations() {
		if (mAccountModule != null && mAccountModule.hasAuthResponse()) {
			AuthResponse authResponse = mAccountModule.getAuthResponse();
			if (!postFields.isEmpty() && !postSuppressionPeriod.isSuppressed()) {
				StandardResponse sr = PostUserSetting.post(authResponse,
						postFields);
				if (sr.isOK()) {
					Log.d(TAG, "Submitted " + postFields.size() + " values");
					postFields.clear();
					requestUpdate = true;
					postSuppressionPeriod.reset();
				} else {
					Log.d(TAG, "/sync response: " + sr.getStatus()
							+ " - retry in " + postSuppressionPeriod.suppress());
				}
				requestSave = true;
			}
			if (requestUpdate && !cardSuppressionPeriod.isSuppressed()) {
				Location loc = getLastLocation();
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
				} else if (cr.getStatus() == Responses.ERROR) {
					Log.e(TAG, "Error CardsResponse - retry in "
							+ cardSuppressionPeriod.suppress());
				}
			}
		}
	}

	private Location getLastLocation() {
		try {
			LocationCache mLocationCache = mLocationModule.getLocationCache();
			return mLocationCache.getLastLocation().getLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public void put(String field, String value) {
		postFields.put(field, value);
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
		postFields.clear();
		requestSave = true;
		Log.i(TAG, "Cleared local sync data");
	}

	public void requestUpdate() {
		this.requestUpdate = true;
	}

	@Override
	public boolean save() {
		String folder = OFiles.getFolder(mLiveService);
		try {
			OFiles.saveObject(postFields, folder + FILE_SYNC_FIELDS);
			OFiles.saveObject(mCardModule.getCards(), folder + FILE_CARDS);
			Log.i(TAG, "Saved local sync data");
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

}
