package com.eben.activities;


import java.util.Enumeration;
import java.util.Vector;

import cn.eben.androidsync.R;
import com.funambol.android.activities.AndroidUISyncSource;
import com.funambol.client.controller.UISyncSourceController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.Bitmap;
import com.funambol.util.Log;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class SourceListFragment extends Fragment{
	public final String TAG = "SourceListFragment";
	
	private LinearLayout buttons;
	private UpdateAvailableSourcesUIThread updateAvailableSourcesUIThread;
	
	private FragmentActivity mActivity;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		buttons = (LinearLayout) getActivity().findViewById(R.id.sourcelayout) ; 
//		updateAvailableSourcesUIThread = new UpdateAvailableSourcesUIThread((EbenHomeScreen) getActivity());
		mActivity = getActivity();
		Log.debug(TAG, "onActivityCreated");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.debug(TAG, "onCreateView");
		
//		return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.eben_source_list, container, false);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
//		super.onHiddenChanged(hidden);		
		Log.debug(TAG, "onHiddenChanged , hidden = "+hidden);
		
		if(!hidden) {
			EbenHomeScreen.viewid = 0;
			getActivity().runOnUiThread(updateAvailableSourcesUIThread);
		}
		
	}

	private class UpdateAvailableSourcesUIThread implements Runnable {

        private EbenHomeScreen screen;

        public UpdateAvailableSourcesUIThread(EbenHomeScreen screen) {
            this.screen = screen;
        }

        public void run() {

            if (EbenHomeScreen.homeScreenController == null) {
                return;
            }

            // Remove all buttons first
            buttons.removeAllViews();

            // Initialize the sources listed
            Vector appSources = EbenHomeScreen.homeScreenController.getVisibleItems();
            Enumeration iter  = appSources.elements();

            int idx = 0;

            while(iter.hasMoreElements()) {
                AppSyncSource appSource = (AppSyncSource) iter.nextElement();


                UISyncSourceController itemController;
                itemController = appSource.getUISyncSourceController();

                // Prepare the source icon to be displayed
                Bitmap sourceIcon = null;
                if (appSource.isWorking() && appSource.isEnabled()) {
                    sourceIcon = EbenHomeScreen.customization.getSourceIcon(appSource.getId());
                } else {
                    sourceIcon = EbenHomeScreen.customization.getSourceDisabledIcon(appSource.getId());
                }

                // Create an item for each entry, if there is only one entry,
                // then we build a stand alone representation
                AndroidUISyncSource item = null;
                LinearLayout.LayoutParams lp = null;

                if (item == null) {
                    if (idx == 0) {
//                        setMultiButtonsLayout();
                    }
                    item = (AndroidUISyncSource)appSource.createButtonUISyncSource(screen);
                    // The buttons shall only wrap the content
                    lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                    int margin = adaptSizeToDensity(2);
                    lp.setMargins(margin, margin, margin, margin);
                    // Enable the status animation supplied by the controller
                    itemController.enableStatusAnimation();
                    // Register the button listener
                    ButtonListener buttonListener = new ButtonListener(idx);
                    item.setOnClickListener(buttonListener);

                }
                item.setSource(appSource);

                itemController.setUISyncSource(item);

                // All these buttons are associated to a given application
                // source
                item.setSource(appSource);
                item.setContainer(screen);

                if (sourceIcon != null) {
                    item.setIcon(sourceIcon);
                }

                item.setTitle(appSource.getName());
                EbenHomeScreen.listItems.add(item);

                // Add this button to the main list and the appropriate listeners
                FocusListener  focusListener = new FocusListener(idx);
                item.setOnFocusChangeListener(focusListener);
                // We use the app source id as view id so we can quickly recognize
                // it in the context menu handling
                item.setId(appSource.getId());
                registerForContextMenu(item);
                buttons.addView(item, lp);
                
                buttons.requestFocus();//lieb
                
                idx++;
            }
        }
    }
	
    private int adaptSizeToDensity(int size) {
        return (int)(size*getResources().getDisplayMetrics().density);
    }	
    
    private class ButtonListener implements OnClickListener {

        private int idx;

        public ButtonListener(int idx) {
            this.idx = idx;
        }

        public void onClick(View v) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Clicked on item: " + idx + " hasFocus=" + v.hasFocus());
            }
            if (!EbenHomeScreen.screenLocked) {
                v.requestFocus();
//                EbenHomeScreen.homeScreenController.buttonPressed(idx);
                mActivity.getSupportFragmentManager().beginTransaction().hide(EbenHomeScreen.fsources).
                hide(EbenHomeScreen.fcontacts).show(EbenHomeScreen.fcontacts).commit();
            }
        }
    }
    private class FocusListener implements OnFocusChangeListener {
        private int idx;

        public FocusListener(int idx) {
            this.idx = idx;
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (Log.isLoggable(Log.DEBUG)) {
                Log.debug(TAG, "Focus moved to item: " + idx);
            }
            if (!EbenHomeScreen.screenLocked && hasFocus) {
            	EbenHomeScreen.homeScreenController.buttonSelected(idx);
            }
        }
    } 
    private void setMultiButtonsLayout() {
        // Set the content view
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
    }
    
}
