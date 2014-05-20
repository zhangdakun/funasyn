package cn.eben.android.source.edisk;


import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.client.customization.Customization;
import com.funambol.storage.StringKeyValueSQLiteStore;
import com.funambol.sync.SourceConfig;
import com.funambol.sync.SyncAnchor;
import com.funambol.sync.SyncConfig;
import com.funambol.sync.SyncManagerI;
import com.funambol.sync.SyncSource;
import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.syncml.spds.SyncMLAnchor;
import com.funambol.syncml.spds.SyncManager;

import android.content.Context;
import android.os.Environment;

public class EdiskSync implements Runnable{

    private Context mContext;
    
    public EdiskSync(Context context){
        mContext = context;
    }
    
    
    @Override
    public void run() {
        
//        int id = EbenConst.EBEN_EDISK_ID;
//        AppInitializer sAppinitializer = App.i().getAppInitializer();
//        AndroidConfiguration sConfiguration = sAppinitializer.getConfiguration();
//        Customization customization = sAppinitializer.getCustomization();
//        SyncConfig  config = sConfiguration.getSyncConfig();
//        DeviceConfig dc = sConfiguration.getDeviceConfig();
//        SyncManagerI manager = new SyncManager(config, dc);
//        String defaultUri = "edisk";//customization.getDefaultSourceUri(id);
//        SourceConfig sc = new SourceConfig("edisk", SourceConfig.FILE_OBJECT_TYPE, defaultUri);
//  
//        sc.setEncoding(SyncSource.ENCODING_NONE);
//        sc.setSyncMode(SyncSource.INCREMENTAL_SYNC);
//        SyncAnchor anchor = new SyncMLAnchor(); 
//        sc.setSyncAnchor(anchor);
//        
//        StringKeyValueSQLiteStore trackerStore = new StringKeyValueSQLiteStore(mContext, ((AndroidCustomization)customization).getFunambolSQLiteDbName(), sc.getName());
//        
//        StringBuffer defaultDir = new StringBuffer();
//        String sdCardRoot = Environment.getExternalStorageDirectory().toString();
//        String directoryName = ((AndroidCustomization)customization).getDefaultFilesSDCardDir();
//        defaultDir = new StringBuffer();
//        defaultDir.append(sdCardRoot);
//        defaultDir.append("/");
//        defaultDir.append(directoryName);
//        defaultDir.append("/");
//        
//        EdiskTracker tracker = new EdiskTracker(mContext, trackerStore);
//        
//        EdiskSyncSource syncSource = new EdiskSyncSource(sc, tracker, defaultDir.toString(),sdCardRoot, customization, mContext);
//        syncSource.setListener(new EdiskSyncListener(customization, id, sAppinitializer.getLocalization(), sConfiguration));
//        manager.sync(syncSource);       
        
    }

}
