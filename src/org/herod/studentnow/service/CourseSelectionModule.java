package org.herod.studentnow.service;

import java.io.File;
import java.io.IOException;

import org.herod.studentnow.ObjectFiles;
import org.studentnow.Course;
import org.studentnow.Institution;

import android.util.Log;

public class CourseSelectionModule implements ServiceModule {

	final String TAG = CourseSelectionModule.class.getName();

	final String institutionSelectionFile = "institution.dat";
	final String courseSelectionFile = "course.dat";

	private boolean requestCourseSelectionSave = false;

	private LiveService service = null;

	private Institution institutionSelection = null;
	private Course courseSelection = null;

	public CourseSelectionModule(LiveService liveService) {
		this.service = liveService;
	}

	@Override
	public void load() {
		final String folder = service.getFilesDir() + File.separator;
		try {
			institutionSelection = (Institution) ObjectFiles.readObject(folder
					+ institutionSelectionFile);
			courseSelection = (Course) ObjectFiles.readObject(folder
					+ courseSelectionFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void schedule() {
	}

	@Override
	public void cancel() {

	}

	@Override
	public void cycle() {
		if (courseSelection != null) {
			while (requestCourseSelectionSave) {
				Log.i(TAG, "Saving new course selection...");
				if (save()) {
					((TimetableSyncModule) service
							.getServiceModule(TimetableSyncModule.class))
							.requestTimetableUpdate();
					requestCourseSelectionSave = false;
					Log.i(TAG, "... done");
				}
			}
		}

	}

	public Institution getInstitution() {
		return institutionSelection;
	}

	public Course getCourse() {
		return courseSelection;
	}

	public boolean isCourseSelected() {
		return institutionSelection != null && courseSelection != null;
	}

	public void setInstitutionSelection(Institution institutionSelection) {
		this.institutionSelection = institutionSelection;
		requestCourseSelectionSave = true;
	}

	public void setCourseSelection(Course courseSelection) {
		this.courseSelection = courseSelection;
		requestCourseSelectionSave = true;
	}

	@Override
	public boolean save() {
		if (isCourseSelected()) {
			final String folder = service.getFilesDir() + File.separator;
			try {
				ObjectFiles.saveObject(institutionSelection, folder + institutionSelectionFile);
				ObjectFiles.saveObject(courseSelection, folder + courseSelectionFile);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

}
