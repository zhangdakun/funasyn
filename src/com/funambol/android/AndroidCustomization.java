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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import cn.eben.android.EbenConst;
import cn.eben.android.util.EbenHelpers;
import cn.eben.androidsync.R;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.SignupHandler;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.Screen;
import com.funambol.platform.DeviceInfo;
import com.funambol.platform.DeviceInfoInterface;
import com.funambol.platform.DeviceInfoInterface.DeviceRole;
import com.funambol.sync.SyncConfig;
import com.funambol.sync.SyncSource;
import com.funambol.sync.client.PercentageStorageLimit;
import com.funambol.sync.client.StorageLimit;
import com.funambol.util.Log;

/**
 * Implements the Customization interface for Android platform
 */
public class AndroidCustomization implements Customization {

    private static final String TAG_LOG = "AndroidCustomization";

    // Settings customization
//    private final String   SERVER_URI              = "http://my.funambol.com/sync";
//    private final String   SERVER_URI              = "http://192.168.6.224:8080/ds";
    private final String   SERVER_URI              = EbenConst.HTTP_HOST+"funambol/ds";
    private final String   USERNAME                = "";
    private final String   PASSWORD                = "";
    
    private final String   VERSION                 = null;
    private final String   FULL_NAME               = "Funambol Android Sync Client";
    private final String   APPLICATION_NAME        = "Funambol Sync";

    // this is the string used to populate the user agent
    private final String   USER_AGENT_NAME         = "Funambol Android Sync Client";

    // About customization
    private final String   ABOUT_COMPANY_NAME      = "Funambol, Inc.";
    private final String   ABOUT_COPYRIGHT_DEFAULT = "Copyright " + (char)169 + " 2009 - 2011";
    private final String   ABOUT_SITE_DEFAULT      = "www.funambol.com";
    private final String   PORTAL_URL              = "http://my.funambol.com";

    // Sync sources customization
//    private final String   CONTACTS_DEFAULT_URI    = "card";
    private final String   CONTACTS_DEFAULT_URI    = "ecard";//lierbao
    private final boolean  CONTACTS_AVAILABLE      = false;
    private final boolean  CONTACTS_ENABLED        = false;

    private final String   BACKUP_DEFAULT_URI    = "ebackup";//lierbao
    private final boolean  BACKUP_AVAILABLE      = true;
    private final boolean  BACKUP_ENABLED        = true;

//    private final String   PHOTO_DEFAULT_URI    = "ephoto";//lierbao
    private final String   PHOTO_DEFAULT_URI    = "bphoto";//lierbao
    private final boolean  PHOTO_AVAILABLE      = true;
    private final boolean  PHOTO_ENABLED        = true;
    
    private final String   EVENTS_DEFAULT_URI      = "event";
    private final boolean  EVENTS_AVAILABLE        = false;
    private final boolean  EVENTS_ENABLED          = false;

    private final String   TASKS_DEFAULT_URI       = "task";
    private final boolean  TASKS_AVAILABLE         = false;
    private final boolean  TASKS_ENABLED           = false;

    private final String   NOTES_DEFAULT_URI       = "note";
    private final boolean  NOTES_AVAILABLE         = false;
    private final boolean  NOTES_ENABLED           = false;

    private final String   PICTURES_DEFAULT_URI    = "picture";
    private final boolean  PICTURES_AVAILABLE      = false;
    private final boolean  PICTURES_ENABLED        = false;

    private final String   VIDEOS_DEFAULT_URI      = "video";
    private final boolean  VIDEOS_AVAILABLE        = false;
    private final boolean  VIDEOS_ENABLED          = false;

    private final String   FILES_DEFAULT_URI      = "file";
    private final boolean  FILES_AVAILABLE        = false;
    private final boolean  FILES_ENABLED          = false;

    private final String   CONFIG_DEFAULT_URI      = "configuration";
    private final boolean  CONFIG_AVAILABLE        = true;

    private final String   DEVICE_ID_PREFIX        = "pc-";//"fac-";

    // Log customization
    private final boolean  SEND_LOG_ENABLED        = true;
    private final boolean  LOG_IN_SETTINGS_SCREEN  = true;
    private final boolean  LOCK_LOG_LEVEL          = false;
    private final int      LOCKED_LOG_LEVEL        = Log.TRACE;
    private final String   LOG_FILE_NAME           = "synclog.txt";

    // Sync items type
    private final String   DEFAULT_CALENDAR_TYPE = "text/x-vcalendar";
    private final String   DEFAULT_TASK_TYPE     = "text/x-vcalendar";
    private final String   DEFAULT_NOTE_TYPE     = "text/plain";
    private final String   DEFAULT_CONTACT_TYPE  = "text/x-vcard";

    private final boolean  DISPLAY_NAME_SUPPORTED = false;

    private final boolean  USE_BANDWIDTH_SAVER_CONTACTS = false;
    private final boolean  USE_BANDWIDTH_SAVER_EVENTS = false;
    private final boolean  USE_BANDWIDTH_SAVER_MEDIA = true;

    // Define the schedule choices
    private final int[] POLLING_PIM_INTERVAL_CHOICES = {
        5,
        15,
        30,
        60,
        120,
        240,
        480,
        720,
        1440
    };
    private final int  DEFAULT_POLLING_INTERVAL = POLLING_PIM_INTERVAL_CHOICES[1];

    // Specifies if the sync url is editable in both the login and setting screens
    private final boolean SYNC_URI_EDITABLE = false;
    
    // This is the account screen class name. It can be customized for versions
    // with a different account screen implementation
    private final String  LOGIN_SCREEN_CLASS_NAME = "com.funambol.android.activities.AndroidLoginScreen";

    // This is the account screen authenticator for the unit tests
    private final String  UT_ACCOUNT_SCREEN_CLASS_NAME = "com.funambol.android.UnitTestAuthenticator";

    // Defines the classto use while displaying a single sync source in the home screen
//    private final String  ALONE_UI_SYNC_SOURCE_CLASS_NAME = "com.funambol.android.activities.AndroidAloneUISyncSource";
    private final String  ALONE_UI_SYNC_SOURCE_CLASS_NAME = "com.eben.activities.EbenAloneUISyncSource";
    
    // Normally the sync adapter set the account as syncable during its
    // initialization. Set this value to false to change this behavior.
    private boolean ENABLE_SYNC_AUTOMATICALLY = true;

    // Note: this array must be kept aligned with the list of sources that we
    // register (see initSourcesInfo below)
    private final int SOURCES_ORDER[] = { AndroidAppSyncSourceManager.CONTACTS_ID,
    									   AndroidAppSyncSourceManager.BACKUP_ID,
    									   AndroidAppSyncSourceManager.PHOTO_ID
    									   
//                                          AndroidAppSyncSourceManager.EVENTS_ID,
                                          //AndroidAppSyncSourceManager.TASKS_ID,
                                          //AndroidAppSyncSourceManager.NOTES_ID,
//                                          AndroidAppSyncSourceManager.PICTURES_ID,
//                                          AndroidAppSyncSourceManager.VIDEOS_ID,
//                                          AndroidAppSyncSourceManager.FILES_ID
                                        };

    // Updater customization
    private final boolean  CHECK_FOR_UPDATE        = false; // Used for MailTrust update
    private final boolean  ENABLE_UPDATER_MANAGER  = false; // Used for Funambol update
    private final long     CHECK_UPDATE_INTERVAL   = (long)24*(long)60*(long)60*(long)1000; // 1 day in milliseconds
    private final long     REMINDER_UPDATE_INTERVAL= (long)2 *(long)60*(long)60*(long)1000; // 2 hours in milliseconds

    private final String   SUPPORT_EMAIL_ADDRESS   = "fac_log@funambol.com";

    // Tha name of the SQLite Database used to store application data
    // (e.g. the configuration, sources tracker data)
    private final String   FUNAMBOL_SQLITE_DB_NAME = "funambol.db";
    
    // Specifies if the sync retry feature is enabled. It allows to start a
    // scheduled synchronization after a connectivity error
    private final boolean SYNC_RETRY_ENABLED   = false;
    private final int[]   SYNC_RETRY_INTERVALS = {};

    // Bandwidth Saver customization
    private final boolean  BANDWIDTH_SAVER_ENABLED = true;

    private final boolean  SHOW_SYNC_ICON_ON_SELECTION = false;

    // Specifies if the user credentials shall be validated
    private final boolean  CHECK_CREDENTIALS_IN_LOGIN_SCREEN = true;

    // Specifies if slow sync needs to be confirmed by the user
    private final boolean  CONFIRM_SLOW_SYNC            = false;

    // Specifies if the user must be warned on large amount of deletes
    private final boolean  WARN_ON_DELETES              = false;

    // Specifies if the S2C SMS push must be enabled in the client
    private final boolean ENABLE_S2C_SMS_PUSH           = false;

    private final int C2S_PUSH_DELAY                    = 10 * 1000; // 1 minute

    // Show non working sources in the home screen
    private final boolean SHOW_NON_WORKING_SOURCES      = false;

    private final boolean  SYNC_ALL_ON_MAIN_SCREEN      = true;

    private final boolean  SYNC_ALL_ACTS_AS_CANCEL_SYNC = true;

    private final boolean  SOURCE_URI_VISIBLE          = true;
    private final boolean  SYNC_DIRECTION_VISIBLE      = true;

    private final int      DEFAULT_SYNC_MODE           = Configuration.SYNC_MODE_MANUAL;
    private final int      S2CPUSH_SMS_PORT            = 50011;

    private final boolean  SYNC_MODE_IN_SETTINGS_SCREEN = true;

    private final int[]    AVAILABLE_SYNC_MODES = {Configuration.SYNC_MODE_PUSH,
                                                   Configuration.SYNC_MODE_MANUAL,
                                                   Configuration.SYNC_MODE_SCHEDULED};

    private final boolean  C2S_PUSH_IN_SETTINGS_SCREEN = true;

    private final boolean  SHOW_ABOUT_LICENCE          = true;
    private final boolean  SHOW_POWERED_BY             = false;
    private final boolean  SHOW_PORTAL_INFO            = true;

    private final boolean  ENABLE_REFRESH_COMMAND      = true;

    private final int      DEFAULT_AUTH_TYPE = SyncConfig.AUTH_TYPE_BASIC;

    // Specifies if the sync messages shall be exchanged using binary xml. If this value is set to false
    // the client decides what is the best encoding based on the server capabilities
    private final boolean USE_WBXML                     = false;

    private final String  HTTP_UPLOAD_PREFIX            = "sapi/media";

    private final boolean CONTACTS_IMPORT_ENABLED       = false;

    // Mobile Sign Up customizations
    private final boolean MOBILE_SIGNUP_ENABLED         = true;
    private final int     DEFAULT_MSU_VALIDATION_MODE   = SignupHandler.VALIDATION_MODE_CAPTCHA;
    private final boolean SHOW_SIGNUP_SUCCEEDED_MESSAGE = true;
    private final boolean ADD_SHOW_PASSWORD_FIELD       = true;
    private final String  TERMS_AND_CONDITIONS_URL      = "http://my.funambol.com/ui/mobile/jsp/toc.jsp";
    private final String  PRIVACY_POLICY_URL            = "http://my.funambol.com/ui/mobile/jsp/pp.jsp";
    private final boolean PREFILL_PHONE_NUMBER          = false;
    
    /**
     * All available source sync modes (i.e. sync directions).
     */
    private static final int[] AVAILABLE_SOURCE_SYNC_MODES_ALL = {
        SyncSource.INCREMENTAL_SYNC,
        SyncSource.INCREMENTAL_UPLOAD,
        SyncSource.INCREMENTAL_DOWNLOAD,
        SyncSource.NO_SYNC };
    /**
     * Source sync modes (i.e. sync directions) available for media sync in smartphones.
     * Currently, this means all of them except download.
     */
    private static final int[] AVAILABLE_SOURCE_SYNC_MODES_SMARTPHONE_MEDIA_SYNC = {
        SyncSource.INCREMENTAL_SYNC,
        SyncSource.INCREMENTAL_UPLOAD,
        SyncSource.NO_SYNC};
    /**
     * Source sync modes (i.e. sync directions) available for PIM sync.
     */
    private static final int[] AVAILABLE_SOURCE_SYNC_MODES_PIM_SYNC = {
        SyncSource.INCREMENTAL_SYNC,
        SyncSource.NO_SYNC };

    /**
     * Source sync modes (i.e. sync directions) available for files sync.
     */
    private static final int[] AVAILABLE_SOURCE_SYNC_MODES_FILE_SYNC = {
        SyncSource.INCREMENTAL_SYNC,
        SyncSource.NO_SYNC };

    private static final StorageLimit LOCAL_STORAGE_SAFETY_THRESHOLD = new PercentageStorageLimit(98);

    private final String DEFAULT_FILES_SDCARD_DIR = "MediaHub-Files";

    private Hashtable sourcesUri          = new Hashtable();
    private Hashtable activeSourcesEnabledState = new Hashtable();
    private Hashtable sourcesIcon         = new Hashtable();
    private Hashtable sourcesDisabledIcon = new Hashtable();
    
    /** Max allowed size for videos, 100Mb */
    private static final long MAX_ALLOWED_FILE_SIZE_FOR_VIDEOS = 100 * 1024 * 1024;
    /** Max allowed size for files, 25Mb */
    private static final long MAX_ALLOWED_FILE_SIZE_FOR_FILES = 25 * 1024 * 1024;

    //// ------------------- END OF CUSTOMIZABLE FIELDS --------------------////

    private static AndroidCustomization instance = null;
    
    /**
     * Private constructor for the singleton pattern enforcement
     */
    private AndroidCustomization() {
        initSourcesInfo();
    }

    /**
     * Set this Object instance to null
     */
    public static void dispose() {
        instance = null;
    }

    public static AndroidCustomization getInstance() {
        if (instance == null) {
            instance = new AndroidCustomization();
        }
        return instance;
    }

    public String getFunambolSQLiteDbName() {
        return FUNAMBOL_SQLITE_DB_NAME;
    }

    public String getApplicationFullname() {
        return FULL_NAME;
    }
    
    public String getUserAgentName() {
        return USER_AGENT_NAME;
    }

    public String getApplicationTitle() {
        return APPLICATION_NAME;
    }

    public int[] getSourcesOrder() {
        return SOURCES_ORDER;
    }

    public Bitmap getSyncAllIcon() {
        return null;
    }

    public Bitmap getPoweredByLogo() {
        return new Bitmap(new Integer(R.drawable.powered_by));
    }

    public Bitmap getSyncAllBackground() {
        return new Bitmap(new Integer(R.drawable.icon_sync_all_blue));
    }

    public Bitmap getSyncAllHighlightedBackground() {
        return new Bitmap(new Integer(R.drawable.icon_sync_all_blue));
    }

    public String getDeviceIdPrefix() {
        return DEVICE_ID_PREFIX;
    }

    public Bitmap getButtonBackground() {
        return null;
    }

    public Bitmap getButtonHighlightedBackground() {
        return null;
    }

    public Bitmap getOkIcon() {
        return new Bitmap(new Integer(R.drawable.icon_complete));
    }

    public Bitmap getErrorIcon() {
        return new Bitmap(new Integer(R.drawable.icon_failed_complete));
    }

    public Bitmap getStatusSelectedIcon() {
        return new Bitmap(new Integer(android.R.drawable.ic_popup_sync));
    }

    public String getContactType() {
        return DEFAULT_CONTACT_TYPE;
    }

    public String getCalendarType() {
        return DEFAULT_CALENDAR_TYPE;
    }

    public String getTaskType() {
        return DEFAULT_TASK_TYPE;
    }

    public String getNoteType() {
        return DEFAULT_NOTE_TYPE;
    }

    public boolean useBandwidthSaverContacts(){
        return USE_BANDWIDTH_SAVER_CONTACTS;
    }

    public boolean useBandwidthSaverEvents(){
        return USE_BANDWIDTH_SAVER_EVENTS;
    }

    public boolean useBandwidthSaverMedia(){
        return USE_BANDWIDTH_SAVER_MEDIA;
    }

    public boolean isDisplayNameSupported() {
        return DISPLAY_NAME_SUPPORTED;
    }

    public String getSupportEmailAddress() {
        return SUPPORT_EMAIL_ADDRESS;
    }

    public String getLoginScreenClassName() {
        if (BuildInfo.UNIT_TEST) {
            return UT_ACCOUNT_SCREEN_CLASS_NAME;
        } else {
            return LOGIN_SCREEN_CLASS_NAME;
        }
    }

    public String getAloneUISyncSourceClassName() {
        return ALONE_UI_SYNC_SOURCE_CLASS_NAME;
    }
        
    public boolean getEnableSyncAutomatically() {
        return ENABLE_SYNC_AUTOMATICALLY;
    }
    
    public boolean getSyncRetryEnabled() {
        return SYNC_RETRY_ENABLED;
    }

    public int[] getSyncRetryIntervals() {
        return SYNC_RETRY_INTERVALS;
    }

    public boolean syncAllOnMainScreenRequired() {
        return SYNC_ALL_ON_MAIN_SCREEN;
    }

    public boolean syncAllActsAsCancelSync() {
        return SYNC_ALL_ACTS_AS_CANCEL_SYNC;
    }

    public boolean syncUriEditable() {
        return SYNC_URI_EDITABLE;
    }

    public boolean getCheckCredentialsInLoginScreen() {
        return CHECK_CREDENTIALS_IN_LOGIN_SCREEN;
    }

    public boolean isSourceUriVisible() {
        return SOURCE_URI_VISIBLE;
    }

    public boolean isSyncDirectionVisible() {
        return SYNC_DIRECTION_VISIBLE;
    }

    public boolean isSourceActive(int id) {
        return activeSourcesEnabledState.containsKey(new Integer(id));
    }

    public boolean isSourceEnabledByDefault(int id) {
        Boolean active = (Boolean) activeSourcesEnabledState.get(new Integer(id));
        return active != null ? active.booleanValue() : false;
    }

    public boolean confirmSlowSync() {
        return CONFIRM_SLOW_SYNC;
    }

    public boolean warnOnDeletes() {
        return WARN_ON_DELETES;
    }

    public boolean checkForUpdates() {
        return CHECK_FOR_UPDATE;
    }

    public boolean enableUpdaterManager() {
        return ENABLE_UPDATER_MANAGER;
    }

    public String getDefaultSourceUri(int id) {
        return (String)sourcesUri.get(new Integer(id));
    }

    /**
     * Gets default sync source available direction modes.
     * 
     * @deprecated use {@link #getDefaultSourceSyncModes(int, DeviceInfoInterface.DeviceRole)} instead 
     */
    public int[] getDefaultSourceSyncModes(int id) {
        
        //FIXME: It's a quick and dirty workaround for obtaining the device role
        Context context = App.i().getApplicationContext();
        DeviceInfo deviceInfo = new DeviceInfo(context);

        return getDefaultSourceSyncModes(id, deviceInfo.getDeviceRole());
    }
    
    /**
     * Gets default sync source available direction modes, based on device information.
     * 
     * @param id
     * @param use one of the predefined values of {@link DeviceInfoInterface.DeviceRole}
     */
    public int[] getDefaultSourceSyncModes(int id, DeviceRole deviceRole) {
        
        // Media sync
        if ((AndroidAppSyncSourceManager.PICTURES_ID == id) || (AndroidAppSyncSourceManager.VIDEOS_ID == id)) {
            if (DeviceInfoInterface.DeviceRole.SMARTPHONE == deviceRole) {
                return AVAILABLE_SOURCE_SYNC_MODES_SMARTPHONE_MEDIA_SYNC;
            } else { // Tablets etc.
                return AVAILABLE_SOURCE_SYNC_MODES_ALL;
            }
        } else if ((AndroidAppSyncSourceManager.FILES_ID == id)) {
            return AVAILABLE_SOURCE_SYNC_MODES_FILE_SYNC;
        } else {
            // PIM sync
            // Currently, the device role makes no difference whatsoever for PIM
            return AVAILABLE_SOURCE_SYNC_MODES_PIM_SYNC;
        }
    }

    public boolean lockLogLevel(){
        return LOCK_LOG_LEVEL;
    }

    public int getLockedLogLevel(){
        return LOCKED_LOG_LEVEL;
    }

    public boolean isLogEnabledInSettingsScreen() {
        return LOG_IN_SETTINGS_SCREEN;
    }

    public boolean isBandwidthSaverEnabled() {
        return BANDWIDTH_SAVER_ENABLED;
    }

    public boolean sendLogEnabled() {
        return SEND_LOG_ENABLED;
    }

    public String getServerUriDefault() {
        return SERVER_URI;
    }

    public String getUserDefault() {
        return USERNAME;
    }

    public String getPasswordDefault() {
        return PASSWORD;
    }

    public long getCheckUpdtIntervalDefault(){
        return CHECK_UPDATE_INTERVAL;
    }

    public long getReminderUpdtIntervalDefault(){
        return REMINDER_UPDATE_INTERVAL;
    }

    public String getLogFileName() {
        return LOG_FILE_NAME;
    }

    public String getCompanyName() {
        return ABOUT_COMPANY_NAME;
    }

    public String getAboutCopyright() {
        return ABOUT_COPYRIGHT_DEFAULT;
    }

    public String getAboutSite() {
        return ABOUT_SITE_DEFAULT;
    }

    public boolean showAboutLicence() {
        return SHOW_ABOUT_LICENCE;
    }

    public boolean showPoweredBy() {
        return SHOW_POWERED_BY;
    }

    public boolean enableRefreshCommand() {
        return ENABLE_REFRESH_COMMAND;
    }

    public int getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    public boolean isS2CSmsPushEnabled() {
        return ENABLE_S2C_SMS_PUSH;
    }
    
    public int getC2SPushDelay() {
        return C2S_PUSH_DELAY;
    }

    public int getS2CPushSmsPort() {
        return S2CPUSH_SMS_PORT;
    }

    public int getDefaultSyncMode() {
        return DEFAULT_SYNC_MODE;
    }

    public int[] getPollingPimIntervalChoices() {
        return POLLING_PIM_INTERVAL_CHOICES;
    }

    public boolean showSyncModeInSettingsScreen() {
        return SYNC_MODE_IN_SETTINGS_SCREEN;
    }

    public boolean showC2SPushInSettingsScreen() {
        return C2S_PUSH_IN_SETTINGS_SCREEN;
    }

    public int[] getAvailableSyncModes() {
        return AVAILABLE_SYNC_MODES;
    }

    /**
     * Get default sync source direction mode
     * @deprecated use {@link #getDefaultSourceSyncMode(int, DeviceInfoInterface.DeviceRole)} instead 
     */
    public int getDefaultSourceSyncMode(int id) {
        
        //FIXME: It's a quick and dirty workaround for obtaining the device role
        Context context = App.i().getApplicationContext();
        DeviceInfo deviceInfo = new DeviceInfo(context);
        
        return getDefaultSourceSyncMode(id, deviceInfo.getDeviceRole());
    }

    /**
     * Get default sync source direction mode, based on device information
     * @param id
     * @param use one of the predefined values of {@link DeviceInfoInterface.DeviceRole}
     */
    public int getDefaultSourceSyncMode(int id, DeviceRole deviceRole) {
        if (!isSourceEnabledByDefault(id)) {
            return SyncSource.NO_SYNC;
        }
        if ((AndroidAppSyncSourceManager.PICTURES_ID == id) || (AndroidAppSyncSourceManager.VIDEOS_ID == id)) {
            if (DeviceRole.TABLET == deviceRole) {
                return SyncSource.INCREMENTAL_SYNC;
            } else { // Smartphones etc.
                return SyncSource.INCREMENTAL_UPLOAD;
            }
        } else {
            return SyncSource.INCREMENTAL_SYNC;
        }
    }

    public boolean showSyncIconOnSelection() {
        return SHOW_SYNC_ICON_ON_SELECTION;
    }

    public boolean showNonWorkingSources() {
        return SHOW_NON_WORKING_SOURCES;
    }

    public int getDefaultAuthType() {
        return DEFAULT_AUTH_TYPE;
    }

    public boolean getUseWbxml() {
        return USE_WBXML;
    }

    public boolean getContactsImportEnabled() {
        return CONTACTS_IMPORT_ENABLED;
    }

    public boolean getMobileSignupEnabled() {
        return MOBILE_SIGNUP_ENABLED;
    }

    public int getDefaultMSUValidationMode() {
        return DEFAULT_MSU_VALIDATION_MODE;
    }

    public boolean getShowSignupSuccededMessage() {
        return SHOW_SIGNUP_SUCCEEDED_MESSAGE;
    }

    public boolean getAddShowPasswordField() {
        return ADD_SHOW_PASSWORD_FIELD;
    }

    public String getTermsAndConditionsUrl() {
        return TERMS_AND_CONDITIONS_URL;
    }

    public String getPrivacyPolicyUrl() {
        return PRIVACY_POLICY_URL;
    }

    public boolean getPrefillPhoneNumber() {
        return PREFILL_PHONE_NUMBER;
    }

    public String getHttpUploadPrefix() {
        return HTTP_UPLOAD_PREFIX;
    }

    public String getDefaultFilesSDCardDir() {
        return DEFAULT_FILES_SDCARD_DIR;
    }

    public Bitmap[] getStatusIconsForAnimation() {
        Bitmap[] bitmaps = new Bitmap[6];

        bitmaps[0] = new Bitmap(new Integer(R.drawable.icon_progress_sync_1));
        bitmaps[1] = new Bitmap(new Integer(R.drawable.icon_progress_sync_2));
        bitmaps[2] = new Bitmap(new Integer(R.drawable.icon_progress_sync_3));
        bitmaps[3] = new Bitmap(new Integer(R.drawable.icon_progress_sync_4));
        bitmaps[4] = new Bitmap(new Integer(R.drawable.icon_progress_sync_5));
        bitmaps[5] = new Bitmap(new Integer(R.drawable.icon_progress_sync_6));

        return bitmaps;
    }

    public Bitmap[] getStatusHugeIconsForAnimation() {
        Bitmap[] bitmaps = new Bitmap[16];

        bitmaps[0] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame01));
        bitmaps[1] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame02));
        bitmaps[2] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame03));
        bitmaps[3] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame04));
        bitmaps[4] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame05));
        bitmaps[5] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame06));
        bitmaps[6] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame07));
        bitmaps[7] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame08));
        bitmaps[8] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame09));
        bitmaps[9] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame10));
        bitmaps[10] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame11));
        bitmaps[11] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame12));
        bitmaps[12] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame13));
        bitmaps[13] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame14));
        bitmaps[14] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame15));
        bitmaps[15] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame16));

        return bitmaps;
    }

    private void initSourcesInfo() {

        if (Log.isLoggable(Log.INFO)) {
            Log.info(TAG_LOG, "Initializing sources info");
        }

        // Initialize the sources available in this application
        if(CONTACTS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.CONTACTS_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), CONTACTS_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), CONTACTS_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts_grey));
        }
        
        if(BACKUP_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.BACKUP_ID;
//            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
//            }
            sourcesUri.put(new Integer(id), BACKUP_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), BACKUP_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts_grey));
        }
        if(PHOTO_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.PHOTO_ID;
//            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
//            }
            sourcesUri.put(new Integer(id), PHOTO_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), PHOTO_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts_grey));
        }        
        if(EVENTS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.EVENTS_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), EVENTS_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), EVENTS_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_calendar));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_calendar_grey));
        }
        if(TASKS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.TASKS_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), TASKS_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), TASKS_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_tasks));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_tasks_grey));
        }
        if(NOTES_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.NOTES_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), NOTES_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), NOTES_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_notes));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_notes_grey));
        }
        if(PICTURES_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.PICTURES_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), PICTURES_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), PICTURES_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_photo));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_photo_grey));
        }
        if(VIDEOS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.VIDEOS_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), VIDEOS_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), VIDEOS_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_video));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_video_grey));
        }
        if(FILES_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.FILES_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), FILES_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), FILES_ENABLED);
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_files));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_files_grey));
        }
        if(CONFIG_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.CONFIG_ID;
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG_LOG, "Initializing source: " + id);
            }
            sourcesUri.put(new Integer(id), CONFIG_DEFAULT_URI);
            activeSourcesEnabledState.put(new Integer(id), true);
        }
    }

    /**
     * Returns an Enumeration of Integer where each item represents the id of an
     * available source. The source is not ready yet to be used, but it is
     * available in this client version. To check if a source is really working
     * and enabled, use the corresponding AppSyncSource methods.
     *
     * @return an enumeration of Integer
     */
    public Enumeration getAvailableSources() {
        Enumeration keys = activeSourcesEnabledState.keys();
        return keys;
    }

    public Bitmap getSourceIcon(int id) {
        Bitmap icon = (Bitmap)sourcesIcon.get(new Integer(id));
        return icon;
    }

    public Bitmap getSourceDisabledIcon(int id) {
        Bitmap icon = (Bitmap)sourcesDisabledIcon.get(new Integer(id));
        return icon;
    }

    public String getPoweredBy() {
        return null;
    }

    // Note that this is hardcoded here because it cannot be translated
    public String getLicense() {
        StringBuffer license = new StringBuffer();

        license.append("This program is provided AS IS, without warranty licensed under AGPLV3. The ")
               .append("Program is free software; you can redistribute it and/or modify it under the ")
               .append("terms of the GNU Affero General Public License version 3 as published by the Free ")
               .append("Software Foundation including the additional permission set forth source code ")
               .append("file header.\n\n")
               .append("The interactive user interfaces in modified source and object code versions of ")
               .append("this program must display Appropriate Legal Notices, as required under Section 5 ")
               .append("of the GNU Affero General Public License version 3.\n\n ")
               .append("In accordance with Section 7(b) of the GNU Affero General Public License version 3, ")
               .append("these Appropriate Legal Notices must retain the display of the \"Powered by ")
               .append("Funambol\" logo. If the display of the logo is not reasonably feasible for ")
               .append("technical reasons, the Appropriate Legal Notices must display the words \"Powered ")
               .append("by Funambol\". Funambol is a trademark of Funambol, Inc.");
        
        return license.toString();
    }

    public String getVersion() {
        StringBuffer result = new StringBuffer();
        if (VERSION == null) {
            // Grab the version from the BuildInfo
            result.append(BuildInfo.VERSION);
        } else {
            result.append(VERSION);
        }
        if (!"release".equals(BuildInfo.MODE)) {
            result.append(" (").append(BuildInfo.DATE).append(")");
        }
        return result.toString();
    }

    public StorageLimit getStorageLimit() {
        return LOCAL_STORAGE_SAFETY_THRESHOLD;
    }

    public long getMaxAllowedFileSizeForVideos() {
        return MAX_ALLOWED_FILE_SIZE_FOR_VIDEOS;
    }

    public long getMaxAllowedFileSizeForFiles() {
        return MAX_ALLOWED_FILE_SIZE_FOR_FILES;
    }

    public String getPortalURL() {
        return PORTAL_URL;
    }

    public boolean showPortalInfo() {
        return SHOW_PORTAL_INFO;
    }
    
    public int getFirstSyncMediaUploadLimit(int mediaId, DeviceRole deviceRole) {
        //actually, device role is useless for upload limit
        switch (mediaId) {
            case AppSyncSourceManager.PICTURES_ID:
                return 5;
            case AppSyncSourceManager.VIDEOS_ID:
                return 2;
            default:
                //fallback values
                return 0;
        }
    }

    public int getFirstSyncMediaDownloadLimit(int mediaId, DeviceRole deviceRole) {
        if (DeviceRole.TABLET.equals(deviceRole)) {
            switch (mediaId) {
                case AppSyncSourceManager.PICTURES_ID:
                    return 20;
                case AppSyncSourceManager.VIDEOS_ID:
                    return 5;
                default:
                    //fallback value
                    return 0;
            }
        }

        //fallback role
        switch (mediaId) {
            case AppSyncSourceManager.PICTURES_ID:
                return 0;
            case AppSyncSourceManager.VIDEOS_ID:
                return 0;
            default:
                //fallback value
                return 0;
        }
    }
    
    public void sendExternalSyncProgeress(String progress, int sourceId){
//        Intent i = new Intent(ACTION_SYNC_PROGRESS);
//        i.putExtra(EXTRAS_SYNC_PROGRESS, progress);
//        i.putExtra(EXTRAS_SYNCID, sourceId);
//        PendingIntent pi = PendingIntent.getBroadcast(App.i().getApplicationContext(), 0, i,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager am = (AlarmManager) App.i().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC, 0, pi);
//        App.i().getApplicationContext().sendBroadcast(i);
    }
    
    public int getEdiskId(){
        return EbenConst.EBEN_EDISK_ID;
    }
    public int getEnoteId(){
        return EbenConst.EBEN_ENOTE_ID;
    }
    public int getEdrawerId(){
        return EbenConst.EBEN_EDRAWER_ID;
    }
    public int getEwriterId(){
        return EbenConst.EBEN_EWRITER_ID;
    }
    public int getEnetclipId(){
        return EbenConst.EBEN_ENETCLIP_ID;
    }  
    public int getBookmarkId(){
        return EbenConst.EBEN_BOOKMARK_ID;
    }    
    public int getEimageId(){
        return EbenConst.EBEN_IMAGE_ID;
    } 
    private static final String CONFIG_SOURCE_SYNC_STATUS = "cn.eben.sourcesync.status";
    private static final String CONFIG_SOURCE_SYNC_DIRECTION = "cn.eben.sourcesync.refreshDirection";
    private static final String CONFIG_SOURCE_SYNCFAILURE_TIME = "cn.eben.sourcesync.failuretime";
      
    public static void saveSourceSyncStatus(Screen screen, long sourceId, boolean isSuccess, int refreshDirection){
            if(screen == null) return;

            
            if(sourceId == EbenConst.EBEN_CARDNAME_GROUP_ID || sourceId == EbenConst.EBEN_CARDNAME_DATA_ID){
            	 sourceId = EbenConst.EBEN_CARDNAME_CARD_ID;
            }
            
            if(sourceId == EbenConst.EBEN_CAL_ALARM_ID ){
           	 sourceId = EbenConst.EBEN_CAL_CALENDAR_ID;
           }
            
            Activity activity = (Activity)screen.getUiScreen();
            if(activity != null) {
	            SharedPreferences sharedP = activity.getPreferences(Context.MODE_PRIVATE);
	            sharedP.edit().putBoolean(CONFIG_SOURCE_SYNC_STATUS + sourceId, isSuccess).commit();
	            sharedP.edit().putInt(CONFIG_SOURCE_SYNC_DIRECTION + sourceId, refreshDirection).commit();
	            sharedP.edit().putLong(CONFIG_SOURCE_SYNCFAILURE_TIME + sourceId, new Date().getTime()).commit();
            }
    }
     
    public static boolean isSourceSyncSuccess(Screen screen, long sourceId){
        if(screen == null || screen.getUiScreen() == null) return true;
        Activity activity = (Activity)screen.getUiScreen();
        SharedPreferences sharedP = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedP.getBoolean(CONFIG_SOURCE_SYNC_STATUS + sourceId, true);
         
    }
    public static String loadSyncFailureTime(Activity activity, long sourceId){
        if(activity == null) return null;
        SharedPreferences sharedP = activity.getPreferences(Context.MODE_PRIVATE);
        long failureTime = sharedP.getLong(CONFIG_SOURCE_SYNCFAILURE_TIME + sourceId, new Date().getTime());
        //CharSequence val = DateFormat.format("yyyy/MM/dd HH:mm", new Date(failureTime));
        CharSequence val = EbenHelpers.toFormatDate(failureTime, EbenConst.formatData);
        return val.toString();
        
    }
    public static int loadRefreshDirection(Screen screen, long sourceId){
        if(screen == null || screen.getUiScreen()==null) return -1;
        Activity activity = (Activity)screen.getUiScreen();
        SharedPreferences sharedP = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedP.getInt(CONFIG_SOURCE_SYNC_DIRECTION + sourceId, -1);
        
    }
    
    public static final  int NOTEPAD_VERSIONCODE = 99;
    
    public static boolean checkNotepadVersionCode(PackageManager pManager){
        
        try {
//            String[] versionName = pManager.getPackageInfo("com.ebensz.notepad", 0).versionName.split("\\.");
//
//            if(versionName.length == 3){
//                if(Integer.valueOf(versionName[0]) > 1){
//                    return true;
//                }
//                if(Integer.valueOf(versionName[1]) > 2 && Integer.valueOf(versionName[2]) > 1){
//                    return true;
//                }
//            }
            return pManager.getPackageInfo("com.ebensz.notepad", 0).versionCode > NOTEPAD_VERSIONCODE;
        } catch (NameNotFoundException e) {
            // not
        }
        return false;
    }
    
    final public static class ActivationDeviceInfo{
        final String  mUserName;
        final String  mMod;
        final String    mCatime;
        final boolean mIsOpen;
        final String  mSerial_no;
        final String  mNickname;
        
        public String getUserName(){
            return mUserName;
        }
        
        public String getMod(){
            return mMod;
        }
        
        public String getCatime(){
            return mCatime;
        }
        
        public boolean getIsOpen(){
            return mIsOpen;
        }
        
        public String getSerial_no(){
            return mSerial_no;
        }
        
        public String getNickname(){
            return mNickname;
        }
        
        private ActivationDeviceInfo(String name, String mod, String catime, boolean isOpen, String serial_no, String nickname) {

            mUserName = name;
            mMod = mod;
            mCatime = catime;
            mIsOpen = isOpen;
            mSerial_no = serial_no;
            mNickname = nickname;
        }
        
        public static ActivationDeviceInfo valueOf(JSONObject user) {

            try {
                final String userName = user.has("username") ? user.getString("username") : null;
                final boolean isopen = user.has("isopen") ? (user.getInt("isopen")==1? true:false) : false;
                final String mod = user.has("mod") ? user.getString("mod") : null;
                final String catime = user.has("catime") ? user.getString("catime") : null;
                final String serial_no = user.has("serial_no") ? user.getString("serial_no") : null;
                String nickname = user.has("nickname") ? user.getString("nickname") : null;
                String strnickname = new String(nickname.getBytes(), "UTF-8");
                
//                CharSequence val = DateFormat.format("yyyy-MM-dd h:mm", new Date(catime));
                
                return new ActivationDeviceInfo(userName, mod, catime, isopen, serial_no, strnickname);
            } catch (final JSONException ex) {
                // Error parsing JSON user object
            }
            catch (UnsupportedEncodingException e) {
             // Error parsing JSON user object
            }
            return null;
        }
    }


    
    @Override
    public int getEbenOkIcon(){
    	return R.drawable.icon_complete;
    }
    
    @Override
    public int getEbenErrorIcon(){
    	return R.drawable.icon_failed_complete;
    }
}
