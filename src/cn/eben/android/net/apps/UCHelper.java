package cn.eben.android.net.apps;

import org.json.JSONException;
import org.json.JSONObject;

import cn.eben.android.net.HttpJSONserviceConst;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.util.Base64;

/**
 * used for user center Certification.
 * @author jason
 *
 */
public class UCHelper {

	private final static String TAG = "UCHelper";
	private static final String TAG_LOG = "UCHelper";
	
	private static UCHelper instance = new UCHelper();
	
	public static synchronized UCHelper i() {
         return instance;
	} 
	
	/**
	 * 
	 * @return
	 *  json:{op:xx,data"{username:xx,pwd:xx,devid:xx}}
	 */
	public JSONObject authen( ) {
		AppInitializer initializer = App.i().getAppInitializer();
		String username = initializer.getUserName();
		String devId = initializer.getConfiguration().getDeviceConfig().getDevID();
		String pwd = initializer.getPassword();
				

			JSONObject result = new JSONObject();
			JSONObject data = new JSONObject();
			try {
				data.put("username",username);
//				data.put("pwd", pwd);
				data.put("pwd",new String(Base64.encode(pwd.getBytes())));
				data.put("devid", devId);
				
				result.put("op", HttpJSONserviceConst.OP_UC_AUTHEN_AUTHOR);
				result.put("data", data);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		return result;
	}
	
	/**
	 * json:{op:xx,data:{status:xx}}
	 * @param json
	 */
	public void author(JSONObject json) {
		AndroidConfiguration Configuration = App.i().getAppInitializer().getConfiguration();

//		Configuration.setAuthor(json.toString());
		Configuration.save();	

	}
}
