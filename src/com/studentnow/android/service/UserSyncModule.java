package com.studentnow.android.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.studentnow.ECard;
import org.studentnow.api.CardsQuery;
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

public class UserSyncModule extends BroadcastReceiver implements ServiceModule {

	private final String TAG = this.getClass().getName();

	private LiveService mLiveService;

	private AlarmManager am;

	private PendingIntent partDailyIntent, fullDailyIntent;

	private boolean requestUpdate = false;

	private HashMap<String, String> postFields = new HashMap<String, String>();

	public UserSyncModule(LiveService live) {
		this.mLiveService = live;

		this.partDailyIntent = PendingIntent.getBroadcast(live, 0, new Intent(
				__.Intent_ProgrammeUpdate), 0);
		this.fullDailyIntent = PendingIntent.getBroadcast(live, 1, new Intent(
				__.Intent_ProgrammeUpdate), 0);

		this.am = (AlarmManager) live.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void load() {

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

		am.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, fullDailyIntent);

		am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis()
				+ (AlarmManager.INTERVAL_DAY / 4),
				(AlarmManager.INTERVAL_DAY / 6), partDailyIntent);

	}

	@Override
	public void cancel() {
		am.cancel(partDailyIntent);
		am.cancel(fullDailyIntent);
		mLiveService.unregisterReceiver(this);
	}

	@Override
	public void cycle() {
		AccountModule mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);
		if (mAccountModule != null && mAccountModule.hasAuthResponse()) {
			if (!postFields.isEmpty()) {
				if (PostUserSetting.post(mAccountModule.getAuthResponse(),
						postFields)) {
					postFields.clear();
				}				
			}
			while (requestUpdate || mLiveService.getCards().size() == 0) {
				Location loc = getLastLocation();
				List<ECard> newCards = CardsQuery.query(
						mAccountModule.getAuthResponse(), loc);
				if (newCards == null) {
					break;
				}
				Log.d(TAG, "Updating cards with " + newCards.size() + " new");
				mLiveService.getCards().clear();
				mLiveService.getCards().addAll(newCards);
				requestUpdate = false;

				((NotificationModule) mLiveService
						.getServiceModule(NotificationModule.class))
						.requestCardsRefresh();
			}
		}
	}

	private LocationModule getLocationModule() {
		return ((LocationModule) mLiveService
				.getServiceModule(LocationModule.class));
	}

	private Location getLastLocation() {
		try {
			return getLocationModule().getLocationCache().getLastLocation()
					.getLocation();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void onReceive(Context c, Intent i) {
		requestUpdate();

		((LocationModule) mLiveService.getServiceModule(LocationModule.class))
				.requestLocationUpdate(true);
	}

	public void clearLocalData() {
		
		postFields.clear();
		
		mLiveService.getCards().clear();
	}

	public void requestUpdate() {
		this.requestUpdate = true;
	}

	@Override
	public boolean save() {
		return true;
	}

}
