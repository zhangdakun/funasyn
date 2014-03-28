package com.eben.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class ExpandedListView extends ListView {

	private final String TAG = "ExpandedListView";
	public ExpandedListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Log.d(TAG, "ExpandedListView");
	}

	public ExpandedListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		Log.d(TAG, "ExpandedListView,2");
	}

	public ExpandedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Log.d(TAG, "ExpandedListView,1");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec( Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
	}

}
