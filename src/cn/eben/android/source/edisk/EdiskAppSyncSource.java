package cn.eben.android.source.edisk;

import com.funambol.android.AndroidAppSyncSource;
import com.funambol.sapisync.source.FileSyncSource;
import com.funambol.sync.SyncFilter;
import com.funambol.util.Log;

public class EdiskAppSyncSource extends AndroidAppSyncSource {
    private static final String TAG_LOG = "EdiskAppSyncSource";

    public EdiskAppSyncSource(String name, FileSyncSource source) {
        super(name, source);
        //setIsMedia(true);
        setIsMedia(false);

    }

    public EdiskAppSyncSource(String name) {
        this(name, null);
    }

    @Override
    public void reapplyConfiguration() {
        super.reapplyConfiguration();

        // Enable/disable upload filters depending on the "Include Older Media" flag
        boolean includeOlderMedia = false;//((MediaAppSyncSourceConfig)config).getIncludeOlderMedia();
        if(getSyncSource() != null) {
            SyncFilter syncFilter = getSyncSource().getFilter();
            if(syncFilter != null) {
                if (Log.isLoggable(Log.DEBUG)) {
                    Log.debug(TAG_LOG, "Setting filters to status " + includeOlderMedia);
                }

                if(syncFilter.getFullUploadFilter() != null) {
                    syncFilter.getFullUploadFilter().setEnabled(!includeOlderMedia);
                }
                if(syncFilter.getIncrementalUploadFilter() != null) {
                    syncFilter.getIncrementalUploadFilter().setEnabled(!includeOlderMedia);
                }
            }
        }
    }
}
