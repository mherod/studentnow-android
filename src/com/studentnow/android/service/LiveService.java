package com.studentnow.android.service;

import java.util.ArrayList;
import java.util.List;

import org.studentnow.ECard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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
		modules = new ArrayList<ServiceModule>();
		modules.add(new AccountModule(this));
		modules.add(new CardSyncModule(this));
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
			try {
				for (ServiceModule m : modules) {
					m.cycle();
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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
