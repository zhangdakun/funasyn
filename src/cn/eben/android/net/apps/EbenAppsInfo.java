package cn.eben.android.net.apps;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.sync.SyncException;

import cn.eben.android.net.HttpJSONServiceBase;


public class EbenAppsInfo extends HttpJSONServiceBase {

    // {"ver":1,"username":"<userid>",
    // "device":"<deviceid>","op":"getappsinfo","param":{

//	{"ver":1,"username":"<userid>", "device":"<deviceid>","op":"getappsinfo"}
    @Override
    public JSONObject processRequestJSON() throws Exception {
	// TODO Auto-generated method stub
//	AppInitializer initializer = App.i().getAppInitializer();
//	String username = initializer.getUserName();
//	String devId = initializer.getConfiguration().getDeviceConfig()
//		.getDevID();
	JSONObject jo = new JSONObject();
//	jo.put("ver", JSONVERSION);
//	
//	if(null == devId || "".equals(devId)
//			|| null == username || "".equals(username)) {
//		throw new SyncException(10000,"null devid or username");
//	}
//	
//	jo.put("username", username);
//	jo.put("device", devId);
//	jo.put("op", OP_EBEN_APPSINFO);

//	JSONArray jApps = new JSONArray();
//	for (String app : EBEN_APPS) {
//	    jApps.put(app);
//	}
//
//	jo.put("param", jApps);
	return jo;
    }

//    {"code":0, "result":"ok","data":[
//    {"app":"notepad","enabled":true,"lstime":13498765907,"usedspace":123},
//    {"app":"writer","enabled":true,"lstime":13498765907,"usedspace":123},
//    {"app":"drawer","enabled":false,"lstime":13498765907,"usedspace":123},

//    {"result":"ok","data":
//    [{"enabled":true,"app":"bookmark","lstime":0,"usedspace":0},
//     {"enabled":true,"app":"ebookmark","lstime":1350219415548,"usedspace":4050799},
//     {"enabled":true,"app":"edisk","lstime":1350271207739,"usedspace":100427776},
//     {"enabled":true,"app":"eimage","lstime":0,"usedspace":0},
//     {"enabled":true,"app":"enote_drawer","lstime":1352185411887,"usedspace":14960484},
//     {"enabled":true,"app":"enote_netclip","lstime":1352185416157,"usedspace":56250845},
//     {"enabled":true,"app":"enote_notepad","lstime":1352188681610,"usedspace":20945133},
//     {"enabled":true,"app":"enote_writer","lstime":1352185405344,"usedspace":3510532},
//     {"enabled":true,"app":"ephoto","lstime":0,"usedspace":0}],"code":0}
    @Override
    public void processResponesJSON(String json) throws Exception {
	// TODO Auto-generated method stub
    	
//    	ArrayList<String> apps = new ArrayList(); 
//    	for(String app:Constants.itemKey) {
//    		if(json.contains(app)) {
//    			apps.add(app);
//    		}
//    	}
//    	
    	JSONObject jo = new JSONObject(json);
//    	
//    	JSONArray jarray = jo.getJSONArray("data");
//    	
//    	for(int i=0;i<jarray.length();i++) {
////    		JSONObject jApp = jarray.
//    		S
//    	}
    	int code = -1;
    	code = jo.getInt("code");
    	if( 0 != code) {
    		return;
    	}
		AndroidConfiguration Configuration = App.i().getAppInitializer().getConfiguration();

//		Configuration.setStatistics(json);
		Configuration.save();
		
    }

    @Override
    public String getRequestURL() {
	// TODO Auto-generated method stub
//	return "http://42.120.48.113/funambol/ecsi";
	
//	return "http://42.121.96.124/funambol/ecsi";
	return DS_ECSI_URI;
    }

}
