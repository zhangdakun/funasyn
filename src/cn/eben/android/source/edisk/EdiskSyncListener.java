package cn.eben.android.source.edisk;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncListener;
import com.funambol.sync.SyncReport;
import com.funambol.sync.SyncSource;
import com.funambol.syncml.protocol.DevInf;
import com.funambol.util.Log;

import java.util.Date;

public class EdiskSyncListener implements SyncListener {

    private static final String TAG_LOG = "EdiskSyncListener";

    private Localization localization = null;
    private Customization customization = null;
    private Configuration configuration = null;
    private int sourceId = -1;
    
    private int              totalSent;
    private int              totalSending;
    private int              totalReceived;
    private int              totalReceiving;

    private long             currentSendingItemSize = 0;
    private long             currentReceivingItemSize = 0;
    
    private boolean          cancelling = false;
    private boolean          syncing    = false;    

    private int              currentStep = 0;
    
    public EdiskSyncListener(Customization customization, int sourceId, Localization localization, Configuration configuration){
        this.customization = customization;
        this.sourceId = sourceId;
        this.localization = localization;
        this.configuration = configuration;
    }
    
    private void sendExternalSyncProgeress(String progress){
//        customization.sendExternalSyncProgeress(progress, sourceId);
    }
    
    @Override
    public void startSession() {
        resetInternalStatus();

        syncing = true;

    }

    @Override
    public void endSession(SyncReport report) {
        if (!syncing) {
            return;
        }

        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "endSession");
        }

        int status = report.getStatusCode();

        
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

        cancelling = false;
        syncing = false;

        // Reset the overall status
        resetInternalStatus();
    }

    @Override
    public void startConnecting() {

        if (!cancelling) {
            sendExternalSyncProgeress(localization.getLanguage("status_connecting"));
        }
    }

    @Override
    public void endConnecting(int action) {


    }

    @Override
    public void syncStarted(int alertCode) {


    }

    @Override
    public void endSyncing() {


    }

    @Override
    public void startFinalizing() {
        if (!cancelling) {
            sendExternalSyncProgeress(localization.getLanguage("status_mapping"));
        }

    }

    @Override
    public void endFinalizing() {


    }

    @Override
    public void startReceiving(int number) {
        if (totalReceiving == ITEMS_NUMBER_UNKNOWN) {
            totalReceiving = number;
            currentStep = 0;
        }

    }

    @Override
    public void itemAddReceivingStarted(String key, String parent, long size) {
        startReceiving(key, size);

    }

    @Override
    public void itemAddReceivingEnded(String key, String parent) {
        

    }

    @Override
    public void itemAddReceivingProgress(String key, String parent, long size) {
        receivedProgress(key, size);

    }

    @Override
    public void itemReplaceReceivingStarted(String key, String parent, long size) {
        startReceiving(key, size);
    }

    @Override
    public void itemReplaceReceivingEnded(String key, String parent) {
        currentStep++;
        updateCurrentProgress();
    }

    @Override
    public void itemReplaceReceivingProgress(String key, String parent, long size) {
        receivedProgress(key, size);

    }

    @Override
    public void itemDeleted(SyncItem item) {
        startReceiving(item.getKey(), 0);
    }

    @Override
    public void endReceiving() {


    }

    @Override
    public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
        totalSending = numNewItems + numUpdItems + numDelItems;
        currentStep++;

    }

    @Override
    public void itemAddSendingStarted(String key, String parent, long size) {
        startSending(key, size);
    }

    @Override
    public void itemAddSendingEnded(String key, String parent) {
        

    }

    @Override
    public void itemAddSendingProgress(String key, String parent, long size) {
        sentProgress(key, size);

    }

    @Override
    public void itemReplaceSendingStarted(String key, String parent, long size) {
        startSending(key, size);
    }

    @Override
    public void itemReplaceSendingEnded(String key, String parent) {
        currentStep++;
        updateCurrentProgress();
    }

    @Override
    public void itemReplaceSendingProgress(String key, String parent, long size) {
        sentProgress(key, size);
    }

    @Override
    public void itemDeleteSent(SyncItem item) {
        startSending(item.getKey(), 0);
    }

    @Override
    public void endSending() {


    }

    @Override
    public boolean startSyncing(int mode, Object devInf) {
        if (mode == com.funambol.syncml.protocol.SyncML.ALERT_CODE_SLOW) {
            if (customization.confirmSlowSync()) {
                String text = localization.getLanguage("status_confirm_slow");
               
                sendExternalSyncProgeress(text);
                return false;
//                if (!confirmSlowSync()) {
//                    abortSlow();
//                    return false;
//                }
            }
        }

        // If the server sends its capabilities, we must decode them and update
        // the configuration accordingly
        if (devInf != null) {
//            if (Log.isLoggable(Log.INFO)) {
//                Log.info(TAG_LOG, "Server sent its capabilities");
//            }
            if (devInf instanceof DevInf) {
//                controller.reapplyServerCaps((DevInf)devInf);
                configuration.setForceServerCapsRequest(false);
                configuration.setServerDevInf((DevInf)devInf);
            }
        }

        return true;
    }

    private void sentProgress(String key, long size) {
        
        StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
        sb.append(" ").append(totalSent);

        if (totalSending > 0) {
            sb.append("/").append(totalSending);
        }

        // This is a LO
        // Compute the percentage of what we have sent so far
        if(currentSendingItemSize > 0) {
            long perc = (size * 100) / currentSendingItemSize;
            if (perc > 100) {
                perc = 100;
            }
            sb.append(" (").append(perc).append("%)");
        }

        if (!cancelling) {
            sendExternalSyncProgeress(sb.toString());
        }
    }
    
    private void startSending(String key, long size) {
        totalSent++;
        currentSendingItemSize = size;

        StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
        sb.append(" ").append(totalSent);

        if (totalSending > 0) {
            sb.append("/").append(totalSending);
        }

        if (!cancelling) {
            sendExternalSyncProgeress(sb.toString());
        }
    }

    private void receivedProgress(String key, long size) {
        totalReceived++;
        StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
        sb.append(" ").append(totalReceived);

        if (totalReceiving > 0) {
            sb.append("/").append(totalReceiving);
        }

        // This is a LO
        // Compute the percentage of what we have sent so far
        if(currentReceivingItemSize > 0) {
            long perc = (size * 100) / currentReceivingItemSize;
            if (perc > 100) {
                perc = 100;
            }
            sb.append(" (").append(perc).append("%)");
        }

        if (!cancelling) {

            sendExternalSyncProgeress(sb.toString());
        }

    }

    private void startReceiving(String key, long size) {
        totalReceived++;
        currentReceivingItemSize = size;

        StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
        sb.append(" ").append(totalReceived);

        if (totalReceiving > 0) {
            sb.append("/").append(totalReceiving);
        }

        if (!cancelling) {
            sendExternalSyncProgeress(sb.toString());
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
        updateCurrentProgress();
    }
    
    protected void updateCurrentProgress() {

       
        // Update the total if new info is available
        int progress = 0;

        if (totalSending > 0 &&
            totalReceiving == ITEMS_NUMBER_UNKNOWN)
        {
            // We are in the sending phase
            progress = (currentStep * 50) / totalSending;
            if (progress > 50) {
                progress = 50;
            }
        }

        if (totalReceiving > 0) {
            // We are in the receiving phase
            progress = 50 + (currentStep * 50) / totalReceiving;
            if (progress > 100) {
                progress = 100;
            }
        }

    }

	
	public void startSession(SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void endSession(SyncReport report, SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void startConnecting(SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void endConnecting(int action,SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void syncStarted(int alertCode, SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void endSyncing(SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void startFinalizing(SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void endFinalizing(SyncSource src) {
		// TODO Auto-generated method stub
		
	}

	
	public void startReceiving(int number, SyncSource src) {
		// TODO Auto-generated method stub
		
	}
}
