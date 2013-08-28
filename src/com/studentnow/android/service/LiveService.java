package com.studentnow.android.service;

import java.util.ArrayList;
import java.util.List;

import org.studentnow.ECard;

import com.google.android.gcm.GCMRegistrar;
import com.studentnow.android.__;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LiveService extends Service implements Runnable {

	// private final String TAG = LiveService.class.getName();

	private final Thread serviceThread = new Thread(this);

	private List<ServiceModule> modules;

	private List<ECard> cards = new ArrayList<ECard>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (serviceThread.isAlive() == false) {
			serviceThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void run() {
		
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(__.DISPLAY_MESSAGE_ACTION));
        
        
		GCMRegistrar.checkDevice(this);
		final String regId = GCMRegistrar.getRegistrationId(this);

		if (regId.equals("")) {
			GCMRegistrar.register(this, __.SENDER_ID);
		} else if (GCMRegistrar.isRegisteredOnServer(this)) {

		}
		
		Log.d("sssssssssss   sssss", "dudd " + regId);

		modules = new ArrayList<ServiceModule>();
		modules.add(new AccountModule(this));
		modules.add(new PushModule(this));
		modules.add(new UserSyncModule(this));
		modules.add(new LocationModule(this));
		// modules.add(new TravelModule(this));
		modules.add(new CardsBuildModule(this));
		modules.add(new NotificationModule(this));

		for (ServiceModule m : modules) {
			m.load();
		}
		for (ServiceModule m : modules) {
			m.schedule();
		}
		while (true) {
			for (ServiceModule m : modules) {
				try {
					m.cycle();
					Thread.sleep(250);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	


    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(__.EXTRA_MESSAGE);
            Log.d("ssssssssssss", newMessage);
        }
    };

	@Override
	public void onDestroy() {
		for (ServiceModule m : modules) {
			m.save();
		}
		for (ServiceModule m : modules) {
			m.cancel();
		}
		super.onDestroy();
	}

	public List<ServiceModule> getModules() {
		return modules;
	}

	public ServiceModule getServiceModule(@SuppressWarnings("rawtypes") Class c) {
		for (ServiceModule m : modules)
			if (m.getClass().equals(c))
				return m;
		return null;
	}

	public List<ECard> getCards() {
		return cards;
	}

	private final IBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		public LiveService getService() {
			return LiveService.this;
		}
	}

}
