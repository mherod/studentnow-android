package com.studentnow.android.service;

import java.util.HashMap;

import org.studentnow.Fields;
import org.studentnow.api.PostUserSetting;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.studentnow.android.Static;
import com.studentnow.android.__;

public class PushModule implements ServiceModule {

	private LiveService mLiveService = null;
	private AccountModule mAccountModule = null;

	private boolean requestGcmRegSubmission = true; // false when comfy

	public PushModule(LiveService liveService) {
		this.mLiveService = liveService;
	}

	public static boolean gcmRegSet() {
		return !Static.GCM_REGID.equals("");
	}

	@Override
	public void load() {

		mAccountModule = (AccountModule) mLiveService
				.getServiceModule(AccountModule.class);

		// GCMRegistrar.checkDevice(service);
		// GCMRegistrar.checkManifest(service);

		Static.GCM_REGID = GCMRegistrar.getRegistrationId(mLiveService);

		if (!gcmRegSet()) {

			Log.i("PushModule", "Requesting registration");

			GCMRegistrar.register(mLiveService, __.SENDER_ID);

			requestGcmRegSubmission = true;

		} else {
			Log.i("PushModule", "Already registered - " + Static.GCM_REGID);
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

		if (mAccountModule == null) {
			// wait
		} else if (requestGcmRegSubmission && gcmRegSet()
				&& mAccountModule.hasAuthResponse()) {
			requestGcmRegSubmission = false;

			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put(Fields.gcmregid, Static.GCM_REGID);
			PostUserSetting.post(mAccountModule.getAuthResponse(), fields);
			
			Log.d("PushModule", "updated account with reg id");
		}

	}

}
