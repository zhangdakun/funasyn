package com.eben.activities;

import cn.eben.androidsync.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class EbenAdaptiveTextView extends TextView {

	private int mCurrentWidth = -1;
	private int mHeight = -1;
	private final float mMaxTextSize = super.getTextSize();
	private final float mMinTextSize;
	private final int mRightMargin;
	
	private final String TAG = "MiuiAdaptiveTextView";

	  public EbenAdaptiveTextView(Context paramContext)
	  {
	    super(paramContext);
	    this.mMinTextSize = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_title_height);
	    this.mRightMargin = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_user_id_text_size);
	  }

	  public EbenAdaptiveTextView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    this.mMinTextSize = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_title_height);
	    this.mRightMargin = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_user_id_text_size);
	  }

	  public EbenAdaptiveTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
	  {
	    super(paramContext, paramAttributeSet, paramInt);
	    this.mMinTextSize = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_title_height);
	    this.mRightMargin = paramContext.getResources().getDimensionPixelSize(R.dimen.cloud_settings_user_id_text_size);
	  }

}
