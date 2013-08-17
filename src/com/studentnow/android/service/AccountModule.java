package com.studentnow.android.service;

import java.io.File;
import java.io.IOException;

import org.studentnow.AuthKey;

import android.util.Log;

import com.studentnow.android.ObjectFiles;

public class AccountModule implements ServiceModule {

	final String TAG = AccountModule.class.getName();

	final String authKeyFile = "authkey.dat";

	private boolean requestSave = false;

	private LiveService service = null;

	private AuthKey authKey = null;

	public AccountModule(LiveService liveService) {
		this.service = liveService;
	}

	private String getFolder() {
		return service.getFilesDir() + File.separator;
	}

	@Override
	public void load() {
		try {
			authKey = (AuthKey) ObjectFiles.readObject(getFolder()
					+ authKeyFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean save() {
		try {
			ObjectFiles.saveObject(authKey, getFolder() + authKeyFile);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public void schedule() {
	}

	@Override
	public void cancel() {

	}

	@Override
	public void cycle() {
		while (requestSave) {
			Log.i(TAG, "Saving new account data...");
			if (save()) {
				((TimetableSyncModule) service
						.getServiceModule(TimetableSyncModule.class))
						.requestUpdate();
				((TravelModule) service.getServiceModule(TravelModule.class))
						.requestUpdate();
				requestSave = false;
				Log.i(TAG, "... done");
			}
		}
	}

//	public Institution getInstitution() {
//		return institutionSelection;
//	}

//	public Course getCourse() {
//		return courseSelection;
//	}

//	public boolean isCourseSelected() {
//		return institutionSelection != null && courseSelection != null;
//	}

	public boolean hasAuthKey() {
		return authKey != null && authKey.getKey() != null;
	}

	public AuthKey getAuthKey() {
		return authKey;
	}

	public void setAuthKey(AuthKey authKey) {
		this.authKey = authKey;
		requestSave = true;
	}

//	public void setInstitutionSelection(Institution institutionSelection) {
//		this.institutionSelection = institutionSelection;
//		requestSave = true;
//	}
//
//	public void setCourseSelection(Course courseSelection) {
//		this.courseSelection = courseSelection;
//		requestSave = true;
//	}

}
