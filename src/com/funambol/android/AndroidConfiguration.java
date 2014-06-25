/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.android;

import java.lang.reflect.Method;
import java.util.UUID;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings.Secure;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.content.Context;


import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.util.Base64;
import com.funambol.util.Log;

/**
 * Container for the main client client configuration information. Relized using
 * the singleton pattern. Access this class using the getInstance() metod
 * invocation
 */
public class AndroidConfiguration extends Configuration {

    private static final String TAG_LOG = "AndroidConfiguration";

    public static final String KEY_FUNAMBOL_PREFERENCES = "fnblPref";
    
    private static AndroidConfiguration instance = null;
    private        Context context;
    protected      SharedPreferences settings;
    protected      SharedPreferences.Editor editor;
    private        DeviceConfig devconf;

    protected static final String CONF_KEY_TOTALCAPACITITY = "cn.eben.page.totalCapacity";
    protected static final String CONF_KEY_NOTEPADCOUNT = "cn.eben.page.notepadCount";
    protected static final String CONF_KEY_NOTEPADUSECAPACITY = "cn.eben.page.notepadUseCapacity";
    protected static final String CONF_KEY_EDISKUSECAPACITY = "cn.eben.page.ediskUseCapacity";

    public static final String CONF_KEY_CAPACITITY_STATISTICS = "cn.eben.capa_statistics";
    protected static final String CONF_KEY_AUTHOR_STATUS = "cn.eben.author";
    protected static final String CONF_KEY_EXTERNAL_NAME = "cn.eben.externalApp";
    protected static final String CONF_KEY_EXTERNAL_TYPE = "cn.ben.externalType";
    protected static final String CONF_KEY_EXTERNAL_PACKAGE = "cn.ben.exPackage";
    protected static final String CONF_KEY_EDISK_OLDNAME = "cn.ben.ediskoldname";
    protected static final String CONF_KEY_EDISK_NEWNAME = "cn.ben.edisknewname";

    private static final String CONFIG_NOTEPAD_SYNC_STATUS = "cn.eben.notepadsync.status";
    private static final String CONFIG_NOTEPAD_SYNC_DIRECTION = "cn.eben.notepadsync.refreshDirection";
    private static final String CONFIG_NOTEPAD_SYNCFAILURE_TIME = "cn.eben.notepadsync.failuretime";

    private static final String CONFIG_CALENDAR_SYNC_STATUS = "cn.eben.calendarsync.status";
    private static final String CONFIG_CALENDAR_SYNC_DIRECTION = "cn.eben.calendarsync.refreshDirection";
    private static final String CONFIG_CALENDAR_SYNCFAILURE_TIME = "cn.eben.calendarsync.failuretime";

    private static final String CONFIT_CARDNAME_SYNC_STATUS = "cn.eben.cardnamesync.status";
    private static final String CONFIG_CARDNAME_SYNC_DIRECTION = "cn.eben.cardnamesync.refreshDirection";
    private static final String CONFIG_CARDNAME_SYNCFAILURE_TIME = "cn.eben.cardnamesync.failuretime";
    private static final String CONFIG_USER_TRUE_NAME_STRING = "cn.eben.usertruename";


    public static final String ACTION_MINDCLOUD_CHANGE_SETTING = "cn.eben.mindcloud.CHANGE_SETTING";
    public static final String KEY_SYNC_WLAN_ONLY = "sync_wlan_only";
    public static final String KEY_NOTIFY_SYNC_STATUS = "notify_sync_status";
    public static final String KEY_DEV_ID = "eben_dev_id";
    public static final String KEY_MINDCLOUD_STATUS = "mindcloud_status";
    public static final String KEY_MINDCLOUD_SOURCE = "mindcloud_source";
    
    public static final String KEY_MINDCLOUD_TOKEN = "eben_token";
    public static final String KEY_MINDCLOUD_TOKENVER = "eben_tokenver";
    
    public static final String KEY_MINDCLOUD_CHECKFP = "eben_checkfp_";
    public static final String KEY_MINDCLOUD_RENAME = "_rename";
    public static final boolean authSyncInter = true;//lierbao
    /**
     * Private contructor to enforce the Singleton implementation
     * @param context the application Context
     * @param customization the Customization object passed by the getInstance
     * call
     * @param appSyncSourceManager the AppSyncSourceManager object. Better to
     * use an AndroidAppSyncSourceManager or an extension of its super class
     */
    private AndroidConfiguration(Context context,
                                 Customization customization,
                                 AppSyncSourceManager appSyncSourceManager)
    {
        super(customization, appSyncSourceManager);
        this.context = context;
        settings = context.getSharedPreferences(KEY_FUNAMBOL_PREFERENCES, 0);
        editor = settings.edit();
    }

    /**
     * Static method that returns the AndroidConfiguration unique instance
     * @param context the application Context object
     * @param customization the AndoidCustomization object used in this client
     * @param appSyncSourceManager the AppSyncSourceManager object. Better to
     * use an AndroidAppSyncSourceManager or an extension of its super class
     * @return AndroidConfiguration an AndroidConfiguration unique instance
     */
    public static AndroidConfiguration getInstance(Context context,
                                                   Customization customization,
                                                   AppSyncSourceManager appSyncSourceManager)
    {
        if (instance == null) {
            instance = new AndroidConfiguration(context, customization, appSyncSourceManager);
        }
        return instance;
    }

    /**
     * Dispose this object referencing it with the null object
     */
    public static void dispose() {
        instance = null;
    }

    /**
     * Load the value referred to the configuration given the key
     * @param key the String formatted key representing the value to be loaded
     * @return String String formatted vlaue related to the give key
     */
    protected String loadKey(String key) {
        return settings.getString(key, null);
    }

    /**
     * Save the loaded twin key-value using the android context package
     * SharedPreferences.Editor instance
     * @param key the key to be saved
     * @param value the value related to the key String formatted
     */
    protected void saveKey(String key, String value) {
        editor.putString(key, value);
    }

    /**
     * Save the loaded twin key-value using the android context package
     * SharedPreferences.Editor instance
     * @param key the key to be saved
     * @param value the value related to the key byte[] formatted
     */
    public void saveByteArrayKey(String key, byte[] value) {
        String b64 = new String(Base64.encode(value));
        saveKey(key, b64);
    }

    /**
     * Load the value referred to the configuration given the key and the
     * default value
     * @param key the String formatted key representing the value to be loaded
     * @param defaultValue the default byte[] formatted value related to the
     * given key
     * @return byte[] String formatted vlaue related to the give key byte[]
     * formatted
     */
    public byte[] loadByteArrayKey(String key, byte[] defaultValue) {
        String b64 = loadKey(key);
        if (b64 != null) {
            return Base64.decode(b64);
        } else {
            return defaultValue;
        }
    }

    /**
     * Commit the changes
     * @return true if new values were correctly written into the persistent
     * storage
     */
    public boolean commit() {
        return editor.commit();
    }

    /**
     * Get the device id related to this client. Useful when doing syncml
     * requests
     * @return String the device id that is formatted as the string "fac-" plus
     * the information of the deviceId field got by the TelephonyManager service
     */
    protected String getDeviceId() {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        // must have android.permission.READ_PHONE_STATE
//        String deviceId = tm.getDeviceId();
//        return ((AndroidCustomization)customization).getDeviceIdPrefix() + deviceId;
//        ;
      return  getDeviceId(context);
    }
    protected String getDeviceId(Context ctx)
    {
    	
		String serialno1 = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
//			serialno1 = (String) get.invoke(c, "ro.serialno");
			serialno1 = (String) get.invoke(c, "gsm.scril.sn");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null != serialno1 && !"".equalsIgnoreCase(serialno1)) {
			return serialno1;
		}
		
		
    	String prefix = ((AndroidCustomization)customization).getDeviceIdPrefix();
//    	String prefix = "";
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice = tm.getDeviceId();
        if(null != tmDevice) {
        	return prefix+tmDevice;
        }
        String androidId = null;
        try{
        	androidId = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
        if(null != androidId) {
        	return prefix+androidId;
        }
        String serial = null;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)  {
        	serial = Build.SERIAL;

        }
        
        if(null != serial) {
        	return prefix+serial;
        }
//		String serialno1 = null;
//		try {
//			Class<?> c = Class.forName("android.os.SystemProperties");
//			Method get = c.getMethod("get", String.class);
//			serial = (String) get.invoke(c, "ro.serialno");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		if (null != serialno1) {
			return prefix+serialno1;
		} else {
			return prefix+UUID.randomUUID().toString();
		}
//    	Log.debug(TAG_LOG, "getDeviceId, "+tmDevice+", "+androidId+", "+serial+", "+serialno1);
    	
    	
//        return null;
    }
    /**
     * Get the device related configuration
     * @return DeviceConfig the DeviceConfig object related to this device
     */
    public DeviceConfig getDeviceConfig() {
        if (devconf != null) {
            return devconf;
        }
        devconf = new DeviceConfig();
        devconf.setMan(Build.MANUFACTURER);
        devconf.setMod(Build.MODEL);
        // See here for possible values of SDK_INT
        // http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
        devconf.setSwV(Build.VERSION.CODENAME + "(" + Build.VERSION.SDK_INT + ")");
        devconf.setFwV(devconf.getSwV());
        devconf.setHwV(Build.FINGERPRINT);
        devconf.setDevID(getDeviceId());
        devconf.setMaxMsgSize(64 * 1024);
        devconf.setLoSupport(true);
        devconf.setUtc(true);
        devconf.setNocSupport(true);
        devconf.setWBXML(customization.getUseWbxml());
        return devconf;
    }

    /**
     * Get the user agent id related to this client. Useful when doing syncml
     * requests
     * @return String the user agent that is formatted as the string
     * "Funambol Android Sync Client " plus the version of the client
     */
    protected String getUserAgent() {
        StringBuffer ua = new StringBuffer(
                ((AndroidCustomization)customization).getUserAgentName());
        ua.append(" ");
        ua.append(BuildInfo.VERSION);
        return ua.toString();
    }

    /**
     * Migrate the configuration (anything specific to the client)
     */
    @Override
    protected void migrateConfig() {

        // From 6 to 7 means from Diablo to Gallardo, where we introduced a new
        // mechanism for picture sync. We need to check what the server supports
        // to switch to the new method.
        if ("6".equals(version)) {
            setForceServerCapsRequest(true);
        }

        // In version 11 we introduced the c2sPushEnabled property. On Android
        // we can use the master auto sync in order to initialize it to a proper
        // value.
        int versionNumber = Integer.parseInt(version);
        if(versionNumber < 11) {
            boolean masterAutoSync = ContentResolver.getMasterSyncAutomatically();
            setC2SPushEnabled(masterAutoSync);
        }

        // Now migrate the basic configuration (this will update version)
        super.migrateConfig();
    }

}
