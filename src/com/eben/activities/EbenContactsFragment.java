package com.eben.activities;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import cn.eben.androidsync.R;
import com.funambol.android.activities.AndroidUISyncSource;
import com.funambol.client.controller.UISyncSourceController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.Bitmap;
import com.funambol.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EbenContactsFragment extends Fragment{
	public final String TAG = "ContactsFragment";
	
	private LinearLayout buttons;
	private UpdateAvailableSourcesUIThread updateAvailableSourcesUIThread;
	
	private ImageView mPimRunningView;
	private ImageView mHomeBack;
	private Button mPimStartButton;
	
	private TextView mPimSyncStateTips;
	private FragmentActivity mActivity;
	
	private long lastSyncTS = 0;
	private CheckBox mPimSyncCheckbox;
	private RelativeLayout mPimAutoSyncLayout;
	 
	private final String auto_sync = "auto_sync";
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
	
	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			switch (v.getId()) {
			case R.id.pim_manual_start:
				if(!EbenHomeScreen.homeScreenController.isSynchronizing()) {
					setPimRunningView();
					EbenHomeScreen.homeScreenController.buttonPressed(0);
				}
				break;
			case R.id.personal_home_back:
//                ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction().hide(EbenHomeScreen.fsources).
//                hide(EbenHomeScreen.fcontacts).show(EbenHomeScreen.fsources).commit();
				break;
			case R.id.pim_auto_sync_layout:
				if(!mPimSyncCheckbox.isChecked()) {
					Toast.makeText(mActivity.getApplicationContext(),
							getString(R.string.auto_contacts), Toast.LENGTH_SHORT).show();
					if(!EbenHomeScreen.homeScreenController.isSynchronizing()) {
						setPimRunningView();
						EbenHomeScreen.homeScreenController.buttonPressed(0);
					}
				}
				mActivity.getSharedPreferences("eben_para", 0).edit().putBoolean
					(auto_sync, !mPimSyncCheckbox.isChecked()).commit();
				mPimSyncCheckbox.setChecked(!mPimSyncCheckbox.isChecked());
				break;
			default:
				break;
			}
		}
		
	};
	
	private OnCheckedChangeListener checkListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			
		}
		
	};
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		buttons = (LinearLayout) getActivity().findViewById(R.id.contact_btn) ; 
//		updateAvailableSourcesUIThread = new UpdateAvailableSourcesUIThread((EbenHomeScreen) getActivity());
		
		Log.debug(TAG, "onActivityCreated");
		mActivity = getActivity();
//		lastSyncTS = appSource.getConfig().getLastSyncTimestamp()
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.debug(TAG, "onCreateView");
		
//		return super.onCreateView(inflater, container, savedInstanceState);
		View con = inflater.inflate(R.layout.eben_source_contacts, container, false);
//		TextView title = (TextView) con.findViewById(R.id.personal_home_title);
//		
//		title.setText(R.string.type_contacts);
		return con;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
//		super.onHiddenChanged(hidden);		
		Log.debug(TAG, "onHiddenChanged , hidden = "+hidden);
		
		if(!hidden) {
			EbenHomeScreen.viewid = 1;
			buttons.setVisibility(View.VISIBLE);
			getActivity().runOnUiThread(updateAvailableSourcesUIThread);
		} else {
			buttons.setVisibility(View.GONE);
		}
		
	}
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0: //sync end
				endRunningAnimation();
				AppSyncSource appSource = 
						(AppSyncSource) EbenHomeScreen.homeScreenController.getVisibleItems().get(0);
                lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
        		if(0 != lastSyncTS) {
        			mPimSyncStateTips.setText(mDateFormat.format(new Date(lastSyncTS)));
        		} else {
        			mPimSyncStateTips.setText(R.string.pim_sync_init_state_tips);
        		}
				break;

			default:
				break;
			}
		}
		
	};
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
                EbenSourceUISyncSource item = null;
                LinearLayout.LayoutParams lp = null;

                if (item == null) {
                    if (idx == 0) {
//                        setMultiButtonsLayout();
                    }
//                    item = (AndroidUISyncSource)appSource.createButtonUISyncSource(screen);
                    item = new EbenSourceUISyncSource(screen);
                    // The buttons shall only wrap the content
                    lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//                    int margin = adaptSizeToDensity(2);
//                    lp.setMargins(margin, margin, margin, margin);
                    // Enable the status animation supplied by the controller
                    itemController.enableStatusAnimation();
                    // Register the button listener
                    ButtonListener buttonListener = new ButtonListener(idx);
                    item.setOnClickListener(buttonListener);
                    item.setHandler(mHandler);
                }
                
                appSource.setUISyncSource(item);
                
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
//                EbenHomeScreen.listItems.add(item);

                // Add this button to the main list and the appropriate listeners
//                FocusListener  focusListener = new FocusListener(idx);
//                item.setOnFocusChangeListener(focusListener);
                // We use the app source id as view id so we can quickly recognize
                // it in the context menu handling
                item.setId(appSource.getId());
                registerForContextMenu(item);
                buttons.addView(item, lp);
        		TextView title = (TextView) buttons.findViewById(R.id.personal_home_title);
        		
        		title.setText(R.string.type_contacts);
                
        		mPimRunningView = (ImageView) buttons.findViewById(R.id.pim_running_view);
        		mPimStartButton = (Button) buttons.findViewById(R.id.pim_manual_start);
        		
        		mHomeBack = (ImageView) buttons.findViewById(R.id.personal_home_back);
        		mHomeBack.setOnClickListener(listener);
//                item.requestFocus();// lieb
//                idx++;
        		mPimStartButton.setOnClickListener(listener);
        		mPimSyncStateTips = ((TextView)buttons.findViewById(R.id.pim_sync_state_tips));
        	
                lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
        		if(0 != lastSyncTS) {
        			mPimSyncStateTips.setText(mDateFormat.format(new Date(lastSyncTS)));
        		}
        		
        		mPimSyncCheckbox = (CheckBox) buttons.findViewById(R.id.pim_sync_checkbox);
        		mPimSyncCheckbox.setChecked(mActivity.getSharedPreferences("eben_para", 0).getBoolean(auto_sync, false));
//        		mPimSyncCheckbox.set
        		mPimAutoSyncLayout = (RelativeLayout) buttons.findViewById(R.id.pim_auto_sync_layout);
        		mPimAutoSyncLayout.setOnClickListener(listener);
                break;
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
                EbenHomeScreen.homeScreenController.buttonPressed(idx);
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
    private void setPimRunningView()
    {
      startRunningAnimation();
      this.mPimSyncStateTips.setText(R.string.pim_sync_running_state_tips);
    }
    private void startRunningAnimation()
    {
      this.mPimRunningView.setBackgroundResource(R.drawable.contact_pim_running_arrow);
      Animation localAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_rotate_animation);
      localAnimation.setInterpolator(new LinearInterpolator());
      this.mPimRunningView.startAnimation(localAnimation);
      this.mPimStartButton.setEnabled(false);
    }
    private void endRunningAnimation()
    {
      this.mPimRunningView.clearAnimation();
      this.mPimRunningView.setBackgroundResource(R.drawable.contact_pim_normal_arrow);
      this.mPimStartButton.setEnabled(true);
    }
}
