package com.studentnow.android.service;

import java.util.Calendar;

import org.studentnow.ECard;
import org.studentnow.Static.TimeMillis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.studentnow.android.CardNotification;
import com.studentnow.android.__;

public class NotificationModule extends ServiceModule {

	private final String TAG = NotificationModule.class.getSimpleName();

	private long lastMs = System.currentTimeMillis();

	private LiveService mLiveService = null;
	private CardProviderModule mCardProviderModule = null;
	private AlarmManager mAlarmManager = null;
	private PendingIntent notificationIntent = null;

	public NotificationModule(LiveService pLiveService) {
		this.mLiveService = pLiveService;
	}

	@Override
	public void linkModules() {
		mCardProviderModule = (CardProviderModule) mLiveService
				.getServiceModule(CardProviderModule.class);
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
				.getTimeInMillis() + TimeMillis.SECS_10, TimeMillis.SECS_10,
				notificationIntent);
	}

	@Override
	public void cancel() {
		mAlarmManager.cancel(notificationIntent);
		mLiveService.unregisterReceiver(updateReciever);
	}

	public void postNotifications() {
		long ct = System.currentTimeMillis();
		for (ECard c : mCardProviderModule.getCards()) {
			long nt = c.getNotificationTime();
			if (lastMs < nt && nt < ct) {
				Intent i = mCardProviderModule.getCardIntent(c);
				CardNotification.notify(mLiveService, c, i, 0);
				Log.i(TAG, "Posted notification scheduled for " + nt);
			} else if (0 < nt) {
				Log.i(TAG, "Notification in " + (nt - ct) / 1000 + "ms");
			}
		}
		lastMs = ct;
		Log.i(TAG, "Checked notifications");
	}

	private BroadcastReceiver updateReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			postNotifications();
		}
	};

}
