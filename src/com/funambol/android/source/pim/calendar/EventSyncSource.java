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

package com.funambol.android.source.pim.calendar;

import android.content.Context;

import com.funambol.android.source.AbstractDataManager;

import com.funambol.sync.client.ChangesTracker;
import com.funambol.sync.SourceConfig;
import com.funambol.sync.SyncException;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;

import com.funambol.util.Log;

/**
 */
public class EventSyncSource extends CalendarSyncSource {
    private static final String TAG = "EventSyncSource";

    public EventSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                           Configuration configuration, AppSyncSource appSource, AbstractDataManager dm) {
        super(config, tracker, context, configuration, appSource, dm);
    }

    @Override
    public void beginSync(int syncMode, boolean resume) throws SyncException {
        // If the calendar is not defined or no longer exists, then we revert to
        // the default one
        CalendarManager cm = (CalendarManager)dm;
        CalendarAppSyncSourceConfig asc = (CalendarAppSyncSourceConfig)appSource.getConfig();
        if (asc.getCalendarId() == -1) {
            if (Log.isLoggable(Log.INFO)) {
                Log.info(TAG, "No calendar defined, the user must have deleted it");
            }
            throw new SyncException(SyncException.CLIENT_ERROR, "No calendar configured");
        }
        // Now begin the real sync
        super.beginSync(syncMode, resume);
    }
}

