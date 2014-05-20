package cn.eben.android.net;

import org.json.JSONException;
import org.json.JSONObject;

import cn.eben.android.net.apps.SettingHelper;
import cn.eben.android.net.apps.StatisticsHelper;
import cn.eben.android.net.apps.SyncStatushelper;
import cn.eben.android.net.apps.UCHelper;

import com.funambol.util.Log;

public class HttpJSONService extends HttpJSONServiceBase {

    private final static String TAG = "DSJSONService";
    private static HttpJSONService instance = new HttpJSONService();

    private int op = -1;
    private String servUrl = null;

    public String getServUrl() {
	return servUrl;
    }

    public void setServUrl(String servUrl) {
	this.servUrl = servUrl;
    }

    public void setOp(int op) {
	this.op = op;
    }

    public int getOp() {
	return op;
    }

    public static synchronized HttpJSONService getInstance() {
	return instance;
    }

    /**
     * json format:{"op":"xx","data":{}}
     */
    @Override
    public JSONObject processRequestJSON() throws Exception {
	JSONObject jo = new JSONObject();

	int op = getOp();
	switch (op) {

	case OP_SYNC_STATUS_PUSH:
	    // TODO:handle the sync stauts requestion from server.
	    try {
		jo = SyncStatushelper.i().push();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_SYNC_STATUS_PULL:
	    // TODO:handle the sync stauts requestion from server.
	    try {
		jo = SyncStatushelper.i().pull();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_STATISTICS_STATUS_REQUESTION:
	    try {
		jo = StatisticsHelper.i().readyData();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_SETTING_POST:
	    try {
		jo = SettingHelper.i().readyData();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_SETTING_RECOVERTY:
	    try {
		jo = SettingHelper.i().readyRecoveryData();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_ENANALE_RECOVERTY:
	    try {
		jo = SettingHelper.i().readyMind();
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_SYNC_LIST:
	    break;

	case OP_UC_AUTHEN_AUTHOR:
	    try {
		jo = UCHelper.i().authen();
	    } catch (Exception e) {
		throw e;
	    }
	    break;

	default:
	    break;
	}

	return jo;
    }

    /**
     * json format:{"op":xx,"data":{}}
     */
    @Override
    public void processResponesJSON(String json) throws Exception {
	// get rsult jsonobject, and then do what you wan to do.

	JSONObject jo = null;
	int op = -1;
	// try {
	jo = new JSONObject(json);
	// op = jo.getInt("op");
	if (null != jo)
	    op = Integer.valueOf(jo.getString("op"));// jo.getString("op");
	// } catch (JSONException e) {
	// Log.info(TAG,"fail to get json from server response.\n" +e);
	// }

	switch (op) {

	case OP_SYNC_STATUS_PUSH:
	    JSONObject ss = null;
	    try {
		ss = new JSONObject(jo.getString("data"));
		SyncStatushelper.i().pushBack(ss);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_SYNC_STATUS_PULL:
	    JSONObject syncdb = null;
	    try {
		syncdb = new JSONObject(jo.getString("data"));
		SyncStatushelper.i().pullBack(syncdb);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_STATISTICS_STATUS_REQUESTION:
	    JSONObject stat = null;
	    Log.error(TAG, "OP_STATISTICS_STATUS_REQUESTION");
	    try {
		stat = new JSONObject(jo.getString("data"));
		StatisticsHelper.i().showStatInfo(stat);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_SETTING_POST:
	    JSONObject settingStatus = null;
	    try {
		settingStatus = new JSONObject(jo.getString("data"));
		SettingHelper.i().getSettingStatus(settingStatus);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_SETTING_RECOVERTY:
	    JSONObject setting = null;
	    try {
		setting = new JSONObject(jo.getString("data"));
		SettingHelper.i().getSettingFromSrv(setting);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_EBEN_ENANALE_RECOVERTY:
	    JSONObject data = null;
	    try {
		data = new JSONObject(jo.getString("data"));
		SettingHelper.i().savaMindInfo(data);
	    } catch (Exception e) {
		throw e;
	    }
	    break;
	case OP_SYNC_LIST:
	    break;
	case OP_UC_AUTHEN_AUTHOR:
	    JSONObject uc = null;
	    try {
		uc = new JSONObject(jo.getString("data"));
		UCHelper.i().author(uc);
	    } catch (Exception e) {
		throw e;
	    }
	    break;

	default:
	    break;
	}

    }

    @Override
    public String getRequestURL() {
	return getServUrl();
    }

}
