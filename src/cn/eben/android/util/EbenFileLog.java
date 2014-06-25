package cn.eben.android.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class EbenFileLog {
	public final static String TAG = "ebenFileLog";
	private static OutputStreamWriter writer;
	private static final String logpath = Environment.getExternalStorageDirectory().toString()+ "/EbenLog/log/";
	private static final String logname = "sync.txt";
	private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final int MEMORY_LOG_FILE_MAX_SIZE =  1024*1024; 
	private static final int MEMORY_LOG_FILE_MONITOR_INTERVAL = 10 * 60 * 1000;
	private static final int SDCARD_LOG_FILE_SAVE_DAYS = 30; 

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HHmmss");
//	private static String logFileName = null;

	// private static String
	public static void recordSyncLog(String msg) {
//		Log.d(TAG, "recordLogServiceLog");
		try {
//			if (null == logFileName)
//				logFileName = sdf.format(new Date()) + ".log";
			checkLogDir();
			try {
				writer = new OutputStreamWriter(new FileOutputStream(logpath
						+ logname, true));
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			if (writer != null) {
				try {
					Date time = new Date();
					writer.write(myLogSdf.format(time) + " : " + msg);
					writer.write("\n");
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void checkLogDir() {
		// TODO Auto-generated method stub
		File dir = new File(logpath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File log = new File(logpath + logname);
		
		if(!log.exists()) {
			try {
				log.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (log.exists() && log.length() > MEMORY_LOG_FILE_MAX_SIZE) {
			String logFileName = sdf.format(new Date()) + ".log";
			log.renameTo(new File(logpath + logFileName));
			
		}
		deleteSDcardExpiredLog();
	}

	private static void deleteSDcardExpiredLog() {
//		Log.d(TAG, "deleteSDcardExpiredLog");
		File file = new File(logpath);
		if (file.isDirectory()) {
			File[] allFiles = file.listFiles();
			for (File logFile : allFiles) {
				String fileName = logFile.getName();
				if (logname.equals(fileName)) {
					continue;
				}
				String createDateInfo = getFileNameWithoutExtension(fileName);
				if(null == createDateInfo) {
					continue;
				}
				if (canDeleteSDLog(createDateInfo)) {
					logFile.delete();
					Log.d(TAG, "delete expired log success,the log path is:"
							+ logFile.getAbsolutePath());

				}
			}
		}
	}

	private static String getFileNameWithoutExtension(String fileName) {
//		Log.d(TAG, "getFileNameWithoutExtension");
		if(!fileName.contains(".")) {
			return null;
		}
		return fileName.substring(0, fileName.indexOf("."));
	}

	/**
	 * 
	 * @param createDateStr
	 * @return
	 */
	private static boolean canDeleteSDLog(String createDateStr) {
//		Log.d(TAG, "canDeleteSDLog");
		boolean canDel = false;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1 * SDCARD_LOG_FILE_SAVE_DAYS);
		Date expiredDate = calendar.getTime();
		try {
			Date createDate = sdf.parse(createDateStr);
			canDel = createDate.before(expiredDate);
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage(), e);
//			canDel = false;
			canDel = true;
		}
		return canDel;
	}

}
