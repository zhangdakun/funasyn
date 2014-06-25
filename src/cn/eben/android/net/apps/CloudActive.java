package cn.eben.android.net.apps;

import org.json.JSONException;
import org.json.JSONObject;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.util.Log;

import cn.eben.android.net.HttpJSONServiceBase;

public class CloudActive extends HttpJSONServiceBase {

	public int code;
	private AndroidConfiguration configuration;


	@Override
	public JSONObject processRequestJSON() throws Exception {
		// TODO Auto-generated method stub
		JSONObject jObject = new JSONObject();
		configuration = App.i().getAppInitializer().getConfiguration();
			try {
				// {"op":"1002","data":{"username":"XXX","isenable":"1"}}
				JSONObject jparam = new JSONObject();
				jObject.put("ver", 1);
				jObject.put("op", "setcloudenable");
				jObject.put("username", configuration.getUsername());
				String devid = configuration.getDeviceConfig().getDevID();
//				if(devid.startsWith("pc-"))
//					devid = devid.substring("pc-".length());
				jObject.put("device", devid);
				jparam.put("enabled", true);
				jObject.put("param", jparam);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			return jObject;
	}

	@Override
	public void processResponesJSON(String result) throws Exception {
		// TODO Auto-generated method stub
		JSONObject jo  ;
		try {
			jo = new JSONObject(result);
			code = jo.getInt("code");
			if (0 != code) {
				Log.error("sync", "enable failed");
				return;
			}
		}catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
	}

	@Override
	public String getRequestURL() {
		// TODO Auto-generated method stub
		return DS_ECSI_URI;
	}

}
