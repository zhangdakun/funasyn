/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eben.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import cn.eben.androidsync.R;
import com.eben.service.DThread;
import com.eben.service.EbpService;
import com.funambol.android.App;
import com.funambol.util.Log;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Service that continues to run in background and respond to the push 
 * notification events from the server. This should be registered as service
 * in AndroidManifest.xml. 
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationService extends EbpService {

    private static final String LOGTAG = LogUtil
            .makeLogTag(NotificationService.class);

    public static final String SERVICE_NAME = "com.eben.client.NotificationService";

    private TelephonyManager telephonyManager;

    //    private WifiManager wifiManager;
    //
    //    private ConnectivityManager connectivityManager;

    private BroadcastReceiver notificationReceiver;

    private BroadcastReceiver connectivityReceiver;

    private PhoneStateListener phoneStateListener;

    private ExecutorService executorService;

    private TaskSubmitter taskSubmitter;

    private TaskTracker taskTracker;

    private XmppManager xmppManager;

    private SharedPreferences sharedPrefs;

    private String deviceId;
//	private void startForegroundCompat() {
//		try {
////			if (Build.VERSION.SDK_INT < 18)
//				startForeground(0x4333, new Notification());
//			return;
//		} catch (Exception e) {
//			Log.error(LOGTAG, "startForegroundCompat error");
//		}
//	}

//	private void stopForegroundCompat() {
//		try {
////			if (Build.VERSION.SDK_INT < 18)
//				stopForeground(true);
//			return;
//		} catch (Exception e) {
//			Log.error(LOGTAG, "stopForegroundCompat error");
//		}
//	}
    public NotificationService() {
        notificationReceiver = new NotificationReceiver();
        connectivityReceiver = new ConnectivityReceiver(this);
        phoneStateListener = new PhoneStateChangeListener(this);
        executorService = Executors.newSingleThreadExecutor();
        taskSubmitter = new TaskSubmitter(this);
        taskTracker = new TaskTracker(this);
    }
    
    private void init() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        // Get deviceId
        deviceId = telephonyManager.getDeviceId();
        // Log.debug(LOGTAG, "deviceId=" + deviceId);
        Editor editor = sharedPrefs.edit();
        editor.putString(Constants.DEVICE_ID, deviceId);
        editor.commit();

        // If running on an emulator
        if (deviceId == null || deviceId.trim().length() == 0
                || deviceId.matches("0+")) {
            if (sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
                deviceId = sharedPrefs.getString(Constants.EMULATOR_DEVICE_ID,
                        "");
            } else {
                deviceId = (new StringBuilder("EMU")).append(
                        (new Random(System.currentTimeMillis())).nextLong())
                        .toString();
                editor.putString(Constants.EMULATOR_DEVICE_ID, deviceId);
                editor.commit();
            }
        }
        Log.debug(LOGTAG, "deviceId=" + deviceId);

        xmppManager = new XmppManager(this);

        taskSubmitter.submit(new Runnable() {
            public void run() {
                NotificationService.this.start();
            }
        });
        
        taskSubmitter.submit(new Runnable() {
            public void run() {
            	runThread();
            }
        });
        
        inited = true;
    }
    
    DThread d;
    @Override
    public void onCreate() {
        Log.debug(LOGTAG, "onCreate()...");
//        startForegroundCompat();
        super.onCreate();
        
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//        	public void uncaughtException(Thread thread, Throwable ex) {
//        	// 
//        	Log.error(TAG, "catch error : "+ex.getLocalizedMessage());
////        	finish();
//        	}
//        	});
        
        String username = App.i().getAppInitializer().getConfiguration().getUsername(); 
//    	SharedPreferences sp =this.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
//        Context.MODE_PRIVATE);
//    	String username = sp.getString(Constants.XMPP_ORI_USERNAME, null);
        initMethod();
        startForegroundCompat(R.string.account_type,new Notification());
//        d = new DThread(this,45555);
//        d.start();
//        alramService();
        if(null == username || "".equals(username)) {
        	Log.debug(LOGTAG, "onCreate ,username : "+username+", return");;
//        	stopSelf();
        	return;
        }
        inited = false;
        init();
        
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.debug(LOGTAG, "onStart()...");

        
//    	SharedPreferences sp =this.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
//    	        Context.MODE_PRIVATE);
//    	    	String username = sp.getString(Constants.XMPP_ORI_USERNAME, null);
//    	    	
//        if(null == username || "".equals(username)) {
//        	Log.debug(LOGTAG, "onStart, username : "+username+", return");
//        	inited = false;
////        	stopSelf();
//        	return;
//        }
//
//        if(!inited) {
//        	init();
//        }
    }

    @Override
    public void onDestroy() {
        Log.debug(LOGTAG, "onDestroy()...");
//        stopForegroundCompat();
        stop();
        inited = false;
        
//        Intent intent = new Intent(Constants.ACTION_START_EBP);     
//        PendingIntent sender = PendingIntent.getBroadcast(this, 0,    
//                intent, 0);    
//        
//        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, sender);
//        System.exit(2);
//        this.sendBroadcast(new Intent(Constants.ACTION_START_EBP));
        super.onDestroy();
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
        
    	SharedPreferences sp =this.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
    	        Context.MODE_PRIVATE);
    	    	String username = sp.getString(Constants.XMPP_ORI_USERNAME, null);
    	    	
        if(null == username || "".equals(username)) {
        	Log.debug(LOGTAG, "onStart, username : "+username+", return");
        	inited = false;
//        	stopSelf();
        	return START_STICKY;
        }
//        startForegroundCompat(R.string.account_type,new Notification());
//        startForegroundCompat();
        if(!inited) {
        	init();
        }
        
        return START_STICKY;
	}

	@Override
    public IBinder onBind(Intent intent) {
        Log.debug(LOGTAG, "onBind()...");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.debug(LOGTAG, "onRebind()...");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.debug(LOGTAG, "onUnbind()...");
        return true;
    }

    public static Intent getIntent() {
        return new Intent(SERVICE_NAME);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }

    public TaskTracker getTaskTracker() {
        return taskTracker;
    }

    public XmppManager getXmppManager() {
        return xmppManager;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPrefs;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void connect() {
        Log.debug(LOGTAG, "connect()...");
        taskSubmitter.submit(new Runnable() {
            public void run() {
                NotificationService.this.getXmppManager().connect();
            }
        });
    }

    public void disconnect() {
        Log.debug(LOGTAG, "disconnect()...");
        taskSubmitter.submit(new Runnable() {
            public void run() {
                NotificationService.this.getXmppManager().disconnect();
            }
        });
    }

    private void registerNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_SHOW_NOTIFICATION);
        filter.addAction(Constants.ACTION_NOTIFICATION_CLICKED);
        filter.addAction(Constants.ACTION_NOTIFICATION_CLEARED);
        registerReceiver(notificationReceiver, filter);
    }

    private void unregisterNotificationReceiver() {
        unregisterReceiver(notificationReceiver);
    }

    private void registerConnectivityReceiver() {
        Log.debug(LOGTAG, "registerConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        IntentFilter filter = new IntentFilter();
        // filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
    }

    private void unregisterConnectivityReceiver() {
        Log.debug(LOGTAG, "unregisterConnectivityReceiver()...");
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(connectivityReceiver);
    }

    private void start() {
        Log.debug(LOGTAG, "start()...");
        registerNotificationReceiver();
        registerConnectivityReceiver();
        // Intent intent = getIntent();
        // startService(intent);
        xmppManager.connect();
    }

    private void stop() {
        Log.debug(LOGTAG, "stop()...");
        unregisterNotificationReceiver();
        unregisterConnectivityReceiver();
        xmppManager.disconnect();
        executorService.shutdown();
        
        isOn = false;
    }

    /**
     * Class for summiting a new runnable task.
     */
    public class TaskSubmitter {

        final NotificationService notificationService;

        public TaskSubmitter(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @SuppressWarnings("unchecked")
        public Future submit(Runnable task) {
            Future result = null;
            if (!notificationService.getExecutorService().isTerminated()
                    && !notificationService.getExecutorService().isShutdown()
                    && task != null) {
                result = notificationService.getExecutorService().submit(task);
            }
            return result;
        }

    }

    /**
     * Class for monitoring the running task count.
     */
    public class TaskTracker {

        final NotificationService notificationService;

        public int count;

        public TaskTracker(NotificationService notificationService) {
            this.notificationService = notificationService;
            this.count = 0;
        }

        public void increase() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count++;
                Log.debug(LOGTAG, "Incremented task count to " + count);
            }
        }

        public void decrease() {
            synchronized (notificationService.getTaskTracker()) {
                notificationService.getTaskTracker().count--;
                Log.debug(LOGTAG, "Decremented task count to " + count);
            }
        }

    }
    
    
    private static Thread timerThread = null;
    private static boolean isOn = false;
    
	private void runThread() {
		// TODO Auto-generated method stub
		Log.debug(TAG, "runThread");
		isOn = true;
		try {

			if (null == timerThread || !timerThread.isAlive()
					|| timerThread.isInterrupted()) {
				Log.debug(TAG, "timerThread new ");
				timerThread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						while(isOn) {
						try {
							Thread.sleep(60 * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.debug(TAG, "to connect ");
						xmppManager.connect();
						
						xmppManager.runTask();
					}
					}
				});
				timerThread.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static final Class<?>[] mSetForegroundSignature = new Class[] {
	    boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
	    int.class, Notification.class};
	private static final Class<?>[] mStopForegroundSignature = new Class[] {
	    boolean.class};

	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	void invokeMethod(Method method, Object[] args) {
	    try {
	        method.invoke(this, args);
	    } catch (InvocationTargetException e) {
	        // Should not happen.
	        Log.error("ApiDemos", "Unable to invoke method", e);
	    } catch (IllegalAccessException e) {
	        // Should not happen.
	        Log.error("ApiDemos", "Unable to invoke method", e);
	    }
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (mStartForeground != null) {
	        mStartForegroundArgs[0] = Integer.valueOf(id);
	        mStartForegroundArgs[1] = notification;
	        invokeMethod(mStartForeground, mStartForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.
	    mSetForegroundArgs[0] = Boolean.TRUE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	    mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (mStopForeground != null) {
	        mStopForegroundArgs[0] = Boolean.TRUE;
	        invokeMethod(mStopForeground, mStopForegroundArgs);
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    mNM.cancel(id);
	    mSetForegroundArgs[0] = Boolean.FALSE;
	    invokeMethod(mSetForeground, mSetForegroundArgs);
	}


	public void initMethod() {
	    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    try {
	        mStartForeground = getClass().getMethod("startForeground",
	                mStartForegroundSignature);
	        mStopForeground = getClass().getMethod("stopForeground",
	                mStopForegroundSignature);
	        return;
	    } catch (NoSuchMethodException e) {
	        // Running on an older platform.
	        mStartForeground = mStopForeground = null;
	    }
	    try {
	        mSetForeground = getClass().getMethod("setForeground",
	                mSetForegroundSignature);
	    } catch (NoSuchMethodException e) {
	        throw new IllegalStateException(
	                "OS doesn't have Service.startForeground OR Service.setForeground!");
	    }
	}

	public static void alramService(Context ctx) {
//        Intent intent = new Intent(Constants.ACTION_START_EBP);     
//        PendingIntent sender = PendingIntent.getBroadcast(ctx, 0,    
//                intent, 0);    
//        long firstime = SystemClock.elapsedRealtime();    
//        AlarmManager am = (AlarmManager) ctx    
//                .getSystemService(Context.ALARM_SERVICE);    
//
//        // 
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,    
//                10 * 1000, sender);  
	}
//	@Override
//	public void onDestroy() {
//	    // Make sure our notification is gone.
//	    stopForegroundCompat(R.string.foreground_service_started);
//	}
}
