package com.studentnow.android.service;

import java.io.File;
import java.io.IOException;

import org.studentnow.AuthResponse;

import android.util.Log;

import com.studentnow.android.io.ObjectFiles;

public class AccountModule implements ServiceModule {

	final String TAG = AccountModule.class.getName();

	final String authKeyFile = "authkey.dat";

	private boolean requestSave = false;
	private boolean requestSyncUpdate = false;
	private boolean requestAccountInvalidate = false;

	private LiveService service = null;

	private UserSyncModule syncModule = null;

	private AuthResponse authResponse = null;

	public AccountModule(LiveService liveService) {
		this.service = liveService;
	}

	private String getFolder() {
		return service.getFilesDir() + File.separator;
	}

	@Override
	public void load() {
		syncModule = ((UserSyncModule) service
				.getServiceModule(UserSyncModule.class));

		try {
			authResponse = (AuthResponse) ObjectFiles.readObject(getFolder()
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
			ObjectFiles.saveObject(authResponse, getFolder() + authKeyFile);
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
			Log.i(TAG, "[" + "requestSave" + "]");
			Log.i(TAG, "Saving AuthResponse...");
			if (save()) {
				requestSave = false;
				Log.i(TAG, "... done");
			}
		}
		while (requestAccountInvalidate) {
			Log.i(TAG, "[" + "requestAccountInvalidate" + "]");
			syncModule.clearLocalData();
			requestAccountInvalidate = false;
			requestSyncUpdate = true;
		}
		while (requestSyncUpdate) {
			Log.i(TAG, "[" + "requestSyncUpdate" + "]");
			syncModule.requestUpdate();
			requestSyncUpdate = false;
		}
	}

	public boolean hasAuthResponse() {
		return authResponse != null && authResponse.getKey() != null;
	}

	public AuthResponse getAuthResponse() {
		return authResponse;
	}

	public void setAuthResponse(AuthResponse authResponse) {
		this.authResponse = authResponse;

		requestSave = true;
		requestAccountInvalidate = true;
	}

}
