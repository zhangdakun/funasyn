package cn.eben.android.net.apps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import cn.eben.android.net.HttpJSONserviceConst;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.client.localization.Localization;

/**
 * used for setting security.
 * 
 * @author jason
 * 
 */
public class SettingHelper {
    private final static String TAG = "SettingHelper";

    private static SettingHelper instance = new SettingHelper();
    private boolean saveSucess = false;

    private String appString;

    public static synchronized SettingHelper i() {
	return instance;
    }

    public void setAppSettings(String apps) {
	this.appString = apps;

    }

    /**
     * {op:xx, data:{ username:xx, devid:xx,apps:[
     * {app:notepad,visible:xx,issync:xx}, {app:cardanme,visible:xx,issync:xx},
     * {app:calendar,visible:xx,issync:xx} ] } }
     * 
     * @param json
     * @return
     */
    public JSONObject readyData() throws JSONException {
	String username = App.i().getAppInitializer().getUserName();
	String devId = App.i().getAppInitializer().getConfiguration()
		.getDeviceConfig().getDevID();

	// Controller controller = App.i().getAppInitializer().getController();
	saveSucess = false;
	// AndroidHomeScreenController homeScreenController =
	// (AndroidHomeScreenController)controller.getHomeScreenController();

	// Initialize the sources listed
	// Vector appSources = homeScreenController.getVisibleItems();
	// Enumeration iter = appSources.elements();

	// JSONArray apps = new JSONArray();
	// while(iter.hasMoreElements()) {
	// AppSyncSource appSource = (AppSyncSource) iter.nextElement();
	// AppSyncSourceConfig sConfig = appSource.getConfig();
	// String appName = convertAppsourceName(appSource.getName());
	// JSONObject jObject = new JSONObject();
	// jObject.put("visible", sConfig.getNetworkSecurity() ? "0" : "1");
	// jObject.put("app", appName);
	// jObject.put("issync", sConfig.getEnabled() ? "0" : "1");
	// apps.put(jObject);
	// }
	
	
//	JSONArray apps = new JSONArray(appString);
	JSONArray apps = new JSONArray();
	JSONObject jObject = new JSONObject();
	jObject.put("visible",  "1");
	jObject.put("issync",  "1");
//	jObject.put("app", ExternalEntryConst.APP_BOOKMARD);
	apps.put(jObject);
	
	
	JSONObject result = new JSONObject();

	result.put("op", HttpJSONserviceConst.OP_EBEN_SETTING_POST);
	JSONObject data = new JSONObject();

	data.put("username", username);
	data.put("devid", devId);

	data.put("apps", apps);

	result.put("data", data);
	return result;

    }

    public JSONObject readyMindcloudStatus() throws JSONException {
	String username = App.i().getAppInitializer().getUserName();
	String devId = App.i().getAppInitializer().getConfiguration()
		.getDeviceConfig().getDevID();

	JSONArray apps = new JSONArray(appString);
	JSONObject result = new JSONObject();

	result.put("op", HttpJSONserviceConst.OP_EBEN_ENANALE_RECOVERTY);
	JSONObject data = new JSONObject();

	data.put("username", username);
	data.put("devid", devId);

	data.put("apps", apps);

	result.put("data", data);
	return result;

    }

    /**
     * {op:xx,data:{status:xx}} status:0 sucess ,and status:1 fail to post.
     * 
     * @param settingStatus
     */
    /**
     * {"op":1000,"data":"{status:0}"} }
     * 
     * @param json
     * @return
     * @throws JSONException
     */
    public void getSettingStatus(JSONObject json) throws JSONException {

	String status = json.getString("status");

	if (HttpJSONserviceConst.RESULT_OK == Integer.parseInt(status)) {
	    saveSucess = true;
	} else {
	    saveSucess = false;
	}

    }

    /**
     * {op:xx, data:{ username:xx, devid:xx } }
     * 
     * @param json
     * @return
     * @throws JSONException
     */

    public JSONObject readyRecoveryData() throws JSONException {
	String username = App.i().getAppInitializer().getUserName();
	String devId = App.i().getAppInitializer().getConfiguration()
		.getDeviceConfig().getDevID();
	// Initialize the sources listed

	JSONObject result = new JSONObject();

	result.put("op", HttpJSONserviceConst.OP_EBEN_SETTING_RECOVERTY);
	JSONObject data = new JSONObject();

	data.put("username", username);
	data.put("devid", devId);

	result.put("data", data);
	return result;
    }

    private String convertAppsourceName(String name) {
	Localization localization = App.i().getAppInitializer()
		.getLocalization();

	if (localization.getLanguage("type_eben_notepad")
		.equalsIgnoreCase(name)) {
	    return "notepad";
	} else if (localization.getLanguage("type_eben_card").equalsIgnoreCase(
		name)) {
	    return "cardname";
	} else if (localization.getLanguage("type_eben_calendar")
		.equalsIgnoreCase(name)) {
	    return "calendar";
	} else if (localization.getLanguage("type_files")
		.equalsIgnoreCase(name)) {
	    return "edisk";
	}
	return "";
    }

    public void getSettingFromSrv(JSONObject json) throws JSONException {

	App.i()
		.getAppInitializer()
		.getConfiguration()
		.saveStringKey(AndroidConfiguration.KEY_MINDCLOUD_SOURCE,
			json.toString());
	App.i().getAppInitializer().getConfiguration().commit();

	// parse json object
	// JSONObject apps = json.getJSONObject("apps");
	//
	// JSONObject joNotepad = apps.getJSONObject("notepad");
	// // String ntVisbile = joNotepad.getString("visible");
	// String ntSync = joNotepad.getString("issync");
	//
	// JSONObject joCardName = apps.getJSONObject("cardname");
	// // String carddVisbile = joCardName.getString("visible");
	// String cardSync = joCardName.getString("issync");
	//
	// JSONObject joCalendar = apps.getJSONObject("calendar");
	// // String calVisbile = joCalendar.getString("visible");
	// String calSync = joCalendar.getString("issync");
	//
	// //set app source config
	// Controller controller = App.i().getAppInitializer().getController();
	// AndroidHomeScreenController homeScreenController =
	// (AndroidHomeScreenController)controller.getHomeScreenController();
	// Localization localization =
	// App.i().getAppInitializer().getLocalization();
	// // Initialize the sources listed
	// Vector appSources = homeScreenController.getVisibleItems();
	// Enumeration iter = appSources.elements();
	//
	// while(iter.hasMoreElements()) {
	// AppSyncSource appSource = (AppSyncSource) iter.nextElement();
	// AppSyncSourceConfig sConfig = appSource.getConfig();
	// String sourceName = convertAppsourceName(appSource.getName());
	// if(sourceName.equalsIgnoreCase("notepad")){
	// // sConfig.setNetworkSecurity(ntVisbile.equals("0"));//
	// sConfig.setEnabled(ntSync.equals("0"));
	// Log.debug(TAG,"vtVisble = "+sConfig.getNetworkSecurity()+" ntSync = "+sConfig.getEnabled());
	// }
	// else if(sourceName.equalsIgnoreCase("cardname")){
	// // sConfig.setNetworkSecurity(carddVisbile.equals("0"));//
	// sConfig.setEnabled(cardSync.equals("0"));
	// Log.debug(TAG,"sdVisble = "+sConfig.getNetworkSecurity()+" sdSync = "+sConfig.getEnabled());
	// }
	// else if(sourceName.equalsIgnoreCase("calendar")){
	// // sConfig.setNetworkSecurity(calVisbile.equals("0"));//
	// sConfig.setEnabled(calSync.equals("0"));
	// Log.debug(TAG,"calVisble = "+sConfig.getNetworkSecurity()+" calSync = "+sConfig.getEnabled());
	// }
	// sConfig.commit();
	// }
    }

    public boolean isSaveSucess() {
	return saveSucess;
    }

    public void savaMindInfo(JSONObject data) {
	// TODO Auto-generated method stub
	App.i()
		.getAppInitializer()
		.getConfiguration()
		.saveStringKey(AndroidConfiguration.KEY_MINDCLOUD_STATUS,
			data.toString());
	App.i().getAppInitializer().getConfiguration().commit();
    }

    public JSONObject readyMind() throws JSONException {
	String username = App.i().getAppInitializer().getUserName();
	String devId = App.i().getAppInitializer().getConfiguration()
		.getDeviceConfig().getDevID();
	// Initialize the sources listed

	JSONObject result = new JSONObject();

	result.put("op", HttpJSONserviceConst.OP_EBEN_ENANALE_RECOVERTY);
	JSONObject data = new JSONObject();

	data.put("username", username);
	data.put("devid", devId);

	result.put("data", data);
	return result;
    }

}
