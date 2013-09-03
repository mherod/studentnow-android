package com.studentnow.android.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.studentnow.android.CardActivity;
import com.studentnow.android.__;
import org.studentnow.Static.TimeMillis;;

public class NotificationModule extends BroadcastReceiver implements
		ServiceModule {

	private final String TAG = CardActivity.class.getName();

	private LiveService mLiveService = null;

	private boolean requestCardRefresh = false;

	private AlarmManager am;
	private PendingIntent notificationIntent;

	public NotificationModule(LiveService liveService) {
		this.mLiveService = liveService;
		this.am = (AlarmManager) liveService
				.getSystemService(Context.ALARM_SERVICE);
	}

	public void requestCardsRefresh() {
		requestCardRefresh = true;
	}

	@Override
	public void onReceive(Context c, Intent i) {
		
	}

	@Override
	public void load() {
		notificationIntent = PendingIntent.getBroadcast(mLiveService, 1,
				new Intent(__.Intent_Notification), 0);
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(this, new IntentFilter(
				__.Intent_Notification));

		am.setRepeating(AlarmManager.RTC, Calendar.getInstance()
				.getTimeInMillis() + TimeMillis.SECS_10, TimeMillis.MINS_1, notificationIntent);
	}

	@Override
	public void cancel() {
		am.cancel(notificationIntent);
		mLiveService.unregisterReceiver(this);
	}

	@Override
	public void cycle() {
		if (requestCardRefresh) {
			requestCardRefresh = false;
			CardModule.updateActivityCards(mLiveService);
		}
	}

	@Override
	public boolean save() {
		return true;
	}

}
