package cn.eben.android.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;


import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.util.Base64;
import com.funambol.util.Log;

public class EbenAuthen {
    private static final String TAG_LOG = "EbenAuthen";
    public static void requestAuthen() {
//	server = 'passport.test.eben.cn'
//		uri = '/funambol/DSJSONServlet'
//
//		#/userapp/terminalinfo/?username=15810388882&passwd=MTIzNDU2
//		#/userapp/getcloudtoken/?username=15810388882
	
//	String server = "http://passport.test.eben.cn/userapp/terminalinfo/";
    String server = "http://passport.eben.cn/userapp/terminalinfo/";
    
	String user = App.i().getAppInitializer().getUserName();
	String pwd = App.i().getAppInitializer().getPassword();
	
	String  password = new String(Base64.encode(pwd.getBytes()));
	String url= server + "?username="+user+"&passwd="+password;
	
//	Log.info(TAG_LOG, "pwd : "+password);
	try {
		Log.info(TAG_LOG, url);
	    URL u = new URL(url);
	    try {
		URLConnection conn = u.openConnection(); 
		conn.setReadTimeout(3*1000);
		conn.setConnectTimeout(3*1000);
		InputStream is = conn.getInputStream();
//		byte[] buff = new byte[1024*1024];
//		int hasRead = is.read(buff);
		StringBuilder authen = new StringBuilder();
		int hasRead = 0;
		byte[] buff = new byte[1024];
		while ((hasRead = is.read(buff)) > 0) {
			// os.write(buff, 0, hasRead);
			authen.append(new String(buff,0,hasRead,"UTF-8"));
//			oSavedFile.write(buff, 0, hasRead);
//			sum += hasRead;
			// Log.info(TAG_LOG, "has read : "+hasRead);
		}
		
//		Log.info(TAG_LOG, "hasread : "+hasRead);
//		String authen =new String(buff,0,hasRead,"UTF-8");
		
//		Log.info(TAG_LOG, "response : "+authen);
		saveAuthen(authen.toString());
		
		
		
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	
    }
    
    private static void saveAuthen(String auth) {
	
	try {
	    JSONObject jo = new JSONObject(auth);
	    String token = jo.getString("token");
	    String tokenver = jo.getString("tokenver");
	    Log.debug(TAG_LOG,"auth resp: "+auth);
	    Log.info(TAG_LOG, "token: " + token + ", tokenver: " + tokenver);

	    App.i()
		    .getAppInitializer()
		    .getConfiguration()
		    .saveStringKey(AndroidConfiguration.KEY_MINDCLOUD_TOKEN,
			    token);
	    
	    App.i()
	    .getAppInitializer()
	    .getConfiguration()
	    .saveStringKey(AndroidConfiguration.KEY_MINDCLOUD_TOKENVER,
		    tokenver);
	    
	    App.i()
	    .getAppInitializer()
	    .getConfiguration().commit();	    

	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
		if(auth.contains("\"data\":[]")) {
			Log.error(TAG_LOG, "maybe miss user or pw,reload it ");
//			App.i().getAppInitializer().loadUserInfo();
		}
	}
    }
//    {"token":"PG8SKGLSDunNPgPuK9qGIrFnQfv9P36qeT+3qq9fg8A=","tokenver":1348558808,"data":[{"serial_no":"8920144f06b81717","mod":"4","username":"15810388882","nickname":"li\u7684E\u672c","catime":"2012-02-14 10:59:27"},{"serial_no":"8888961091634","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-07-24 12:20:56"},{"serial_no":"8888273089643","mod":"4","username":"15810388882","nickname":"QQ\u7684E\u672c","catime":"2011-09-21 18:57:03"},{"serial_no":"8888899336247","mod":"4","username":"15810388882","nickname":"QQ\u7684E\u672c","catime":"2011-09-29 11:30:54"},{"serial_no":"4052144f06a00d06","mod":"4","username":"15810388882","nickname":"\u5b5f\u5360\u519b\u7684E\u672c","catime":"2012-03-06 15:36:25"},{"serial_no":"8888353450123","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-07-26 13:59:57"},{"serial_no":"8888678580660","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-08-07 12:22:24"},{"serial_no":"8888249628624","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-08-08 11:45:21"},{"serial_no":"8888174397005","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-08-08 19:03:11"},{"serial_no":"F2718SBSH0000679","mod":"5","username":"15810388882","nickname":"123\u7684E\u672c","catime":"2012-08-10 12:19:33"}],"status":"200"}

    public static boolean isChecked() {
	boolean bAuthed = false;
	
	AndroidConfiguration config = App.i().getAppInitializer().getConfiguration();
	
	if (null != config.loadStringKey(
		AndroidConfiguration.KEY_MINDCLOUD_TOKENVER, null)
		&& null != config.loadStringKey(
			AndroidConfiguration.KEY_MINDCLOUD_TOKEN, null)) {
	    bAuthed = true;
	}
	Log.info(TAG_LOG, "checkAuthed : "+bAuthed);
	return bAuthed;
    }
    
    public static String getToken() {
	return App.i().getAppInitializer().getConfiguration().loadStringKey(
		AndroidConfiguration.KEY_MINDCLOUD_TOKEN, null);
    }
    
    public static String getTokenVersion() {
	return App.i().getAppInitializer().getConfiguration().loadStringKey(
		AndroidConfiguration.KEY_MINDCLOUD_TOKENVER, null);
    }    
    
    
}
