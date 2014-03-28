package com.eben.activities;

import cn.eben.androidsync.R;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TwoLevelSyncStatePreference extends Preference {

	public static final String TAG = "TwoLevelSyncStatePreference";
	public TwoLevelSyncStatePreference(Context context) {
		super(context);
		Log.d(TAG,"TwoLevelSyncStatePreference");
		initLayout();
	}

	  public TwoLevelSyncStatePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initLayout();
		// TODO Auto-generated constructor stub
	}

	public TwoLevelSyncStatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
		// TODO Auto-generated constructor stub
	}

	private void initLayout()
	  {
		Log.d(TAG,"initLayout");
	    super.setLayoutResource(R.layout.ebencloud_settings_preference);
	    super.setWidgetLayoutResource(R.layout.preference_widget_sub_sync_toggle);
	    

	  }
	  
	  public static TwoLevelSyncStatePreference create(Context paramContext, PreferenceManager paramPreferenceManager)
	  {
		  Log.d(TAG,"create");
	    TwoLevelSyncStatePreference sPreference = new TwoLevelSyncStatePreference(paramContext);
	    sPreference.onAttachedToHierarchy(paramPreferenceManager);
	    return sPreference;
	  }

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		Log.d(TAG,"onBindView");
//		TextView actText = (TextView) view.findViewById(R.id.sync_status);
		
		boolean isAuto = getContext().getSharedPreferences("eben_para", 0).
				getBoolean("auto_sync", false);
//		sync_status
		TextView sync_status = (TextView) view.findViewById(R.id.sync_status);
		if(isAuto)
			sync_status.setText(R.string.cloud_open);
		else 
			sync_status.setText(R.string.cloud_close);
		
//		if(null != actText)
//		actText.setText(getContext().getString(R.string.sync_on));
		
//		actText.setText("sync on");
	}
	  
}
