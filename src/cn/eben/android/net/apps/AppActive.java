package cn.eben.android.net.apps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.App;
import com.funambol.util.Log;

import cn.eben.android.net.HttpJSONServiceBase;

public class AppActive  extends HttpJSONServiceBase {

	private AndroidConfiguration configuration;
	public int code = 1;

	@Override
	public JSONObject processRequestJSON() throws Exception {
		// TODO Auto-generated method stub
		JSONObject jObject = new JSONObject();
//		String prefix = ((AndroidCustomization)customization).getDeviceIdPrefix()
		configuration = App.i().getAppInitializer().getConfiguration();

			try {

				jObject.put("ver", 1);
				jObject.put("username", configuration.getUsername());
				String devid = configuration.getDeviceConfig().getDevID();
//				if(devid.startsWith("pc-"))
//					devid = devid.substring("pc-".length());
				jObject.put("device", devid);
				jObject.put("op", "setappsenable");

				JSONArray jApps = new JSONArray();
				
					JSONObject jo = new JSONObject();
//					jo.put("app", "ephoto");//bphoto
					jo.put("app", "bphoto");
					jo.put("enabled", true);
					jApps.put(jo);
				

				jObject.put("param", jApps);

			} catch (JSONException e) {
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
