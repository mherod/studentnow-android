package com.studentnow.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.studentnow.android.service.LiveService;

public class LiveServiceStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myIntent = new Intent(context, LiveService.class);
		context.startService(myIntent);
	}

}
