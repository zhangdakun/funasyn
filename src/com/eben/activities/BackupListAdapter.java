// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.eben.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.funambol.android.App;
import com.funambol.util.Log;

import cn.eben.androidsync.R;

public class BackupListAdapter extends BaseAdapter
{
    public static final class ViewHolder
    {

        public ImageView mImage;
//        public Button mInstall;
//        public TextView mPercentText;
        public TextView mText;

        public ViewHolder()
        {
        }
    }


    public BackupListAdapter(Context context1, int i, List list)
    {
        mapList = null;
        layoutInflater = LayoutInflater.from(context1);
        layoutID = i;
        mapList = list;
        context = context1;
        installMethod = 1;
        curClickIndex = -1;
    }
    Handler mHandler;
    public BackupListAdapter(Context context1, int i, List list, Handler mHandler)
    {
        mapList = null;
        layoutInflater = LayoutInflater.from(context1);
        layoutID = i;
        mapList = list;
        context = context1;
        this.mHandler = mHandler;
        curClickIndex = -1;
    }



    private void clearAnimationRotate(ImageView imageview)
    {
//        if(imageview != null && AnimationRotateMgr.getInstance().getInitialized())
//            imageview.clearAnimation();
    }

    private android.view.View.OnClickListener clickInstallButtonListener(final int index)
    {
        return new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
//                File file = new File(buildApkPath((String)((Map)mapList.get(index)).get("CLOUDAPPFULLNAME")));
//                if(!file.exists())
//                {
//                    Logging.d("-------------------not exist");
//                    Toast.makeText(BackupApplication.getContext(), (new StringBuilder(String.valueOf(context.getString(0x7f09020b)))).append(((Map)mapList.get(index)).get("CLOUDAPPFULLNAME")).toString(), 0).show();
//                    curClickIndex = -1;
//                } else
//                {
//                    Intent intent = new Intent("android.intent.action.VIEW");
//                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//                    curClickIndex = index;
//                    context.startActivity(intent);
//                }
            }
//
//            final DataProcessListAdapter this$0;
//            private final int val$index;

            
            {
//                this$0 = DataProcessListAdapter.this;
//                index = i;
//                super();
            }
        }
;
    }

    private void drawInstallButton(int i, ViewHolder viewholder)
    {
//        viewholder.mImage.setVisibility(8);
//        viewholder.mInstall.setVisibility(0);
//        if(((Boolean)((Map)mapList.get(i)).get("MAP_IS_INSTALL")).booleanValue())
//            viewholder.mInstall.setText(context.getString(0x7f090058));
//        else
//            viewholder.mInstall.setText(context.getString(0x7f09020a));
    }

    private boolean isInstallNoRoot()
    {
        boolean flag;
        if(installMethod == 0)
            flag = true;
        else
            flag = false;
        return flag;
    }

    private void setStaticPic(int i, ViewHolder viewholder)
    {
//        if(isInstallNoRoot() && ((Boolean)((Map)mapList.get(i)).get("MAP_APPDOWNLOAD_SUCCESS")).booleanValue())
//            drawInstallButton(i, viewholder);
//        else
            viewholder.mImage.setBackgroundDrawable((Drawable)((Map)mapList.get(i)).get("STATUS"));
//    	viewholder.mImage.set
//            viewholder.mImage.set
    }

    private void startRunningAnimationRotate(ImageView imageview)
    {
//        if(imageview != null)
//        {
//            AnimationRotateMgr animationrotatemgr = AnimationRotateMgr.getInstance();
//            if(!animationrotatemgr.getInitialized())
//                animationrotatemgr.initAnimation();
//            imageview.startAnimation(animationrotatemgr.getAnimation());
//        }
    }

    public int getCount()
    {
        return mapList.size();
    }

    public int getCurClickIndex()
    {
        return curClickIndex;
    }

    public Object getItem(int i)
    {
        return mapList.get(i);
    }

    public long getItemId(int i)
    {
        return (long)i;
    }

    public View getView(int i, View view, ViewGroup viewgroup)
    {
        ViewHolder viewholder;
        if(view == null)
        {
            viewholder = new ViewHolder();
            view = layoutInflater.inflate(layoutID, null);
            viewholder.mText = (TextView)view.findViewById(R.id.list_title);
            viewholder.mImage = (ImageView)view.findViewById(R.id.list_icon);
//            viewholder.mPercentText = (TextView)view.findViewById(0x7f0c0059);
//            viewholder.mInstall = (Button)view.findViewById(0x7f0c005a);
            view.setTag(viewholder);
        } else
        {
            viewholder = (ViewHolder)view.getTag();
        }
        viewholder.mText.setText((String)((Map)mapList.get(i)).get("DATA_NAME"));
//        if(((Map)mapList.get(i)).get("PERCENT") != null)
//        {
//            viewholder.mPercentText.setVisibility(0);
//            viewholder.mPercentText.setText((String)((Map)mapList.get(i)).get("PERCENT"));
//        } else
//        {
//            viewholder.mPercentText.setVisibility(8);
//        }
//        viewholder.mInstall.setVisibility(8);
//        viewholder.mInstall.setOnClickListener(clickInstallButtonListener(i));
//        if(((Map)mapList.get(i)).get("animationDrawable") != null && ((Boolean)((Map)mapList.get(i)).get("animationDrawable")).booleanValue())
//        {
//            viewholder.mImage.setVisibility(0);
//            clearAnimationRotate(viewholder.mImage);
//            viewholder.mImage.setBackgroundDrawable((Drawable)((Map)mapList.get(i)).get("STATUS"));
//            startRunningAnimationRotate(viewholder.mImage);
//        } else
        if(((Map)mapList.get(i)).get("STATUS") != null)
        {
            viewholder.mImage.setVisibility(View.VISIBLE);
            clearAnimationRotate(viewholder.mImage);
//            setStaticPic(i, viewholder);
            viewholder.mImage.setImageDrawable((Drawable)((Map)mapList.get(i)).get("STATUS"));
        } else
        {
            clearAnimationRotate(viewholder.mImage);
            viewholder.mImage.setVisibility(8);
        }
        
        view.setOnClickListener(listener);
        return view;
    }
    OnClickListener  listener = new OnClickListener (){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.debug("backup", "onClick");
			
//			Log.debug("backup", "start backup");
//			Intent intent = new Intent();
//			
//			intent.setClass(App.i().getApplicationContext(), BackupActivity.class);
//			
//			App.i().getApplicationContext().startActivity(intent);
//			
			if(null != mHandler) {
				mHandler.sendEmptyMessage(2);
			}
		}
    	
    };
    public static final String MAP_APPDOWNLOAD_SUCCESS = "MAP_APPDOWNLOAD_SUCCESS";
    public static final String MAP_CLOUDAPPFULLNAME = "CLOUDAPPFULLNAME";
    public static final String MAP_DATA_DRAWN = "drawn";
    public static final String MAP_DATA_NAME = "DATA_NAME";
    public static final String MAP_DATA_PERCENT = "PERCENT";
    public static final String MAP_DATA_STATUS = "STATUS";
    public static final String MAP_DATA_TYPE = "DATA_TYPE";
    public static final String MAP_IS_INSTALL = "MAP_IS_INSTALL";
    private Context context;
    private int curClickIndex;
    private int installMethod;
    private int layoutID;
    private LayoutInflater layoutInflater;
    private List mapList;




}
