package com.studentnow.android.service;

import org.studentnow.Fields;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.studentnow.android.Static;
import com.studentnow.android.__;

public class PushModule implements ServiceModule {

	final static String TAG = PushModule.class.getSimpleName();

	private LiveService mLiveService = null;
	private AccountModule mAccountModule = null;

	private boolean requestGcmRegSubmission = true; // false when comfy

	public PushModule(LiveService liveService) {
		this.mLiveService = liveService;
	}

	@Override
	public void load() {

		mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);

		// GCMRegistrar.checkDevice(service);
		// GCMRegistrar.checkManifest(service);

		Static.GCM_REG_ID = GCMRegistrar.getRegistrationId(mLiveService);

		if (!gcmRegSet()) {

			Log.i(TAG, "Requesting registration");

			GCMRegistrar.register(mLiveService, __.SENDER_ID);

			requestGcmRegSubmission = true;

		} else {
			Log.i(TAG, "Already registered - " + Static.GCM_REG_ID);
		}

	}

	@Override
	public boolean save() {

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
		if (mAccountModule != null && mAccountModule.hasAuthResponse()
				&& requestGcmRegSubmission && gcmRegSet()) {
			requestGcmRegSubmission = false;

			UserSyncModule mUserSyncModule = (UserSyncModule) mLiveService
					.getServiceModule(UserSyncModule.class);
			mUserSyncModule.put(Fields.GCM_REG_ID, Static.GCM_REG_ID);

			Log.d(TAG, "Updated account with " + Fields.GCM_REG_ID);
		}
	}

	public static boolean gcmRegSet() {
		return !Static.GCM_REG_ID.equals("");
	}

}
