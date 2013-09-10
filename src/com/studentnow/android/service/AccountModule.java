package com.studentnow.android.service;

import java.io.File;
import java.io.IOException;

import org.studentnow.AuthResponse;
import org.studentnow.Static.Validation;

import android.util.Log;

import com.studentnow.android.util.OFiles;

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
		if (!hasAuthResponse()) {
			new File(folder + authKeyFile).delete();
			return true;
		}
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
		if (requestSave) {
			Log.i(TAG, "[" + "requestSave" + "]");
			if (save()) {
				requestSave = false;
			}
		}
		if (requestAccountInvalidate) {
			Log.i(TAG, "[" + "requestAccountInvalidate" + "]");
			syncModule.clearLocalData();
			requestAccountInvalidate = false;
			requestSyncUpdate = true;
		}
		if (requestSyncUpdate) {
			Log.i(TAG, "[" + "requestSyncUpdate" + "]");
			syncModule.requestUpdate();
			requestSyncUpdate = false;
		}
	}

	public boolean hasAuthResponse() {
		return Validation.validAuthResponse(authResponse);
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
