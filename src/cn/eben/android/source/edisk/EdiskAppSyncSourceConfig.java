package cn.eben.android.source.edisk;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.sync.SourceConfig;

import android.content.Context;


public class EdiskAppSyncSourceConfig extends AppSyncSourceConfig {
	
    private static final String TAG_LOG = "EdiskAppSyncSourceConfig";

    protected static final String CONF_KEY_BASE_DIRECTORY = "CONF_KEY_BASE_DIRECTORY";
    
    protected static final String CONF_KEY_SECURITYSETTING = "cn.eben.edisk.securitysetting";

//    protected final EdiskAppSyncSource ediskAppSyncSource;

    protected String baseDirectory;
    protected Context context;
    
    protected boolean mNetworkSecurity;

    public EdiskAppSyncSourceConfig(Context context, EdiskAppSyncSource appSource,
                                    Customization customization, Configuration configuration)
    {
        super(appSource, customization, configuration);
//        this.ediskAppSyncSource = appSource;
        this.context = context;
    }

    @Override
    public void load(SourceConfig sourceConfig) {
        super.load(sourceConfig);
        baseDirectory = configuration.loadStringKey(CONF_KEY_BASE_DIRECTORY, "");
        mNetworkSecurity = configuration.loadBooleanKey(CONF_KEY_SECURITYSETTING, false);
    }
    
    @Override
    public void save() {
        configuration.saveStringKey(CONF_KEY_BASE_DIRECTORY, baseDirectory);
        configuration.saveBooleanKey(CONF_KEY_SECURITYSETTING, mNetworkSecurity);
        super.save();
    }
    
    public void setBaseDirectory(String value) {
        baseDirectory = value;
        dirty = true;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }
    
    public void setNetworkSecurity(Boolean value) {
        mNetworkSecurity = value;
    }
    
    public boolean getNetworkSecurity() {
        return mNetworkSecurity;
    }
}
