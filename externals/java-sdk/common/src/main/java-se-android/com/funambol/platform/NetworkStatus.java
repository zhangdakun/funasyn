/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 * 
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.platform;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.ServiceState;

public class NetworkStatus {

    private Context context;
    private ConnectivityManager connectivityManager;

    public NetworkStatus(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isWiFiConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    public boolean isMobileConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    public boolean isConnected() {
        return isWiFiConnected() || isMobileConnected();
    }

    public boolean isRadioOff() {
        ServiceState serviceState = new ServiceState();
        return serviceState.getState() == ServiceState.STATE_POWER_OFF;
    }
    
    
    public  boolean isNetworkAvailable() {
    	/* 【WLAN和3G、GSM等网络、有线网络】 */
    	ConnectivityManager cwjManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
        if(null == info || !info.isAvailable()) {
        	sendprogress();
        	return false;
        }
        return true;
    }  
   //new EbenSyncProgress(NOTSYNC, 0, 0, "notepad", "notepad", "", err).send(); 
    private void sendprogress() {
		// TODO Auto-generated method stub

		Intent intent = new Intent("cn.eben.sync.PROGRESS");
		intent.putExtra("issync", 0);
		intent.putExtra("source", "edisk");
		intent.putExtra("needsync", 0);
		intent.putExtra("cursync", 0);

		intent.putExtra("itemname", "");
		intent.putExtra("appname", "edisk");
		intent.putExtra("errcode", 2);
		long time = new Date().getTime();
		intent.putExtra("time", time);
	   
		context.sendBroadcast(intent);

	}

}
