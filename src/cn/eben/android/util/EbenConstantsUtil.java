package cn.eben.android.util;

import android.R.integer;
import android.net.Uri;

public class EbenConstantsUtil {
	/**

	 */
	public static final int MSG_RESET = 1;
	public static final int MSG_SYNC = 2;
	public static final int MSG_ALL_SUNC = 3;
	
	public static final int MSG_QUIT = 4;
	
	public static final int MSG_DELAY = 100;
	
	public static final int MSG_VERSION = 101;
	
	public static final int MSG_SD_CARD = 102;
	
	/**

	 */
	public static final int EBEN_CHECK_SD = 102;
	
	public static final int MSG_SAVE_CONFIGURATION= 103;
	
	public static final int MSG_SAVE_CONFIGURATION_SUCCESS= 104;
	
	/**

	 */
	public static final int MSG_SAVA = 500;
	
	/**
	 */
	public static final int EBEN_RESULT_OK_ACCOUNT = 11;
	
	/**
	 */
	public static final int EBEN_RESULT_DOWNLOAD = 10;
	/**
	 */
	public static final int EBEN_RESULT_PASSWORD_CONFIRM = 12;

	/**
	 * return from progress dialog 
	 */
	public static final int EBEN_RESULT_OK_PROGDLG = 255;
	
	/**
	 */
	public static final int EBEN_HTTP_OK = 200;
	/**
	 */
	public static final int EBEN_HTTP_LOSS = 406;
	/**
	 */
	public static final int EBEN_HTTP_PASSWORD_ERROR = 408;
	/**
	 */
	public static final int EBEN_HTTP_INVALID = 409;
	/**
	 */
	public static final int EBEN_HTTP_ACTIVATE_DELETE = 411;
	/**
	 */
	public static final int EBEN_HTTP_UNACTIVATE = 407 ;
	/**
	 */
	public static final int EBEN_HTTP_NO_USER = 404;
	/**
	 */
	public static final int EBEN_HTTP_PARA_NULL = 401;
	
	/**
	 * 
	 */
	public static int SDCARD_MOUNTED = 1;
	public static int SDCARD_UNMOUNTED = 2;
	
	/**
	 */
	public static final int EBEN_UPDATA_UI = 501;
	
	/**
	 */
	public static String DEFAULT_CAPACITY = "0K";
	/**
	 */
	public static String DEFAULT_TOTAL_CAPACITY = "0G";
	/**
	 */
	public static final String DEFAULT_AUTHOR = "{\"status\":\"200\"}";
	public static final String DEFAULT_STATIICS = "{\"lstime\":\"1318769417164\",\"spaces\":\"2G\",\"apps\":{\"notepad\":{\"counts\":\"0\",\"usedspace\":\"0M\"},\"cardname\":{\"counts\":\"0\",\"usedspace\":\"0K\"},\"calendar\":{\"counts\":\"0\",\"usedspace\":\"0K\"}}}";
	public static int DEFAULT_NOTEPAD_COUNT = 0;
	/**
	 */
	public static String DEFAULT_PERCENT = "0";
	
	public static final String SYNC_PROGRESS_SCREEN_NAME = "cn.eben.android.activities.AndroidSyncProgressScreen";
	public static final String SYNC_HOME_SCREEN_NAME = "cn.eben.android.activities.AndroidHomeScreen";	
	public static final String SYNC_RECOVER_SCREEN_NAME = "cn.eben.android.activities.AndroidRecoverActivity";	
    public static final String SOURCEINDEX = "source_index";
    
    //feedback cloud status
    public static final String CLOUD_START = "cloud_start";
    public static final String CLOUD_CLOSE = "cloud_close";
    public static final String CLOUD_ACTIVE = "activate_create";
    public static final String CLOUD_LOGIN = "activate";
    public static final String CLOUD_ENTRY = "cloud_from";
    
    public static final String CLOUD_SYNC = "cloud_sync";    
    public static final String ECLOUD_SYNC_SUMMERY = "cloud_sync_summery";
    public static final String ECLOUD_UPLOAD = "cloud_upload";
    public static final String ECLOUD_DOWNLOAD = "cloud_restore";
    
    public static final int EBEN_MODE_SYNC = 0;
    public static final int EBEN_MODE_DOWNLOAD = 1;
    public static final int EBEN_MODE_UPLOAD = 2;
    //feedback cloud status end
    
	 // external app status
	
	 
	     public static final String UPGRADE_APP_NAME = "upgrade_app_name";
    public static final String NOTEPAD_PK = "com.ebensz.notepad";
    public static final String CARDNAME_PK = "com.ebensz.cardname";
    public static final String CALENDAR_PK = "com.ebensz.calendar";
    
    public static final int  MSG_DIALOG_NO_USER = 20000;
    
    public static  final String AUTHORITY = "cn.eben.activation4";
    public static  final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    public static final int  MSG_DIALOG_ACCOUNT_CHANGE= 20001;
    public static final int  MSG_DIALOG_NETWORK_EXCEPTION = 20002;
    public static final int  MSG_DIALOG_RECOVER_ID = 20003;
    public static final int MSG_DIALOG_NONETWORK = 10010;
    public static final int MSG_DIALOG_REDUCTION_ID = 10011;
    public static final int MSG_DIALOG_ACTIVATION = 10012;
    public static final int MSG_DIALOG_START_PROGRESSBAR = 10013;
    public static final String EBEN_SYNC_DIRECTION = "SYNC_DIRECTION";
    
    public static final String fontPrefix = "<font color='red'>";
    public static final String fontSuffix = "</font>";
    
    public static final  int NOTEPAD_VERSIONCODE = 99;
    public static final  int CARDNAME_VERSIONCODE = 1;
    public static final  int CALENDAR_VERSIONCODE = 1;
    
    public static final String NOTEPAD_DOWN_URL= "http://update.eben.cn/t4/notepad.apk";
    public static final String CARDNAME_DOWN_URL= "http://update.eben.cn/t4/cardname.apk";
    public static final String CALENDAR_DOWN_URL= "http://update.eben.cn/t4/EbenCalendar.apk";
    
}
