package com.studentnow.android.service;

import herod.android.LocationHandler;

import java.util.Calendar;

import org.studentnow.Static.Fields;
import org.studentnow.gd.Location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.studentnow.android.Static;
import com.studentnow.android.__;

public class LocationModule extends ServiceModule {

	private Handler mainHandler = null;

	private LiveService mLiveService;
	private UserSyncModule mUserSyncModule = null;
	private LocationCache mLocationCache;
	private MyLocationHandler mLocationHandler;
	private AlarmManager mAlarmManager;

	private PendingIntent intent;

	private boolean requestLocationUpdate = false, isLocationUpdating = false;
	private long locUpdatedStartMs = 0;

	public LocationModule(LiveService pLiveService) {
		mLiveService = pLiveService;
		mLocationCache = new LocationCache(pLiveService);
		mLocationHandler = new MyLocationHandler(pLiveService);

		this.intent = PendingIntent.getBroadcast(pLiveService, 0, new Intent(
				__.INTENT_POLL_LOC), 0);

		mainHandler = new Handler(pLiveService.getMainLooper());
	}

	@Override
	public void linkModules() {
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
		mUserSyncModule = (UserSyncModule) mLiveService
				.getServiceModule(UserSyncModule.class);
	}

	public void requestLocationUpdate(boolean requestLocationUpdate) {
		this.requestLocationUpdate = requestLocationUpdate;
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(updateReciever, new IntentFilter(
				__.INTENT_POLL_LOC));

		final int from = 4;
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.HOUR_OF_DAY) >= from) {

		}
		mAlarmManager.setInexactRepeating(AlarmManager.RTC,
				cal.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, intent);

		requestLocationUpdate(true);
	}

	@Override
	public void cycle() {
		if (requestLocationUpdate && !isLocationUpdating) {
			isLocationUpdating = true;
			mainHandler.post(getLocationHandler().startListeners);
			locUpdatedStartMs = System.currentTimeMillis();
		} else if (!requestLocationUpdate && isLocationUpdating) {
			isLocationUpdating = false;
			mainHandler.post(getLocationHandler().stopListeners);
		} else if (isLocationUpdating
				&& (locUpdatedStartMs + (12 * 1000)) < System
						.currentTimeMillis()) {
			requestLocationUpdate = false;

			Location loc = getLastLocation();
			mUserSyncModule.put(Fields.LOCATION, loc.getString());
		}
	}

	@Override
	public void cancel() {
		mAlarmManager.cancel(intent);
		mLiveService.unregisterReceiver(updateReciever);
	}

	private BroadcastReceiver updateReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			requestLocationUpdate(true);
		}
	};

	public LocationHandler getLocationHandler() {
		return mLocationHandler;
	}

	private Location getLastLocation() {
		try {
			LocationCache mLocationCache = getLocationCache();
			return mLocationCache.getLastLocation().getLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public LocationCache getLocationCache() {
		return mLocationCache;
	}

	public class MyLocationHandler extends LocationHandler {

		public MyLocationHandler(Context context) {
			super(context);
		}

		@Override
		public void onNewBestLocation(android.location.Location loc) {
			mLocationCache.storeLocation(loc);
		}

	}

	@Override
	public boolean save() {
		return true;
	}

}
