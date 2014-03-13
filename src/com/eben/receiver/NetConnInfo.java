package com.eben.receiver;



import com.eben.androidsync.R;
import com.eben.client.NotificationService;
import com.eben.client.ServiceManager;
import com.eben.service.EbpService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetConnInfo extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
	    if (!(EbpService.inited))
	    {
//	    	context.startService(new Intent(context, NotificationService.class));
	        ServiceManager serviceManager = new ServiceManager(context);
//	        serviceManager.setNotificationIcon(R.drawable.icon);
	        serviceManager.startService();
	    }
	}

}
