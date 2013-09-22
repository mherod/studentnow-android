package com.studentnow.android.service;

import java.io.IOException;
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
import com.studentnow.android.util.OFiles;

public class NotificationModule extends ServiceModule {

	public final String TAG = NotificationModule.class.getSimpleName();
	public final String FILE_PERSISTANCE = (TAG + ".dat");

	private NotificationPersistance persistence = null;

	private LiveService mLiveService = null;
	private CardProviderModule mCardProviderModule = null;

	private AlarmManager mAlarmManager = null;
	private PendingIntent notificationIntent = null;

	public NotificationModule(LiveService pLiveService) {
		this.mLiveService = pLiveService;
	}

	@Override
	public void link() {
		mCardProviderModule = (CardProviderModule) mLiveService
				.getServiceModule(CardProviderModule.class);
		mAlarmManager = (AlarmManager) mLiveService
				.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void load() {
		String folder = OFiles.getFolder(mLiveService);
		try {
			persistence = (NotificationPersistance) OFiles.readObject(folder
					+ FILE_PERSISTANCE);
			Log.i(TAG, "Recovered " + FILE_PERSISTANCE);
		} catch (Exception e) {
			persistence = new NotificationPersistance();
			Log.e(TAG, e.toString() + " loading " + FILE_PERSISTANCE);
		}
	}

	@Override
	public void schedule() {
		mLiveService.registerReceiver(updateReciever, new IntentFilter(
				__.INTENT_NOTIFICATION));

		notificationIntent = PendingIntent.getBroadcast(mLiveService, 1,
				new Intent(__.INTENT_NOTIFICATION), 0);
		mAlarmManager.setRepeating(AlarmManager.RTC, Calendar.getInstance()
				.getTimeInMillis() + TimeMillis.SECS_10, TimeMillis.SECS_10,
				notificationIntent);
	}

	@Override
	public void cancel() {
		mAlarmManager.cancel(notificationIntent);
		mLiveService.unregisterReceiver(updateReciever);
	}

	@Override
	public boolean save() {
		String folder = OFiles.getFolder(mLiveService);
		try {
			OFiles.saveObject(persistence, folder + FILE_PERSISTANCE);
			Log.i(TAG, "Saved " + FILE_PERSISTANCE);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	public void postNotifications() {
		long ct = System.currentTimeMillis();
		for (ECard c : mCardProviderModule.getCards()) {
			long nt = c.getNotificationTime();
			if (persistence.lastMs < nt && nt < ct) {
				Intent i = mCardProviderModule.getCardIntent(c);
				CardNotification.notify(mLiveService, c, i, 0);
				Log.i(TAG, "Posted notification scheduled for " + nt);
			} else if (0 < nt) {
				Log.i(TAG, "Notification in " + (nt - ct) / 1000 + "ms");
			}
		}
		persistence.lastMs = ct;
		Log.i(TAG, "Checked notifications");
	}

	private BroadcastReceiver updateReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			postNotifications();
		}
	};

}
