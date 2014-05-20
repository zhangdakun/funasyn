package cn.eben.android.net.apps;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.sync.SyncException;
import com.funambol.util.Log;

import cn.eben.android.net.HttpJSONServiceBase;
import cn.eben.android.util.EbenHelpers;


public class EbenDownFileAuth extends HttpJSONServiceBase {

	private String uri;
	private final String TAG_LOG = "EbenDownFileAuth";

	private Hashtable<String, String> downList = null;

	public EbenDownFileAuth(String uri) {

		this.uri = uri;
	}

	public Hashtable<String, String> getDownList() {
		if (null == downList) {
			downList = new Hashtable<String, String>();
		}
		return downList;
	}

	public void setDownList(Hashtable<String, String> downList) {
		this.downList = downList;
	}

	// {"ver":1,"username":"<userid>","device":"<deviceid>","op":"downfileauth","param":[

//	 {"result":"ok","data":[{"result":"ok","durl":"aHR0cDovL3N0b3JhZ2UuYWxpeXVuLmNvbS9wcm9kdWN0LXRlc3QvMTUyMDE1OTg5NDUvMDE5MThCMzI3ODJGRUFBOEE1QUMyNEMxNTc4NzVGMzgtMUY5NTVBP0V4cGlyZXM9MTM1MzA0NjcwMSZPU1NBY2Nlc3NLZXlJZD1na3J0YWdjemFscmM1dXd2ajQzbm56anUmU2lnbmF0dXJlPVFDeW5kNGl3Nnd1cFVteElmUk90aDRUQ0dXRSUzRA==",
//		 "uri":"OSS://product-test/15201598945/01918B32782FEAA8A5AC24C157875F38-1F955A"}],"code":0}
	@Override
	public JSONObject processRequestJSON() throws Exception {
		downList = new Hashtable<String, String>();
		// TODO Auto-generated method stub
		AppInitializer initializer = App.i().getAppInitializer();
		String username = initializer.getUserName();
		String devId = initializer.getConfiguration().getDeviceConfig()
				.getDevID();
		
		if(null == devId || "".equals(devId)
				|| null == username || "".equals(username)) {
			throw new SyncException(10000,"null devid or username");
		}
		
		JSONObject jo = new JSONObject();
		jo.put("ver", JSONVERSION);

		jo.put("username", username);
		jo.put("device", devId);
		jo.put("op", OP_EBEN_DOWNFILE);

		JSONObject jApp = new JSONObject();
		jApp.put("uri", uri);

		// JSONObject japps = new JSONObject();

		JSONArray jArrayApp = new JSONArray();
		jArrayApp.put(jApp);

		jo.put("param", jArrayApp);

		return jo;
	}

	// {"code":0, "result":"ok","data":[
	// {"uri":"<URI>","result":"ok","ossurl":"<URL>"},


	private int lastError = 0;
	public int getLastErr() {
		return lastError;
	}
	@Override
	public void processResponesJSON(String json) throws Exception {
		// TODO Auto-generated method stub
		Log.info(TAG_LOG, "processResponesJSON : " + json);

		JSONObject jo = new JSONObject(json);
		if (0 != jo.getLong("code")) {
			Log.info(TAG_LOG, "error code!!");
			lastError = (int) jo.getLong("code");
			return;
		}

		JSONArray jData = jo.getJSONArray("data");

		for (int i = 0; i < jData.length(); i++) {
			String result = jData.getJSONObject(i).getString("result");
			if (!"ok".equalsIgnoreCase(result)) {
				int code = jData.getJSONObject(i).getInt("code");
				lastError = code;
				return;
			}
			String ossurl = EbenHelpers.decodeKey(jData.getJSONObject(i).getString("durl"));
			String uri = jData.getJSONObject(i).getString("uri");
			addlist(uri, ossurl);
		}
	}

	private void addlist(String uri, String ossurl) {
		// TODO Auto-generated method stub
		if (null == downList) {
			downList = new Hashtable<String, String>();
		}
		if (!downList.containsKey(uri)) {
			downList.put(uri, ossurl);
		}
	}

	@Override
	public String getRequestURL() {
		// TODO Auto-generated method stub
		return DS_ECSI_URI;
	}

}
