/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.eben.activities;

import android.widget.LinearLayout;
import android.widget.ImageView;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.app.Activity;

import cn.eben.androidsync.R;
import com.funambol.android.activities.AndroidUISyncSource;
import com.funambol.util.Log;

public class EbenSourceUISyncSource extends AndroidUISyncSource {

    private static final String TAG = "EbenButtonUISyncSource";

    private Handler handler;
    public EbenSourceUISyncSource(Activity activity) {
        super(activity);

        this.activity = activity;

        LayoutInflater inflater = activity.getLayoutInflater();
        
        this.addView(inflater.inflate(R.layout.eben_pim_layout, null),
        		new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        activity.ge
//        this.setBackgroundResource(R.drawable.sync_shape);
//
//        LinearLayout statusIconViewContainer = new LinearLayout(activity);
//        statusIconViewContainer.setPadding(0, adaptSizeToDensity(TOP_PADDING),
//                adaptSizeToDensity(STATUS_ICON_R_PADDING),
//                adaptSizeToDensity(BOTTOM_PADDING));
//
//        statusIconViewContainer.addView(statusIconView);
//
//        LinearLayout ll2 = new LinearLayout(activity);
//        // This is the weight parameter that we associate to the central item
//        // (title and status) so it is grown to occupy all the available space
//        // (pushing the iconcs on the left/right)
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                                                          LayoutParams.WRAP_CONTENT,
//                                                          1.0f);
//        ll2.setLayoutParams(lp);
//        ll2.addView(titleTextView, lp);
//        ll2.addView(statusTextView, lp);
//        ll2.setOrientation(LinearLayout.VERTICAL);
//        ll2.setPadding(0, adaptSizeToDensity(TOP_PADDING),
//                       0, adaptSizeToDensity(BOTTOM_PADDING));
//
//        sourceIconView.setAdjustViewBounds(true);
//        sourceIconView.setScaleType(ImageView.ScaleType.FIT_XY);
//
//        // Sets the linear layout
//        ButtonLayout ll1 = new ButtonLayout(activity, sourceIconView, ll2, statusIconViewContainer);
//        // All items in ll1 are vertically centered
//        ll1.setGravity(Gravity.CENTER_VERTICAL);
//        ll1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
//                                                          LayoutParams.WRAP_CONTENT));
//
//        ll1.addView(sourceIconView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                                                                  LayoutParams.WRAP_CONTENT));
//        ll1.addView(ll2);
//        ll1.addView(statusIconViewContainer);
//
//        this.addView(ll1);
//
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        setClickable(true);
    }

    /**
     * This is the layout manager for the button. It works under the assumtpion
     * that 3 views are added. The first one being an ImageView (source icon),
     * the second one being a LinearLayout and the third one being another
     * LinearLayout. The main purpose of this component, is to limit the size of
     * the source icon, according to the size of the central field. In
     * particular the height of this field is used to bound the maxSize for the
     * icon.
     */
    private class ButtonLayout extends LinearLayout {

        private boolean first = true;

        private ImageView imageView;
        private LinearLayout textView;
        private LinearLayout statusView;

        public ButtonLayout(Activity activity, ImageView imageView, LinearLayout textView, LinearLayout statusView) {
            super(activity);
            this.imageView = imageView;
            this.textView = textView;
            this.statusView = statusView;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // First of all measure the text part
            textView.measure(widthMeasureSpec, heightMeasureSpec);

            // Measure the status
            statusView.measure(widthMeasureSpec, heightMeasureSpec);
 
            // Now limit the max size for the icon and then compute its measure
            int height = textView.getMeasuredHeight();
            // We occupy 80% of the available space
            height = (height * 8) / 10;

            imageView.setMaxWidth(height);
            imageView.setMaxHeight(height);
            imageView.measure(widthMeasureSpec, heightMeasureSpec);

            // Now compute this item measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

	@Override
	public void syncEnded() {
		
		if(null != handler) {
			handler.sendEmptyMessage(0);
		}
	}

	@Override
	public void setSelection(boolean selected, boolean fromUi) {
		// TODO Auto-generated method stub
//		super.setSelection(selected, fromUi);
	}

	@Override
	public void setProgress(int percentage) {
		// TODO Auto-generated method stub
//		super.setProgress(percentage);
		Log.debug(TAG, "");
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub
//		super.redraw();
	}

	@Override
	public void syncStarted() {
		// TODO Auto-generated method stub
		super.syncStarted();
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
    
    
}

