package com.studentnow.android.service;

import java.io.IOException;

import org.studentnow.AuthResponse;

import android.util.Log;

import com.studentnow.android.io.OFiles;

public class AccountModule implements ServiceModule {

	final String TAG = AccountModule.class.getSimpleName();

	final String authKeyFile = "authkey.dat";

	private boolean requestSave = false;
	private boolean requestSyncUpdate = false;
	private boolean requestAccountInvalidate = false;

	private LiveService mLiveService = null;

	private UserSyncModule syncModule = null;

	private AuthResponse authResponse = null;

	public AccountModule(LiveService liveService) {
		this.mLiveService = liveService;
	}

	@Override
	public void load() {
		syncModule = ((UserSyncModule) mLiveService
				.getServiceModule(UserSyncModule.class));

		final String folder = OFiles.getFolder(mLiveService);
		try {
			authResponse = (AuthResponse) OFiles.readObject(folder
					+ authKeyFile);
			Log.i(TAG, "Recovered authResponse");
		} catch (Exception e) {
			Log.i(TAG, "Error recovering authResponse " + e.toString());
		}
	}

	@Override
	public boolean save() {
		final String folder = OFiles.getFolder(mLiveService);
		try {
			OFiles.saveObject(authResponse, folder + authKeyFile);
			Log.i(TAG, "Saved authResponse");
		} catch (IOException e) {
			Log.i(TAG, "Error saving authResponse: " + e.toString());
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
			if (save()) {
				requestSave = false;
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

	public void setAuthResponse(AuthResponse pAuthResponse) {
		authResponse = pAuthResponse;
		requestSave = true;
		requestAccountInvalidate = true;
	}

}
