package com.studentnow.android.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.studentnow.android.CardActivity;
import com.studentnow.android.__;

public class NotificationModule extends BroadcastReceiver implements
		ServiceModule {

	private final String TAG = CardActivity.class.getName();

	private LiveService liveService = null;

	private boolean requestCardRefresh = false;
	private boolean requestNextSessionNotification = false;

	private AlarmManager am;
	private PendingIntent notificationIntent;

	public NotificationModule(LiveService liveService) {
		this.liveService = liveService;
		this.am = (AlarmManager) liveService
				.getSystemService(Context.ALARM_SERVICE);
	}

	public void requestCardsRefresh() {
		requestCardRefresh = true;
	}

	public void requestNextSessionNotification() {
		requestNextSessionNotification = true;
	}

	@Override
	public void onReceive(Context c, Intent i) {
		requestNextSessionNotification();
	}

	@Override
	public void load() {
		notificationIntent = PendingIntent.getBroadcast(liveService, 1,
				new Intent(__.Intent_Notification), 0);
	}

	@Override
	public void schedule() {
		liveService.registerReceiver(this, new IntentFilter(
				__.Intent_Notification));

		am.setRepeating(AlarmManager.RTC, Calendar.getInstance()
				.getTimeInMillis() + 10 * 1000, 60 * 1000, notificationIntent);
	}

	@Override
	public void cancel() {
		am.cancel(notificationIntent);
		liveService.unregisterReceiver(this);
	}

	final int MINS30 = 30 * 60 * 1000;

	@Override
	public void cycle() {
		if (requestNextSessionNotification) {
			requestNextSessionNotification = false;
			Log.d(TAG, "requestNextSessionNotification");
//
//			Timetable tt;
//			Session nextSession;
//			if ((tt = liveService.getTimetable()) != null
//					&& (nextSession = tt.refreshStatus().getNextSession()) != null) {
//				long nowDate = new Date().getTime();
//				long nextDate = nextSession.getNextDate();
//
//				Log.d(TAG, "nowDate " + nowDate);
//				Log.d(TAG, "nextDate " + nextDate);
//
//				if (nextDate > 0) {
//					int travelTime = 0;
//					if (nextSession.isSet(_.FIELD_TRAVEL_DURATION)) {
//						travelTime = nextSession
//								.getInt(_.FIELD_TRAVEL_DURATION) * 1000;
//					}
//					Log.d(TAG,
//							((nextDate - nowDate) + " > " + (MINS30 + travelTime)));
//					if ((nextDate - nowDate) < (MINS30 + travelTime)) {
//						Log.d(TAG, "sendNotification");
//					}
//				}
//			}
//
		}
		if (requestCardRefresh) {
			requestCardRefresh = false;
			liveService.sendBroadcast(new Intent(__.Intent_CardUpdate));
		}
	}

	@Override
	public boolean save() {
		return true;
	}

}
