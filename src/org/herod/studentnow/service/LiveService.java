package org.herod.studentnow.service;

import java.util.ArrayList;
import java.util.List;

import org.herod.studentnow.__;
import org.studentnow.Course;
import org.studentnow.Institution;
import org.studentnow.Timetable;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LiveService extends Service implements Runnable {

	private final String TAG = "LiveService";

	private final Thread serviceThread = new Thread(this);

	private List<ServiceModule> modules;

	private boolean requestCourseSelectionSave = false;

	private Institution institutionSelection = null;
	private Course courseSelection = null;

	private Timetable timetable = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (serviceThread.isAlive() == false) {
			serviceThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void run() {

		SharedPreferences prefs = getSharedPreferences(__.File_Settings,
				MODE_PRIVATE);

		modules = new ArrayList<ServiceModule>();
		modules.add(new TimetableSyncModule(this));
		modules.add(new LocationModule(this));
		// modules.add(new TravelModule(this));
		modules.add(new NotificationModule(this));

		for (ServiceModule m : modules) {
			m.load();
		}

		for (ServiceModule m : modules) {
			m.schedule();
		}

		if (courseSelection == null) {
			courseSelection = loadCourseSelection(prefs);
		}

		while (true) {

			try {

				if (courseSelection != null) {
					while (requestCourseSelectionSave) {

						Log.i(TAG, "Saving new course selection...");

						if (saveCourseSelection(prefs)) {

							((TimetableSyncModule) getServiceModule(TimetableSyncModule.class))
									.requestTimetableUpdate();

							requestCourseSelectionSave = false;

							Log.i(TAG, "... done");

						}
					}
				}

				for (ServiceModule m : modules) {
					m.cycle();
				}

				Thread.sleep(1000);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		for (ServiceModule m : modules) {
			m.cancel();
		}
		super.onDestroy();
	}

	public List<ServiceModule> getModules() {
		return modules;
	}

	public Institution getInstitutionSelection() {
		return institutionSelection;
	}

	public Course getCourseSelection() {
		return courseSelection;
	}

	public void setInstitutionSelection(Institution institutionSelection) {
		this.institutionSelection = institutionSelection;
		requestCourseSelectionSave = true;
	}

	public void setCourseSelection(Course courseSelection) {
		this.courseSelection = courseSelection;
		requestCourseSelectionSave = true;
	}

	public ServiceModule getServiceModule(@SuppressWarnings("rawtypes") Class c) {
		for (ServiceModule m : modules) {
			if (m.getClass().equals(c)) {
				return m;
			}
		}
		return null;
	}

	public Timetable getTimetable() {
		return timetable;
	}

	public void setTimetable(Timetable timetable) {
		this.timetable = timetable;
	}

	private Course loadCourseSelection(SharedPreferences prefs) {
		String cName = prefs.getString(__.Save_CourseName, null);
		String cProgID = prefs.getString(__.Save_CourseProgID, null);
		if (cName == null || cProgID == null) {
			return null;
		}
		return new Course(cName, cProgID);
	}

	private boolean saveCourseSelection(SharedPreferences prefs) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(__.Save_CourseName, courseSelection.getName());
		e.putString(__.Save_CourseProgID, courseSelection.getProgrammeID());
		e.commit();
		return true;
	}

	private final IBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public LiveService getService() {
			return LiveService.this;
		}
	}

}
