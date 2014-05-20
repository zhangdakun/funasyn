package cn.eben.android.source.edisk;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.ExternalAppManager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.MediaStore.Images.Media;

import java.util.List;

public class EdiskExternalAppManager implements ExternalAppManager {

    private AppSyncSource source;
    private Context       context;

    public EdiskExternalAppManager(Context context, AppSyncSource source) {
        this.context = context;
        this.source = source;
    }

    public void launch(AppSyncSource source, Object args[]) throws Exception {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("cn.eben.edisk");
        if(null != intent){
            context.startActivity(intent);
        }
        
    }

}
