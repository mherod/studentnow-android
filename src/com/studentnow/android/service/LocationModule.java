package com.studentnow.android.service;

import herod.android.LocationHandler;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;

import com.studentnow.android.__;

public class LocationModule extends BroadcastReceiver implements ServiceModule {

	private Handler mainHandler = null;

	private AlarmManager am;

	private LiveService liveService;
	private LocationCache locationCache;
	private MyLocationHandler locationHandler;

	private PendingIntent intent;

	private boolean requestLocationUpdate = false, isLocationUpdating = false;
	private long locUpdatedStartMs = 0;

	public LocationModule(LiveService live) {

		this.liveService = live;
		this.locationCache = new LocationCache(live);
		this.locationHandler = new MyLocationHandler(live);

		this.intent = PendingIntent.getBroadcast(live, 0, new Intent(
				__.Intent_HomeLocPoll), 0);

		this.mainHandler = new Handler(live.getMainLooper());

		this.am = (AlarmManager) live.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void load() {

	}

	public void requestLocationUpdate(boolean requestLocationUpdate) {
		this.requestLocationUpdate = requestLocationUpdate;
	}

	@Override
	public void schedule() {
		liveService.registerReceiver(this, new IntentFilter(
				__.Intent_HomeLocPoll));

		Calendar cal = Calendar.getInstance();

		final int from = 4;

		int hour = cal.get(Calendar.HOUR_OF_DAY);

		if (hour > from) {

		}

		am.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
				AlarmManager.INTERVAL_HOUR, intent);
		
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
		}
	}

	@Override
	public void cancel() {
		am.cancel(intent);
		liveService.unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context c, Intent i) {

	}

	public LocationHandler getLocationHandler() {
		return locationHandler;
	}

	public LocationCache getLocationCache() {
		return locationCache;
	}

	public class MyLocationHandler extends LocationHandler {

		public MyLocationHandler(Context context) {
			super(context);
		}

		@Override
		public void onNewBestLocation(Location loc) {
			locationCache.storeLocation(loc);
		}

	}

	@Override
	public boolean save() {
		return true;		
	}

}
