/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eben.client;

import com.funambol.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/** 
 * Broadcast receiver that handles push notification messages from the server.
 * This should be registered as receiver in AndroidManifest.xml. 
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public final class NotificationReceiver extends BroadcastReceiver {

    private static final String LOGTAG = LogUtil
            .makeLogTag(NotificationReceiver.class);

    //    private NotificationService notificationService;

    public NotificationReceiver() {
    }

    //    public NotificationReceiver(NotificationService notificationService) {
    //        this.notificationService = notificationService;
    //    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.debug(LOGTAG, "NotificationReceiver.onReceive()...");
        String action = intent.getAction();
        Log.debug(LOGTAG, "action=" + action);

        if (Constants.ACTION_SHOW_NOTIFICATION.equals(action)) {
            String notificationId = intent
                    .getStringExtra(Constants.NOTIFICATION_ID);
            String notificationApiKey = intent
                    .getStringExtra(Constants.NOTIFICATION_API_KEY);
            String notificationTitle = intent
                    .getStringExtra(Constants.NOTIFICATION_TITLE);
            String notificationMessage = intent
                    .getStringExtra(Constants.NOTIFICATION_MESSAGE);
            String notificationUri = intent
                    .getStringExtra(Constants.NOTIFICATION_URI);

            Log.debug(LOGTAG, "notificationId=" + notificationId);
            Log.debug(LOGTAG, "notificationApiKey=" + notificationApiKey);
            Log.debug(LOGTAG, "notificationTitle=" + notificationTitle);
            Log.debug(LOGTAG, "notificationMessage=" + notificationMessage);
            Log.debug(LOGTAG, "notificationUri=" + notificationUri);

            SmsUtil.sendSMS(context, notificationTitle, notificationMessage);
//            Notifier notifier = new Notifier(context);
//            notifier.notify(notificationId, notificationApiKey,
//                    notificationTitle, notificationMessage, notificationUri);
        }


    }

}
