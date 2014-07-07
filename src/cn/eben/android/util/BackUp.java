package cn.eben.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import se.tactel.contactcleanapp.cleanapp.CleanAppAdapter;
import se.tactel.contactcleanapp.cleanapp.CleanAppManager;


import com.funambol.android.App;
import com.funambol.util.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.webkit.MimeTypeMap;


public class BackUp {

	public static String TAG = "backUp";
	public static boolean exportVcf(String target) {

//		File dir = new File(Contants.backUpRoot);
//		if (!dir.exists()) {
//			dir.mkdirs();
//		}
		File targetFile = new File(target);

		if (targetFile.exists()) {
			return true;
		} else {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		ContentResolver cr = App.i().getApplicationContext()
				.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		int index = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
		try {

			FileOutputStream fout = new FileOutputStream(target);
			byte[] data = new byte[1024 * 1];
			while (cur.moveToNext()) {
				String lookupKey = cur.getString(index);
				Uri uri = Uri.withAppendedPath(
						ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
				AssetFileDescriptor fd = cr.openAssetFileDescriptor(uri, "r");
				FileInputStream fin = fd.createInputStream();
				int len = -1;
				while ((len = fin.read(data)) != -1) {
					fout.write(data, 0, len);
				}
				fin.close();
			}
			fout.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean importVcfFile(String address) {
		if(openBackup(new File(address))) {
			if(isImportFinish() ){
		        CleanAppManager cleanappmanager = CleanAppManager.get(App.i().getApplicationContext());
		        cleanappmanager.startSearch(App.i().getApplicationContext(), new CleanAppAdapter());
			}
		} else {
			
		}
		return true;
	}
	private boolean openBackup(File savedVCard) {
		if (!savedVCard.exists()) {
			Log.error(TAG, "openBackup ,file not exist");
			return false;
		}
		try {
			String vcfMimeType = MimeTypeMap.getSingleton()
					.getMimeTypeFromExtension("vcf");
			Intent openVcfIntent = new Intent(Intent.ACTION_VIEW);
			openVcfIntent.setDataAndType(Uri.fromFile(savedVCard), vcfMimeType);
			// Try to explicitly specify activity in charge of opening the vCard
			// so that the user doesn't have to choose
			// http://stackoverflow.com/questions/6827407/how-to-customize-share-intent-in-android/9229654#9229654
			try {
				if (App.i().getApplicationContext()
						.getPackageManager() != null) {
					List<ResolveInfo> resolveInfos = App.i()
							.getApplicationContext().getPackageManager()
							.queryIntentActivities(openVcfIntent, 0);
					if (resolveInfos != null) {
						for (ResolveInfo resolveInfo : resolveInfos) {
							ActivityInfo activityInfo = resolveInfo.activityInfo;
							if (activityInfo != null) {
								String packageName = activityInfo.packageName;
								String name = activityInfo.name;
								// Find the needed Activity based on Android
								// source files:
								// http://grepcode.com/search?query=ImportVCardActivity&start=0&entity=type&n=
								if (packageName != null
										&& packageName
												.equals("com.android.contacts")
										&& name != null
										&& name.contains("ImportVCardActivity")) {
									openVcfIntent.setPackage(packageName);
									break;
								} else if (packageName != null
										&& packageName
										.equals("com.ebensz.contacts")
								&& name != null
								&& name.contains("ImportVCardActivity")) {
									openVcfIntent.setPackage(packageName);
									break;
								}
							}
						}
					}
				}
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
			openVcfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			App.i().getApplicationContext()
					.startActivity(openVcfIntent);
		} catch (Exception exception) {
			exception.printStackTrace();
			// No app for openning .vcf files installed (unlikely)
			return false;
		}

		return true;
	}
	
	
	private boolean isImportServiceRunning() {
		Log.debug(TAG, "isMyServiceRunning,");
		ActivityManager manager = (ActivityManager) App.i()
				.getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.android.contacts.common.vcard.VCardService"
					.equals(service.service.getClassName())) {
				return true;
			}
//			AgentLog.info(TAG,
//					"service name : " + service.service.getClassName());
		}
		return false;
	}
	
	private boolean isImportFinish() {
		boolean isrun = true;
		while(isrun) {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!isImportServiceRunning()) {
			isrun = false;
		}
		}
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
