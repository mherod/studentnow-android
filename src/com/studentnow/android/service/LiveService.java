package com.studentnow.android.service;

import java.util.ArrayList;
import java.util.List;

import org.studentnow.ECard;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.studentnow.android.__;

public class LiveService extends Service implements Runnable {

	private ServiceModule sm = null;
	private long smt = 0;

	private final String TAG = LiveService.class.getSimpleName();

	private final Runnable mMaintainanceRunnable = new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (smt == 0 || sm == null) {
					// not yet
				} else if (mServiceThread.isInterrupted()) {
					// wait
				} else if ((System.currentTimeMillis() - smt) > 10000) {
					System.err.println("Stuck on "
							+ sm.getClass().getSimpleName() + ", interrepted!!!!");
					mServiceThread.interrupt();
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					
				}
			}
		}
	};

	private final Thread mServiceThread = new Thread(this);
	private final Thread mMaintainanceThread = new Thread(mMaintainanceRunnable);

	private List<ServiceModule> modules = new ArrayList<ServiceModule>();

	private List<ECard> cards = new ArrayList<ECard>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (mServiceThread.isAlive() == false) {
			mServiceThread.start();
			mMaintainanceThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void run() {

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				__.DISPLAY_MESSAGE_ACTION));

		modules.add(new AccountModule(this));
		modules.add(new UserSyncModule(this));
		modules.add(new PushModule(this));
		modules.add(new CardModule(this));
		modules.add(new NotificationModule(this));
		modules.add(new LocationModule(this));

		for (ServiceModule m : modules) {
			m.load();
		}
		for (ServiceModule m : modules) {
			m.schedule();
		}

		long t = 0, tt = 0;

		int saveTicker = 0;
		while (true) {
			for (ServiceModule m : modules) {
				sm = m;
				smt = System.currentTimeMillis();

				try {
					t = System.currentTimeMillis();
					m.cycle();
					tt = System.currentTimeMillis() - t;
					Thread.sleep(75);
				} catch (Exception e) {
					e.printStackTrace();
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
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// String newMessage =
			// intent.getExtras().getString(__.EXTRA_MESSAGE);
		}
	};

	@Override
	public void onDestroy() {
		Log.i(TAG, "Service shutting down");
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
