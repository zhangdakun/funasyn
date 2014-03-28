package com.eben.activities;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.funambol.client.source.AppSyncSource;
import com.funambol.util.Log;

import cn.eben.androidsync.R;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class EbenCloudSettingsFragment extends PreferenceFragment {
	private View mRootView;
	private final String TAG = "MiCloudSettingsFragment";
	
	TwoLevelSyncStatePreference mPrefContact;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.debug(TAG,"onCreateView");
		mRootView = inflater.inflate(R.layout.ebencloud_settings, container, false);
		super.addPreferencesFromResource(R.xml.ebencloud_settings_preference);
		
		AppSyncSource appSource = 
				(AppSyncSource) EbenHomeScreen.homeScreenController.getVisibleItems().get(0);
        long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
        
        this.mPrefContact = ((TwoLevelSyncStatePreference)super.findPreference("cloud_contacts"));
        
		if(0 != lastSyncTS) {
			mPrefContact.setSummary(mDateFormat.format(new Date(lastSyncTS)));
		} else {
			mPrefContact.setSummary("");
		}
		

//		this.getPreferenceScreen();
//		mRootView.findViewById(R.id.list).setSelector(new ColorDrawable(Color.BLACK));
//		this.getPreferenceScreen().getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));
//		mRootView.setBackgroundResource(R.drawable.itembg);
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.debug(TAG,"onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		Log.debug(TAG,"onPreferenceTreeClick");
		if("cloud_contacts".equalsIgnoreCase(preference.getKey())) {
			startConacts();
		}
		
		return true;
	}
	private void startConacts() {
		// TODO Auto-generated method stub
		Log.debug(TAG, "startConacts");
		Intent intent = new Intent();
		
		intent.setClass(getActivity(), ContactActivity.class);
		
		getActivity().startActivity(intent);
	}

}
