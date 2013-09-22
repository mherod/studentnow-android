package com.studentnow.android.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.studentnow.android.CardActivity;
import com.studentnow.android.__;

public class LiveService extends Service implements Runnable {

	private final String TAG = LiveService.class.getSimpleName();

	private final Runnable mNetworkOpsRunnable = new Runnable() {
		@Override
		public void run() {
			while (!mNetworkOpsThread.isInterrupted()) {
				if (serviceCycle) {
					for (ServiceModule m : modules) {
						try {
							m.cycleNetwork();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
	};

	private final Thread mServiceThread = new Thread(this);
	private final Thread mNetworkOpsThread = new Thread(mNetworkOpsRunnable);

	private boolean serviceCycle = false;

	private final List<ServiceModule> modules = new ArrayList<ServiceModule>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		mServiceThread.setName("ServiceThread");
		if (!mServiceThread.isAlive()) {
			mServiceThread.start();
		}
		mNetworkOpsThread.setName("NetworkOperationsThread");
		if (!mNetworkOpsThread.isAlive()) {
			mNetworkOpsThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void run() {
		Log.i(TAG, mServiceThread.getName() + " started");
		sendBroadcast(new Intent(__.INTENT_CONNECT_SERVICE));

		modules.add(new AccountModule(this));
		modules.add(new UserSyncModule(this));
		modules.add(new PushModule(this));
		modules.add(new CardProviderModule(this));
		modules.add(new NotificationModule(this));
		modules.add(new LocationModule(this));
		modules.add(new SignatureCheckModule(this));
		
		for (ServiceModule m : modules) {
			m.link();
		}
 		for (ServiceModule m : modules) {
			m.load();
		}
		for (ServiceModule m : modules) {
			m.schedule();
		}

		long t = 0, tt = 0;
		int saveTicker = 0;

		serviceCycle = true;
		while (!mServiceThread.isInterrupted()) {
			for (ServiceModule m : modules) {
				try {
					t = System.currentTimeMillis();
					m.cycle();
					tt = System.currentTimeMillis() - t;
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(75);
				} catch (InterruptedException e) {
				}
				if (tt > 100) {
					Log.w(TAG, "Cycle for " + m.getClass().getSimpleName()
							+ " took " + tt + "ms");
				}
			}
			if (saveTicker++ > 30) {
				saveTicker = 0;
			}
			if (saveTicker == 0) {
				for (ServiceModule m : modules) {
					try {
						m.save();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Closing activity...");
		CardActivity.finishAll(this);

		Log.i(TAG, "Closing service...");
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
		for (ServiceModule m : modules) {
			if (m.getClass().equals(c)) {
				return m;
			}
		}
		return null;
	}

	// private void restart() {
	// stopService(new Intent(this, LiveService.class));
	// startService(new Intent(this, LiveService.class));
	// }

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
