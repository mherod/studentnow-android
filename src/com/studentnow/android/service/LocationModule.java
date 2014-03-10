package com.studentnow.android.service;

import java.util.Calendar;

import org.studentnow.Static.Fields;
import org.studentnow.gd.Loc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.studentnow.android.__;
import com.studentnow.android.service.LocationCache.CachedLoc;

public class LocationModule extends ServiceModule implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	public final static String TAG = LocationModule.class.getSimpleName();

	private int playResultCode = ConnectionResult.INTERNAL_ERROR;

	private int FASTEST_INTERVAL = 5 * 60 * 1000;
	private int UPDATE_INTERVAL = 15 * 60 * 1000;

	private Handler mainHandler = null;

	private LiveService mLiveService;
	private UserSyncModule mUserSyncModule = null;
	private LocationCache mLocationCache;
	private MyLocationHandler mLocationHandler;
	private AlarmManager mAlarmManager;

	private PendingIntent updateLocIntent;

	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;

	private boolean enableCompatibleUpdates = false;

	private boolean isLocationUpdating = false;

	private boolean requestLocationUpdate = false;
	private boolean requestLocationSubmission = false;

	private long locUpdatedStartMs = 0;

	public LocationModule(LiveService pLiveService) {
		mLiveService = pLiveService;
		mLocationCache = new LocationCache(pLiveService);
		mLocationHandler = new MyLocationHandler(pLiveService);
		mainHandler = new Handler(pLiveService.getMainLooper());
		updateLocIntent = PendingIntent.getBroadcast(pLiveService, 0,
				new Intent(__.INTENT_POLL_LOC), 0);
	}

	@Override
	public void link() {
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
		mUserSyncModule = (UserSyncModule) mLiveService
				.getServiceModule(UserSyncModule.class);

		playResultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mLiveService);

		if (isPlayServicesAvailable()) {
			Log.i(TAG, "Play Services are available!");
		} else {
			Log.i(TAG, "Unsuccessful connecting Play Services (result "
					+ playResultCode + ")");
			enableCompatibleUpdates = true;
		}
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(updateReciever, new IntentFilter(
				__.INTENT_POLL_LOC));

		final int from = 4;
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.HOUR_OF_DAY) >= from) {

		}
		if (enableCompatibleUpdates) {
			mAlarmManager.setInexactRepeating(AlarmManager.RTC,
					cal.getTimeInMillis(), AlarmManager.INTERVAL_HOUR,
					updateLocIntent);
		}

		// requestLocationUpdate(true);

		mLocationRequest = LocationRequest.create()

		.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

		.setInterval(UPDATE_INTERVAL)

		.setFastestInterval(FASTEST_INTERVAL);

		mLocationClient = new LocationClient(mLiveService, this, this);
		mLocationClient.connect();

	}

	@Override
	public void cycle() {
		if (requestLocationUpdate && !isLocationUpdating) {

			isLocationUpdating = true;
			Runnable startListeningRunnable = getLocationHandler().startListeners;
			mainHandler.post(startListeningRunnable);
			locUpdatedStartMs = System.currentTimeMillis();

		} else if (!requestLocationUpdate && isLocationUpdating) {

			isLocationUpdating = false;
			Runnable stopListeningRunnable = getLocationHandler().stopListeners;
			mainHandler.post(stopListeningRunnable);

		} else if (isLocationUpdating
				&& (locUpdatedStartMs + (12 * 1000)) < System
						.currentTimeMillis()) {

			requestLocationUpdate = false;
			requestLocationSubmission = true;

		}
		if (requestLocationSubmission) {
			requestLocationSubmission = false;

			Loc loc = getLastLocation();
			if (loc != null) {
				mUserSyncModule.put(Fields.LOCATION, loc.getString());
				mUserSyncModule.put("playservices",
						String.valueOf(isPlayServicesAvailable()));
			}
		}
	}

	@Override
	public void cancel() {
		mLocationClient.disconnect();
		mAlarmManager.cancel(updateLocIntent);
		mLiveService.unregisterReceiver(updateReciever);
	}

	@Override
	public boolean save() {
		return true;
	}

	public boolean isPlayServicesAvailable() {
		return playResultCode == ConnectionResult.SUCCESS;
	}

	public void requestLocationUpdate(boolean requestLocationUpdate) {
		this.requestLocationUpdate = requestLocationUpdate;
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

	private CachedLoc getLastLocation() {
		try {
			LocationCache mLocationCache = getLocationCache();
			return mLocationCache.getLastLocation();
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
		public void onNewBestLocation(Location loc) {
			mLocationCache.storeLocation(loc);
		}

	}

	@Override
	public void onLocationChanged(Location loc) {
		mLocationCache.storeLocation(loc);
		requestLocationSubmission = true;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}
