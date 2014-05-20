package cn.eben.android.source.edisk;

import java.io.File;

import android.app.Activity;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;



import cn.eben.androidsync.R;

import com.funambol.android.activities.settings.AndroidSettingsUISyncSource;
import com.funambol.util.Log;

public class EdiskSettingsUISyncSource extends AndroidSettingsUISyncSource {
	 private static final String TAG = "EdiskSettingsUISyncSource";

	    private TextView fileSizeAlert;
	    private TextView lblAlert;
	    private boolean syncOlderInitialValue;

	    public EdiskSettingsUISyncSource(Activity activity) {

	        super(activity);

	        // Converts 12 dip into its equivalent px
	        Resources r = getResources();
	        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, r.getDisplayMetrics());
	        
	        fileSizeAlert = new TextView(activity);
	        fileSizeAlert.setPadding((int)px, fileSizeAlert.getPaddingTop(), 0,
	                fileSizeAlert.getPaddingBottom());
	        fileSizeAlert.setTextAppearance(this.getContext(), R.style.funambol_small_text);
	        
	        lblAlert = new TextView(activity);
	        lblAlert.setPadding((int)px, lblAlert.getPaddingTop(), 0,
	                lblAlert.getPaddingBottom());
	        lblAlert.setTextAppearance(this.getContext(), R.style.funambol_small_text);
	    }

	    @Override
	    public void layout() {
	        super.layout();

	        //first of all, get the default mediahub folder
	        String mediaHubPath = "---";
	        if (null != appSyncSource) {
	            EdiskAppSyncSourceConfig config = ((EdiskAppSyncSourceConfig)appSyncSource.getConfig());
	            if (null != config) {
	                File file = new File(config.getBaseDirectory());
	                mediaHubPath = file.getName();
	            } else {
	                Log.error(TAG, "config is null " + appSyncSource.getConfig());
	            }
	        } else {
	            Log.error(TAG, "appSyncSource is null");
	        }
	        String alertMessage = loc.getLanguage("conf_file_path_text")
	                .replaceAll("__FOLDER__", mediaHubPath);
	        

	        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
	                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	        
	        lblAlert.setText(alertMessage);
//	        mainLayout.addView(lblAlert, lp);
	        
	        fileSizeAlert.setText(loc.getLanguage("description_file"));
//	        mainLayout.addView(fileSizeAlert, lp);
	    }   
}
