package org.herod.studentnow.service;

import java.util.Calendar;
import java.util.Random;

import org.studentnow.Timetable;
import org.studentnow.api.TimetableQuery;

import com.studentnow.android.__;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class TimetableSyncModule extends BroadcastReceiver implements
		ServiceModule {

	private final String TAG = this.getClass().getName();

	private AlarmManager am;

	private LiveService liveService;

	private PendingIntent partDailyIntent;
	private PendingIntent fullDailyIntent;

	private boolean requestTimetableUpdate = false;

	public TimetableSyncModule(LiveService live) {
		this.liveService = live;

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
		liveService.registerReceiver(this, new IntentFilter(
				__.Intent_ProgrammeUpdate));

		Random randomGenerator = new Random();

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.set(Calendar.MINUTE, 15 + randomGenerator.nextInt(30));
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
		liveService.unregisterReceiver(this);
	}

	@Override
	public void cycle() {
		CourseSelectionModule courseSelection = (CourseSelectionModule) liveService
				.getServiceModule(CourseSelectionModule.class);
		while ((requestTimetableUpdate || liveService.getTimetable() == null)
				&& courseSelection.getCourse() != null) {
			
			Log.i(TAG, "Querying timetable information...");
			
			long l = System.currentTimeMillis();

			Timetable t = TimetableQuery.query(courseSelection.getCourse()
					.getProgrammeID(), true);

			if (t != null) {
				liveService.setTimetable(t);
				requestTimetableUpdate = false;
			}

			((NotificationModule) liveService
					.getServiceModule(NotificationModule.class))
					.requestCardsRefresh();

			Log.i(TAG,
					"... " + "timetable updated in "
							+ (System.currentTimeMillis() - l) + "ms");
		}

	}

	@Override
	public void onReceive(Context c, Intent i) {
		requestTimetableUpdate();

		((LocationModule) liveService.getServiceModule(LocationModule.class))
				.requestLocationUpdate(true);
	}

	public void requestTimetableUpdate() {
		this.requestTimetableUpdate = true;
	}

	@Override
	public boolean save() {
		return true;
	}

}
