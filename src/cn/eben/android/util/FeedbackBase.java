package cn.eben.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class FeedbackBase {
	
	public static final String DB_TABLE_STATISTICS = "StatisticsInfo";

	public final static String AUTHORITY = "cn.eben.agent4.provider.AgentProvider";
	public final static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE_STATISTICS);
	
	public static class StatisticsInfoTable  {

		public static final String PACKAGE = "package";

		public static final String VERSION = "version";

		public static final String TIME = "time";

		public static final String EVENT = "event";

		public static final String PARAM1 = "parameter1";
		public static final String PARAM2 = "parameter2";
	}

	public class StatisticsStruct{
		String packageName;
		int version;
		long time;
		String event;
		String param1;
		String param2;
	}

	
	public void insertData(Context context, StatisticsStruct statistics) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(StatisticsInfoTable.PACKAGE, statistics.packageName);
		initialValues.put(StatisticsInfoTable.VERSION, statistics.version);
		initialValues.put(StatisticsInfoTable.TIME, statistics.time);
		initialValues.put(StatisticsInfoTable.EVENT, statistics.event);
		initialValues.put(StatisticsInfoTable.PARAM1, statistics.param1);
		initialValues.put(StatisticsInfoTable.PARAM2, statistics.param2);
		
		try {
			context.getContentResolver().insert(AUTHORITY_URI, initialValues);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void insertData(Context context, String event, String param1, String param2) {
		ContentValues initialValues = new ContentValues();
		int iVer = 1;
		PackageManager pm = context.getPackageManager(); 
        PackageInfo pi = null;
        try {
			pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if(null != pi) {
//			strVer = String.format(" %s build %d", pi.versionName, pi.versionCode);
			iVer = pi.versionCode;
		}

		initialValues.put(StatisticsInfoTable.PACKAGE, context.getPackageName());
		initialValues.put(StatisticsInfoTable.VERSION, iVer);
		initialValues.put(StatisticsInfoTable.TIME, System.currentTimeMillis());
		initialValues.put(StatisticsInfoTable.EVENT, event);
		initialValues.put(StatisticsInfoTable.PARAM1, param1);
		initialValues.put(StatisticsInfoTable.PARAM2, param2);
		try {
			context.getContentResolver().insert(AUTHORITY_URI, initialValues);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
