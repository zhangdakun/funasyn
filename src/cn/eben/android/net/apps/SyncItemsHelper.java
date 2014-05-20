package cn.eben.android.net.apps;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.eben.android.net.HttpJSONServiceBase;
import cn.eben.android.net.HttpJSONserviceConst;

import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.util.Log;

public class SyncItemsHelper extends HttpJSONServiceBase {
    private final String TAG = "SyncItemsHelper";
    private final String source;
    
    public static long update=0;
    public static long add=0;;
    public static long delete=0;;

    public SyncItemsHelper(String source) {
    	update = 0;
    	add = 0;
    	delete = 0;
	this.source = source;
    }

    // {"data":{"username":"15810388882","device":"F2718SBSH0000679","source":["edisk"]},"op":901}
    // send
    // {"data":{"username":"15810388882","device":"F2718SBSH0000679","source":["edisk"]},"op":901}
    //
    // {"op":901,"data":{"edisk":{"update":0,"listupdate":[],"delete":0,"add":0,"listadd":[],"listdelete":[]}}}
    @Override
    public JSONObject processRequestJSON() throws Exception {
    	update = 0;
    	add = 0;
    	delete = 0;
	AppInitializer initializer = App.i().getAppInitializer();
	String username = initializer.getUserName();
	String devId = initializer.getConfiguration().getDeviceConfig()
		.getDevID();
//	String pwd = initializer.getPassword();

	JSONObject result = new JSONObject();
	JSONObject data = new JSONObject();

	data.put("username", username);

	data.put("device", devId);
	JSONArray ja = new JSONArray();
	ja.put(source);
	data.put("source", ja);

	result.put("op", HttpJSONserviceConst.OP_SYNC_LIST);
	result.put("data", data);

	return result;
    }
//    {"op":901,"data":{"enote_netclip":{"update":0,"listupdate":[],"delete":0,"add":0,"listadd":[],"listdelete":[]}}}
    @Override
    public void processResponesJSON(String json) throws Exception {
	// TODO Auto-generated method stub
	Log.info(TAG, "processResponesJSON");
	
	JSONObject result = new JSONObject(json);
	JSONObject data = result.getJSONObject("data").getJSONObject(source);
	
	update = data.getLong("update");
	add = data.getLong("add");
	
	delete = data.getLong("delete");
	
	Log.info(TAG, "update : "+update+", add : "+add+", delete: "+delete);
	
	

    }

    @Override
    public String getRequestURL() {
	// TODO Auto-generated method stub
	return HttpJSONserviceConst.DS_URI;
    }

}
