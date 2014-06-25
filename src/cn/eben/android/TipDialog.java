package cn.eben.android;


import com.eben.activities.EbenHomeScreen;

import cn.eben.androidsync.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class TipDialog {

//	public static final Object syncSingal = new Object();
//	public static boolean lock = false;
	public void showTip(final Activity activity){
		
		SharedPreferences sp = activity.getSharedPreferences("net", Context.MODE_PRIVATE);
		
		boolean tip = sp.getBoolean("tip", false);
		if (tip) {
			launche(activity);

			return;
		}
		
		Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, android.R.style.Theme_DeviceDefault_Light));//.Theme_Holo_Dialog));//
		builder.setTitle(R.string.network_title);
		builder.setMessage(R.string.network_msg);
		builder.setNegativeButton(R.string.network_refuse, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				SharedPreferences sp = activity.getSharedPreferences("net", Context.MODE_PRIVATE);
				SharedPreferences.Editor  editor = sp.edit();
				editor.putBoolean("tip", false);
				editor.clear();
				editor.commit();

				dialog.dismiss();
				activity.finish();
				

			}
			
		});
        builder.setPositiveButton(R.string.network_agree, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				launche(activity);

			}
			
		});
        CheckBox cb = new CheckBox(activity);
        cb.setText(R.string.not_pop);
        
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences sp = activity.getSharedPreferences("net", Context.MODE_PRIVATE);
				SharedPreferences.Editor  editor = sp.edit();
				editor.putBoolean("tip", isChecked);
				editor.clear();
				editor.commit();
			}
        	
        });
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(10, 0, 0, 0);
		cb.setLayoutParams(lp);
		cb.setTextColor(Color.BLACK);
		LinearLayout linearLayout = new LinearLayout(activity);
		linearLayout.addView(cb);

		// View view = View.inflate(activity, R.layout.tip_network, null);
		builder.setView(linearLayout);

		Dialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}
	
	public void launche(final Activity activity){
		Intent i = new Intent();
		i.setClass(activity, EbenHomeScreen.class);
		activity.startActivity(i);
		activity.finish();
	}
	

}
