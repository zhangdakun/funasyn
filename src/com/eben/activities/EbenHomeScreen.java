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

package com.eben.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.eben.androidsync.R;

import com.eben.client.Constants;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.android.BuildInfo;
import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.activities.AndroidUISyncSource;
import com.funambol.android.controller.AndroidHomeScreenController;
import com.funambol.android.source.pim.PimTestRecorder;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.Controller;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.DisplayManager;
import com.funambol.client.ui.HomeScreen;
import com.funambol.client.ui.UISyncSourceContainer;
import com.funambol.util.Log;
import com.umeng.analytics.MobclickAgent;

/**
 */
public class EbenHomeScreen extends Activity implements HomeScreen, UISyncSourceContainer {
//	public class EbenHomeScreen extends PreferenceActivity implements HomeScreen, UISyncSourceContainer {
    private static final String TAG = "EbenHomeScreen";

    private static final String FIRST_SYNC_ALERT_PENDING = "FirstSyncAlertPending";
    private static final String WIFI_NOT_AVAILABLE_ALERT_PENDING = "WifiNotAvailableAlertPending";

    private final int SETTINGS_ID = Menu.FIRST;
    private final int LOGOUT_ID   = SETTINGS_ID + 1;
    private final int ABOUT_ID    = LOGOUT_ID + 1;

    // These two constants are used only in test recording mode /////////
    private final int START_TEST_ID    = ABOUT_ID + 1;
    private final int END_TEST_ID      = START_TEST_ID + 1;
    /////////////////////////////////////////////////////////////////////
   
    private final int SYNC_SOURCE_ID = Menu.FIRST;
    private final int GOTO_SOURCE_ID = SYNC_SOURCE_ID + 1;
    private final int SETTINGS_SOURCE_ID = GOTO_SOURCE_ID + 1;
    private final int CANCEL_SOURCE_ID = SETTINGS_SOURCE_ID + 1;

    public static AndroidHomeScreenController homeScreenController;
    public static List<AndroidUISyncSource> listItems = new ArrayList<AndroidUISyncSource>();

    private Localization localization;
    public static Customization customization;
    private AndroidAppSyncSourceManager appSyncSourceManager;
    private AndroidDisplayManager dm;

    private Button syncAllButton;
    private LinearLayout mainLayout;
    private LinearLayout buttons;
    private String syncAllText = null;

    private SetSyncAllTextUIThread setSyncAllTextUIThread = new SetSyncAllTextUIThread();
    private SetSyncAllEnabledUIThread setSyncAllEnabledUIThread = new SetSyncAllEnabledUIThread();
    private RedrawUIThread redrawUIThread = new RedrawUIThread();
    private UpdateAvailableSourcesUIThread updateAvailableSourcesUIThread = new UpdateAvailableSourcesUIThread(this);

    // This is the sync item menu entry. It is global because we need to
    // dynamically change its label, depending on the sync status
    private String syncItemText;

    public static boolean screenLocked = false;

    public static Fragment fsources;
    public static Fragment fcontacts;
    
    LinearLayout contact_layout;
	public static int viewid = 0;
	
	protected List dataMapList;
	ListView backupList;
	BackupListAdapter myAdapter;
	 
	 
    private void addDatatoMap(String name,int image)//(DataType datatype)
    {
//        mDataList.add(datatype);
        HashMap hashmap = new HashMap();
//        hashmap.put("DATA_TYPE", datatype);
        hashmap.put("DATA_NAME", name);//getString(/*CommonFunctionsStringRes.getDataNameRes(datatype)*/));
        hashmap.put("PERCENT", null);
//        hashmap.put("STATUS", image);
        hashmap.put("animationDrawable", Boolean.valueOf(false));
        hashmap.put("drawn", Boolean.valueOf(false));
        hashmap.put("STATUS", getResources().getDrawable(image));
        
        dataMapList.add(hashmap);
    }
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {

        // Set up the activity
    	Log.debug(TAG, "onCreate 0");
        super.onCreate(icicle);
        Log.debug(TAG, "onCreate 1");
        MobclickAgent.onError(this);
        Log.debug(TAG, "onCreate 2");
        // Lock the screen orientation to vertical for this screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.debug(TAG, "onCreate 3");
        AppInitializer initializer = App.i().getAppInitializer();
        Log.debug(TAG, "onCreate 4");
        initializer.init(this);
        
        Log.debug(TAG, "after init app");
        
        // Initialize the localization
        localization = initializer.getLocalization();

        // By default we set the multi buttons layout
//        setMultiButtonsLayout();
//        fsources = new SourceListFragment();
//        fcontacts = new ContactsFragment();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        
//        getSupportFragmentManager().beginTransaction().add(R.id.main_home, fsources)
//        	.add(R.id.main_home, fcontacts).commit();

        // Now initialize everything
        customization = initializer.getCustomization();
        appSyncSourceManager = initializer.getAppSyncSourceManager();

        Controller controller = initializer.getController();
        homeScreenController = (AndroidHomeScreenController)controller.getHomeScreenController();
        homeScreenController.setHomeScreen(this);

        this.dm = (AndroidDisplayManager) controller.getDisplayManager();
        setContentView(R.layout.eben_pim_main);
        
        dataMapList = new ArrayList();
        addDatatoMap(this.getString(R.string.eben_backup),R.drawable.backup);
        addDatatoMap(this.getString(R.string.eben_restore),R.drawable.restore);
        
        setEbenLayout();
        
//        contact_layout = (LinearLayout) findViewById(R.id.contact_layout);
//        contact_layout.setClickable(true);
//        contact_layout.setOnClickListener(viewListener);
//        
        // We have to explicitely call the initialize here
        initialize(homeScreenController);

        // Refresh the set of available sources
        homeScreenController.updateEnabledSources();
        homeScreenController.selectFirstAvailable();

        homeScreenController.attachToRunningSyncIfAny();

        int firstSyncAlertId = 0;
        int wifiNotAvailableId = 1;

        if (icicle != null) {
            firstSyncAlertId = icicle.getInt(FIRST_SYNC_ALERT_PENDING);
            wifiNotAvailableId = icicle.getInt(WIFI_NOT_AVAILABLE_ALERT_PENDING);
        }
        if (firstSyncAlertId == DisplayManager.FIRST_SYNC_DIALOG_ID) {
            if (Log.isLoggable(Log.INFO)) {
                Log.info(TAG, "Removing bundle property and displaying alert after rotation");
            }
            icicle.remove(FIRST_SYNC_ALERT_PENDING);
            //Resume the last sync dialog alert if it was displayed before
            //resuming this activity
            controller.getDialogController().resumeLastFirstSyncDialog(this);
        } else if(wifiNotAvailableId == DisplayManager.NO_WIFI_AVAILABLE_ID) {
            if (Log.isLoggable(Log.INFO)) {
                Log.info(TAG, "Removing bundle property and displaying alert after rotation");
            }
            icicle.remove(WIFI_NOT_AVAILABLE_ALERT_PENDING);
            //Resume the WI-FI not available dialog alert if it was displayed before
            //resuming this activity
            controller.getDialogController().resumeWifiNotAvailableDialog(this);
        } else {
            // We shall remove all pending alerts here, in the case the app was
            // closed and restarted
            dm.removePendingAlert(DisplayManager.FIRST_SYNC_DIALOG_ID);
            // There is another case we must handle. The application (UI) was closed but a
            // automatic sync triggered the first sync dialog. In this case we
            // don't have anything in the activity state, but we need to show
            // the alert
            homeScreenController.showPendingFirstSyncQuestion();
        }

        // If during the upgrade some source was disabled because its sync type
        // is no longer supported, then we shall inform the user
        Configuration configuration = initializer.getConfiguration();
        if (Log.isLoggable(Log.INFO)) {
            Log.info(TAG, "source sync type changed = " + configuration.getPimSourceSyncTypeChanged());
        }
        if (configuration.getPimSourceSyncTypeChanged()) {
            dm.showOkDialog(this, localization.getLanguage("upg_one_way_no_longer_supported"),
                                  localization.getLanguage("dialog_ok"));
            configuration.setPimSourceSyncTypeChanged(false);
            configuration.commit();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        // Do not change anything
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dm.isAlertPending(DisplayManager.FIRST_SYNC_DIALOG_ID)) {
            dm.dismissSelectionDialog(DisplayManager.FIRST_SYNC_DIALOG_ID);
            outState.putInt(FIRST_SYNC_ALERT_PENDING, DisplayManager.FIRST_SYNC_DIALOG_ID);
        } else if (dm.isAlertPending(DisplayManager.NO_WIFI_AVAILABLE_ID)) {
            dm.dismissSelectionDialog(DisplayManager.NO_WIFI_AVAILABLE_ID);
            outState.putInt(WIFI_NOT_AVAILABLE_ALERT_PENDING, DisplayManager.NO_WIFI_AVAILABLE_ID);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG, "onCreateDialog: " + id);
        }
        Dialog result = null;
        if(dm != null) {
            result = dm.createDialog(id);
        }
        if(result != null) {
            return result;
        } else {
            return super.onCreateDialog(id);
        }
    }

    @Override
    public void onDestroy() {
        if (Log.isLoggable(Log.DEBUG)) {
            Log.debug(TAG, "Nullifying home screen controller reference");
        }
     
        homeScreenController.setHomeScreen(null);
        super.onDestroy();
    }

    /** Create the Activity menu. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (syncItemText == null) {
            syncItemText = localization.getLanguage("menu_sync");
        }

//        MenuItem settingsItem = menu.add(0, SETTINGS_ID, Menu.NONE, localization.getLanguage("menu_settings"));
//        settingsItem.setIcon(android.R.drawable.ic_menu_preferences);
        MenuItem logoutItem = menu.add(0, LOGOUT_ID, Menu.NONE, localization.getLanguage("menu_logout"));
        logoutItem.setIcon(R.drawable.ic_menu_logout);
//        MenuItem aboutItem = menu.add(0, ABOUT_ID, Menu.NONE, localization.getLanguage("menu_about"));
//        aboutItem.setShortcut('0', 'A');
//        aboutItem.setIcon(android.R.drawable.ic_menu_info_details);

        // This code is here only for the test recording build
        if (BuildInfo.TEST_RECORDING_ENABLED) {
            MenuItem startTestItem = menu.add(0, START_TEST_ID, Menu.NONE, "Start new test");
            MenuItem endTestItem = menu.add(0, END_TEST_ID, Menu.NONE, "End test");
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        
        homeScreenController.setForegroundStatus(false);
        Log.trace(TAG, "Paused activity (foreground status off)");
    }
	private void addSettingFragment() {
		Log.debug(TAG,"addSettingFragment");
		EbenCloudSettingsFragment fragment = new EbenCloudSettingsFragment();

		getFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(android.R.id.content, fragment, "activated").commit();
	}    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        homeScreenController.setHomeScreen(this);
        
        homeScreenController.setForegroundStatus(true);
        Log.trace(TAG, "Resumed activity (foreground status on)");
        String user = App.i().getAppInitializer().getConfiguration().getUsername();
        String name = App.i().getAppInitializer().getConfiguration().getSyncConfig().getUserName();
        Log.debug(TAG, "user, " +user+", name , "+name);
        if(!App.i().getAppInitializer().getConfiguration().getCredentialsCheckPending()) {
        	SharedPreferences sp =App.i().
    		getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE);
        	if(!sp.contains(Constants.XMPP_ORI_USERNAME)) {
	        Editor editor = sp.edit();
	        
	        editor.putString(Constants.XMPP_ORI_USERNAME,user);
	        editor.commit();
        	}
//        	this.sendBroadcast(new Intent(Constants.ACTION_START_EBP));
//        	mHandler.sendEmptyMessageDelayed(0, 2*1000);

        }
        
        setEbenLayout();
//        addSettingFragment();
    }

	Handler mHandler = new Handler() {
//		Context context;
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
		        String user = App.i().getAppInitializer().getConfiguration().getUsername();
		        String name = App.i().getAppInitializer().getConfiguration().getSyncConfig().getUserName();
		        Log.debug(TAG, "user, " +user+", name , "+name);
		        
//		        Editor editor = App.i().
//		        		getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
//		                Context.MODE_PRIVATE).edit();
//		        editor.putString(Constants.XMPP_ORI_USERNAME,user);
//		        editor.commit();
		        Intent in = new Intent(Constants.ACTION_START_EBP);
		        in.putExtra(Constants.PARA_USER, user);
				App.i().getApplicationContext().sendBroadcast(in);
				break;
			case 2:
				Intent intent = new Intent();
				if(0 == msg.getData().getInt("index")) {
					intent.putExtra("type", 0);
				} else {
					intent.putExtra("type", 1);
				}
				intent.setClass(EbenHomeScreen.this, BackupActivity.class);
				
				
				EbenHomeScreen.this.startActivity(intent);
				break;
			default:
				break;
			}
		}

	};
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions
        switch (item.getItemId()) {
            case SETTINGS_ID:
                homeScreenController.showConfigurationScreen();
                break;
            case LOGOUT_ID:
                homeScreenController.logout();
                break;
            case ABOUT_ID:
                homeScreenController.showAboutScreen();
                break;

            //// This code is for test recording mode only ///////////////
            case START_TEST_ID:
                PimTestRecorder.getInstance().startTestPressed(this);
                break;
            case END_TEST_ID:
                PimTestRecorder.getInstance().endTestPressed();
                break;
            //// This code is for test recording mode only ///////////////
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        v.requestFocus();

        AppSyncSource appSource = appSyncSourceManager.getSource(v.getId());
        createContextMenuForSource(appSource, menu);
    }

    public void createContextMenuForSource(AppSyncSource appSource, ContextMenu menu) {
        if (appSource != null) {
            if (!appSource.isEnabled() || !appSource.isWorking()) {
                // If we get a requirement to allow sources to be enabled via
                // context menu, this can be done here
                return;
            }

            if (homeScreenController.isSynchronizing()) {
                // If a sync is in progress, the context menu can only be used
                // to stop the current sync of the current source
                AppSyncSource currentSource = homeScreenController.getCurrentSource();
                if (currentSource != null && currentSource.getId() == appSource.getId()) {
                    int cancelId = appSource.getId() << 16 | CANCEL_SOURCE_ID;
                    menu.add(0, cancelId, 0, localization.getLanguage("menu_cancel_sync"));
                }
            } else {
                // This works if the number of sources is < 16 which is a fairly
                // safe assumption
                int syncId = appSource.getId() << 16 | SYNC_SOURCE_ID;
                int gotoId = appSource.getId() << 16 | GOTO_SOURCE_ID;
                int settingsId = appSource.getId() << 16 | SETTINGS_SOURCE_ID;

                StringBuffer label = new StringBuffer();
                label.append(localization.getLanguage("menu_sync")).append(" ").append(appSource.getName());
                menu.add(0, syncId, 0, label.toString());
                // Add goto menu option only if an external app manager is set
                if(appSource.getAppManager() != null) {
                    label = new StringBuffer();
                    label.append(localization.getLanguage("menu_goto")).append(" ").append(appSource.getName());
                    menu.add(0, gotoId, 0, label.toString());
                }
                menu.add(0, settingsId, 0, localization.getLanguage("menu_settings"));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        int id = item.getItemId();
        int sourceId = id >> 16;
        int itemId   = id & 0xFFFF;

        AppSyncSource appSource = appSyncSourceManager.getSource(sourceId);

        if (appSource == null) {
            Log.error(TAG, "Cannot find view associated to this context menu");
            return super.onContextItemSelected(item);
        }

        switch (itemId) {
            case SYNC_SOURCE_ID:
                homeScreenController.syncMenuSelected();
                return true;
            case GOTO_SOURCE_ID:
                homeScreenController.gotoMenuSelected();
                return true;
            case SETTINGS_SOURCE_ID:
                homeScreenController.showConfigurationScreen();
                return true;
            case CANCEL_SOURCE_ID:
                homeScreenController.cancelMenuSelected();
                return true;
            default:
                Log.error(TAG, "Unknwon context menu id " + id);
                return super.onContextItemSelected(item);
        }
    }


    /**************** Home Screen Implementation **********************/

    public void initialize(HomeScreenController controller) {
        if (Log.isLoggable(Log.INFO)) {
            Log.info(TAG, "Initializing");
        }

        // We force the controller to recompute the available sources
        homeScreenController.updateAvailableSources();
        homeScreenController.redraw();
        // Now update the list of visible items in the UI
//        updateVisibleItems();
        
//        getSupportFragmentManager().beginTransaction().hide(fsources).
//        hide(fcontacts).show(fsources).commit();

//        getSupportFragmentManager().beginTransaction().hide(fsources).
//        hide(fcontacts).show(fcontacts).commit();
        
    }

    public void lock() {
        screenLocked = true;
    }

    public void unlock() {
        screenLocked = false;
    }

    public boolean isLocked() {
        return screenLocked;
    }

    public void setSelectedIndex(int index) {
        // We can receive events before the list is actually populated. Just
        // ignore them
        if (listItems.isEmpty()) {
            return;
        }
        AndroidUISyncSource button = listItems.get(index);
        // Show the given element as selected
        button.setSelection(true, false);
        button.requestFocus();
    }

    public void deselectIndex(int index) {
        // We can receive events before the list is actually populated. Just
        // ignore them
        if (listItems.isEmpty()) {
            return;
        }
        AndroidUISyncSource button = listItems.get(index);
        // Show the given element as selected
        button.setSelected(false);
    }

    public void redraw() {
    	Log.debug(TAG, "redraw");
//        runOnUiThread(redrawUIThread);
    }

    public void updateVisibleItems() {
        runOnUiThread(updateAvailableSourcesUIThread);
//    	getSupportFragmentManager().beginTransaction().hide(fsources).show(fsources);
    	
//    	setEbenLayout();
    }

    //////////////////////////////////////////////////////////////////////

    public Object getUiScreen() {
        return this;
    }
	private ImageView mAvatarView;
	private View mTitleView;
	private TextView mUserIdView;
	private TextView contact_summary;
	
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
	
    private void setEbenLayout() {
        // Set the content view
//        setContentView(R.layout.eben_homescreen);
//    	setContentView(R.layout.eben_pim_main);
//		ActionBar localActionBar = getActionBar();
		Log.debug(TAG,"setEbenLayout");
//		View custom = getLayoutInflater().inflate(R.layout.eben_cloud_title, null);
//		if (localActionBar != null) {
//			localActionBar.setCustomView(custom,
//					new  ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//			getActionBar().setDisplayShowHomeEnabled(false);
//
//			getActionBar().setDisplayShowTitleEnabled(false);
//
//			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//
//			getActionBar().setDisplayShowCustomEnabled(true);
			this.mAvatarView = ((ImageView) findViewById(R.id.avatar));
			this.mUserIdView = ((TextView) findViewById(R.id.user_id));
			this.mTitleView = findViewById(R.id.title_bar);
			
			String user = App.i().getAppInitializer().getConfiguration().getUsername();
			if(null !=user && !user.isEmpty())
			mUserIdView.setText(user);
//		} else {
//			Log.error(TAG, "get action bar null!!!");
//		}
        // Grab the views 
//        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);

			
			boolean isAuto = this.getSharedPreferences("eben_para", 0).
					getBoolean("auto_sync", false);
//			sync_status
			TextView sync_status = (TextView) this.findViewById(R.id.sync_status);
			if(isAuto)
				sync_status.setText(R.string.cloud_open);
			else 
				sync_status.setText(R.string.cloud_close);
	
	        contact_layout = (LinearLayout) findViewById(R.id.contact_layout);
	        contact_layout.setClickable(true);
	        contact_layout.setOnClickListener(viewListener);
	        
	        contact_summary = (TextView) findViewById(R.id.contact_summary);
			AppSyncSource appSource = 
					(AppSyncSource)homeScreenController.getVisibleItems().get(0);
	        long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
	        
	        
			if(0 != lastSyncTS) {
				contact_summary.setText(mDateFormat.format(new Date(lastSyncTS)));
			} else {
				contact_summary.setText("");
			}
			
			backupList = (ListView) findViewById(R.id.listViewbackup);
			
			myAdapter = new BackupListAdapter(this, R.layout.backuplist, dataMapList,mHandler);
			backupList.setAdapter(myAdapter);
			
//			backupList.setOnItemClickListener(listener);
    }
    OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.debug(TAG, "onItemClick, "+view.getId()+", pos, "+position+", id , "+id);
			
		}
    	
    };
//    private void setMultiButtonsLayout() {
//        // Set the content view
//        setContentView(R.layout.homescreen);
//
//        // Grab the views 
//        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
//        buttons = (LinearLayout)findViewById(R.id.buttons);
//        syncAllButton = (Button)findViewById(R.id.syncAllButton);
//
//        // Add the sync all button if required
//        if (syncAllText != null) {
//            syncAllButton.setFocusable(false);
//            syncAllButton.setText(syncAllText);
//            SyncAllButtonListener buttonListener = new SyncAllButtonListener();
//            syncAllButton.setOnClickListener(buttonListener);
//        } else {
//            // Remove the button bar
//            LinearLayout buttonBar = (LinearLayout)findViewById(R.id.homeScreenButtonBar);
//            mainLayout.removeView(buttonBar);
//        }
//    }

//    private void setSingleButtonLayout() {
//        setContentView(R.layout.homescreen_single);
//        mainLayout = (LinearLayout)findViewById(R.id.mainLayoutSingle);
//        buttons = mainLayout;
//        syncAllButton = null;
//    }

    private class ButtonListener implements OnClickListener {

        private int idx;

        public ButtonListener(int idx) {
            this.idx = idx;
        }

        public void onClick(View v) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Clicked on item: " + idx + " hasFocus=" + v.hasFocus());
            }
            if (!screenLocked) {
                v.requestFocus();
                homeScreenController.buttonPressed(idx);

            }
        }
    }

    private class SyncAllButtonListener implements OnClickListener {

        public SyncAllButtonListener() {
        }

        public void onClick(View v) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Clicked on sync all");
            }
            homeScreenController.syncAllPressed();
        }
    }

    private class AloneButtonListener implements OnClickListener {

        public AloneButtonListener() {
        }

        public void onClick(View v) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Clicked on the alone source");
            }
            if(!homeScreenController.isSynchronizing())
            homeScreenController.aloneSourcePressed();
        }
    }

    /**
     * A call-back for when the sync buttons focus changes
     */
    private class FocusListener implements OnFocusChangeListener {
        private int idx;

        public FocusListener(int idx) {
            this.idx = idx;
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Focus moved to item: " + idx);
            }
            if (!screenLocked && hasFocus) {
                homeScreenController.buttonSelected(idx);
            }
        }
    }

    public void addSyncAllButton(String text, Bitmap icon, Bitmap bg, Bitmap bgSel) {
        // We ignore the icons because on Android we keep the button very
        // simple, just text
        syncAllText = text;
    }

    public void setSyncAllText(String text) {
        setSyncAllTextUIThread.setText(text);
        runOnUiThread(setSyncAllTextUIThread);
    }

    public String getSyncAllText() {
        if (syncAllButton != null) {
            return syncAllButton.getText().toString();
        } else {
            return null;
        }
    }

    public void setSyncAllEnabled(boolean enabled) {
        setSyncAllEnabledUIThread.setEnabled(enabled);
        runOnUiThread(setSyncAllEnabledUIThread);
    }

    public void setSyncAllSelected(boolean selected) {
        // Ignore this because in this view the button is separate from the
        // sources list
    }

    public void setSyncMenuText(String text) {
        syncItemText = text;
    }

    private class RedrawUIThread implements Runnable {

        public RedrawUIThread() {
        }

        public void run() {
            mainLayout.invalidate();
        }
    }

    private class SetSyncAllTextUIThread implements Runnable {
        private String text;

        public SetSyncAllTextUIThread() {
        }

        public void setText(String text) {
            this.text = text;
        }

        public void run() {
            if (syncAllButton != null) {
                syncAllButton.setText(text);
            }
        }
    }

    private class SetSyncAllEnabledUIThread implements Runnable {
        private boolean enabled;

        public SetSyncAllEnabledUIThread() {
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void run() {
            if (syncAllButton != null) {
                syncAllButton.setClickable(enabled);
            }
        }
    }

    private class UpdateAvailableSourcesUIThread implements Runnable {

        private EbenHomeScreen screen;

        public UpdateAvailableSourcesUIThread(EbenHomeScreen screen) {
            this.screen = screen;
        }

        public void run() {
        	setEbenLayout();

//            if (homeScreenController == null) {
//                return;
//            }
//
//            // Remove all buttons first
//            buttons.removeAllViews();
//
//            // Initialize the sources listed
//            Vector appSources = homeScreenController.getVisibleItems();
//            Enumeration iter  = appSources.elements();
//
//            int idx = 0;
//
//            while(iter.hasMoreElements()) {
//                AppSyncSource appSource = (AppSyncSource) iter.nextElement();
//
//
//                UISyncSourceController itemController;
//                itemController = appSource.getUISyncSourceController();
//
//                // Prepare the source icon to be displayed
//                Bitmap sourceIcon = null;
//                if (appSource.isWorking() && appSource.isEnabled()) {
//                    sourceIcon = customization.getSourceIcon(appSource.getId());
//                } else {
//                    sourceIcon = customization.getSourceDisabledIcon(appSource.getId());
//                }
//
//                // Create an item for each entry, if there is only one entry,
//                // then we build a stand alone representation
//                AndroidUISyncSource item = null;
//                LinearLayout.LayoutParams lp = null;
//                if (idx == 0 && !iter.hasMoreElements()) {
//                    // The source is alone
//                    item = (AndroidUISyncSource)appSource.createAloneUISyncSource(screen);
//                    if (item != null) {
//                        // Change the overall layout
//                        setSingleButtonLayout();
//                        // Remove the sync all button
//                        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//                        int margin = adaptSizeToDensity(10);
//                        lp.setMargins(0, 0, 0, margin);
//                        // Disable the status animation supplied by the controller
//                        itemController.disableStatusAnimation();
//                        // Set localization and customization
//                        if (item instanceof EbenAloneUISyncSource) {
//                        	EbenAloneUISyncSource aloneItem = (EbenAloneUISyncSource)item;
//                            aloneItem.setLocalization(localization);
//                            aloneItem.setCustomization(customization);
//                            aloneItem.setHomeScreenView(EbenHomeScreen.this);
//                        }
//                        // Register the button listener
//                        AloneButtonListener buttonListener = new AloneButtonListener();
//                        item.setOnClickListener(buttonListener);
//                    }
//                }
//                if (item == null) {
//                    if (idx == 0) {
//                        setMultiButtonsLayout();
//                    }
//                    item = (AndroidUISyncSource)appSource.createButtonUISyncSource(screen);
//                    // The buttons shall only wrap the content
//                    lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//                    int margin = adaptSizeToDensity(2);
//                    lp.setMargins(margin, margin, margin, margin);
//                    // Enable the status animation supplied by the controller
//                    itemController.enableStatusAnimation();
//                    // Register the button listener
//                    ButtonListener buttonListener = new ButtonListener(idx);
//                    item.setOnClickListener(buttonListener);
//
//                }
//                item.setSource(appSource);
//
//                itemController.setUISyncSource(item);
//
//                // All these buttons are associated to a given application
//                // source
//                item.setSource(appSource);
//                item.setContainer(screen);
//
//                if (sourceIcon != null) {
//                    item.setIcon(sourceIcon);
//                }
//
//                item.setTitle(appSource.getName());
//                listItems.add(item);
//
//                // Add this button to the main list and the appropriate listeners
//                FocusListener  focusListener = new FocusListener(idx);
//                item.setOnFocusChangeListener(focusListener);
//                // We use the app source id as view id so we can quickly recognize
//                // it in the context menu handling
//                item.setId(appSource.getId());
//                registerForContextMenu(item);
//                buttons.addView(item, lp);
//                idx++;
//            }
        }
    }

    private int adaptSizeToDensity(int size) {
        return (int)(size*getResources().getDisplayMetrics().density);
    }

    long exitTime = 0;
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		if(0 != viewid) {
//	        getSupportFragmentManager().beginTransaction().hide(fsources).
//	        hide(fcontacts).show(fsources).commit();
//		} else 
		{
	        if((System.currentTimeMillis()-exitTime) > 2000){  
		            Toast.makeText(getApplicationContext(),getString(R.string.press_exit), Toast.LENGTH_SHORT).show();                                
		            exitTime = System.currentTimeMillis();   
		        } else {
		            finish();
		            System.exit(0);
		        }


		}
	}
    
	private void Exit() {
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
				this);
		builder.setTitle("\u63D0\u793A");
		builder.setIcon(R.drawable.logo);
		builder.setMessage("\u662F\u5426\u8981\u9000\u51FA");
		builder.setPositiveButton("\u662F",
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialoginterface, int i) {
						finish();
						System.exit(0);
					}

				});
		builder.setNegativeButton("\u5426",
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialoginterface, int i) {
						dialoginterface.dismiss();
					}

				});
		builder.show();
	}

//	@Override
//	@Deprecated
//	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
//			Preference preference) {
//		Log.debug(TAG,"onPreferenceTreeClick");
//		if("cloud_contacts".equalsIgnoreCase(preference.getKey())) {
//			startConacts();
//		}
//		
//		return true;
//	}

	private void startConacts() {
		// TODO Auto-generated method stub
		Log.debug(TAG, "startConacts");
		Intent intent = new Intent();
		
		intent.setClass(this, EbenContactActivity.class);
		
		this.startActivity(intent);
	}

	View.OnClickListener viewListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.debug(TAG, "onClick");
			
			if(v.getId() == R.id.contact_layout) {
				 startConacts();
			}
		}
	};
	
	public void onClick(View v) {
		Log.debug(TAG, "onClick2");
		
		if(v.getId() == R.id.contact_layout) {
			 startConacts();
		}
	
	}
	
}
