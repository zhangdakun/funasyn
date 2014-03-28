package com.eben.receiver;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eben.client.Constants;
import com.eben.client.ServiceManager;
import com.eben.service.EbpService;
import com.funambol.util.Log;

public class NetConnInfo extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		if(null != arg1) {
			Log.debug("NetConnInfo"," onReceive, "+ arg1.getAction());
		}
		// TODO Auto-generated method stub
	    if (!(EbpService.inited))
	    {
//	    	context.startService(new Intent(context, NotificationService.class));
	        ServiceManager serviceManager = new ServiceManager(context);
//	        serviceManager.setNotificationIcon(R.drawable.icon);
	        serviceManager.startService(arg1.getStringExtra(Constants.PARA_USER));
	    }
	}

}
