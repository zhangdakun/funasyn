package cn.eben.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;


import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.util.Base64;
import com.funambol.util.Log;

public class EbenHelpers {
	private static final String TAG = "EbenHelpers";
	protected static final String NOTEPAD_PK = "com.ebensz.notepad";
	protected static final String CARDNAME_PK = "com.ebensz.cardname";
	protected static final String CALENDAR_PK = "com.ebensz.calendar";

	public static final String DOCS_UPDATEINFO_ACTION = "cn.eben.overview.docs.UPDATE_INFO";
	public static final String KEY_DOCS_UPDATE_INOF_TYTE = "docsUpdateInfoTyte";

	public static final int DOCS_UPDATE_INFO_TYPE_CLOUDBACKUP = 0;
	private static String fontPrefix = "<font color='red'>";
	private static String fontSuffix = "</font>";

	/**

	 * 
	 * @param context

	 * 
	 */
	public static boolean sdIsExit(Context context) {

		try {
			File file = Environment.getExternalStorageDirectory();
			File[] list = file.listFiles();
			if (null == list) {
				Intent intent = new Intent();
				intent.setClassName("cn.eben.android.activities.dialogs",
						"cn.eben.android.activities.dialogs.PasswordCheckAlertDialog");
				context.startActivity(intent);

				return false;
			}
		} catch (Exception e) {
			Intent intent = new Intent();
			intent.setClassName("cn.eben.android.activities.dialogs",
					"cn.eben.android.activities.dialogs.PasswordCheckAlertDialog");
			context.startActivity(intent);
			return false;
		}


		String state = android.os.Environment.getExternalStorageState();
		if ("".equals(state) || null == state
				|| android.os.Environment.MEDIA_BAD_REMOVAL.equals(state)
				|| android.os.Environment.MEDIA_REMOVED.equals(state)
				|| android.os.Environment.MEDIA_UNMOUNTED.equals(state)) {
			Intent intent = new Intent();
			intent.setClassName("cn.eben.android.activities.dialogs",
					"cn.eben.android.activities.dialogs.PasswordCheckAlertDialog");
			context.startActivity(intent);
			return false;
		} else {
			return true;
		}
	}

	/**

	 */
	public static boolean isSdCardNotInsert() {
		boolean bFlag = false;
		if (isSdCardFile()) {
			bFlag = true;
		} else {

			String state = android.os.Environment.getExternalStorageState();
			if ("".equals(state) || null == state
					|| android.os.Environment.MEDIA_BAD_REMOVAL.equals(state)
					|| android.os.Environment.MEDIA_REMOVED.equals(state)
					|| android.os.Environment.MEDIA_UNMOUNTED.equals(state)) {
				bFlag = true;
			}
		}
		return bFlag;
	}

	/**

	 */
	private static boolean isSdCardFile() {
		boolean bFlag = false;

		try {
			File file = Environment.getExternalStorageDirectory();
			File[] list = file.listFiles();
			if (null == list) {
				bFlag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFlag;
	}

	/**
	 * @param notepadUse

	 * @param ediskUse


	 */
	public static float computerUseCapacity(String notepadUse, String ediskUse) {
		float fUseCapacity = 0;
		float fNotepadUseCapacity = 0;
		float fEdiskUseCapacity = 0;
		try {
			if (notepadUse != null && ediskUse != null) {
				fNotepadUseCapacity = dataConversion(notepadUse);
				fEdiskUseCapacity = dataConversion(ediskUse);
				fUseCapacity = fNotepadUseCapacity + fEdiskUseCapacity;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return fUseCapacity;
	}

	/**
	 * @param total

	 */
	public static float computerTotalCapacity(String total) {
		float fTotalCapacity = 0;
		try {
			if (total != null) {

				fTotalCapacity = dataConversion(total);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return fTotalCapacity;
	}

	/**
	 * @param notepadUse

	 */
	public static String computeCapacityPercent(String notepadUse,
			String ediskUse, String allCapacity) {
		float fUseCapacity = 0;
		float fPercent = 0;
		float fTotalCapacity = 0;
		String sPercent = "100%";
		try {
			if (notepadUse != null && allCapacity != null) {

				fTotalCapacity = computerTotalCapacity(allCapacity);
				fUseCapacity = computerUseCapacity(notepadUse, ediskUse);
				if (0 != fTotalCapacity) {
					fPercent = fUseCapacity / fTotalCapacity;
					fPercent = 1 - fPercent;
					DecimalFormat formatter = new DecimalFormat("0.00");
					if (fPercent < 0) {
						fPercent = 0;
					}
					sPercent = formatter.format(fPercent * 100);
					sPercent = sPercent + "%";
				} else {
					sPercent = EbenConstantsUtil.DEFAULT_PERCENT + "%";
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return sPercent;
	}

	/**
	 * @param str

	 */
	public static float dataConversion(String str) {
		String useCapacity = str;
		float fCapacity = 0;

		try {
			int useLength = useCapacity.length();
			char lastCharOfStr = useCapacity.charAt(useLength - 1);
			String useCapacityStr = useCapacity.substring(0, useLength - 1);

			if (lastCharOfStr == 'K' || lastCharOfStr == 'K') {
				fCapacity = Float.parseFloat(useCapacityStr);
			} else if (lastCharOfStr == 'M' || lastCharOfStr == 'm') {
				fCapacity = Float.parseFloat(useCapacityStr);
				fCapacity = 1024 * fCapacity;
			} else if (lastCharOfStr == 'G' || lastCharOfStr == 'g') {
				fCapacity = Float.parseFloat(useCapacityStr);
				fCapacity = 1024 * 1024 * fCapacity;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return fCapacity;
	}

	/**
	 * @param s

	 */
	public static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0, pos) + c + s.substring(pos + 1);
	}

	/**

	 * @return"yyyy/MM/dd HH:mm:ss"
	 */
	public static String toFormatDate(Long l, String format) {
		return new java.text.SimpleDateFormat(format)
				.format(new java.util.Date(l));
	}

	public static String updataIntroduceContent(Context context) {
		StringBuffer introduceContentStr = new StringBuffer();
		String contentStr = "";
		final String ORIGINAL_DATA = "1970-01-01";
		String fontPrefix = "<font color='red'>";
		String fontSuffix = "</font>";
		String formatData = "yyyy-MM-dd HH:mm";
//		try {
//			StatisticsService statSer = new StatisticsService();
//			statSer.getCalendarCount();
//
//			String lastSyncTime = EbenHelpers.toFormatDate(
//					Long.parseLong(statSer.getLsTime()), formatData);
//			String cardNameNum = statSer.getCardnameCount();
//			String calendarNum = statSer.getCalendarCount();
//
//			String percent = statSer.getRemainderPerc();
//			String totalCapacity = statSer.getSpace();
//			String notepadNum = statSer.getPageCount();
//			introduceContentStr.append(context.getResources().getString(
//					R.string.eben_introduce_for_overview, totalCapacity,
//					percent, notepadNum, cardNameNum, calendarNum));
//			if (null != lastSyncTime) {
//				if (lastSyncTime.contains(ORIGINAL_DATA)) {
//					introduceContentStr.append(context
//							.getString(R.string.eben_introduce_new));
//				} else {
//					introduceContentStr.append(context.getString(
//							R.string.eben_introduce_three, lastSyncTime));
//				}
//			}
//			introduceContentStr.append(context
//					.getString(R.string.eben_introduce_help));
//			contentStr = introduceContentStr.toString();
//			contentStr = ContentConvertUtil.replaceAll(contentStr, "br",
//					"<br/>");
//			contentStr = ContentConvertUtil.replaceAll(contentStr, "font_pre",
//					fontPrefix);
//			contentStr = ContentConvertUtil.replaceAll(contentStr, "font_suf",
//					fontSuffix);
//			// introduceContent.setText(Html.fromHtml(contentStr));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return contentStr;
	}

	public static void sentIntentForExteranl(Context context) {
		if (Log.isLoggable(Log.DEBUG))
			Log.debug(TAG, "sentIntentForExteranl...");
		Intent intent = new Intent();
		intent.setAction(DOCS_UPDATEINFO_ACTION);
		intent.putExtra(KEY_DOCS_UPDATE_INOF_TYTE,
				DOCS_UPDATE_INFO_TYPE_CLOUDBACKUP);
		context.sendBroadcast(intent);
	}

	public static String stringReplace(String struserName) {
		String userNameStr = struserName;
		userNameStr = ContentConvertUtil.replaceAll(userNameStr, "br", "<br/>");
		userNameStr = ContentConvertUtil.replaceAll(userNameStr, "font_pre",
				fontPrefix);
		userNameStr = ContentConvertUtil.replaceAll(userNameStr, "font_suf",
				fontSuffix);
		return userNameStr;
	}

	public static void getStatisticsFromServer() {
		Log.error(TAG, "getStatisticsFromServer...");
		new Thread(new Runnable() {
			public void run() {
//				EbenSyncCmd.getAppsInfo();
				// HttpJSONService jServ = HttpJSONService.getInstance();
				// jServ.setOp(HttpJSONserviceConst.OP_STATISTICS_STATUS_REQUESTION);
				// jServ.setServUrl(HttpJSONService.DS_URI);// jason
				// try {
				// jServ.handler();
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		}).start();
	}

	public static String decodeKey(String key){
		String decode = null;

		try {
			decode = new String(Base64.decode(key.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(TAG, "base64 decode err , key = " + key);
			EbenFileLog.recordSyncLog("error in decode key : " + key);
			decode = null;
		}
		return decode;
	}

	public static String encodeKey(String key){
		String encode = null;
		try {
			encode = new String(Base64.encode(key.getBytes()));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.error(TAG, "base64 encode err , key = " + key);
			encode = null;
		}
		return encode;
	}

	public static boolean isWifiOnly() {
		// 0 for wifi only set
		boolean isWifi = true;
//		try {
//			String status = new EbenSyncConfig().getString(
//					AndroidConfiguration.KEY_SYNC_WLAN_ONLY, "0");
//
//			if ("0".equals(status)) {
//				return true;
//			} else if ("1".equalsIgnoreCase(status)) {
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return true;
//		}

		return isWifi;

	}
//	public static int NETWORK_OK = 0;
//	public static int NETWORK_ERROR = 1;
//	public static int WLAN_CLOSED = 2;
//	public static int WLAN_DISABLE = 3;
//	public static int NETWORK_OK = 0;
	
	public static int isNetworkAvailable() {
		// TODO Auto-generated method stub
//		WifiManager wf = (WifiManager) App.i().getApplicationContext()
//				.getSystemService(Context.WIFI_SERVICE);
//		ConnectivityManager cm = (ConnectivityManager) App.i()
//				.getApplicationContext()
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		int bret = ExternalEntryConst.NETWORK_OK;
//		if (null == cm) {
//			Log.error(TAG, "ConnectivityManager err ,return false");
//			// return false;
//			bret = ExternalEntryConst.NETWORK_ERROR;
//		} else {
//			NetworkInfo net = cm.getActiveNetworkInfo();
//			if (null == net) {
//				Log.error(TAG,
//						"ConnectivityManager NetworkInfo err ,return false");
//				// return false;
//				bret = ExternalEntryConst.NETWORK_NOTACTIVE;
//			} else {
//
//				int netType = net.getType();
//				String info = net.getExtraInfo();
//				Log.debug(TAG, "net type : " + netType + ", info: " + info);
//
//				if (EbenHelpers.isWifiOnly()) { // set
//					Log.debug(TAG, "wlan only");
//					if (!wf.isWifiEnabled()) {
//						Log.info(TAG, "wlan not enable ,exit");
//						// return false;
//						bret = ExternalEntryConst.WLAN_CLOSED;
//					} else if (ConnectivityManager.TYPE_WIFI != net.getType()) {
//						Log.error(TAG, "wifi not active , exist");
////						return false;
//						bret = ExternalEntryConst.WLAN_DISABLE;
//					}
//				}
//
//			}
//		}
//
//		return bret;
		return 0;
	}
	
	/**
	 * get wifi rssi
	 * @return
	 */
	public static int getWifiRssi() {
		WifiManager wifi_service = (WifiManager) App.i()
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();

		int rss = wifiInfo.getRssi();
		Log.info(TAG, "wifi rssi: " + rss);

		return rss;
	}
	public static int getNetworkType() {
		// TODO Auto-generated method stub
		WifiManager wf = (WifiManager) App.i().getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager cm = (ConnectivityManager) App.i()
				.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		int bret = -1;
		if (null == cm) {
			Log.error(TAG, "ConnectivityManager err ,return false");
			// return false;
			bret = -1;
		} else {
			NetworkInfo net = cm.getActiveNetworkInfo();
			if (null == net) {
				Log.error(TAG,
						"ConnectivityManager NetworkInfo err ,return -1");
				// return false;
				bret = -1;
			} else {

				int netType = net.getType();
				String info = net.getExtraInfo();
				Log.debug(TAG, "net type : " + netType + ", info: " + info);

				if (EbenHelpers.isWifiOnly()) { // set
					Log.debug(TAG, "wlan only");
					if (!wf.isWifiEnabled()) {
						Log.info(TAG, "wlan not enable ,return -1");
						// return false;
						bret = -1;
					} else if (ConnectivityManager.TYPE_WIFI != net.getType()) {
						Log.error(TAG, "wifi not active , return -1");
//						return false;
						bret = -1;
					}
				} else {
					bret = net.getType();
				}

			}
		}

		return bret;
	}
	public static String getDevid() {
		// 0 for wifi only set
//		String status = new EbenSyncConfig().getString(
//				AndroidConfiguration.KEY_DEV_ID, "");

//		return status;
		return "";

	}

	public static boolean isShowSyncing() {
		// 0 for show set
//		String status = new EbenSyncConfig().getString(
//				AndroidConfiguration.KEY_NOTIFY_SYNC_STATUS, "0");
//
//		if ("0".equals(status)) {
//			return true;
//		}

		return false;

	}

	public static long getFileSizes(File f) {
		return f.length();
		// long s = EdiskSyncSource.MAX_FILE_SIZE;
		// if (f.exists()) {
		// FileInputStream fis = null;
		// try {
		// fis = new FileInputStream(f);
		// s = fis.available();
		// fis.close();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// return s;
	}
	
	
    private static String getError(Context context)
    {
        String s1 = "";
        try
        {
            String s2 = context.getPackageName();
            String s3 = "";
            boolean flag = false;
            boolean flag1 = false;
            ArrayList arraylist = new ArrayList();
            arraylist.add("logcat");
            arraylist.add("-d");
            arraylist.add("-v");
            arraylist.add("raw");
            arraylist.add("-s");
            arraylist.add("AndroidRuntime:E");
            arraylist.add("-p");
            arraylist.add(s2);
            Process process = Runtime.getRuntime().exec((String[])arraylist.toArray(new String[arraylist.size()]));
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            for(String s4 = bufferedreader.readLine(); s4 != null; s4 = bufferedreader.readLine())
            {
                if(s4.indexOf("thread attach failed") < 0)
                    s3 = (new StringBuilder()).append(s3).append(s4).append('\n').toString();
                if(!flag1 && s4.toLowerCase().indexOf("exception") >= 0)
                    flag1 = true;
                if(!flag && s4.indexOf(s2) >= 0)
                    flag = true;
            }

            if(s3.length() > 0 && flag1 && flag)
                s1 = s3;
            try
            {
                Runtime.getRuntime().exec("logcat -c");
            }
            catch(Exception exception1)
            {
                Log.error("MobclickAgent", "Failed to clear log in exec(logcat -c)", exception1);
            }
        }
        catch(Exception exception)
        {
            Log.error("MobclickAgent", "Failed to catch error log in catchLogError", exception);
        }
        return s1;
    }
}
