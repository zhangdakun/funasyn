package com.eben.activities;

import cn.eben.androidsync.R;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EbenCloudSettingsActivity extends PreferenceActivity {
	private ImageView mAvatarView;
	private View mTitleView;
	private TextView mUserIdView;
	
	private final String TAG = "MiCloudSettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar localActionBar = getActionBar();
		Log.d(TAG,"onCreate");
		View custom = getLayoutInflater().inflate(R.layout.ebencloud_settings_title, null);
		if (localActionBar != null) {
			localActionBar.setCustomView(custom,
					new  ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			getActionBar().setDisplayShowHomeEnabled(false);

			getActionBar().setDisplayShowTitleEnabled(false);

			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

			getActionBar().setDisplayShowCustomEnabled(true);
			this.mAvatarView = ((ImageView) findViewById(R.id.avatar));
			this.mUserIdView = ((TextView) findViewById(R.id.user_id));
			this.mTitleView = findViewById(R.id.title_bar);
			
			
			mUserIdView.setText("123456789");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG,"onResume");
		addSettingFragment();

	}

	private void addSettingFragment() {
		Log.d(TAG,"addSettingFragment");
		EbenCloudSettingsFragment fragment = new EbenCloudSettingsFragment();

		getFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(android.R.id.content, fragment, "activated").commit();
	}

}
