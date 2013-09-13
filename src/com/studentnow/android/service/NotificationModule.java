package com.studentnow.android.service;

import java.util.Calendar;

import org.studentnow.Static.TimeMillis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.studentnow.android.CardActivity;
import com.studentnow.android.__;

public class NotificationModule extends ServiceModule {

	private final String TAG = CardActivity.class.getSimpleName();

	private LiveService mLiveService = null;
	private AlarmManager mAlarmManager = null;
	private PendingIntent notificationIntent = null;

	private boolean requestCardRefresh = false;

	public NotificationModule(LiveService pLiveService) {
		this.mLiveService = pLiveService;
	}

	@Override
	public void linkModules() {
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void load() {
		notificationIntent = PendingIntent.getBroadcast(mLiveService, 1,
				new Intent(__.INTENT_NOTIFICATION), 0);
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(updateReciever, new IntentFilter(
				__.INTENT_NOTIFICATION));

		mAlarmManager.setRepeating(AlarmManager.RTC, Calendar.getInstance()
				.getTimeInMillis() + TimeMillis.SECS_10, TimeMillis.MINS_1,
				notificationIntent);
	}

	@Override
	public void cancel() {
		mAlarmManager.cancel(notificationIntent);
		mLiveService.unregisterReceiver(updateReciever);
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

	public void requestCardsRefresh() {
		requestCardRefresh = true;
	}

	private BroadcastReceiver updateReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};

}
