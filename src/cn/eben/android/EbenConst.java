package cn.eben.android;

public class EbenConst {

	/** define the id for sync source. */
	public static final int EBEN_NOTEPAD_BOOK_ID = 512;
	public static final int EBEN_NOTEPAD_PAGE_ID = EBEN_NOTEPAD_BOOK_ID * 2;
	public static final int EBEN_EDISK_ID = EBEN_NOTEPAD_PAGE_ID * 2;
	public static final int EBEN_CARDNAME_GROUP_ID = EBEN_EDISK_ID * 2;
	public static final int EBEN_CARDNAME_CARD_ID = EBEN_CARDNAME_GROUP_ID * 2;
	public static final int EBEN_CARDNAME_DATA_ID = EBEN_CARDNAME_CARD_ID * 2;
	public static final int EBEN_CAL_CALENDAR_ID = EBEN_CARDNAME_DATA_ID * 2;
	public static final int EBEN_CAL_ALARM_ID = EBEN_CAL_CALENDAR_ID * 2;
	public static final int EBEN_ENOTE_ID = EBEN_CAL_ALARM_ID * 2;
	public static final int EBEN_EWRITER_ID = EBEN_ENOTE_ID * 2;
	public static final int EBEN_EDRAWER_ID = EBEN_EWRITER_ID * 2;
	public static final int EBEN_ENETCLIP_ID = EBEN_EDRAWER_ID * 2;
	public static final int EBEN_BOOKMARK_ID = EBEN_ENETCLIP_ID * 2;
	public static final int EBEN_IMAGE_ID = EBEN_BOOKMARK_ID * 2;

	/** define the MIME type for sync source. */
	public static final String EBEN_BOOK_TYPE = "text/plain";
	public static final String EBEN_PAGE_TYPE = "text/plain";
	public static final String EBEN_CARDNAME_GROUP_TYPE = "text/plain";
	public static final String EBEN_CARDNAME_CARD_TYPE = "text/plain";
	public static final String EBEN_CARDNAME_DATA_TYPE = "text/plain";
	public static final String EBEN_CAL_CALENDAR_TYPE = "text/plain";
	public static final String EBEN_CAL_ALARM_TYPE = "text/plain";
	
	public static final int EBEN_OVERVIEW_TAB_INDEX_NOTEPAD = 1;
	public static final int EBEN_OVERVIEW_TAB_INDEX_CALENDAR = 2;
	public static final int EBEN_OVERVIEW_TAB_INDEX_CARDNAME = 3;
	
	public static final int PUSH_PORT = 4745;
//	public static final String HOST = "42.120.48.112";
//	public static final String HOST = "42.120.17.218";
	public static final String HOST = "sync.eben.cn";
	
//	public static final String HOST = "42.121.96.124";
//	public static final String HTTP_HOST = "http://sync02.eben.cn/";
//	public static final String HTTP_HOST = "http://sync.eben.cn/";
//	public static final String HTTP_HOST = "http://42.121.96.124/";
//	public static final String HTTP_HOST = 	"http://110.76.46.18/";
	
	public static final String HTTP_HOST = "http://"+HOST+"/";

//    public static final String HTTP_HOST = "http://42.120.17.218:80/";
//	 public static final String HTTP_HOST = "http://192.168.5.10/";
//	public static final String HTTP_HOST = "http://192.168.2.26:8080/";
//	public static final String HTTP_HOST = "http://192.168.4.111:8080/";
//	public static final String HTTP_HOST = "http://42.121.96.124/";
//    public static final String HTTP_HOST = 	"http://192.168.4.98:8080/";
//	public static final String HTTP_HOST = "http://192.168.4.50/";
//	public static final String HTTP_HOST = "http://192.168.4.155:8080/";	
//	public static final String HTTP_HOST = "http://192.168.6.252/";
//	public static final String HTTP_HOST = "http://192.168.4.61:8080/";

	 public static final String formatData = "yyyy/MM/dd HH:mm";
}
