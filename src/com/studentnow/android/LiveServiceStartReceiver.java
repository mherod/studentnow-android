package com.studentnow.android;

import org.herod.studentnow.service.LiveService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LiveServiceStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myIntent = new Intent(context, LiveService.class);
		context.startService(myIntent);
	}

}
