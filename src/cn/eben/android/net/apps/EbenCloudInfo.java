package cn.eben.android.net.apps;

import org.json.JSONObject;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.sync.SyncException;
import com.funambol.util.Log;

import cn.eben.android.net.HttpJSONServiceBase;

public class EbenCloudInfo extends HttpJSONServiceBase {
    private final String TAG_LOG = "EbenCloudInfo";

    // {"ver":1,"username":"<userid>","device":"<deviceid>","op":"getcloudinfo"}
    @Override
    public JSONObject processRequestJSON() throws Exception {
	// TODO Auto-generated method stub
//	AppInitializer initializer = App.i().getAppInitializer();
//	String username = initializer.getUserName();
//	String devId = initializer.getConfiguration().getDeviceConfig()
//		.getDevID();
//	
//	if(null == devId || "".equals(devId)
//			|| null == username || "".equals(username)) {
//		throw new SyncException(10000,"null devid or username");
//	}
	
	JSONObject jo = new JSONObject();
//	jo.put("ver", JSONVERSION);
//
//	jo.put("username", username);
//	jo.put("device", devId);
//	jo.put("op", OP_EBEN_CLOUDINFO);

	return jo;

    }

//    {"code":0, "result":"ok","data":{
//	"enabled":true,
//	"btime":1332470458720,
//	"etime":1332470678720,
//	"spaces":1024,
//	"leftday":365}}

//    {"result":"ok",
//    "data":{"spaces":2147483648,"enabled":true,"leftday":273,"etime":1375849368886,"btime":1344313368886},
//    "code":0}

    @Override
    public void processResponesJSON(String json) throws Exception {
	// TODO Auto-generated method stub
	Log.debug(TAG_LOG, "processResponesJSON : " + json);
//	if (!OP_EBEN_CLOUDINFO.equals(json)) {
//	    Log.error(TAG_LOG, "op not matched ,error !!");
//	}
	int code = -1;
	JSONObject jo = new JSONObject(json);
	code = jo.getInt("code");
	if( 0 != code) {
		return;
	}
	App.i().getAppInitializer().getConfiguration()
		.saveStringKey(AndroidConfiguration.KEY_MINDCLOUD_STATUS, json);
	App.i().getAppInitializer().getConfiguration().commit();

    }

    @Override
    public String getRequestURL() {
	// TODO Auto-generated method stub
//	return EbenConst.HTTP_HOST + "/ecsi/device";
//	return "http://42.120.48.113/ecsi/device";
//	return "http://42.121.96.124/funambol/ecsi";
    	return DS_ECSI_URI;
    }

}
