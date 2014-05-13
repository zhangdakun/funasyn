package com.funambol.android.source.pim.contact;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.funambol.android.MD5Util;
import com.funambol.android.controller.AndroidController;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.sync.ItemStatus;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncSource;
import com.funambol.sync.client.TrackableSyncSource;
import com.funambol.sync.client.TrackerException;
import com.funambol.util.Base64;
import com.funambol.util.Log;
import com.funambol.util.MD5;
import com.funambol.util.StringUtil;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

public class DirtyChangesTrackerMd5 extends DirtyChangesTracker {
	
	private final String TAG_LOG = "DirtyChangesTrackerMd5";
	
    protected StringKeyValueStore status;
    protected Hashtable newItems;
    protected Hashtable deletedItems;
    protected Hashtable updatedItems;
    private ContentResolver resolver;

	public StringKeyValueStore getStatus() {
		return status;
	}
	public void setStatus(StringKeyValueStore status) {
		this.status = status;
	}
	public DirtyChangesTrackerMd5(Context context, ContactManager cm) {
		super(context, cm);
		// TODO Auto-generated constructor stub
		
		this.resolver = context.getContentResolver();
	}
    /**
     * Create an hashtable with all files and their fingerprints
     * @return
     * @throws SyncException
     */
    protected Hashtable getAllFilesFingerprint()
            throws SyncException
    {
    	Hashtable snapshot = new Hashtable();
    	if(ss instanceof ContactSyncSource) {
    		ContactSyncSource css = (ContactSyncSource) ss;
			Enumeration allItemsKeys = css.getAllItemsKeys();
			

			while (allItemsKeys.hasMoreElements()) {
				String key = (String) allItemsKeys.nextElement();
				SyncItem item = new SyncItem(key);
				item = css.getItemContent(item);
				// Compute the fingerprint for this item
				if (Log.isLoggable(Log.TRACE)) {
					Log.trace(TAG_LOG,
							"Computing fingerprint for " + item.getKey());
				}
				 String fp = computeFingerprint(item);
				 if (Log.isLoggable(Log.TRACE)) {
				 Log.trace(TAG_LOG, "Fingerprint is: " + fp);
				 }
				
				 // Store the fingerprint for this item
				 snapshot.put(item.getKey(), fp);
			}
    	}
        return snapshot;
    }


    
    protected String computeFingerprint(SyncItem item) {
    	
    	return MD5Util.md5(item.getContent());

    }
    public static String EntryMD5 = "90E5DB91BA9D01165E91B8B5C59F44BE";
    public void begin(int syncMode, boolean reset) throws TrackerException {
        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "begin");
        }

        this.syncMode = syncMode;
        
        newItems      = new Hashtable();
        updatedItems  = new Hashtable();
        deletedItems  = new Hashtable();

        if(syncMode == SyncSource.INCREMENTAL_SYNC ||
           syncMode == SyncSource.INCREMENTAL_UPLOAD) {

            Hashtable snapshot;
            try {
                snapshot = getAllFilesFingerprint();
            } catch (SyncException e) {
                Log.error(TAG_LOG, "Cannot compute fingerprint for items ", e);
                throw new TrackerException(e.toString());
            }

            // Initialize the status by loading its content
            try {
                this.status.load();
            } catch (Exception e) {
                if (Log.isLoggable(Log.DEBUG)) {
                    Log.debug(TAG_LOG, "Cannot load tracker status, create an empty one");
                }
                try {
                    this.status.save();
                } catch (Exception e1) {
                    Log.error(TAG_LOG, "Cannot save tracker status");
                    throw new TrackerException(e.toString());
                }
            }
            
            // Now compute the three lists
            Enumeration snapshotKeys = snapshot.keys();
            // Detect new items and updated items
            while (snapshotKeys.hasMoreElements()) {
                String newKey = (String)snapshotKeys.nextElement();
                if (status.get(newKey) == null) {
                    if (Log.isLoggable(Log.TRACE)) {
                        Log.trace(TAG_LOG, "Found a new item with key: " + newKey);
                    }
                    String newFP = (String)snapshot.get(newKey);
                    if(!EntryMD5.equalsIgnoreCase(newFP)) {
                    newItems.put(newKey, snapshot.get(newKey));
                    }  else {
                    	Log.error(TAG_LOG, "a empty new,ignore,maybe deleted");
                    }
                } else {
                    // Check if their fingerprints are the same
                    String oldFP = (String)this.status.get(newKey);
                    String newFP = (String)snapshot.get(newKey);
                    if (!oldFP.equals(newFP)) {
                        if (Log.isLoggable(Log.TRACE)) {
                            Log.trace(TAG_LOG, "Found an updated item with key: " + newKey);
                            Log.trace(TAG_LOG, "New fingerprint is: " + newFP);
                            Log.trace(TAG_LOG, "Old fingerprint is: " + oldFP);
                        }
                        if(!EntryMD5.equalsIgnoreCase(newFP)) {
                        	updatedItems.put(newKey, newFP);
                        } else {
                        	Log.error(TAG_LOG, "a empty update,ignore,maybe deleted");
                        }
                    }
                }
            }
            // Detect deleted items
            Enumeration statusKeys = this.status.keys();
            while (statusKeys.hasMoreElements()) {
                String oldKey = (String)statusKeys.nextElement();
                if (snapshot.get(oldKey) == null) {
                    if (Log.isLoggable(Log.TRACE)) {
                        Log.trace(TAG_LOG, "Found a deleted item with key: " + oldKey);
                    }
                    deletedItems.put(oldKey, (String)status.get(oldKey));
                }
            }
            

            
        } else if(syncMode == SyncSource.FULL_SYNC ||
                  syncMode == SyncSource.FULL_UPLOAD ||
                  syncMode == SyncSource.FULL_DOWNLOAD) {
            // Reset the status when performing a slow sync
            try {
                status.reset();
            } catch(IOException ex) {
                Log.error(TAG_LOG, "Cannot reset status", ex);
                throw new TrackerException("Cannot reset status");
            }
        }
    }
    
    
    public void end() throws TrackerException {
        if(Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "end");
        }
        // Allow the GC to pick this memory
        newItems      = null;
        updatedItems  = null;
        deletedItems  = null;
    }

    public boolean hasChangedSinceLastSync(String key, long ts) {
        if (updatedItems != null) {
//            return updatedItems.contains(key);
        	return updatedItems.containsKey(key);
        } else {
            return false;
        }
    }

    public boolean supportsResume() {
        return true;
    }

 

    public Enumeration getNewItems() throws TrackerException {
        if(Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "getNewItems");
        }
        // Any item in the sync source which is not part of the
        // old state is a new item
        if (newItems != null) {
//            return newItems.elements();
        	return newItems.keys();
        } else {
            return null;
        }
    }

    public int getNewItemsCount() throws TrackerException {
        if (newItems != null) {
            return newItems.size();
        } else {
            return 0;
        }
    }

    public Enumeration getUpdatedItems() throws TrackerException {
        if(Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "getUpdatedItems");
        }
        // Any item whose fingerprint has changed is a new item
        if (updatedItems != null) {
//            return updatedItems.elements();
        	return updatedItems.keys();
        } else {
            return null;
        }
    }

    public int getUpdatedItemsCount() throws TrackerException {
        if (updatedItems != null) {
            return updatedItems.size();
        } else {
            return 0;
        }
    }

    public Enumeration getDeletedItems() throws TrackerException {
        if(Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "getDeletedItems");
        }
        // Any item in the sync source which is not part of the
        // old state is a new item
        if (deletedItems != null) {
//            return deletedItems.elements();
        	return deletedItems.keys();
        } else {
            return null;
        }
    }

    public int getDeletedItemsCount() throws TrackerException {
        if (deletedItems != null) {
            return deletedItems.size();
        } else {
            return 0;
        }
    }

    public void setItemsStatus(Vector itemsStatus) throws TrackerException {
        Vector filteredItemsStatus = new Vector();
        for(int i=0;i<itemsStatus.size();++i) {
            ItemStatus status = (ItemStatus)itemsStatus.elementAt(i);
            String key = status.getKey();
            long id = Long.parseLong(key);
            int itemStatus = status.getStatus();
            if (isSuccess(itemStatus) && itemStatus != SyncSource.CHUNK_SUCCESS_STATUS) {
//                if (deletedItems.contains(key)) {
//                    cm.hardDelete(id);
//                } else {
//                    filteredItemsStatus.addElement(status);
//                }
            	setItemStatus(key,itemStatus);
            }
        }

        // Now apply all the changes in one shot
//        try {
//            cm.refreshSourceIdAndDirtyFlag(filteredItemsStatus);
//        } catch (IOException ioe) {
//            throw new TrackerException("Cannot set dirty flag");
//        }
    }

    public boolean filterItem(String key, boolean removed) {
        return false;
    }

    protected Uri addCallerIsSyncAdapterFlag(Uri uri) {
        Uri.Builder b = uri.buildUpon();
        b.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");
        return b.build();
    }
    
    protected boolean isSuccess(int status) {
        if(Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "isSuccess " + status);
        }
        return SyncSource.SUCCESS_STATUS == status;
    }

    public void empty() throws TrackerException {
        // Nothing to do
    }

    public boolean removeItem(SyncItem item) throws TrackerException {
        // Nothing to do
        return true;
    }

    public void reset() throws TrackerException {

        try{
            status.reset();
        } catch (Exception e){
            throw new TrackerException(e.toString());
        }
        
        try {
			status.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void setSyncSource(TrackableSyncSource ss) {
        this.ss = ss;
    }

    public boolean hasChanges() {
//        boolean result = false;
//
//        StringBuffer whereClause = getAccountWhereClause();
//        whereClause.append(" AND ");
//        whereClause.append(RawContacts.DIRTY).append("=1");
//
//        Cursor items = resolver.query(RawContacts.CONTENT_URI, 
//                new String[] {ContactsContract.RawContacts.DIRTY},
//                whereClause.toString(), null, null);
//
//        result = items.getCount() > 0;
//        items.close();
//
//        return result;
    	
    	return true;
    }

    private StringBuffer getAccountWhereClause() {

        StringBuffer whereClause = new StringBuffer();
        
        Account account = AndroidController.getNativeAccount();
        if(account != null) {
            String accountType = account.type;
            String accountName = account.name;
            
            if(accountName != null && accountType != null) {
                whereClause.append(RawContacts.ACCOUNT_NAME).append("='")
                        .append(accountName).append("'");
                whereClause.append(" AND ");
                whereClause.append(RawContacts.ACCOUNT_TYPE).append("='")
                        .append(accountType).append("'");
            }
        } else {
            whereClause.append("(0)");
        }
        return whereClause;
    }
    
    protected void setItemStatus(String key, int itemStatus) throws TrackerException {
        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "setItemStatus " + key + "," + itemStatus);
        }

        if(isSuccess(itemStatus) && (syncMode == SyncSource.FULL_SYNC ||
                syncMode == SyncSource.FULL_UPLOAD)) {
            SyncItem item = new SyncItem(key);
            try {
            	if(ss instanceof ContactSyncSource) {
            		ContactSyncSource css = (ContactSyncSource) ss;
                item = css.getItemContent(item);
            	}
            } catch(SyncException ex) {
                throw new TrackerException(ex.toString());
            }
            if(status.get(key) != null) {
                status.update(key, computeFingerprint(item));
            } else {
                status.add(key, computeFingerprint(item));
            }
        } else if (isSuccess(itemStatus) && itemStatus != SyncSource.CHUNK_SUCCESS_STATUS) {
            // We must update the fingerprint store with the value of the
            // fingerprint at the last sync
            if (newItems.get(key) != null) {
                // This is a new item
                String itemFP = (String)newItems.get(key);
                // Update the fingerprint
                status.add(key, itemFP);
            } else if (updatedItems.get(key) != null) {
                // This is a new item
                String itemFP = (String)updatedItems.get(key);
                // Update the fingerprint
                status.update(key, itemFP);
            } else if (deletedItems.get(key) != null) {
                // Update the fingerprint
                status.remove(key);
            }
            // Save the status after each item
            try {
                this.status.save();
            } catch (Exception e) {
                // We try to let this error go trough as we save the status at
                // the end of the sync. Even though it is likely that operation
                // will fail as well and an exception will be thrown
                Log.error(TAG_LOG, "Cannot save tracker status, the status will be written at the end");
            }
        } else {
            // On error we do not change the fp so the change will
            // be reconsidered at the next sync
        }
        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "status set for item: " + key);
        }
    }
}
