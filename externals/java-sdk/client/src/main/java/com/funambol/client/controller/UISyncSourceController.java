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

package com.funambol.client.controller;

import java.util.Date;
import java.util.Enumeration;


import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.UISyncSource;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncListener;
import com.funambol.sync.SyncReport;
import com.funambol.sync.SyncSource;
import com.funambol.syncml.protocol.DevInf;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Log;



public class UISyncSourceController implements SyncListener {

    private static final String TAG_LOG = "UISyncSourceController";
    private Localization         localization = null;
    private Customization        customization = null;
    private AppSyncSourceManager appSyncSourceManager = null;
    private Controller           controller = null;
    private UISyncSource         uiSource   = null;
    private AppSyncSource        appSource  = null;
    private SyncSource 			syncSrc = null;

    private int              totalSent;
    private int              totalSending;
    private int              totalReceived;
    private int              totalReceiving;

    private long             currentSendingItemSize = 0;
    private long             currentReceivingItemSize = 0;

    private Bitmap           statusIcon = null;
    private Bitmap           statusSelectedIcon = null;
    private Bitmap           okIcon = null;
    private Bitmap           errorIcon = null;

    private SyncingAnimation animation = null;
    private SyncReport       lastSyncReport = null;

    private boolean          cancelling = false;
    private boolean          syncing    = false;

    private long             syncStartedTimestamp = 0;
    private long             syncLastedTimestamp = 0;

    // The progress is computed as follow: 50% for the connecting/sending phase
    // and 50% for the receiving/mapping phase
    // Connecting and mapping counts for one item
    private int              currentStep = 0;
    //complete counts
    private int              totalSucessCount = 0;
    private int              totalSucessBookCount = 0;
    private String mStatusMsg = null;
    //counts of sync source at one time.
    private int nSyncCount = 0;
    private int              totalSucessSentCount = 0;
    private int              totalSucessReceivedCount = 0;
    private int              totalSucessSentBookCount = 0;
    private int              totalSucessReceivedBookCount = 0;
    
	/** define the id for sync source. */
	public static final int EBEN_NOTEPAD_BOOK_ID = 512;
	public static final int EBEN_NOTEPAD_PAGE_ID = EBEN_NOTEPAD_BOOK_ID * 2;
	public static final int EBEN_EDISK_ID = EBEN_NOTEPAD_PAGE_ID * 2;
	public static final int EBEN_CARDNAME_GROUP_ID = EBEN_EDISK_ID * 2;
	public static final int EBEN_CARDNAME_CARD_ID = EBEN_CARDNAME_GROUP_ID * 2;
	public static final int EBEN_CARDNAME_DATA_ID = EBEN_CARDNAME_CARD_ID * 2;
	public static final int EBEN_CAL_CALENDAR_ID = EBEN_CARDNAME_DATA_ID * 2;
	public static final int EBEN_CAL_ALARM_ID = EBEN_CAL_CALENDAR_ID * 2;
	
	private boolean isSyncProgress = false;
	private boolean isFailed = false;
    private int MINUS_ONE = -1;
    private int           statusIconId = MINUS_ONE;     
    private int           okIconId = MINUS_ONE;
    private int           errorIconId = MINUS_ONE;
    
    private final int CONNECTING_PERCENT=1;
    private final int CONNECTED_PERCENT=3;
    private final int DATA_PERCENT=98;
    
    private void sendExternalSyncProgeress(String progress){
//    	if(uiSource != null && (isLastSource(syncSrc)||cancelling||isFailed )){
//    		if(localization.getLanguage("status_mapping").equals(progress)){    			
////        	   	uiSource.setProgress(100);	
//    		}	
////    		else if(progress.contains("eben_download_source_thistime")
////    			 || progress.contains("eben_upload_source_thistime")
////    			 || progress.contains("eben_backup_content_thistime")) {
////    			uiSource.setProgress(-1);	
////    		}
//    	}

//    	customization.sendExternalSyncProgeress(progress, appSource == null ? -1 : appSource.getId());
    }

    public UISyncSourceController(Customization customization, Localization localization,
                                  AppSyncSourceManager appSyncSourceManager,
                                  Controller controller, AppSyncSource appSource)
    {
        init(customization, localization, appSyncSourceManager, controller, appSource);
    }

    public UISyncSourceController() {
    }

    public void init(Customization customization, Localization localization,
                     AppSyncSourceManager appSyncSourceManager,
                     Controller controller, AppSyncSource appSource)
    {
        this.customization = customization;
        this.localization  = localization;
        this.appSyncSourceManager = appSyncSourceManager;
        this.controller = controller;
        this.appSource = appSource;

        okIcon = customization.getOkIcon();
        errorIcon = customization.getErrorIcon();
        okIconId = customization.getEbenOkIcon();
        errorIconId = customization.getEbenErrorIcon();
        
        statusSelectedIcon = customization.getStatusSelectedIcon();

        // Create the animation object (depends on the customization, so this
        // can only be invoked after the customization has been set)
        animation = new SourceSyncingAnimation();
    }

    public void setUISyncSource(UISyncSource uiSource) {
        this.uiSource = uiSource;
        if (uiSource != null) {
            String lastStatus;

            if (!appSource.isWorking()) {
                lastStatus = "home_not_available";//localization.getLanguage("home_not_available");
                uiSource.setEnabled(false);
            } else if (!appSource.getConfig().getEnabled()) {
                lastStatus = "home_disabled";//localization.getLanguage("home_disabled");
                uiSource.setEnabled(false);
            } else {
                int status = appSource.getConfig().getLastSyncStatus();
                if (status == SyncListener.COMPRESSED_RESPONSE_ERROR) {
                    return;
                }
                lastStatus = getLastSyncStatus(status, null);
                //statusIcon   = getLastSyncIcon(status);
                statusIconId = getLastSyncIconId(status);
                if (statusIconId != MINUS_ONE) {
                    uiSource.setStatusIconId(statusIconId);
                }
//                if (statusIcon != null) {
//                    uiSource.setStatusIcon(statusIcon);
//                }
                uiSource.setEnabled(true);
            }
            uiSource.setStatusString(lastStatus);
            uiSource.redraw();
            
        }
    }

    public boolean isSyncing() {
        return syncing;
    }

    public void disableStatusAnimation() {
        if(animation != null) {
            animation.stopAnimation();
        }
        animation = null;
    }

    public void enableStatusAnimation() {
        if(animation == null) {
            animation = new SourceSyncingAnimation();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endConnecting(int)
     */
    public void endConnecting(int action) {

    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endFinalizing()
     */
    public void endFinalizing() {
    	if(isLastSource(syncSrc)) {
	        if (uiSource != null) {
	            if (!cancelling) {
//	                uiSource.setStatusString(localization.getLanguage("status_mapping_done"));
	                uiSource.setProgress(99);
	                uiSource.redraw();
	            }
	        }
    	}
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endReceiving()
     */
    public void endReceiving() {
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSending()
     */
    public void endSending() {
    }

    public void disable() {
        String status;
        if (!appSource.isWorking()) {
            status = "home_not_available";//localization.getLanguage("home_not_available");
        } else {
            status = "home_disabled";//localization.getLanguage("home_disabled");
        }
        if (uiSource != null) {
            uiSource.setStatusString(status);
            Bitmap sourceIcon = customization.getSourceDisabledIcon(appSource.getId());
            if (sourceIcon != null) {
                uiSource.setIcon(sourceIcon);
            }
            uiSource.setStatusIcon(null);
            uiSource.setEnabled(false);
            uiSource.redraw();
        }

        sendExternalSyncProgeress(status);
    }

    public void enable() {
       if(uiSource == null) return;
        AppSyncSource appSource = uiSource.getSource();
        int status = appSource.getConfig().getLastSyncStatus();
        if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
            return;
        }            
        String lastStatus = getLastSyncStatus(status, null);
        sendExternalSyncProgeress(lastStatus);
        if (uiSource != null) {
            uiSource.setStatusString(lastStatus);
            Bitmap sourceIcon = customization.getSourceIcon(appSource.getId());
            if (sourceIcon != null) {
                uiSource.setIcon(sourceIcon);
            }
           
            statusIconId = getLastSyncIconId(status);
            if (statusIconId != MINUS_ONE) {
                uiSource.setStatusIconId(statusIconId);
            }
            uiSource.setEnabled(true);
            uiSource.redraw();
           
        }
    }
    

	 //lierbao 2011-9-22
     public void enableDefaultProgress(int direction, boolean isSyncFinish, int index) {
        if(uiSource == null) return;
         int status = appSource.getConfig().getLastSyncStatus();
         if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
             return;
         }   
         int iCount = appSource.getConfig().getSyncCount();
         String lastStatus = null;

         String res = null;
    	//add eddie
    	 if(!isSyncFinish || -1 == iCount){
    		 res =  "eben_sync_waiting";// localization.getLanguage("eben_sync_waiting");
    	     if(-1 == index) {// for sync ll    	     
	    	     if(1 == direction) //download
	    	    	 lastStatus = "eben_download_source";// res + localization.getLanguage("eben_download_source");
	    	     else if(2 == direction)    
	    		     lastStatus = "eben_download_source";//res + localization.getLanguage("eben_upload_source");
	    	     else { //sync
	    	    	 lastStatus = "eben_download_source";//res + localization.getLanguage("eben_sync_souce"); 
	    	     }  
//	    		 if((localization.getLanguage("type_eben_notepad").equalsIgnoreCase(appSource.getName()))){
//
//	        	     if(1 == direction) {
//	        	    	 lastStatus = "eben_download_source";//localization.getLanguage("eben_now_download_source"); 
//	        	     }
//	        	     else{
//	        	    	 lastStatus = "eben_download_source";//localization.getLanguage("eben_now_sync_souce"); 
//	        	     }     			 
//	    		 }  
    	     }
    	     else {
        	     if(1 == direction) {
        	    	 lastStatus = "eben_download_source";//localization.getLanguage("eben_now_download_source"); 
        	     }
        	     else{
        	    	 lastStatus = "eben_download_source";//localization.getLanguage("eben_now_sync_souce"); 
        	     }     	    	 
    	     }
    	 }
         if(nSyncCount > 0){
        	 nSyncCount = 0;
        	 appSource.getConfig().setSyncSourceIdFinish(false);
             appSource.getConfig().commit();
         }
         if(Log.isLoggable(Log.DEBUG))
            Log.debug(TAG_LOG, "lastStatus==..."+lastStatus);

         if (uiSource != null) {
             uiSource.setStatusString(lastStatus);           
             uiSource.setEnabled(true);
             uiSource.redraw();
            
         }
     }
   
   
    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSession(SyncReport)
     */
    public void endSession(SyncReport report) {
        if (!syncing) {
	        if (uiSource != null) {
	            uiSource.syncEnded();
	            
	        }
            return;
        }

        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "endSession");
        }

        lastSyncReport = report;
        int status = report.getStatusCode();

        if (Log.isLoggable(Log.INFO)) {
            Log.info(TAG_LOG, report.toString());
        }

        // Stop any animation in progress
        if (animation != null) {
            animation.stopAnimation();
        }
        
        //The following condition is made to trap the compression error when a 
        //wap compression error occur. 
        //Notice that this change introduce a dependency on the class SyncEngine 
        //and it can happen that the status is not correctly update the http 
        //compression is disabled.
        if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
            //This error is the result for a problem reading the compressed 
            //stream. In this case the sync client retries to send
            //an uncompressed request
            Log.error(TAG_LOG, "Compressed Header Error");
            return;
        }

        // set the status into the app source

        appSource.getConfig().setLastSyncStatus(status);	

        if(Log.isLoggable(Log.DEBUG)) {
        	Log.debug(TAG_LOG, "endSession totalSucess=="+totalSucessCount);
        	Log.debug(TAG_LOG, "endSession totalSucessBookCount=="+totalSucessBookCount);
        }
        totalSucessCount = totalSucessReceivedCount + totalSucessSentCount;
        appSource.getConfig().setSyncCount(totalSucessCount);
        totalSucessBookCount = totalSucessReceivedBookCount + totalSucessSentBookCount;
        appSource.getConfig().setSyncBookCount(totalSucessBookCount);
        //add 20111012
        saveConfigSyncFinishedValue(appSource.getConfig().getSyncDirection());
        syncLastedTimestamp = new Date().getTime();
        appSource.getConfig().setLastSyncTimestamp(syncLastedTimestamp);
        //add 20111103
        int alarmCount = getSentCount(syncSrc) + getReceivedCount(syncSrc);
        if(Log.isLoggable(Log.DEBUG)) {
        	Log.debug(TAG_LOG, "alarmCount== "+alarmCount);
        	Log.debug(TAG_LOG, "alarmCount== "+alarmCount);
        }
        appSource.getConfig().setSyncEbenAlarmCount(alarmCount);
        appSource.getConfig().commit();
        
        isFailed = isSyncFail(syncSrc);
        if(isLastSource(syncSrc)|| isFailed) {
        	String statusMsg;
        	if(isFailed)
        		statusMsg = getSimplyStatusString(appSource);
        	else
        		statusMsg =  getSyncFinshedString(appSource) ;

	        sendExternalSyncProgeress(statusMsg);
	        customization.sendExternalSyncProgeress(statusMsg, appSource == null ? -1 : appSource.getId());
	        // This source sync is over, set the proper status	        
	        if (uiSource != null) {
	            statusIconId   = getLastSyncIconId(status);
	            uiSource.setStatusString(statusMsg);
	            if (statusIcon != null) {
	                uiSource.setStatusIconId(statusIconId);
	            }
	            uiSource.syncEnded();
	            
	        }
	       
	        if (uiSource != null) {
	            uiSource.redraw();
	        }
	        cancelling = false;
	        syncing = false;
	
	        // Reset the overall status
	        resetInternalStatus();
        }
    }
    
    /**
     * Resets the current status
     */
    public void resetStatus() {
        // Stop any animation in progress
        if (animation != null) {
            animation.stopAnimation();
        }
        int status = appSource.getConfig().getLastSyncStatus();
        String lastStatus = getLastSyncStatus(status, null);
//        statusIcon = getLastSyncIcon(status);
//        sendExternalSyncProgeress(lastStatus);
//        if(uiSource != null){
//            uiSource.setStatusIcon(statusIcon);
//            uiSource.setStatusString(lastStatus);
//    
//            uiSource.syncEnded();
//            uiSource.redraw();
//        }
        
        statusIconId = getLastSyncIconId(status);
        sendExternalSyncProgeress(lastStatus);
        if(uiSource != null){
            uiSource.setStatusIconId(statusIconId);
            uiSource.setStatusString(lastStatus);
    
            uiSource.syncEnded();
            uiSource.redraw();
        }
        
        cancelling = false;
        syncing = false;
        
    }

    public void setSelected(boolean value, boolean fromUi) {
        if (uiSource != null && !cancelling) {
            // Sets the proper icon (if the source is enabled)
            if (appSource.getConfig().getEnabled()) {
                if (customization.showSyncIconOnSelection()) {
                    if (value) {
                        uiSource.setStatusIcon(statusSelectedIcon);
                    } else {
                        uiSource.setStatusIcon(statusIcon);
                    }
                } else {
                    uiSource.setStatusIcon(statusIcon);
                }
            }
            uiSource.setSelection(value, fromUi);
            uiSource.redraw();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSyncing()
     */
    public void endSyncing() {
        // We force the progress bar to the end so that also empty syncs move
        // the bar
    	
//        if (uiSource != null && isLastSource(syncSrc)) {
//            updateCurrentProgress(100);
//        }
    }

    /*
     * (non-Javadoc)
     */
    public void itemAddSendingEnded(String key, String parent) {
    }

    public void itemAddSendingStarted(String key, String parent, long size) {
        startSending(key, size);
    }

    public void itemAddSendingProgress(String key, String parent, long size) {
        if(0==size){ // for server space full
//            customization.sendExternalSyncProgeress(localization.getLanguage("eben_sync_space_full"), appSource == null ? -1 : appSource.getId());
        }
        else {
            sentProgress(key, size);
        }
    }

    public void itemAddReceivingEnded(String key, String parent) {
    }

    public void itemAddReceivingStarted(String key, String parent, long size) {
        startReceiving(key, size);
    }

    public void itemAddReceivingProgress(String key, String parent, long size) {
        receivedProgress(key, size);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemDeleteSent(java.lang.Object)
     */
    public void itemDeleteSent(SyncItem syncItem) {
        startSending(syncItem.getKey(), 0);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemDeleted(java.lang.Object)
     */
    public void itemDeleted(SyncItem item) {
        startReceiving(item.getKey(), 0);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemReplaceSent(java.lang.Object)
     */
    public void itemReplaceSendingStarted(String key, String parent, long size) {
        startSending(key, size);
    }

    public void itemReplaceSendingEnded(String key, String parent) {
        currentStep++;
//        updateCurrentProgress();
    }

    public void itemReplaceSendingProgress(String key, String parent, long size) {
        sentProgress(key, size);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemReplaceReceivingStarted(java.lang.Object)
     */
    public void itemReplaceReceivingStarted(String key, String parent, long size) {
        startReceiving(key, size);
    }

    public void itemReplaceReceivingEnded(String key, String parent) {
        currentStep++;
//        updateCurrentProgress();
    }

    public void itemReplaceReceivingProgress(String key, String parent, long size) {
        receivedProgress(key, size);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startConnecting()
     */
    public void startConnecting() {
		if(nSyncCount == 0){
			totalSucessCount = 0;
			totalSucessBookCount = 0;
			totalSucessSentCount = 0;        
			totalSucessReceivedCount = 0;    
			totalSucessSentBookCount = 0;    
			totalSucessReceivedBookCount = 0;
		}
//		if(appSource.getId() == EBEN_CARDNAME_CARD_ID){
//			nSyncCount++;
//		}
		
		nSyncCount++;
//		if(isFirstSource(syncSrc)) {
//	        sendExternalSyncProgeress(localization.getLanguage("status_connecting"));
//			if(isSyncMode(syncSrc)) {
//				sendExternalSyncProgeress(localization.getLanguage("eben_now_sync_souce"));
//			}
//			else {
//				sendExternalSyncProgeress(localization.getLanguage("eben_now_download_source"));
//			}
//	        if (uiSource != null) {
//	            if (animation != null && !animation.isRunning()) {
//	                animation.startAnimation();
//	            }
//	            if (!cancelling) {
//	                uiSource.setStatusString(localization.getLanguage("status_connecting"));
//	    			if(isSyncMode(syncSrc)) {
//	    				sendExternalSyncProgeress(localization.getLanguage("eben_now_sync_souce"));
//	    			}
//	    			else {
//	    				sendExternalSyncProgeress(localization.getLanguage("eben_now_download_source"));
//	    			}	
//	    			uiSource.setProgress(CONNECTING_PERCENT);
//	                uiSource.redraw();
//	            }
//	        }
//		}
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startFinalizing()
     */
    public void startFinalizing() {
//    	if(isLastSource(syncSrc)) {
//	        sendExternalSyncProgeress(localization.getLanguage("status_mapping"));
//	        if (uiSource != null) {
//	            if (!cancelling) {
//	                uiSource.setStatusString(localization.getLanguage("status_mapping"));
//	                uiSource.redraw();
//	            }
//	        }
//    	}
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startReceiving(int)
     */
    public void startReceiving(int numItems) {
        if (totalReceiving == ITEMS_NUMBER_UNKNOWN) {
            totalReceiving = numItems;
            //currentStep = 0;
            currentStep++;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSending(int, int, int)
     */
    public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
        totalSending = numNewItems + numUpdItems + numDelItems;
        currentStep++;
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSession()
     */
    public void startSession() {
        if (Log.isLoggable(Log.DEBUG)) {
            Log.debug(TAG_LOG, "startSession");
        }
        resetInternalStatus();
        // It is possible that this method gets invoked more than once. This is
        // the case because it is invoked by SyncManager but also by the
        // HomeScreenController.
        if (uiSource != null && !syncing) {
            uiSource.syncStarted();
        }
        syncStartedTimestamp = new Date().getTime();
        appSource.getConfig().setStartSyncTimestamp(syncStartedTimestamp);
        appSource.getConfig().setSyncIsCancel(false);
        appSource.getConfig().commit();
        syncing = true;
    }

    public void attachToSession() {
        if (Log.isLoggable(Log.INFO)) {
            Log.info(TAG_LOG, "Attaching to session");
        }
        syncing = true;
//        String text = localization.getLanguage("status_connecting");
//        sendExternalSyncProgeress(text);
        if (uiSource != null) {
            uiSource.syncStarted();
//            if(isFirstSource(syncSrc));
//            	uiSource.setStatusString(text);
//            if (animation != null && !animation.isRunning()) {
//                animation.startAnimation();
//            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSyncing(int, Object)
     */
    public boolean startSyncing(int mode, Object devInf) {
    	
    	if(isFirstSource(syncSrc)) {
	        if (uiSource != null) {
//	            uiSource.setStatusString(localization.getLanguage("eben_server_connected"));
	        	uiSource.setProgress(CONNECTED_PERCENT);
	            uiSource.redraw();
	        }
    	}
        if (mode == com.funambol.syncml.protocol.SyncML.ALERT_CODE_SLOW) {
            if (customization.confirmSlowSync()) {
                String text = "eben_download_source";//localization.getLanguage("status_confirm_slow");
                sendExternalSyncProgeress(text);
                if (uiSource != null) {
                    uiSource.setStatusString(text);
                    uiSource.redraw();
                }
                if (!confirmSlowSync()) {
                    abortSlow();
                    return false;
                }
            }
        }

        // If the server sends its capabilities, we must decode them and update
        // the configuration accordingly
        if (devInf != null) {
            if (Log.isLoggable(Log.INFO)) {
                Log.info(TAG_LOG, "Server sent its capabilities");
            }
            if (devInf instanceof DevInf) {
                controller.reapplyServerCaps((DevInf)devInf);
            }
        }

        return true;
    }

    public void startCancelling() {
//        sendExternalSyncProgeress(localization.getLanguage("status_cancelling"));
        if (uiSource != null) {
//            uiSource.setStatusString(localization.getLanguage("status_cancelling"));
            uiSource.redraw();
        }
        cancelling = true;
        appSource.getConfig().setSyncIsCancel(true);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#syncStarted(int)
     */
    public void syncStarted(int arg0) {
    	if(null != syncSrc && null != uiSource) {
//    		StringBuffer sb = new StringBuffer(localization.getLanguage(getSyncModeString(syncSrc)));
//            uiSource.setStatusString(sb.toString());
            uiSource.redraw();
    	}
    }

    public void removingAllData() {
//        sendExternalSyncProgeress(localization.getLanguage("status_recover"));
        if (uiSource != null) {
//            if (animation != null && !animation.isRunning()) {
//                animation.startAnimation();
//            }
//            uiSource.setStatusString(localization.getLanguage("status_recover"));
            uiSource.redraw();
        }
    }

    public void itemRemoved(int current, int size) {
//        StringBuffer sb = new StringBuffer(localization.getLanguage("status_removing_item"));
//        sb.append(" ").append(current);
//        sb.append("/").append(size);
//        if (Log.isLoggable(Log.TRACE)) {
//            Log.trace(TAG_LOG, "notifyRemoved " + sb.toString());
//        }
//        currentStep++;
//        sendExternalSyncProgeress(sb.toString());
        if (uiSource != null) {
            if (!cancelling) {
//                uiSource.setStatusString(sb.toString());
                uiSource.redraw();
            }
//            updateCurrentProgress();
        }
    }
    
    public Controller getController() {
        return controller;
    }

    public SyncReport getLastSyncReport() {
        return lastSyncReport;
    }

    public void setAnimationIcons(Bitmap[] icons) {
        if (animation != null) {
            animation.setAnimationIcons(icons);
        }
    }

    private void sentProgress(String key, long size) {
//        String currentItem = localization.getLanguage("eben_sync_current_item");
//        StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
//        StringBuffer sb = new StringBuffer(localization.getLanguage(getSyncModeString(syncSrc)));
        //add by eddie20110921
        //sb.append(" ").append("(").append(totalSent);

//        if (totalSending > 0) {
//            sb.append(" ").append("(").append(totalSent);
//            sb.append("/").append(totalSending).append(")");
//        }

        // This is a LO
        // Compute the percentage of what we have sent so far
        if(currentSendingItemSize > 0) {
            long perc = (size * 100) / currentSendingItemSize;
            if (perc > 100) {
                perc = 100;
            }
            //mark eddie 
            //sb.append(" (").append(currentItem).append(perc).append("%)");
        }
        if(!cancelling){
//            sendExternalSyncProgeress(sb.toString());
        }
        if (uiSource != null) {
            if (!cancelling) {
//                uiSource.setStatusString(sb.toString());
                uiSource.redraw();
            }
        }
    }

    private void receivedProgress(String key, long size) {
//    	String currentItem = localization.getLanguage("eben_sync_current_item");
    	totalReceived++;
		if(appSource.getId() == EBEN_CARDNAME_CARD_ID){
			if(nSyncCount == 2){
				//totalSucessCount = totalReceived;
				totalSucessReceivedCount = totalReceived;
			}
		}else if(appSource.getId() == EBEN_CAL_CALENDAR_ID){
			if(nSyncCount == 1){
				//totalSucessCount = totalReceived;
				totalSucessReceivedCount = totalReceived;
			}
		}
//        StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
//        StringBuffer sb = new StringBuffer(localization.getLanguage(getSyncModeString(syncSrc)));
        //add by eddie20110922
        //sb.append(" ").append("(").append(totalReceived);

//        if (totalReceiving > 0) {
//            sb.append(" ").append("(").append(totalReceived);
//            sb.append("/").append(totalReceiving).append(")");
//        }

        // This is a LO
        // Compute the percentage of what we have sent so far
        if(currentReceivingItemSize > 0) {
            long perc = (size * 100) / currentReceivingItemSize;
            if (perc > 100) {
                perc = 100;
            }
            //sb.append(" (").append(currentItem).append(perc).append("%)");
        }
        if (uiSource != null) {
            if (!cancelling) {
//                uiSource.setStatusString(sb.toString());
                updateCurrentProgress();
                uiSource.redraw();
            }
        }
        if(!cancelling){

//            sendExternalSyncProgeress(sb.toString());
        }
    }

    private void startSending(String key, long size) {
        totalSent++;
		if(appSource.getId() == EBEN_CARDNAME_CARD_ID){
			if(nSyncCount == 2)
				totalSucessSentCount = totalSent;
		}else if( appSource.getId() == EBEN_NOTEPAD_PAGE_ID){
			if(nSyncCount == 1){
				totalSucessSentBookCount = totalSent;
			}
			else if(nSyncCount == 2){
				totalSucessSentCount = totalSent;
			}
		}else if(appSource.getId() == EBEN_CAL_CALENDAR_ID){
			if(nSyncCount == 1)
				totalSucessSentCount = totalSent;
			getSentCount(syncSrc);
		}else {
			totalSucessSentCount = totalSent;
		}
        currentSendingItemSize = size;

//        StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
//        StringBuffer sb = new StringBuffer(localization.getLanguage(getSyncModeString(syncSrc)));
        //add by eddie20110921
        //sb.append(" ").append(totalSent);

//        if (totalSending > 0) {
//        	sb.append(" ").append("(").append(totalSent);
//            sb.append("/").append(totalSending).append(")");
//        }

        if (uiSource != null) {
            if (!cancelling) {
//                uiSource.setStatusString(sb.toString());
                updateCurrentProgress();
                uiSource.redraw();
            }
        }
        if(!cancelling){

//            sendExternalSyncProgeress(sb.toString());
        }
    }

    private void startReceiving(String key, long size) {
        totalReceived++;
		if(appSource.getId() == EBEN_CARDNAME_CARD_ID){
			if(nSyncCount == 2){
				totalSucessReceivedCount = totalReceived;
			}
		}else if( appSource.getId() == EBEN_NOTEPAD_PAGE_ID){
			if(nSyncCount == 1){
				totalSucessReceivedBookCount = totalReceived;
			}else if(nSyncCount == 2){
				totalSucessReceivedCount = totalReceived;
			}
		}else if(appSource.getId() == EBEN_CAL_CALENDAR_ID){
			if(nSyncCount == 1)
				totalSucessReceivedCount = totalReceived;
			getReceivedCount(syncSrc);
		}else {
			totalSucessReceivedCount = totalReceived;
		}
		
        currentReceivingItemSize = size;

//        StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
//        StringBuffer sb = new StringBuffer(localization.getLanguage(getSyncModeString(syncSrc)));
        //add by eddie 
//        sb.append(" ").append(totalReceived);

//        if (totalReceiving > 0) {
//            sb.append(" ").append("(").append(totalReceived);
//            sb.append("/").append(totalReceiving).append(")");
//        }

        if (uiSource != null) {
            if (!cancelling) {
//                uiSource.setStatusString(sb.toString());
                updateCurrentProgress();
                uiSource.redraw();
            }
        }
        if(!cancelling){

//            sendExternalSyncProgeress(sb.toString());
        }
    }

    private String createLastSyncedString(AppSyncSource app,long anchor) {

        String res = "";
//        String syncDirect = getSourceType(app);
////        res = localization.getLanguage("eben_sync_renew");
//        String time = localization.getTime(anchor);
//        res = res.replace("type", syncDirect).replace("time", time);

        return res;
    }

    private void abortSlow() {
    }

    private boolean confirmSlowSync() {

        Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
        String sourceNames = getListOfSourceNames(sources).toLowerCase();

        StringBuffer question = new StringBuffer();
//        question.append(localization.getLanguage("dialog_slow_text1")).append(" ").append(
//                sourceNames).append(localization.getLanguage("dialog_slow_text2"));

        return controller.getDialogController().askYesNoQuestion(question.toString(), true, 20000);
    }

    private String getListOfSourceNames(Enumeration sourceNameList) {
        StringBuffer sourceNames = new StringBuffer();

        int x = 0;
        AppSyncSource appSource = (AppSyncSource)sourceNameList.nextElement();

        while(appSource != null) {

            String name = appSource.getName();
            appSource = (AppSyncSource)sourceNameList.nextElement();

            if (x > 0) {
                sourceNames.append(", ");
                if (appSource == null) {
//                    sourceNames.append(localization.getLanguage("dialog_and").toLowerCase());
                }
            }

            sourceNames.append(name);
        }

        return sourceNames.toString();
    }

    private String getLastSyncStatus(int status, SyncReport report) {

        String res = null;
        switch (status) {
            case SyncListener.SUCCESS:
            {
                SyncSource source = appSource.getSyncSource();
                long lastSyncTS;
                if (source != null) {
                    lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
                } else {
                    lastSyncTS = 0;
                }

                if (lastSyncTS > 0) {

                	res = createLastSyncedString(appSource,lastSyncTS);	

                } else {
//                    res = localization.getLanguage("home_unsynchronized");
                }
                break;
            }
            case SyncListener.ERR_SYNC_SOURCE_DISABLED://2012

            case SyncListener.INVALID_CREDENTIALS:
//                res = localization.getLanguage("status_invalid_credentials");
//                break;
            case SyncListener.FORBIDDEN_ERROR:
//                res = localization.getLanguage("status_forbidden_error");
//                break;
            case SyncListener.READ_SERVER_RESPONSE_ERROR:
            case SyncListener.WRITE_SERVER_REQUEST_ERROR:
//            	//add by eddie 20110815
//                res = localization.getLanguage("status_server_busy");
//                break;
            case SyncListener.CONN_NOT_FOUND:
//                if (report != null && (report.getReceivedItemsCount() > 0 || report.getSentItemsCount() > 0)) {
//                    res = localization.getLanguage("status_partial_failure");
//                } else {
//                    res = localization.getLanguage("status_network_error");
//                }
//                break;
            case SyncListener.CONNECTION_BLOCKED_BY_USER:
//                res = localization.getLanguage("status_connection_blocked");
//                break;
            case SyncListener.CANCELLED:
//                res = localization.getLanguage("status_cancelled");
//                break;
            case SyncListener.SERVER_FULL_ERROR:
//                res = localization.getLanguage("status_quota_exceeded");
//                break;
            case SyncListener.LOCAL_CLIENT_FULL_ERROR:
//                res = localization.getLanguage("status_no_space_on_device");
//                break;
            case SyncListener.NOT_SUPPORTED:
//                res = localization.getLanguage("status_not_supported");
//                break;
            case SyncListener.SD_CARD_UNAVAILABLE:
//                res = localization.getLanguage("status_sd_card_unavailable");
//                break;
            case SyncListener.LOCAL_CLIENT_TIMEOUT:
//            	res = localization.getLanguage("connecting_timeout_hint");

//            	break;
            default:
            	
//                if (report != null && (report.getReceivedItemsCount() > 0 || report.getSentItemsCount() > 0)) {
//                    res = localization.getLanguage("status_partial_failure");
//                } else {
//                    res = localization.getLanguage("status_complete_failure");
//                }

            	res = getFailStatusString(appSource);
                break;
        }
        if (Log.isLoggable(Log.DEBUG)) {
            Log.debug(TAG_LOG, "getLastSyncStatus " + res);
        }
        return res;
    }

//    private Bitmap getLastSyncIcon(int status) {
//        Bitmap res;
//        if (status == SyncListener.SUCCESS) {
//            long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
//            if (lastSyncTS > 0) {
//            	if(appSource.getConfig().getSyncIsCancel()){
//            		res = errorIcon;
//            	}else{
//                    res = okIcon;	
//            	}
//            } else {
//                res = null;
//            }
//        } else {
//            res = errorIcon;
//        }
//        return res;
//    }

    protected void updateCurrentProgress() {
        if (uiSource != null) {
            // Update the total if new info is available
            int progress = 0;

            if (totalSending > 0 &&
                totalReceiving == ITEMS_NUMBER_UNKNOWN)
            {
//                progress = 0 + (currentStep * 100) / totalSending;
            	progress = 0 + (totalSent * DATA_PERCENT) / totalSending;
                if (progress > DATA_PERCENT) {
                    progress = DATA_PERCENT;
                }
                if(isSyncMode(syncSrc)) {
                	progress = (progress*8)/10; //set send progress 80 percent in sync mode
                }
                // We are in the sending phase
//                progress = (currentStep * 50) / totalSending;
//                if (progress > 50) {
//                    progress = 50;
//                }
            }

            if (totalReceiving > 0) {
                // We are in the receiving phase
                progress = 0 + (totalReceived * DATA_PERCENT) / totalReceiving;
                if (progress > DATA_PERCENT) {
                    progress = DATA_PERCENT;
                }
                if(isSyncMode(syncSrc)) {
                	progress = 80+(progress*2)/10;
                }
            }
            if(!isFailed && !cancelling) {
            int cur = getCurOrder(syncSrc);
            int sourceCount = getSourceCount(syncSrc);
            switch (sourceCount) {
            	case 1:
            		progress = (int) (CONNECTED_PERCENT + (progress));
            		progress = (progress>10)  ? 10:progress;
            	break;
				case 2:
					// for notepad and cardname ,book is assigned to 10%
					if(1 == cur) {
						progress = (int) (CONNECTED_PERCENT + (progress/10));
						progress = (progress>10)  ? 10:progress;
					}
					else {
						progress = (int) (10 + (progress*9)/10);
					}
					break;
				case 3:
					if(1 == cur) {
						progress = (int) (CONNECTED_PERCENT + progress/10);
						progress = (progress>10)  ? 10:progress;
					}
					else if(2 == cur) {
						progress = (int) (10 + (progress*3)/10);
					}
					else {
						progress = (int) (40 + (progress*6)/10);
					}
					break;
				default:
				break;
			}
//            progress = (int) (((cur-1)*100)/sourceCount + progress/sourceCount);
            }
            // Update the uiSource
            uiSource.setProgress(progress);
        }
    }

    /**
     * This method forces the progress bar to a given value. Must be used
     * carefully to avoid the bar to move forward and backward.
     */
    protected void updateCurrentProgress(int percentage) {
        if (uiSource != null) {
            uiSource.setProgress(percentage);
        }
    }

    private class SourceSyncingAnimation extends SyncingAnimation {

        public SourceSyncingAnimation() {
            super(customization.getStatusIconsForAnimation());
        }

        protected void showBitmap(Bitmap bitmap) {
            uiSource.setStatusIcon(bitmap);
            uiSource.redraw();
        }
    }

    private void resetInternalStatus() {
        totalReceiving = ITEMS_NUMBER_UNKNOWN;
        totalReceived = 0;
        totalSent = 0;
        totalSending = 0;
        cancelling = false;
        // Reset the current step
        currentStep = 0;
        if(!isLastSource(syncSrc) || isFailed){
        	if(isFailed && uiSource != null )
        		uiSource.setProgress(-1);
        	else if (isFirstSource(syncSrc) && uiSource != null) 
        		uiSource.setProgress(0);
        	else 
        		updateCurrentProgress();
        }
    }
    
    public int getTotalSending(){
    	return totalSending;
    }
    
    public int getTotalReceiving(){
    	return totalReceiving;
    }
    
    public boolean getSyncProgress(){
    	return isSyncProgress;
    }
    
    public void setSyncProgress(boolean b){
    	isSyncProgress = b;
    }
    
    private void saveConfigSyncFinishedValue(int direction){
    	if(direction == 0){
            if(nSyncCount==2 && appSource.getId() == EBEN_NOTEPAD_PAGE_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==3 && appSource.getId() == EBEN_CARDNAME_CARD_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==2 && appSource.getId() == EBEN_CAL_CALENDAR_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }
    	}else if(direction == 1){
            if(nSyncCount==2 && appSource.getId() == EBEN_NOTEPAD_PAGE_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==3 && appSource.getId() == EBEN_CARDNAME_CARD_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==2 && appSource.getId() == EBEN_CAL_CALENDAR_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }
    	}else if(direction == 2){
            if(nSyncCount==2 && appSource.getId() == EBEN_NOTEPAD_PAGE_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==3 && appSource.getId() == EBEN_CARDNAME_CARD_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }else if(nSyncCount==2 && appSource.getId() == EBEN_CAL_CALENDAR_ID){
            	appSource.getConfig().setSyncSourceIdFinish(true);
            }
    	}

    }

	
	public void startSession(SyncSource src) {

		syncSrc = src;
		isFailed = false;
		if(Log.isLoggable(Log.DEBUG))
			Log.debug(TAG_LOG,"startSesssion src name is "+src.getName());
		startSession();
		
	}

	

	public void endSession(SyncReport report, SyncSource src) {
		
		 syncSrc = src;   
		 endSession(report);
		 syncSrc = null;
	
	}

	
	public void startConnecting(SyncSource src) {
		syncSrc = src;
		startConnecting();

		
	}

	
	public void endConnecting(int action,SyncSource src) {
		syncSrc = src;
		endConnecting(action);
		
	}

	
	public void syncStarted(int alertCode, SyncSource src) {
		syncSrc = src;
		syncStarted(alertCode);
		
	}

	
	public void endSyncing(SyncSource src) {
		// TODO Auto-generated method stub
		syncSrc = src;
		endSyncing();
		syncSrc = null;
	}

	
	public void startFinalizing(SyncSource src) {
		// TODO Auto-generated method stub
		syncSrc = src;
		startFinalizing();
	}

	
	public void endFinalizing(SyncSource src) {
		// TODO Auto-generated method stub
		syncSrc = src;
		endFinalizing();
	}

	
	public void startReceiving(int number, SyncSource src) {
		// TODO Auto-generated method stub
		syncSrc = src;
		startReceiving(number);
	}
	    
    private boolean isSyncFail(SyncSource src) {
    	boolean isFail = false;
    	if(null == src)
    		isFail = true;
    	else if(null != lastSyncReport && null != src) {
	        int status = lastSyncReport.getStatusCode();
	        
	        if(status != SyncListener.SUCCESS) {
	        	isFail = true;
	        }
	        else {
	            SyncSource source = appSource.getSyncSource();
	            long lastSyncTS;
	            if (source != null) {
	                lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
	            } else {
	                lastSyncTS = 0;
	            }
	
	            if (lastSyncTS > 0) {
	            	if(appSource.getConfig().getSyncIsCancel()){
	            		isFail = true;
	            	}
	            } else {
	            	isFail = true;
	            }        	
	        }
    	}
    	if(Log.isLoggable(Log.DEBUG))
    		Log.debug(TAG_LOG,"source Fail is "+isFail);
		return isFail;
	}
	private boolean isLastSource(SyncSource src) {
		boolean islast = false;
		if( null != src) 
			islast = (getCurOrder(src) == getSourceCount(src));
		return islast;
	}
	
	private boolean isFirstSource(SyncSource src) {
		boolean isfirst = true;
		if(null != src)
			isfirst = (1 == getCurOrder(src));
		return isfirst;
	}
	private int getSourceCount(SyncSource src) {
		
		int count = 1;
		if(null != src) {
			String sourceName = src.getConfig().getName();
			if(sourceName.equals("ebenbook")) {
				count = 2;
			}
			else if(sourceName.equals("ebenpage")) {
				count = 2;
			}
			else if(sourceName.equals("ebengroup")) {
				count = 3;
			}
			else if(sourceName.equals("ebencard")) {
				count = 3;
			}
			else if(sourceName.equals("ebendata")) {
				count = 3;
			}
			else if(sourceName.equals("ebencalendar")) {
				count = 2;				
			}
			else if(sourceName.equals("ebenalarm")) {
				count = 2;
			}
		}
		
		return count;
	}

	private int getCurOrder(SyncSource src) {
		
		int cur = 1;
		if(null != src) {
			String sourceName = src.getConfig().getName();
			if(sourceName.equals("ebenbook")) {
				cur = 1;
			}
			else if(sourceName.equals("ebenpage")) {
				cur = 2;
			}
			else if(sourceName.equals("ebengroup")) {
				cur = 1;
			}
			else if(sourceName.equals("ebencard")) {
				cur = 2;
			}
			else if(sourceName.equals("ebendata")) {
				cur =3;
			}
			else if(sourceName.equals("ebencalendar")) {
				cur = 1;				
			}
			else if(sourceName.equals("ebenalarm")) {
				cur = 2;
			}			
		}
		return cur;
	}
    private String getSyncModeString(SyncSource src) {
		// TODO Auto-generated method stub
    	String syncMode = "";
    	if(null != src) {
    		switch(src.getSyncMode()) {
    		case SyncML.ALERT_CODE_FAST:
    			syncMode = "eben_now_sync_souce";
    			break;
    		case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
    			syncMode = "eben_now_upload_source";
    			break;
    		case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
    			syncMode = "eben_now_download_source";
    			break;
    		default:
    			break;
    				
    		}
    	}
		return syncMode;
	}


    private String getSourceType(AppSyncSource appSource) {

    	String syncName = "";
    	
		switch(appSource.getConfig().getSyncDirection()) {
		case 0://EbenConstantsUtil.EBEN_MODE_SYNC:
//			syncName = localization.getLanguage("eben_sync_souce");
			break;

		case 1://EbenConstantsUtil.EBEN_MODE_DOWNLOAD:
//			syncName = localization.getLanguage("eben_download_source");
			break;
		case 2://EbenConstantsUtil.EBEN_MODE_UPLOAD:
//			syncName = localization.getLanguage("eben_upload_source");
			break;			

		default:
			break;
				
		}

		return syncName;
	}    
    private String getFailStatusString(AppSyncSource app) {	
    	String res = "" ;
//    	AppSyncSourceConfig ssConfig = app.getConfig();
//
//		String SyncName =getSourceType(app);
//		String time = localization.getTime(ssConfig.getLastSyncTimestamp());
//		res = localization.getLanguage("eben_status_sync_failed");
//		res = res.replace("time",time);
//		res=res.replaceAll("type",SyncName);

		return res;
    }

    private String getSimplyStatusString(AppSyncSource app) {	
        int status = lastSyncReport.getStatusCode();
        String res = "" ;
        switch (status) {
        case SyncListener.ERR_SYNC_SOURCE_DISABLED://2012:
//            res = localization.getLanguage("eben_sync_not_active");
            break;

        default:
//            res = localization.getLanguage("status_partial_failure");
            break;
        }
    	
		String SyncName =getSourceType(app);

//		res=res.replaceAll("type",SyncName);

		return res;
    }    
    
    private String getSyncFinshedString(AppSyncSource app) {
    	//eben_sync_source_success_thistime
        String res = "";
//    	res = localization.getLanguage("eben_sync_source_success_thistime");
//    	String SyncName =getSourceType(app);
//    	String time = localization.getTime(app.getConfig().getLastSyncTimestamp());
//    	
//    	String count = "";
//    	String sourceName= "";
//		 if(appSource.getConfig().getSyncSourceIdFinish()){
//			 if((localization.getLanguage("type_eben_calendar")).equalsIgnoreCase(appSource.getName())){
//				 count = String.valueOf(appSource.getConfig().getSyncEbenAlarmCount());
//			 }
//			 else{
//				 count = String.valueOf(appSource.getConfig().getSyncCount());
//			 } 
//		 }
//
//		 if((localization.getLanguage("type_eben_notepad").equalsIgnoreCase(appSource.getName()))){
//			 count = String.valueOf(appSource.getConfig().getSyncCount()); 
//        	 sourceName = localization.getLanguage("eben_sync_page")+appSource.getName();
//         }else if((localization.getLanguage("type_eben_card").equalsIgnoreCase(appSource.getName()))){
//        	 count = String.valueOf(appSource.getConfig().getSyncCount());
//        	 sourceName = localization.getLanguage("eben_units_card") + localization.getLanguage("type_eben_card_name");
//         }
//	     else if((localization.getLanguage("eben_units_calendar").equalsIgnoreCase(appSource.getName()))){
//        	 count = String.valueOf(appSource.getConfig().getSyncEbenAlarmCount());
//        	 sourceName =  localization.getLanguage("eben_units_calendar") + appSource.getName();
//	     }		 
//         else{
//        	 count = String.valueOf(appSource.getConfig().getSyncCount());
//        	 sourceName =  localization.getLanguage("eben_units_calendar") + appSource.getName();
//         }
//		 
//		 res = res.replaceAll("type", SyncName).replace("Count", count).replace("Source", sourceName);
//        res = localization.getLanguage("eben_sync_complete");
    	return res;  	
    }
    private boolean isSyncMode(SyncSource src) {
		boolean isSync = false;
		if(null != src) {
			isSync = (src.getSyncMode() == SyncML.ALERT_CODE_FAST);
		}
		return isSync;
	}
    
    private int getSentCount(SyncSource src) {

    	int syncFinishedCount = 0;
    	if(null != src) {
	    	String sourceName = src.getConfig().getName();
	    	if(sourceName.equals("ebenalarm")){
	    		syncFinishedCount = totalSent;
	    	}
	    	if(Log.isLoggable(Log.DEBUG))
	    		Log.debug(TAG_LOG, "getSentCount:sourceName===" + sourceName+"     syncFinishedCount=="+syncFinishedCount);
    	}
    	return syncFinishedCount;
    }
    
    private int getReceivedCount(SyncSource src) {
    	int syncFinishedCount = 0;
    	if(null != src) {
	    	String sourceName = src.getConfig().getName();
	    	if(sourceName.equals("ebenalarm")){
	    		syncFinishedCount = totalReceived;
	    	}
	    	if(Log.isLoggable(Log.DEBUG))
	    		Log.debug(TAG_LOG, "getSentCount:sourceName===" + sourceName+"     syncFinishedCount=="+syncFinishedCount);
	    	}
    	return syncFinishedCount;
	}
    
    private String  stringReplace (int direction, String lastStr){
    	String lastStatus = "";
//    	if(lastStr != null && lastStr.equals(localization.getLanguage("status_partial_failure"))){
//	    		if(1 == direction){
//	   			 lastStatus = localization.getLanguage("status_download_failure");
//	   		 }else if(2 == direction){
//	   			 lastStatus = localization.getLanguage("status_upload_failure");
//	   		 }else{
//	   			 lastStatus = lastStr;
//	   		 }
//    	}
		 return lastStatus;
    }
    
    private int getLastSyncIconId(int status) {
        int res;
        if (status == SyncListener.SUCCESS) {
            long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
            if (lastSyncTS > 0) {
            	if(appSource.getConfig().getSyncIsCancel()){
            		res = errorIconId;
            	}else{
                    res = okIconId;	
            	}
            } else {
                res = 0;
            }
        } else {
            res = errorIconId;
        }
        return res;
    }
    
}

