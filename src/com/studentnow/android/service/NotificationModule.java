package com.studentnow.android.service;

import java.util.Calendar;

import org.studentnow.Session;

import com.studentnow.android.__;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class NotificationModule extends BroadcastReceiver implements
		ServiceModule {

	private LiveService liveService = null;

	private boolean requestCardRefresh = false;
	private boolean requestNextSessionNotification = false;

	private AlarmManager am;
	private PendingIntent notificationIntent;

	public NotificationModule(LiveService liveService) {
		this.liveService = liveService;
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
		
		if (liveService.getTimetable() == null) {
			return;
		}

		Session nextSession = liveService.getTimetable().getNextSession();

		if (nextSession.isToday()) {

			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 0);
			c.set(Calendar.HOUR_OF_DAY, nextSession.getStartTime().getHour());
			c.set(Calendar.MINUTE, nextSession.getStartTime().getMin());
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);

			am.set(AlarmManager.RTC, c.getTimeInMillis(), notificationIntent);

		}
	}

	@Override
	public void cancel() {
		am.cancel(notificationIntent);
		liveService.unregisterReceiver(this);
	}

	@Override
	public void cycle() {
		if (requestNextSessionNotification) {
			requestNextSessionNotification = false;

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
