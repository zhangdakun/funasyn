package com.eben.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.funambol.util.Log;

public class EbpService extends Service {
	
	public static volatile boolean inited = false;
	public static final String TAG = "EbpService";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		
//		Build.VERSION_CODES.ICE_CREAM_SANDWICH
		return null;
	}

	private void startForegroundCompat() {
		try {
//			if (Build.VERSION.SDK_INT < 18)
				startForeground(0x3333, new Notification());
			return;
		} catch (Exception e) {
			Log.error(TAG, "startForegroundCompat error");
		}
	}

	private void stopForegroundCompat() {
		try {
//			if (Build.VERSION.SDK_INT < 18)
				stopForeground(true);
			return;
		} catch (Exception e) {
			Log.error(TAG, "stopForegroundCompat error");
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		serviceInit(this);
		startForegroundCompat();
	}

	public void serviceInit(Context context) {

		if (!(inited)) {

			inited = true;
		}

		return;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopForegroundCompat();
		super.onDestroy();
	}
 
}
