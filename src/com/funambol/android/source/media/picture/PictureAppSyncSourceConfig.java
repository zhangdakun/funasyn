/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.android.source.media.picture;

import android.content.Context;

import com.funambol.android.source.media.MediaAppSyncSourceConfig;
import com.funambol.android.source.media.MediaAppSyncSource;

import com.funambol.client.customization.Customization;
import com.funambol.client.configuration.Configuration;
import com.funambol.sync.SourceConfig;

import com.funambol.util.Log;

public class PictureAppSyncSourceConfig extends MediaAppSyncSourceConfig {

    private static final String TAG = "PictureAppSyncSourceConfig";

    private static final String CONF_KEY_USE_FILENAME_TRACKER_STORE =
            "CONF_KEY_USE_FILENAME_TRACKER_STORE";

    private boolean useFileNameTrackerStore;

    public PictureAppSyncSourceConfig(Context context, MediaAppSyncSource appSource,
            Customization customization, Configuration configuration) {
        super(context, appSource, customization, configuration);
    }

    @Override
    public void save() {
        // Save the custom fields
        configuration.saveBooleanKey(CONF_KEY_USE_FILENAME_TRACKER_STORE,
                useFileNameTrackerStore);

        // Now save the basic configuration (this must be done after save the
        // basic stuff)
        super.save();
    }

    @Override
    public void load(SourceConfig sourceConfig) {
        useFileNameTrackerStore = configuration.loadBooleanKey(
                CONF_KEY_USE_FILENAME_TRACKER_STORE, false);
        super.load(sourceConfig);
    }

    public void setUseFileNameTrackerStore(boolean value) {
        useFileNameTrackerStore = value;
        dirty = true;
    }

    public boolean getUseFileNameTrackerStore() {
        return useFileNameTrackerStore;
    }
    
    @Override
    public void migrateConfig(String from, String to, SourceConfig config) {
        super.migrateConfig(from, to, config);
        if (from == null) {
            // The include older media setting got generalized
            String CONF_KEY_INCLUDE_OLDER_PICTURES =
                    "CONF_KEY_INCLUDE_OLDER_PICTURES";
            boolean includeOldPics = configuration.loadBooleanKey(
                    CONF_KEY_INCLUDE_OLDER_PICTURES, false);
            if (includeOldPics) {
                if (Log.isLoggable(Log.INFO)) {
                    Log.info(TAG, "Migrating include old picture flag");
                }
                int sourceId = appSource.getId();
                includeOlderMedia = true;
            }
        }
    }

}
 
