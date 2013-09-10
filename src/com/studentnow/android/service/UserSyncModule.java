package com.studentnow.android.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.studentnow.ECard;
import org.studentnow.api.Cards;
import org.studentnow.api.PostUserSetting;
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

public class UserSyncModule extends BroadcastReceiver implements ServiceModule {

	final static String TAG = UserSyncModule.class.getSimpleName();

	final static String sCardsFile = "cards.dat";
	final static String sPostFieldsFile = "postfields.dat";

	private LiveService mLiveService;
	private LocationModule mLocationModule;
	private AccountModule mAccountModule;
	private NotificationModule mNotificationModule;

	private AlarmManager mAlarmManager;

	private PendingIntent partDailyIntent, fullDailyIntent;

	private boolean requestUpdate = false;
	private boolean requestCardRefresh = false;
	private boolean requestSave = false;

	private HashMap<String, String> postFields = new HashMap<String, String>();

	public UserSyncModule(LiveService liveService) {
		this.mLiveService = liveService;

		this.partDailyIntent = PendingIntent.getBroadcast(liveService, 0,
				new Intent(__.Intent_ProgrammeUpdate), 0);
		this.fullDailyIntent = PendingIntent.getBroadcast(liveService, 1,
				new Intent(__.Intent_ProgrammeUpdate), 0);

		this.mAlarmManager = (AlarmManager) liveService
				.getSystemService(Context.ALARM_SERVICE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		mLocationModule = (LocationModule) mLiveService
				.getServiceModule(LocationModule.class);
		mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);
		mNotificationModule = ((NotificationModule) mLiveService
				.getServiceModule(NotificationModule.class));

		String folder = OFiles.getFolder(mLiveService);
		try {
			List<ECard> loadCards = (List<ECard>) OFiles.readObject(folder
					+ sCardsFile);
			mLiveService.getCards().clear();
			mLiveService.getCards().addAll(loadCards);

			Log.i(TAG, "Recovered " + loadCards.size()
					+ " cards from previous service session");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading cards");
		}
		try {
			postFields = (HashMap<String, String>) OFiles.readObject(folder
					+ sPostFieldsFile);

			Log.i(TAG, "Recovered " + postFields.size()
					+ " waiting field syncs from previous service session");
		} catch (Exception e) {
			Log.e(TAG, e.toString() + " loading field syncs");
		}
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(this, new IntentFilter(
				__.Intent_ProgrammeUpdate));

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
		mLiveService.unregisterReceiver(this);
	}

	@Override
	public void cycle() {
		if (mAccountModule != null && mAccountModule.hasAuthResponse()) {
			if (!postFields.isEmpty()) {
				if (PostUserSetting.post(mAccountModule.getAuthResponse(),
						postFields).isOK()) {
					Log.d(TAG, "Submitted " + postFields.size() + " values");
					postFields.clear();
					requestUpdate = true;
				}
				requestSave = true;
			}
			if (requestUpdate || mLiveService.getCards().size() == 0) {
				Location loc = getLastLocation();
				List<ECard> newCards = Cards.query(
						mAccountModule.getAuthResponse(), loc);

				if (newCards != null) {
					Log.d(TAG, "Updating cards with " + newCards.size()
							+ " new");
					mLiveService.getCards().clear();
					mLiveService.getCards().addAll(newCards);

					requestUpdate = false;
					requestCardRefresh = true;
					requestSave = true;
				}
			}
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

	@Override
	public void onReceive(Context c, Intent i) {
		requestUpdate();
		mLocationModule.requestLocationUpdate(true);
	}

	public void clearLocalData() {
		mLiveService.getCards().clear();
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
			OFiles.saveObject(postFields, folder + sPostFieldsFile);
			OFiles.saveObject(mLiveService.getCards(), folder + sCardsFile);
			Log.i(TAG, "Saved local sync data");
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

}
