package cn.eben.android.net;

import cn.eben.android.EbenConst;



public interface HttpJSONserviceConst {

    // here for operation define.
    public static final int OP_EDISK_SET_RENAME = 400;

    public static final int OP_NOTEPAD_MERGE_BOOK = 700;
    public static final int OP_NOTEPAD_ADJUST_PN = 701;
    public static final int OP_NOTEPAD_PULL_SYNCED_BOOK = 702;
    public static final int OP_CALENDAR_DEL_ALARM = 750;

    public static final int OP_SYNC_STATUS_PUSH = 800;
    public static final int OP_SYNC_STATUS_PULL = 801;
    public static final int OP_STATISTICS_STATUS_REQUESTION = 900;
    public static final int OP_SYNC_LIST = 901;

    public static final int OP_EBEN_SETTING_POST = 1000;
    public static final int OP_EBEN_SETTING_RECOVERTY = 1001;
    public static final int OP_EBEN_ENANALE_RECOVERTY = 1003;

    public static final int OP_UC_AUTHEN_AUTHOR = 10000;

    // here for server url define.
    public static final String DS_URI = EbenConst.HTTP_HOST
	    + "funambol/DSJSONServlet";
    public static final String UC_USERINFO_URI = "http://passport.eben.cn/userapp/userinfoterminalapi/";
//    public static final String DS_ECSI_URI = "http://42.121.96.124/funambol/ecsi/device";
    
    public static final String DS_ECSI_URI = EbenConst.HTTP_HOST +"funambol/ecsi/device";

    // here define status result
    public static final int RESULT_OK = 0;
    
//    public static final String JSONVERSION = "1";
    public static final int JSONVERSION = 2;
    
    public static final String OP_EBEN_CLOUDINFO="getcloudinfo";// op 1003
    public static final String OP_EBEN_UPFILE="upfileauth";
    public static final String OP_EBEN_DOWNFILE="downfileauth";
    
    public static final String OP_EBEN_APPSINFO = "getappsinfo";
    
    public static final String[] EBEN_APPS = {"edisk","enote_notepad","enote_writer","enote_drawer","enote_netclip"};

}
