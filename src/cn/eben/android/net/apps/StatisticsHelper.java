package cn.eben.android.net.apps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.util.Log;

import cn.eben.android.net.HttpJSONserviceConst;

/**
 * used for statistics feature of sync cloud.
 * @author jason
 *
 */
public class StatisticsHelper {
	private final static String TAG = "StatisticsHelper";

	
	private static StatisticsHelper instance = new StatisticsHelper();
	
	public static synchronized StatisticsHelper i() {
         return instance;
	}  
	
	/**
	 *  {op:xx,username:xx,devid:xx}
	 * @return
	 */
	public JSONObject readyData() throws JSONException {
		String username = App.i().getAppInitializer().getUserName();
		String devId = App.i().getAppInitializer().getConfiguration().getDeviceConfig().getDevID();
		JSONObject result = new JSONObject();
		result.put("op",HttpJSONserviceConst.OP_STATISTICS_STATUS_REQUESTION);
		JSONObject data = new JSONObject();
		data.put("username",username);
		data.put("devid",devId);
		
		result.put("data", data);
		
		return result;
	}
	/**
	 * * * 
	 * * {op:xx,data:{space:xx,lstime:xx,apps:[{notepad:{usedspace:xx,counts:xx},{cardname:{usedspace:xx,counts:xx},{calendar:{usedspace:xx,counts:xx}}}}

	 * @param jo
	 * @throws JSONException 
	 */
	public void showStatInfo( JSONObject jo ) {
		Log.error(TAG, "showStatInfo....");
		AndroidConfiguration Configuration = App.i().getAppInitializer().getConfiguration();

//		Configuration.setStatistics(jo.toString());
		Configuration.save();
	}
}
