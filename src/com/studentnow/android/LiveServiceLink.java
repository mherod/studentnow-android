package com.studentnow.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.studentnow.android.service.LiveService;

public class LiveServiceLink implements ServiceConnection {

	private static final String TAG = "LiveServiceLink";

	private Context context = null;
	private LiveService liveService = null;
	
	public LiveServiceLink(Context context) {
		this.context = context;
	}

	public void start() {
		Intent serviceIntent = new Intent(context, LiveService.class);
		context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
		context.startService(serviceIntent);
	}

	public void stop() {
		context.unbindService(this);
	}

	public LiveService getLiveService() {
		return liveService;
	}

	public void onServiceConnected(ComponentName name, IBinder service) {
		liveService = ((LiveService.MyBinder) service).getService();
		Log.v(TAG, "Service Connected: " + name.getClassName() + ", "
				+ (liveService == null ? "null" : "not null"));
	}

	public void onServiceDisconnected(ComponentName name) {
		liveService = null;
		Log.v(TAG, "Service Disconnected: " + name.getClassName() + ", "
				+ (liveService == null ? "null" : "not null"));
	}

}
