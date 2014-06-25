package cn.eben.android;

import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.AndroidLocalization;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.configuration.Configuration;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

public class Launcher extends Activity{

//	{act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER]
//			flg=0x10200000 cmp=cn.eben.activation4/.Launcher} from pid 3125
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean isActive = loadUserInfo(this);
		if(!isActive) {
			try{
			Intent intent = new Intent();
			intent.setClassName("cn.eben.activation4", "cn.eben.activation4.Launcher");
			
			startActivity(intent);
			finish();
			}catch(Exception e) {
				finish();
				e.printStackTrace();
			}
			
		} else {
			boolean isclouded = checkActivation(this);
		
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		TipDialog tip = new TipDialog();
		tip.showTip(this);
		}

	}

	private final String AUTHORITY = "cn.eben.activation4";
	private final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
	private boolean DEVICE_ACTIVATION = true;
	private AndroidCustomization customization;
	public static String username;
	public static String password;


	/**
	 * get User Info
	 * */
	public boolean loadUserInfo(Context context) {
		Uri url = Uri.parse(AUTHORITY_URI + "/userinfo");

		Cursor c = context.getContentResolver().query(url, null, null, null,
				null);

		if (c != null) {
			c.moveToNext();
			username = c.getString(0);
			password = c.getString(1);
			if (username == null || password == null || (username != null  && username.equals("") )|| (password != null && password.equals("")))
				DEVICE_ACTIVATION = false;
			else {

				DEVICE_ACTIVATION = true;
			}
			c.close();
		} else
			DEVICE_ACTIVATION = false;

		return DEVICE_ACTIVATION;

	}
	
	
	public boolean checkActivation(Context context) {

		boolean sActionation = true;

		Account account = AndroidController.getNativeAccount();
		customization = AndroidCustomization.getInstance();

		AndroidLocalization localization = AndroidLocalization.getInstance(context);

		AndroidAppSyncSourceManager appSyncSourceManager = AndroidAppSyncSourceManager.getInstance(
				customization, localization, context);

		AndroidConfiguration configuration = AndroidConfiguration.getInstance(context,
				customization, appSyncSourceManager);
		configuration.load();
		configuration.getDeviceConfig();
		
		String  defuaturl =  customization.getServerUriDefault();
		String url = configuration.getSyncUrl();
		if ((!DEVICE_ACTIVATION && account != null)
				||username == null || (!username.equals(configuration.getUsername()))
				|| !defuaturl.equals(url)) {

			configuration.setCredentialsCheckPending(true);
			configuration.save();
			sActionation = false;

		}
//		else if(account == null){
//			sActionation = false;
//		}//
		else {
			if (!password.equals(configuration.getPassword())) {
				configuration.setPassword(password);
				configuration.save();
			}
		}
		return sActionation;
	}
	
}
