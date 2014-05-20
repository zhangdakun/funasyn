package cn.eben.android.net.apps;

import org.json.JSONObject;

public class SyncStatushelper {
	private final static String TAG = "NotePadHelper";
	private static final String TAG_LOG = "NotePadHelper";
	
	private static SyncStatushelper instance = new SyncStatushelper();
	
	public static synchronized SyncStatushelper i() {
         return instance;
	}  
    /**
     * json:{op:xx:data:{username:xx,devid:xxxxx,ebensyncdb:xx}};
     * @param json
     * @return
     * result :
     * {op:xx,data:{status:0} or {op:xx,data:{status:0}
     */
	public JSONObject push() {
		JSONObject result = null;
		
		
		return result;
	}
	/**
     * json: {op:xx,data:{username:xx,devid:xx}}
     * @param json
     * @return
     * {op:xx,data:{ebensyncdb:xx}
     */
	public JSONObject pull() {
		JSONObject result = null;
		
		
		return result;
		
	}
	
	public void  pushBack(JSONObject json) {
		
		
	}
	
	public void  pullBack(JSONObject json) {
		
		
	}
}
