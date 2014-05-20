/**
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2011 Funambol, Inc.
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

package cn.eben.android.source.edisk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;


import cn.eben.android.net.apps.EDiskHelper;
import cn.eben.android.net.apps.EbenDownFileAuth;
import cn.eben.android.net.apps.EbenUpFileAuth;
import cn.eben.android.providers.SyncSourceProvider;
import cn.eben.android.util.DiskMemory;
import cn.eben.android.util.EbenHelpers;
import cn.eben.android.util.MD5Util;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.App;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.platform.FileAdapter;
import com.funambol.platform.FileSystemInfo;
import com.funambol.sync.ItemDownloadInterruptionException;
import com.funambol.sync.NonBlockingSyncException;
import com.funambol.sync.ResumableSource;
import com.funambol.sync.SourceConfig;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncSource;
import com.funambol.sync.TwinDetectionSource;
import com.funambol.sync.client.TrackableSyncSource;
import com.funambol.sync.client.TrackerException;
import com.funambol.syncml.client.FileObject;
import com.funambol.syncml.client.FileObjectInputStream;
import com.funambol.syncml.client.FileObjectOutputStream;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.spds.SyncMLAnchor;
import com.funambol.util.Log;

public class EdiskSyncSource extends TrackableSyncSource implements
		TwinDetectionSource, ResumableSource {
	private static final String TAG_LOG = "EdiskSyncSource";
	public static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;
	public static final String TEMPFILE = ".sync~";
//	public static boolean bMakeingItems; // for copy temp file. true: not copy temp
	/** file in getitemcontent */
	private AppSyncSource appSource;
	private int allKeyCount;
	/** filter enote from edisk */
	private FileFilter filter = null;// new EdiskFileFilter();

	public static final String EmptyMd5 = "D41D8CD98F00B204E9800998ECF8427E";
	public static final String WAVELINE = "~";
	public static final int MaxItemCount = 2000;
	
	public static int netstate = 0;
	ArrayList<UpfileInfo> uplist = null;
	ArrayList<UpfileInfo> localList = null;

	private ArrayList<EdiskLuidInfo> luidinfo;
	private ArrayList<EdiskLuidInfo> syncedluidinfo;// 
	private ArrayList<EdiskLuidInfo> updateluidinfo;//
	private ArrayList<EdiskLuidInfo> newluidinfo;//
	private ArrayList<EdiskLuidInfo> deleteluidinfo;//
	
	
	int updateAnchor = 0;
	int newAnchor = 0;
	int deleteAnchor = 0;
	
	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}
	
	public FileFilter getfileFileter() {
		return filter;
	}

	/**
	 * server delelte folder ,should ensure all files delted in folder ,del
	 * folder in end sync
	 */
	private ArrayList<String> delList;

	public AppSyncSource getAppSource() {
		return appSource;
	}

	public void setAppSource(AppSyncSource appSource) {
		this.appSource = appSource;
	}

	protected class FileSyncItem extends SyncItem {

		protected OutputStream os = null;
		protected InputStream is = null;
		protected String prologue;
		protected String epilogue;
		protected String fileName;

		protected String key;

		public FileSyncItem(String fileName, String key) throws IOException {
			this(fileName, key, null, SyncItem.STATE_NEW, "");
		}

		public FileSyncItem(String fileName, String key, String type,
				char state, String parent) throws IOException {

			super(key, type, state, parent);
			Log.debug(TAG_LOG, "FileSyncItem filename is " + fileName
					+ " key is " + key + " type is " + type);
			this.fileName = fileName;
			this.key = key;
			EdiskSyncSource.fullDirecoty = directory;
			FileAdapter file = new FileAdapter(fileName);
			boolean isExist = file.exists();
			if (SourceConfig.FILE_OBJECT_TYPE.equalsIgnoreCase(getType())) {
				Log.trace(TAG_LOG, "FileSyncItem is file object");
				// Initialize the prologue
				FileObject fo = new FileObject();
				// fo.setName(fileName.replace(directory,
				// ""));//directory/fileName
//				String decodeName = EbenHelpers.decodeKey(key);// new
				// String(Base64.decode(key.getBytes()));
				if(true) {//lierbao
					String name = file.getName();
					if(file.isDirectory()) {
						name = name + File.separator;
					}
					fo.setName(name);
					
				}else{
//				fo.setName(decodeName); // lierbao
				}
				int bodySize = 0;
//				if (!bMakeingItems) {
					// if(null != getKeyContent(key))
//					bodySize = Base64.computeEncodedSize(getKeyContent(key)
//							.length());
					bodySize = getKeyContent(key).length();
//				} 

				
				if(!isExist)  {
					if(isDelResumeKey(key)) {
						fo.setModified(new Date(0));
						if(fileName.endsWith(File.separator)) {
							String path = fileName.replace(directory, "");
							if(path.substring(0, path.length()-1).contains(File.separator)) {
								String name = path.substring(path.substring(0,path.length()-1).lastIndexOf(File.separator)+1);
								fo.setName(name);
							} else {
								fo.setName(path);
							}
						}
					} else {
//						EbenFileLog.recordSyncLog("some file missing in syncing , break sync");
						throw new IOException("some file missing in syncing");
					}
				}
				else {
					fo.setModified(new Date(file.lastModified()));
				}
				
				

				fo.setSize(bodySize);
				if(isExist) {
				fo.setHidden(file.isHidden());
				fo.setWritable(file.canWrite());
				fo.setReadable(file.canRead());
				}

				fo.setAuthSyncInter(AndroidConfiguration.authSyncInter);
				prologue = fo.formatPrologue();

				// Initialize the epilogue
				epilogue = fo.formatEpilogue();
				// Set the size
				setObjectSize(prologue.getBytes().length + bodySize
						+ epilogue.length());
				Log.trace(TAG_LOG, "FileSyncItem size:" + getObjectSize()
						+ ", fos:" + fo.getSize());
			} else {
				// The size is the raw file size
				Log.error(TAG_LOG, "error !!! FileSyncItem is not file object");
				setObjectSize(file.getSize());
			}
			// Release the file object
			file.close();
		}

		public FileSyncItem(String fileName, String key, String type,
				char state, EdiskLuidInfo info) throws IOException {

			super(key, type, state, info.getParent());
			Log.debug(TAG_LOG, "FileSyncItem filename is " + fileName
					+ " key is " + key + " type is " + type);
			this.fileName = fileName;
			this.key = key;
			EdiskSyncSource.fullDirecoty = directory;
			FileAdapter file = new FileAdapter(fileName);
			boolean isExist = file.exists();
			if (SourceConfig.FILE_OBJECT_TYPE.equalsIgnoreCase(getType())) {
				Log.trace(TAG_LOG, "FileSyncItem is file object");
				// Initialize the prologue
				FileObject fo = new FileObject();

				String name = file.getName();
				if (file.isDirectory()) {
					name = name + File.separator;
				}
				
//				newName = newName.replace("&amp;", "&");
//				newName = newName.replace("&lt;", "<");
				name = name.replace("&", "&amp;");
				name = name.replace("<", "&lt;");
				fo.setName(name);

				int bodySize = 0;

				bodySize = getKeyContent(key).length();
				
				if(!isExist)  {
					if(isDelResumeKey(key)) {
						fo.setModified(new Date(0));
						if(fileName.endsWith(File.separator)) {
							String path = fileName.replace(directory, "");
							if(path.substring(0, path.length()-1).contains(File.separator)) {
								String fullname = path.substring(path.substring(0,path.length()-1).lastIndexOf(File.separator)+1);
								fo.setName(fullname);
							} else {
								fo.setName(path);
							}
						}
					} else {
//						EbenFileLog.recordSyncLog("some file missing in syncing , break sync");
						throw new IOException("some file missing in syncing");
					}
				}
				else {
					if(null != info.getUpdateOp()) {
						fo.setUpdateOp(info.getUpdateOp());
					}
					fo.setModified(new Date(file.lastModified()));
				}
				
				

				fo.setSize(bodySize);
				if(isExist) {
				fo.setHidden(file.isHidden());
				fo.setWritable(file.canWrite());
				fo.setReadable(file.canRead());
				}

				fo.setAuthSyncInter(AndroidConfiguration.authSyncInter);
				prologue = fo.formatPrologue();

				// Initialize the epilogue
				epilogue = fo.formatEpilogue();
				// Set the size
				setObjectSize(prologue.getBytes().length + bodySize
						+ epilogue.length());
				Log.trace(TAG_LOG, "FileSyncItem size:" + getObjectSize()
						+ ", fos:" + fo.getSize());
			} else {
				// The size is the raw file size
				Log.error(TAG_LOG, "error !!! FileSyncItem is not file object");
				setObjectSize(file.getSize());
			}
			// Release the file object
			file.close();
		}


		// lierbao for incoming item. not set file name 2011-4-20
		public FileSyncItem(String fileName, String key, String type, char state)
				throws IOException {

			super(key, type, state, null);
			Log.debug(TAG_LOG, "FileSyncItem incoming filename is " + fileName
					+ " key is " + key + " type is " + type);
			this.fileName = fileName;
			this.key = key;
			EdiskSyncSource.fullDirecoty = directory;
		}

		/**
		 * Creates a new output stream to write to. If the item type is
		 * FileDataObject, then the output stream takes care of parsing the XML
		 * part of the object and it fills a FileObject that can be retrieved
		 * later.
		 * 
		 * @see FileObjectOutputStream for more details Note that the output
		 *      stream is unique, so that is can be reused across different
		 *      syncml messages.
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			if (os == null) {
				FileAdapter file = new FileAdapter(fileName);
				os = file.openOutputStream();
				file.close();
				if (SourceConfig.FILE_OBJECT_TYPE.equalsIgnoreCase(getType())) {
					FileObject fo = new FileObject();
					os = new FileObjectOutputStream(fo, os);
				}
			}
			// }
			return os;
			// if (os == null) {
			// FileAdapter file = new FileAdapter(fileName);
			// os = file.openOutputStream();
			// file.close();
			// }
			// return os;
		}

		/**
		 * Creates a new in to read from. If the source is configured to handle
		 * File Data Object, then the stream returns the XML description of the
		 * file.
		 * 
		 * @see FileObjectInputStream for more details.
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			if (is == null) {
				FileAdapter file = new FileAdapter(fileName); // lierbao for
																// upfile stream
				// InputStream is = super.getInputStream();
				// JSONObject jo = new JSONObject();
				String content = null;
				if (AndroidConfiguration.authSyncInter) {

					content = getKeyContent(key);
					is = new ByteArrayInputStream(content.getBytes("utf-8"));
				} else {
					is = file.openInputStream(); // lierbao for upfile stream
				}
				// If this item is a file object, we shall use the
				// FileObjectOutputStream
				if (SourceConfig.FILE_OBJECT_TYPE.equalsIgnoreCase(getType())) {
					if (AndroidConfiguration.authSyncInter)
						is = new FileObjectInputStream(prologue, is, epilogue,
								content.length()); // lierbao
					else
						is = new FileObjectInputStream(prologue, is, epilogue,
								(int) file.getSize());

				}
			}
			return is;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String name) {
			this.fileName = name;
		}
		// If we do not reimplement the getContent, it will return a null
		// content, but this is not used in the ss, so there's no need to
		// redefine it
	}

	public String directory;

	public String getDirectory() {
		return directory;
	}

	public String getKeyContent(String key) throws IOException {
		// TODO Auto-generated method stub
		// EdiskTracker et = (EdiskTracker) tracker;
		String content = null;
		JSONObject jo = new JSONObject();
		for (UpfileInfo in : uplist) {
			// if (EbenHelpers.encodeKey(fileName).equals(in.luid)) {
			if (key.equalsIgnoreCase(in.luid)) {
				try {
					jo.put("DURL", in.durl);
					jo.put("ETAG", in.etag);
					jo.put("SIZE", in.size);
					content = jo.toString();
					content = content.replaceAll("\\\\", "");// for json special
																// char
				} catch (JSONException e) {
					// TODO Auto-generated catch block
//					EbenFileLog.recordSyncLog("getKeyContent err "+e.toString());
					e.printStackTrace();
				}
				break;
			}

		}
		if(null == content) // this not correct {
		{
//			content = buildResume();
//			EbenFileLog.recordSyncLog("getKeyContent, file not find, maybe deleted by others in sync process,break syncing,"+key);
			throw new IOException("file not find, maybe deleted by others in sync process,break syncing,"+key);
		}
		return content;
	}
/**
 * for delete protection, build a content to conflict in server 
 * @return
 */
//	private String buildResume() {
//		// TODO Auto-generated method stub
//		JSONObject jo = new JSONObject();
//		try {
//			jo.put("DURL", "");
//
//			jo.put("ETAG", "");
//			jo.put("SIZE", "");
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		return jo.toString();		
//
//	}

	protected String extensions[] = {};
	public static String fullDirecoty = null;
	protected String tempDirectory;
	protected Customization customization = null;
	// protected Configuration configuration = null;
	protected Context mContext;

	protected EdiskTracker mTracker;

	// -------------------------------------------------------------
	// Constructors

	/**
	 * FileSyncSource constructor: initialize source config
	 */
	public EdiskSyncSource(SourceConfig config, EdiskTracker tracker,
			String directory, String tempDirectory,
			Customization customization, Context context) {

		// super(config, tracker, directory);
		super(config, tracker);
		this.directory = directory;
		this.tempDirectory = tempDirectory;
		this.customization = customization;
		mContext = context;
		mTracker = tracker;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}
	protected SyncItem getItemContentbyluid(final EdiskLuidInfo inf,char state) throws SyncException {
		SourceConfig config = getConfig();
		String type = config.getType();
		// We send the item with the type of the SS
		String key = inf.getLuid();
		Log.trace(TAG_LOG, "getItemContentbyluidkey is :" + key);
//		String fileName = EbenHelpers.decodeKey(key);// new
        EdiskTracker eTracker = (EdiskTracker) tracker;

        String fileName = inf.getPath();

		try {

			FileSyncItem fsi = new FileSyncItem(directory + fileName, key, type,
					state, inf);
			return fsi;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new SyncException(SyncException.CLIENT_ERROR,
					"Cannot create RawFileSyncItem: " + ioe.toString());
		}
	}

	@Override
	public SyncItem getNextItem() throws SyncException {
		// TODO Auto-generated method stub
        if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "getNextItem");
        }
        if (luidinfo == null) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Internal error: allItems not initialized");
        }
        SyncItem item = null;
        EdiskTracker eTracker = (EdiskTracker) tracker;
        if(!luidinfo.isEmpty()) {
        	if(updateAnchor >= luidinfo.size()) {
        		return null;
        	}
        	EdiskLuidInfo inf =  luidinfo.get(updateAnchor);
        	updateAnchor++;

        	item = getItemContentbyluid(inf,SyncItem.STATE_UPDATED);
        }
        
        return item;
	}

	@Override
	public SyncItem getNextNewItem() throws SyncException {
		// TODO Auto-generated method stub
        if (newluidinfo == null) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Internal error: newItems not initialized");
        }
		
        SyncItem item = null;
        EdiskTracker eTracker = (EdiskTracker) tracker;
        if(!newluidinfo.isEmpty()) {
        	if(newAnchor >= newluidinfo.size()) {
        		return null;
        	}
        	EdiskLuidInfo inf =  newluidinfo.get(newAnchor);
        	newAnchor++;
//        	item = new SyncItem(inf.getLuid());
//        	item.setParent(inf.getParent());
//        	
//        	item.setState(SyncItem.STATE_NEW);
        	item = getItemContentbyluid(inf,SyncItem.STATE_NEW);
        }
//        if (newItems.hasMoreElements()) {
//            String key = (String)newItems.nextElement();
//            item = new SyncItem(key);
//            String parent = EdiskLuidInfo.getParentluid(eTracker.getLuidinfo(), key);
//            item.setState(SyncItem.STATE_NEW);
//            item.setParent(parent);
//            item = getItemContentbyluid(item);
//        }
        return item;
	}

	@Override
	public SyncItem getNextUpdatedItem() throws SyncException {
		// TODO Auto-generated method stub
		
        if (updateluidinfo == null) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Internal error: updItems not initialized");
        }
        SyncItem item = null;
        EdiskTracker eTracker = (EdiskTracker) tracker;
        if(!updateluidinfo.isEmpty()) {
        	if(updateAnchor >= updateluidinfo.size()) {
        		return null;
        	}
        	EdiskLuidInfo inf =  updateluidinfo.get(updateAnchor);
        	updateAnchor++;

        	item = getItemContentbyluid(inf,SyncItem.STATE_UPDATED);
        }
        
        return item;

	}

	@Override
	public SyncItem getNextDeletedItem() throws SyncException {
		// TODO Auto-generated method stub
//		super.getNextDeletedItem()
        if (deleteluidinfo == null) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Internal error: delItems not initialized");
        }
        SyncItem item = null;
        EdiskTracker eTracker = (EdiskTracker) tracker;
        if(!deleteluidinfo.isEmpty()) {
        	if(deleteAnchor >= deleteluidinfo.size()) {
        		return null;
        	}
        	EdiskLuidInfo inf =  deleteluidinfo.get(deleteAnchor);
        	deleteAnchor++;
        	item = new SyncItem(inf.getLuid());
        	item.setState(SyncItem.STATE_DELETED);
//        	item = getItemContentbyluid(inf,SyncItem.STATE_DELETED);
        }
        
//        SyncItem item = null;
//        if (delItems.hasMoreElements()) {
//            String key = (String)delItems.nextElement();
//            item = new SyncItem(key);
//            item.setState(SyncItem.STATE_DELETED);
//        }
//        return item;
        
        return item;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void applyChanges(Vector syncItems) throws SyncException {
//		bMakeingItems = true;
		Log.info(TAG_LOG, "applyChanges , size : " + syncItems.size());
//		if (syncItems.size() > 0) {
//			ExSyncManager.i().showSyncNotification(
//					App.i().getApplicationContext(), appSource,
//					ExSyncManager.SYNC_START);
//		}
		downtb = new Hashtable();
		for (int i = 0; i < syncItems.size(); ++i) {
			int status = UNDEFINED_STATUS;
			cancelIfNeeded();
			SyncItem item = (SyncItem) syncItems.elementAt(i);
			try {
				if (item.getState() == SyncItem.STATE_NEW) {
					status = addItem(item);
				} else if (item.getState() == SyncItem.STATE_UPDATED) {
					status = updateItem(item);
				} else { // STATE_DELETED
					status = deleteItem(item.getKey());
				}
			} catch (ItemDownloadInterruptionException ide) {
				// The download got interrupted with a network error (this
				// interrupts the application of other items)
				throw ide;
			} catch (NonBlockingSyncException se) {
				// We interrupt the sync, but first we mark this item
				// according to the type of non-blocking exception raised
				if (se.getCode() == SyncException.LOCAL_DEVICE_FULL) {
					// This item is invalid, nullify its key so that it will
					// not be persisted in the mapping
					item.setSyncStatus(DEVICE_FULL_ERROR_STATUS);
//					EbenFileLog.recordSyncLog(se.toString());
					throw se;
				}
			} catch (Exception e) {
				status = ERROR_STATUS;
//				EbenFileLog.recordSyncLog(e.toString());
			}
			item.setSyncStatus(status);
		}
//		bMakeingItems = false;
	}

	@Override
	protected int getAllItemsCount() throws SyncException {
		// TODO Auto-generated method stub
		return allKeyCount;
		// return super.getAllItemsCount();
	}

	@Override
	public void beginSync(int syncMode, boolean resume) throws SyncException {
		createTempDir();
		removeTempFiles();
		resetDelist();


		String sdCardRoot = FileSystemInfo.getSDCardRoot();
		if (sdCardRoot != null) {
			if (directory.startsWith(sdCardRoot)
					&& !FileSystemInfo.isSDCardAvailable()) {
				// The directory to synchronize is on the sd card but actually
				// it is not available
//				EbenFileLog.recordSyncLog("exception : the sd card is not available");
				throw new SyncException(SyncException.SD_CARD_UNAVAILABLE,
						"The sd card is not available");
			}
		}
		try {
			// Create the default folder if it doesn't exist
			FileAdapter d = new FileAdapter(directory);
			if (!d.exists()) {
				d.mkdirs();
			}
			d.close();
		} catch (IOException ex) {
//			EbenFileLog.recordSyncLog(ex.toString());
			Log.error(TAG_LOG, "Cannot create directory to sync: " + directory,
					ex);
		}

		/*
		 * try { NetworkUtilities.loadEdiskRename(mContext); } catch
		 * (JSONException e) { //no android.util.Log.v(TAG_LOG,
		 * "loadEdiskRename jsonException"); }
		 */
		
//		EbenExsyncProgress prg = new EbenExsyncProgress(1, 1, 1, appSource.getSyncSource()
//				.getName(), appSource.getName(), "", 0);
//		if(EDiskHelper.syncCount + 
//				getClientAddNumber() + getClientReplaceNumber()
//						+ getClientDeleteNumber() > 0 ) {
//
//		prg.setstage("update");
//		prg.send();
//		}
		
//		bMakeingItems = true;
		Log.info(TAG_LOG, "start sync,"+getName()+", sync mode : " +syncMode);
//		EbenFileLog.recordSyncLog("start sync, "+getName()+", sync mode : " +syncMode);
		if (SyncSource.FULL_SYNC == syncMode || SyncSource.FULL_UPLOAD == syncMode) {
			resetPendingDown();
//			netstate = EbenHelpers.isNetworkAvailable();
//			Log.info(TAG_LOG, "netstate : "+netstate);
//			prg.setstage("begin");
//			prg.send();
			EdiskTracker eTracker = (EdiskTracker) tracker;

			eTracker.setScanType(EdiskTracker.ALLSCAN);

			
			beginSyncEdisk(syncMode, resume);
			
			checkRenameList();
			setProgressInfo(syncMode);
			

			requestUpAllupdatefiles() ;
			
			luidinfo=((EdiskTracker) tracker).getLuidinfo();
			
//			prg.setstage("update");
//			prg.send();
		}

//		bMakeingItems = false;
		
		// createTempFiles(syncMode);
	}
	private void resetPendingDown() {
		// TODO Auto-generated method stub
		try {
			

		Log.info(TAG_LOG,"resetPendingDown");
		String sourceName = appSource.getSyncSource().getName();
		ContentResolver cr = App.i().getContentResolver();

		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				SyncSourceProvider.PRIFEX_DOWN+sourceName);
		
		cr.delete(uri, null, null);	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void requestUpAllfiles() {
		// TODO Auto-generated method stub
		// EdiskSyncSource ess = (EdiskSyncSource) ss;
		// String directory = ess.getDirectory();
		// String fileName;
		// boolean hasChanged = false;
		// upFileQuery = new EbenUpFileAuth(ss.getName(), directory);
		// Enumeration.
		Log.info(TAG_LOG, "requestUpAllfiles ");
//		if(null == uplist) {
			uplist = new ArrayList();
//		}
		EbenUpFileAuth upFileQuery = new EbenUpFileAuth(getName(), directory,
				getTempDirectory(), getAppSource());
		boolean hasChanged = false;
		localList = new ArrayList();
		int count = 0;
		boolean isNotExist = false;
		while (allItems.hasMoreElements()) {
			String luid = (String) allItems.nextElement();

			String fileName = directory + EbenHelpers.decodeKey(luid);
			Log.debug(TAG_LOG, "fileName : " + fileName);

			File file = new File(fileName);
			 
//			if (!file.exists()) {
//				Log.error(TAG_LOG, "file not exists : " + fileName);
//				continue;
//			}
			if (file.exists()) {
				if (file.isDirectory() || 0 == file.length()) {
					UpfileInfo item = new UpfileInfo(luid, EmptyMd5, 0, "");
					localList.add(item);
					if (allItems.hasMoreElements()) {
						continue;
					}
				} else {
					String md5 = MD5Util.md5(file);
					if (EmptyMd5.equalsIgnoreCase(md5)) {
						UpfileInfo item = new UpfileInfo(luid, EmptyMd5, 0, "");
						localList.add(item);
						if (allItems.hasMoreElements()) {
							continue;
						}
					}
					long size = file.length();
					int parts = 0;
					if (size > EbenUpFile.PARTSIZE) {
						parts = (int) ((size + EbenUpFile.PARTSIZE - 1) / EbenUpFile.PARTSIZE);
					}
//					upFileQuery.setList(luid, file.length(), md5, parts);
					count++;
				}
			}
			if(count > EdiskSyncSource.MaxItemCount||!allItems.hasMoreElements()) {
				count = 0;

				try {
					upFileQuery.handler();
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					EbenFileLog.recordSyncLog(e.toString());
					e.printStackTrace();
					throw new SyncException(0, e.getMessage());// just for 203 backup .
				}
				uplist.addAll(upFileQuery.getSuccessList());
				upFileQuery = new EbenUpFileAuth(getName(), directory,
						getTempDirectory(), getAppSource());
			}

			hasChanged = true;

		}
		if (hasChanged) {
//			try {
//				upFileQuery.handler();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			uplist = upFileQuery.getSuccessList();
			uplist.addAll(localList);
			Vector items = new Vector();
			if (null != uplist && uplist.size() > 0) {

				for (UpfileInfo info : uplist) {
					items.add(info.luid);

				}

				allItems = items.elements();
			}

		}


	}

	private void setProgressInfo(int syncMode) {
		// TODO Auto-generated method stub
		if (INCREMENTAL_SYNC == syncMode) {
			if (null != newluidinfo && newluidinfo.size()>0) {
				long sum = 0;
				boolean isError = false ;
				for(EdiskLuidInfo info:newluidinfo) {
					String fileName = directory + info.getPath();
					Log.info(TAG_LOG, "new file is " + fileName);
					File file = new File(fileName);
					if (file.length() > MAX_FILE_SIZE) {
						Log.error(TAG_LOG,
								"a large file ,error : "
										+ fileName);
//						EbenFileLog.recordSyncLog("a large file ,error : not sync new ,"+fileName);
						isError = true;
						break;

					}
					if (file.exists()) {
						sum += file.length();		
					}
					
				}

				if(isError) {
////					newItems = new Hashtable().keys();
//					((EdiskTracker) tracker).resetNewItems();
//					newItems = tracker.getNewItems();
					newluidinfo = new ArrayList<EdiskLuidInfo>();
					sum = 0;
					clientAddItemsNumber = 0;					
				}

				if (sum > 0) {

					// space = 10;//for test
//					getStatisticsFromServer();
//					long space = StatisticsService.getSpaces();
//					Log.info(TAG_LOG, "new bytes : " + sum + ", space : "
//							+ space);
//
//					if (space >= 0 && sum > space) {
//						ExSyncManager.i().prompDialog("");
//
//						newluidinfo = new ArrayList<EdiskLuidInfo>();
////						EbenFileLog.recordSyncLog("server space full not sync new items, local new : "+sum+", server remained: " + space );
//						clientAddItemsNumber = 0;
//					}
				}
			}

		}

		long upCount = 0;

//		upCount = EDiskHelper.syncCount;
//
//		if (syncMode == SyncML.ALERT_CODE_SLOW
//				|| syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT) {
//			ExSyncManager.i().setNeedSync(getAllItemsCount());
//			ExSyncManager.i().setCurSync(0);
//		} else {
//			ExSyncManager.i().setCurSync(0);
//
//
//		}
	}

	private void checkRenameList() {
		// TODO Auto-generated method stub
		EdiskTracker et = (EdiskTracker) tracker;
		et.checkRenameList();
	}

//	private void checkFingerPrinter() {
//		// TODO Auto-generated method stub
//		AndroidConfiguration config = App.i().getAppInitializer()
//				.getConfiguration();
//		if (!config.loadBooleanKey(AndroidConfiguration.KEY_MINDCLOUD_CHECKFP
//				+ getName(), false)) {
//			EdiskdbStore.resetStoreFingerPrinter(mContext, getName());
//			config.saveBooleanKey(AndroidConfiguration.KEY_MINDCLOUD_CHECKFP
//					+ getName(), true);
//			config.commit();
//		}
//
//	}
/**
 * 
 * @throws SyncException
 */
	private void requestUpfiles() throws SyncException{
		// TODO Auto-generated method stub
		EdiskTracker et = (EdiskTracker) tracker;
		et.upFileQuery();
		Log.debug(TAG_LOG, "requestUpfiles");

//		uplist = et.getUplist();
		uplist.addAll(et.getUplist());
	}
/**
 * 
 * @throws SyncException
 */
	private void requestUpAllupdatefiles() throws SyncException{
		// TODO Auto-generated method stub
		uplist = new ArrayList();
		EdiskTracker et = (EdiskTracker) tracker;
		et.upAllFileQuery();

//		uplist = et.getUplist();
		uplist.addAll(et.getUplist());
	}
	
	private void getStatisticsFromServer() {
//		EbenSyncCmd.getAppsInfo();
		// HttpJSONService jServ = HttpJSONService.getInstance();
		// jServ.setOp(HttpJSONserviceConst.OP_STATISTICS_STATUS_REQUESTION);
		// jServ.setServUrl(HttpJSONService.DS_URI);// jason
		// try {
		// jServ.handler();
		//
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// Log.error(TAG_LOG, "exception err" + e);
		// }
	}

	@Override
	public void applyItemsStatus(Vector itemsStatus) {
		// delete the temp files
//		for (int i = 0; i < itemsStatus.size(); ++i) {
//			ItemStatus itemStatus = (ItemStatus) itemsStatus.elementAt(i);
//			String key = itemStatus.getKey();
//			String fileName = EbenHelpers.decodeKey(key);// new
//			// String(Base64.decode(key));
//			EbenFileLog.recordSyncLog("upinfo: " + directory + fileName
//					+ " --> cloud ");
//			deleteFiles(new File(tempDirectory + fileName));
//		}
//		bMakeingItems = true;
		super.applyItemsStatus(itemsStatus);
//		bMakeingItems = false;
	}

	@Override
	protected void setItemStatus(String key, int status) throws SyncException {
		if (status == SyncSource.SERVER_FULL_ERROR_STATUS) {
			// The user reached his quota on the server
			if (Log.isLoggable(Log.INFO)) {
				Log.info(TAG_LOG, "Server is full");
			}
//			EbenFileLog.recordSyncLog("excepiton: Server is full");
			throw new SyncException(SyncException.DEVICE_FULL, "Server is full");
		}
		super.setItemStatus(key, status);
	}

	@Override
	public SyncItem createSyncItem(String key, String type, char state,
			String parent, long size) throws SyncException {
		Log.debug(TAG_LOG, "createSyncItem key ," + key + ", state , " + state);
		String fileName;
		boolean longName = false;
		if(key.length() > 20 ) {
			Log.info(TAG_LOG, "key length is long");
			longName = true;
		}
		switch (state) {
		case SyncItem.STATE_NEW:
		case SyncItem.STATE_UPDATED:
			if(longName) {
				fileName = tempDirectory+MD5Util.md5(key);
			}
			else {
				fileName = tempDirectory + key;
			}
			if (key.contains("/")) {
				String folder = key.substring(0, key.lastIndexOf("/"));
				String name = key.substring(key.lastIndexOf("/") + 1,
						key.length());
				Log.debug(TAG_LOG, "folder is " + folder + ", fileName is "
						+ name);
				File fileFolder = new File(tempDirectory + folder);
				fileFolder.mkdirs();
			}
			break;
		case SyncItem.STATE_DELETED:
			if(longName) {
				fileName = directory + MD5Util.md5(key);
			} else {
				fileName = directory + key;
			}
		default:
			fileName = key;
			break;
		}
		/*
		 * if (state == SyncItem.STATE_NEW) { // when createSyncItem is called
		 * for a new file item, the directory // name must be appended //
		 * fileName = directory + key; fileName = tempDirectory + key; } else if
		 * (state == SyncItem.STATE_UPDATED) {
		 * if(key!=null&&key.contains("/")&&key.contains(directory)){ fileName =
		 * key; // item.setKey(fileName.replace(directory, ""));
		 * 
		 * } else { fileName = tempDirectory + key; } // when createSyncItem is
		 * called for an update file item, the key // has // already the
		 * complete file path // TODO // check if this method is called for the
		 * creation of an item that // must be deleted (theorically, no) //
		 * fileName = key; }
		 */
		try {
			FileSyncItem item = new FileSyncItem(fileName, key, type, state);
			item.setParent(parent);
			return item;
		} catch (IOException ioe) {
//			EbenFileLog.recordSyncLog(ioe.toString());
			throw new SyncException(SyncException.CLIENT_ERROR,
					"Cannot create RawFileSyncItem: " + ioe.toString());
		}
	}
	
	protected void applyDownProperties(FileSyncItem fsi) throws IOException {
		OutputStream os = fsi.getOutputStream();
		if (os instanceof FileObjectOutputStream) {
			FileObjectOutputStream foos = (FileObjectOutputStream) os;
		}
		
		if (os instanceof FileObjectOutputStream) {
			FileObjectOutputStream foos = (FileObjectOutputStream) os;
			
		}
	}


	protected void applyFileProperties(FileSyncItem fsi) throws IOException {
		OutputStream os = fsi.getOutputStream();
		if (os instanceof FileObjectOutputStream) {
			FileObjectOutputStream foos = (FileObjectOutputStream) os;
			applyFileObjectProperties(fsi, foos);
			// The key for this item must be updated with the real
			// file name
			FileObject fo = foos.getFileObject();
			String newName = fo.getName();
			// The name is mandatory, but we try to be more robust here
			// and deal with items with no name
			if (newName != null) {
				// fsi.setKey(directory + newName);
				String encodeKey = EbenHelpers.encodeKey(newName);// new
				// String(Base64.encode(newName.getBytes()));
				Log.debug(TAG_LOG, "encode Key : " + encodeKey);
				fsi.setKey(encodeKey);
			} else {
				Log.error(TAG_LOG, "got null name, some error!!");
			}
			// foos.close();// lierbao 12-02-07 . it has close at
			// syncsourceLOhandler
		}
	}
	private Hashtable downtb ;
	protected boolean applyFileInfo(FileSyncItem fsi) throws IOException {
		OutputStream os = fsi.getOutputStream();
		FileObjectOutputStream foos = (FileObjectOutputStream) os;
		EdiskTracker eTracker = (EdiskTracker) tracker;
//		applyFileObjectProperties(fsi, foos);
		FileObject fo = foos.getFileObject();
		String newName = fo.getName();
		
		newName = newName.replace("&amp;", "&");
		newName = newName.replace("&lt;", "<");
		
		FileAdapter file = new FileAdapter(fsi.getFileName());
		
		if (fo.iswithdata()) {
			Log.error(TAG_LOG,"old interface data");
//			EbenFileLog.recordSyncLog("Exception : is withdata ,old interface data");
			throw new IOException("old interface data");
		}
		boolean hasExist = false;
		boolean isrename = false;
		InputStream in = null;
		final int Length = 1024;

		in = file.openInputStream();
		int available = in.available();
		Log.info(TAG_LOG, "applyFileInfo available = " + available);
		if (available < Length && available > 0) {			
			byte[] tempbytes = new byte[Length];
			int hasRead = in.read(tempbytes);
			// Log.info(TAG_LOG,"url : "+tempbytes);
			String uri = new String(tempbytes, 0, hasRead);
			Date lastModified = fo.getModified();
//			lastModified.getTime();
			String luid = fsi.getKey();
			String parent = fsi.getSourceParent();
			if(null == parent) {
				parent = fsi.getParent();
			}
//			fsi.get
			// isparentillegl
//			EdiskLuidInfo(String luid, String parent, String path, String fp)
			EdiskLuidInfo itemInfo = new EdiskLuidInfo(null,parent,null,null);
			Log.debug(TAG_LOG, "luid : "+luid+",parent : "+parent);
			if(!EdiskLuidInfo.isLuidFound(mContext,getName(),parent,itemInfo)){
				if(downtb.containsKey(parent)) {
					parent= (String) downtb.get(parent);
					Log.debug(TAG_LOG, "new parent : "+parent);
//					itemInfo.setp
					
				} else {
				Log.error(TAG_LOG, "error !! parent not found : "+parent);
//				EbenFileLog.recordSyncLog("error !! parent not found : "+parent);
				throw new SyncException(SyncMLStatus.PARENT_DELETED, "parent not found");
//				return false;
				}
			} 
//			String parentPath = "";
			if(!EdiskLuidInfo.ROOT.equals(parent)){
				String  parentPath = EdiskLuidInfo.getLuidPath(mContext, getName(), parent);
				if(!parentPath.endsWith(File.separator)) {
					Log.error(TAG_LOG, "parent not a folder,"+parent.toString());
					throw new SyncException(SyncMLStatus.PARENT_DELETED, "parent not a folder");
				}
				if(null != parentPath) {
					newName = parentPath + newName;
				}
			}
			String newluid = luid;
			Log.debug(TAG_LOG, "state: "+fsi.getState()+", new name : "+newName+",newluid : "+newluid);
			// add cmd not process as replace ,for replace is luid and add is guid got
			if(fsi.getState() == SyncItem.STATE_NEW) {
				// find the same file ,shoud delete at table
//				if(EdiskLuidInfo.isLuidFound(mContext,getName(),newluid,itemInfo) ){
//					Log.error(TAG_LOG, "error !!, find same luid ,should delete old");
//					EbenFileLog.recordSyncLog("error !!, find same luid ,should delete old,  "+newName);
//					EdiskLuidInfo.deleteyncedItemChild(mContext, getName(), itemInfo, eTracker.getLuidinfo());
//				}
				// if we delete same  file ,should createluid first ,
//				must delete otherwise same file issue , this new luid put to table at function end
//				newluid = EdiskLuidInfo.createLuid(mContext,getName());
				String checkluid = EdiskLuidInfo.getExistluid(mContext, getName(), newName, itemInfo);
				if(null != checkluid) {
					Log.error(TAG_LOG, "error !!, find same file on new  ,should not delete old,use old");
//					EbenFileLog.recordSyncLog("error !!, find same file  on new,not delete old,ued old,  "+newName);
//					EdiskLuidInfo delInfo = new EdiskLuidInfo(checkluid,parent,newName,null);
//					EdiskLuidInfo.deleteyncedItemChild(mContext, getName(), delInfo, eTracker.getLuidinfo());
					newluid = checkluid;
				}else  {
				// if we not delete same file we should create luid after
					newluid = EdiskLuidInfo.createLuid(mContext,getName());
				}
//				EdiskLuidInfo.getluid(mContext, getName(), name, info)
				fsi.setKey(newluid);
				
				itemInfo.setLuid(newluid);
				itemInfo.setParent(parent);
				itemInfo.setPath(newName);
				itemInfo.setFp(getFileEtag(uri));
				itemInfo.setissynced(EdiskLuidInfo.SYNC_NOTDOWN);
				Log.debug(TAG_LOG, "put to hashtable : "+"luid, "+luid+", newluid, "+newluid);
				downtb.put(luid, newluid);
				
			} else if (fsi.getState() == SyncItem.STATE_UPDATED)  {
				if(EdiskLuidInfo.isLuidFound(mContext,getName(),newluid,itemInfo)) {
					if(!newName.equals(itemInfo.getPath())) {
						Log.info(TAG_LOG,"got a renamed , "+itemInfo.getPath()+" --> "+newName);
						if(!new File(directory + itemInfo.getPath()).exists()) {
							Log.error(TAG_LOG, "error !! update file not exist  : "+itemInfo.getPath()+" --> "+newName);
//							EbenFileLog.recordSyncLog("error !! update file not exist  : "+itemInfo.getPath()+" --> "+newName);
							if(newName.endsWith(File.separator)) {
//								EbenFileLog.recordSyncLog("create dir  : "+directory+newName);
								new File(directory+newName).mkdirs();
							} else {
//							return false;
//								Log.error(TAG_LOG, "error !! update file not exist  : "+itemInfo.getPath()+" --> "+newName);
							}
							
						} else {
						if(renamefile(itemInfo.getPath(),newName)) {
//							ExSyncManager.i().notifyItemResult(appSource, itemInfo.getPath(),
//									SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END,
//									SyncItem.STATE_DELETED);
							
							// write data to main db table
							itemInfo.setPath(newName);
							itemInfo.setParent(parent);
//							lastModified
							new File(directory+newName).setLastModified(lastModified.getTime());
							Log.info(TAG_LOG,"renamed ok, parent "+parent +" newName "+newName);
//							ExSyncManager.i().notifyItemResult(appSource, newName,
//									SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END,
//									SyncItem.STATE_NEW);
							
							isrename = true;
							
						} else {
							Log.error(TAG_LOG, "error !! rename failed  : "+itemInfo.getPath()+" --> "+newName);
//							EbenFileLog.recordSyncLog("error !! rename failed  : "+itemInfo.getPath()+" --> "+newName);
							return false;						
						}
						}
					}
				} 
				 else {
						Log.error(TAG_LOG, "error !!!,replaced luid not found ,something wrong,contiue as add  ");
//						EbenFileLog.recordSyncLog("error !!!,replaced luid not found ,something wrong ,contiue as add");
//						return false;
//						if(EdiskLuidInfo.isLuidFound(mContext,getName(),newluid,itemInfo) ){
//							Log.error(TAG_LOG, "error !! ,find same luid ,should delete old");
//							EdiskLuidInfo.deleteyncedItemChild(mContext, getName(), itemInfo, eTracker.getLuidinfo());
//						}
						String checkluid = EdiskLuidInfo.getExistluid(mContext, getName(), newName, itemInfo);
						if(null != checkluid) {
							Log.error(TAG_LOG, "error !!, find same file on update  ,should delete old");
//							EbenFileLog.recordSyncLog("error !!, find same file  on update,should delete old,  "+newName);
							EdiskLuidInfo delInfo = new EdiskLuidInfo(checkluid,parent,newName,null);
							EdiskLuidInfo.deleteyncedItemChild(mContext, getName(), delInfo, eTracker.getLuidinfo());
						}						
						newluid = EdiskLuidInfo.createLuid(mContext,getName());
//						EdiskLuidInfo.getluid(mContext, getName(), name, info)
						fsi.setKey(newluid);
						
						itemInfo.setLuid(newluid);
						itemInfo.setParent(parent);
						itemInfo.setPath(newName);
						itemInfo.setFp(getFileEtag(uri));
						itemInfo.setissynced("notdown");
						Log.debug(TAG_LOG, "update,put to hashtable : "+"luid, "+luid+", newluid, "+newluid);
						downtb.put(luid, newluid);
					}
			} else {
				Log.error(TAG_LOG, "error at item state");
				return false;	
			}
//			if(EdiskLuidInfo.isLuidFound(mContext,getName(),newluid,itemInfo)) {
//				if(!newName.equals(itemInfo.getPath())) {
//					Log.info(TAG_LOG,"got a renamed , "+itemInfo.getPath()+" --> "+newName);
//					if(renamefile(itemInfo.getPath(),newName)) {
//						// write data to main db table
//						itemInfo.setPath(newName);
//						itemInfo.setParent(parent);
//						Log.info(TAG_LOG,"renamed ok, parent "+parent +" newName "+newName);
//					} else {
//						Log.error(TAG_LOG, "error !! rename failed  : "+itemInfo.getPath()+" --> "+newName);
//						EbenFileLog.recordSyncLog("error !! rename failed  : "+itemInfo.getPath()+" --> "+newName);
//						return false;						
//					}
//				}
//			} else {
//				luid = EdiskLuidInfo.createLuid(mContext,getName());
////				EdiskLuidInfo.getluid(mContext, getName(), name, info)
//				fsi.setKey(luid);
//				
//				itemInfo.setLuid(luid);
//				itemInfo.setParent(parent);
//				itemInfo.setPath(newName);
//				itemInfo.setFp(getFileEtag(uri));
//				itemInfo.setissynced("0");
//			}
			hasExist = false;
			// maybe this for renamed file. in theoris ,may renamed and update in one cmd. so hava renamed and down 
			// check here,maybe renamed will cause down load.
			// maybe not notify ,notify at renamed and down.
			if(itemInfo.getFp().equalsIgnoreCase(getFileEtag(uri))) {
				File hasFile = new File(directory+newName);
				if(hasFile.exists()) {
					if(hasFile.isDirectory() ||MD5Util.md5(hasFile).equalsIgnoreCase(getFileEtag(uri))) {
						hasExist = true;
						// set at renamed .
						if(!isrename) {
						hasFile.setLastModified(lastModified.getTime());
						}
					}
				}
				

			}
			if(hasExist) {
				itemInfo.setissynced(EdiskLuidInfo.SYNCED);
				
				// has notify at rename before
				// just notfiy end
//				if(!isrename) {
//				ExSyncManager.i().notifyItemResult(appSource, newName,
//						SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_START, SyncItem.STATE_UPDATED);
//				}
			}
			// set data to db table
			EdiskLuidInfo.updateSyncedItemResult(mContext, getName(), itemInfo);
			
			Log.debug(TAG_LOG, "newluid: "+newluid+",parent: "+parent);
			if(!hasExist) {
				Log.debug(TAG_LOG, "add to down table : "+newName);
				// wheather  set synced false ? set state to notdown state ?
				addItemDown(newName,getFileEtag(uri),lastModified.getTime(),getFileDurl(uri),
						getFileSize(uri),newluid,parent);
			} else {

				if(!isrename) {
//					ExSyncManager.i().notifyItemResult(appSource, newName,
//						SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END, SyncItem.STATE_UPDATED);
				}
			}
			// set map here 
//			if (newName != null) {
//				// fsi.setKey(directory + newName);
//				String encodeKey = EbenHelpers.encodeKey(newName);// new
//				// String(Base64.encode(newName.getBytes()));
//				Log.debug(TAG_LOG, "encode Key : " + encodeKey);
//				fsi.setKey(encodeKey);
//			} else {
//				Log.error(TAG_LOG, "got null name, some error!!");
//			}
			// set map here
		} else {
			Log.error(TAG_LOG, "stream url is too lenth");
//			EbenFileLog.recordSyncLog("Exception:"));
			throw new IOException("not found file ");
		}
		
		return true;
	}
	
	private boolean renamefile(String path, String newpath) {
		// TODO Auto-generated method stub
		String oldname = directory + path;
		String newname = directory + newpath;
		
		File file = new File(oldname);
		File newFile = new File(newname);
		if(file.renameTo(newFile)) {
//			file.set
			Log.info(TAG_LOG, "rename ok");
			return true;
		}
		
		return false;
	}

	private void addItemDown(String name, String fileEtag, long time,
			String fileuri, long fileSize, String luid, String parent) throws IOException{
		// TODO Auto-generated method stub
		Log.info(TAG_LOG, "addItemDown name : "+name+", etag: "
		+fileEtag+", uri : "+fileuri+", size: "+fileSize);
		
		ContentResolver cr = App.i().getContentResolver();

		String sourceName = getName();
		Log.info(TAG_LOG, "source name is : " + sourceName);

		Uri uri = Uri.withAppendedPath(Uri.parse(SyncSourceProvider.AUTHORITY_URI), SyncSourceProvider.PRIFEX_DOWN+sourceName);
		
		String[] projection = { SyncSourceProvider.NAME };
		String selection = SyncSourceProvider.NAME+" = ?";
		String[] args = { name };

		boolean isNew = true;
		Cursor c = cr.query(uri, projection, selection, args, null);
		if (null != c && c.getCount() > 0) {
			isNew = false;
		}
		if(null != c) {
			c.close();
		}
		ContentValues cv = new ContentValues();
		cv.put(SyncSourceProvider.NAME, name);
		cv.put(SyncSourceProvider.ETAG, fileEtag);
		cv.put(SyncSourceProvider.MODIFY, time);
		cv.put(SyncSourceProvider.SIZE, fileSize);
		cv.put(SyncSourceProvider.URI, fileuri);
		cv.put(SyncSourceProvider.LUID, luid);
		cv.put(SyncSourceProvider.PARENT, parent);

		if (isNew) {
			cr.insert(uri, cv);
		} else {
			cr.update(uri, cv, selection, args);
		}
		
	}

	protected void applyFileObjectProperties(FileSyncItem fsi,
			FileObjectOutputStream foos) throws IOException {
		Log.trace(TAG_LOG, "applyFileObjectProperties");
		FileObject fo = foos.getFileObject();
		String newName = fo.getName();
		FileAdapter file = new FileAdapter(fsi.getFileName());
		// foos.ge
		// stream download start
		boolean hasExist = false;
		if (!fo.iswithdata()) {
			file = applyFileData(fo, file, newName, fsi);
			if(null == file) {
				hasExist = true;
			}
		}
		// stream downlad end

		boolean bMk = false;
		boolean bFolder = false;
		Log.debug(TAG_LOG,
				"newName is " + newName + ", fileName is " + fsi.getFileName());
		if (newName != null&&!hasExist) {
			// File file2 = new File();
			// file2.
			// Rename the file
			// newName tt/yy/host.txt
			// file: Path /mnt/sdcard/MediaHub-Files/1417
			newName = newName.replace("&amp;", "&");
			newName = newName.replace("&lt;", "<");
			if (newName.endsWith(File.separator)) {
				Log.info(TAG_LOG, "a folder : " + newName);
				File fileFolder = new File(directory + newName);
				bMk = fileFolder.mkdirs();
				if (bMk) {
					Log.info(TAG_LOG, " mkdir ok");
//					EbenFileLog.recordSyncLog("down: cloud --> " + directory + newName);
				} else {
					Log.info(TAG_LOG, "failed to mkdir");
				}
			} else if (newName.contains("/")) {
				// if(newName.)
				String folder = newName.substring(0, newName.lastIndexOf("/"));// tt/yy
				String tempNewFileName = newName.substring(
						newName.lastIndexOf("/"), newName.length());// /host.txt
				Log.info(TAG_LOG, "folder is " + folder
						+ ", tempNewFileName is " + tempNewFileName);
				bFolder = tempNewFileName.equalsIgnoreCase("/" + TEMPFILE);
				File fileFolder = new File(directory + folder);
				bMk = fileFolder.mkdirs();
				if (bMk) {
					Log.info(TAG_LOG, " mkdir ok");
				} else {
					Log.info(TAG_LOG, "failed to mkdir");
				}
				String nFileName = directory + folder + tempNewFileName;
				File checkfile = new File(nFileName);
				String backup = tempDirectory
						+ String.valueOf(System.currentTimeMillis())
						+ ".backup";
				if (checkfile.exists()) {
					if (!checkfile.renameTo(new File(backup))) {
						throw new IOException("backup failed");
					}
				}
				if (file.rename(nFileName)) {
					Log.info(TAG_LOG, "rename is ok : " + nFileName);
//					EbenFileLog.recordSyncLog("down: cloud --> " + nFileName);
					new File(backup).delete();
				} else {
					new File(backup).renameTo(new File(nFileName));
					throw new IOException("rename failed");
					// Log.error(TAG_LOG, "rename is failed : " + nFileName);
					// if (copyFile(file.toString(), nFileName)) {
					// Log.info(TAG_LOG, "copy ok");
					// file.delete();
					// } else {
					// throw new IOException("copy failed ");
					// }
				}
			} else {
				if (file.rename(directory + newName)) {
					Log.info(TAG_LOG, "rename ok " + directory + newName);
//					EbenFileLog.recordSyncLog("down: cloud --> " + directory
//							+ newName);
				} else {
					throw new IOException(newName+" rename failed ");
					// if (copyFile(file.toString(), directory + newName)) {
					// Log.info(TAG_LOG, "copy ok");
					// file.delete();
					// } else {
					// Log.error(TAG_LOG, "rename failed " + directory
					// + newName);
					// throw new IOException("rename failed ");
					// }
				}
			}

			// Runtime.getRuntime().exec("ls");//if the file is folder ,we need
			// mv the file to folder and then rename it

		} else {
			Log.error(TAG_LOG, "The received item does not have a valid name or has exist .");
		}
		if(null != file)
		file.close();

		// Apply the modified date if present
		FileAdapter newFile = new FileAdapter(directory + newName);
		if (newFile != null && newFile.exists()) {
			Date lastModified = fo.getModified();
			Log.debug(TAG_LOG, "lastModified , " + lastModified.toString());
			if (newFile.isSetLastModifiedSupported() && lastModified != null) {
				Log.debug(TAG_LOG,
						"lastModified.getTime() " + lastModified.getTime());
				if (newFile.setLastModified(lastModified.getTime())) {
					Log.trace(TAG_LOG, "set LastModified ok");
//					EbenFileLog.recordSyncLog("down: cloud --> " + directory + newName+", set lastModified local : "+lastModified.toLocaleString());
				} else {
					Log.error(TAG_LOG, "failed to set last modified");
				}
				if (!AndroidConfiguration.authSyncInter) {
					if (bMk && bFolder) {
						FileAdapter parent = newFile.getParentFile();
						Log.debug(TAG_LOG, "parent folder " + parent.getName());
						if(null != parent)
						parent.setLastModified(lastModified.getTime());
					}
				}
			}
			newFile.close();
		} else {
			Log.error(TAG_LOG, "error!! , not exist, maybe same file in local ,but error in process");
		}
		
	}

	private FileAdapter applyFileData(FileObject fo, FileAdapter file,
			String newName, FileSyncItem fsi) throws IOException {
		// TODO Auto-generated method stub

		Log.info(TAG_LOG, "to stream download , size : " + fo.getSize());
		{
			InputStream in = null;
			final int Length = 1024;

			in = file.openInputStream();
			int available = in.available();
			Log.info(TAG_LOG, "available = " + available);
			if (available < Length && available > 0) {
//				ExSyncManager.i().notifyItemResult(getAppSource(), newName,
//						SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_START,
//						fsi.getState());
				
				EdiskTracker eTracker = (EdiskTracker) tracker;
				// lierbao temp
				//eTracker.updateSyncDb(EbenHelpers.encodeKey(newName), CHUNK_SUCCESS_STATUS);

				byte[] tempbytes = new byte[Length];
				int hasRead = in.read(tempbytes);
				// Log.info(TAG_LOG,"url : "+tempbytes);
				String uri = new String(tempbytes, 0, hasRead);
				Log.info(TAG_LOG, "data: " + uri);
				Log.info(TAG_LOG, "file size : " + getFileSize(uri));
				if (getFileSize(uri) > 0) {
					String url = getFileDurl(uri);
					String eTag = getFileEtag(uri);
					Log.info(TAG_LOG, "luid is : " + url + ", eTag: " + eTag);
					if (null != url && null != eTag) {
						String syncedLuid = EdiskdbStore.checkItemExist(
								mContext, getName(), eTag);
						String fileName = null;
						if (null != syncedLuid) {
							String syncedFile = EbenHelpers
									.decodeKey(syncedLuid);
							Log.info(TAG_LOG,
									"this file exist ,just copy file not down: "
											+ syncedFile);
							File localFile = new File(directory
									+ syncedFile);
							if(localFile.exists()) {
								String localTag = MD5Util.md5(localFile);
								Log.info(TAG_LOG, "localTag : " + localTag);
								if (!eTag.equalsIgnoreCase(localTag)) {
									Log.error(TAG_LOG, "eTag not match");
									// throw new IOException("eTag not match");
								} else {
									if(syncedFile.equalsIgnoreCase(newName)) {
										Log.info(TAG_LOG, "the same file has exist , do nothing");
//										file.close();
//										file = new FileAdapter(localFile);
										return null;
//										return file;
									}
									// just copy file not down
									if (copyFile(directory + syncedFile,
											tempDirectory + syncedFile)) {
										if (!MD5Util
												.md5(new File(tempDirectory
														+ syncedFile))
												.equalsIgnoreCase(eTag)) {
											throw new IOException(
													"copy error !!, eTag not match");
										}
										Log.info(TAG_LOG, "etag check ok, copy ok");
										fileName = syncedFile;
									}
								}
							}
						}
						if (null == fileName) {
							if (AndroidConfiguration.authSyncInter) {
								EbenDownFileAuth df = new EbenDownFileAuth(url);
								try {
									df.handler();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Hashtable<String, String> list = df
										.getDownList();
								Iterator iter = list.entrySet().iterator();
								while (iter.hasNext()) {
									Map.Entry entry = (Map.Entry) iter.next();
									String key = (String) entry.getKey();
									if (key.equalsIgnoreCase(url)) {
										url = (String) entry.getValue();
									}

								}

							}
							fileName = downloadFile(url, 0, getFileSize(uri),
									0, null);
							if (null != fileName) {
								String localTag = MD5Util.md5(new File(
										tempDirectory + fileName));
								Log.info(TAG_LOG, "localTag : " + localTag);
								// if (!eTag.equals(localTag)) {
								if (!eTag.equalsIgnoreCase(localTag)) {
									Log.error(TAG_LOG, "eTag not match");
									throw new IOException("eTag not match");
								} else {
									Log.info(TAG_LOG, "etag check ok");
								}
							}
						}
						Log.info(TAG_LOG, "down file is : " + fileName);
						if (null != fileName) {

							Log.info(TAG_LOG, "reset file");
							file.close();
							file = new FileAdapter(tempDirectory + fileName);
							fsi.setFileName(fileName);
						} else {
							throw new IOException("down load file failed");
						}
					}
				} else {
					if (!newName.endsWith(File.separator)) {// a folder
															// ,directory
						final String temp =  String.valueOf(System.currentTimeMillis())+"nullFile.temp00";//
						File tempFile = new File(tempDirectory + temp);
						if (!tempFile.exists()) {
							if (tempFile.createNewFile()) {
								Log.info(TAG_LOG, "null file");
								file.close();
								file = new FileAdapter(tempDirectory + temp);
								fsi.setFileName(temp);
							}
						}
					}
				}
			} else {
				Log.error(TAG_LOG, "stream url is too lenth");
				throw new IOException("not found file ");
			}
		}
		return file;

	}

	@Override
	protected Enumeration getAllItemsKeys() throws SyncException {
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "getAllItemsKeys");
		}
		// Scan the briefcase directory and return all keys
		// try {
		// FileAdapter dir = new FileAdapter(directory);
		// Enumeration files = dir.list(true);
		// dir.close();
		// // We use the full file name as key, so we need to scan all the
		// // items and prepend the directory
		// Vector keys = new Vector();
		// while (files.hasMoreElements()) {
		// String file = (String) files.nextElement();
		// keys.addElement(file);
		// }
		// return keys.elements();
		// } catch (Exception e) {
		// Log.error(TAG_LOG, "Cannot get list of files", e);
		// throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
		// }
		Vector keys = new Vector();
		try {
			getAll(new File(directory), 0, keys);
		} catch (Exception e) {
			Log.error(TAG_LOG, "Cannot get list of files", e);
			throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
		}
		allKeyCount = keys.size();
		return keys.elements();
	}

	public void getAll(File dir, int level, Vector resultVector) {
//		if(ExternalEntryConst.NETWORK_OK != EdiskSyncSource.netstate) {
//			throw new SyncException(EdiskSyncSource.netstate,"network not available");
//		}
		
		level++;
		File[] files = dir.listFiles(filter);
		for (int x = 0; x < files.length; x++) {
			String name = dir.getAbsolutePath() + "/" + files[x].getName();
			name = name.substring(directory.length());

			Log.debug(TAG_LOG, "name : " + name);
			if(files[x].getName().startsWith(WAVELINE)) {
				Log.info(TAG_LOG, "ignore file : "+files[x].getName());
				continue;
			}
			if (files[x].isDirectory()) {
				// if (isInFilter(files[x].getAbsolutePath())) {
				// Log.info(TAG_LOG,
				// "in filter ignore : " + files[x].getAbsolutePath());
				// continue;
				// }
				if (AndroidConfiguration.authSyncInter) {
					// String a = File.separator;
					String dirItem = name + File.separator;
					Log.debug(TAG_LOG, "dir Item : " + dirItem);
					resultVector.addElement(EbenHelpers.encodeKey(dirItem));
					// throw new Exception("");
				} else {
					createNullFile(files[x].toString());
				}
				getAll(files[x], level, resultVector);
			} else {

				// Log.info(TAG_LOG,
				// "dir.getAbsolutePath() is "+dir.getAbsolutePath()+", name is "+files[x].getName());
				// resultVector.addElement(name.replace(directory, ""));
				if (files[x].getName().contains(".aux")) {
					continue;
				}
				if (AndroidConfiguration.authSyncInter) {
					if (files[x].getName().contains(TEMPFILE)) {
						Log.error(TAG_LOG, "scan files ,find .sync~ file, remove it");
						deleteItem(EbenHelpers.encodeKey(name));
						continue;
					}					
				}
				// if (0 == MAX_FILE_SIZE || files[x].length() < MAX_FILE_SIZE)
				// {
				if (0 == MAX_FILE_SIZE || EbenHelpers.getFileSizes(files[x]) < MAX_FILE_SIZE) {
					// resultVector.addElement(name);
					String encodeName = EbenHelpers.encodeKey(name);// new
					// String(Base64.encode(name.getBytes()));
					Log.debug(TAG_LOG, "encodeName : " + encodeName);
					resultVector.addElement(encodeName);// key to base64
				} else {
					Log.info(TAG_LOG,
							"dir.getAbsolutePath() is " + dir.getAbsolutePath()
									+ ", name is " + files[x].getName());
//					ExSyncManager.i().toastInfo(mContext.getString(R.string.mindcloud_large_file));
					// ExternalEntryService.i().prompDialog("");
					Log.error(TAG_LOG, "file is too large, ignore it ");
				}
			}
		}
	}



	/**
	 * Add an item to the local store. The item has already been received and
	 * the content written into the output stream. The purpose of this method is
	 * to simply apply the file object meta data properties to the file used to
	 * store the output stream. In particular we set the proper name and
	 * modification timestamp.
	 * 
	 * @param item
	 *            the received item
	 * @throws SyncException
	 *             if an error occurs while applying the file attributes
	 * 
	 */
	@Override
	public int addItem(SyncItem item) throws SyncException {
		String key = item.getKey();
		Log.debug(TAG_LOG, "addItem key is " + key);

		if (Log.isLoggable(Log.DEBUG)) {
			Log.debug(TAG_LOG, "addItem " + key);
		}
		int ret;

		// Update the item's key
		// item.setKey(directory + key);//lierbao no used here

		// The stream has already been written, but we may need to rename the
		// underlying file, according to the FileObject metadata
		if (item instanceof FileSyncItem) {
			// The RawFileSyncItem has a name and other properties
			// we shall apply them here
			FileSyncItem fsi = (FileSyncItem) item;
			try {
//				applyFileProperties(fsi);
				if(applyFileInfo(fsi)) {
					ret = SyncSource.SUCCESS_STATUS;
				} else {
					ret = SyncSource.ERROR_STATUS;
				}
			} catch(SyncException e) {
				throw e;
			}
			catch (Exception e) {
				Log.error(TAG_LOG, "Failed at applying file object properties",
						e);
				ret = SyncSource.ERROR_STATUS;

				//lierbao check memory full ,toast info
				if(DiskMemory.freeMemory(directory) <= MAX_FILE_SIZE) {
//					ExSyncManager.i().toastInfo(mContext.getString(R.string.mindcloud_localmemory_full));
					throw new SyncException(SyncException.SD_CARD_UNAVAILABLE, "disk memory full ");
				}
				return ret; // return ,cant not add item to status db. for file
				// parse error
			}
		} else {
			ret = SyncSource.ERROR_STATUS;
		}

		// Invoke the super method to update the tracker. We don't want this new
		// item to be considered as a change to send to the server regardless of
		// the status of this operation. This method must be invoked after the
		// LUID has been defined
//		if(SyncSource.SUCCESS_STATUS == ret ) {
//			try {
//				// super ,add item not read filename , so set filename to key
//				SyncItem newItem = new FileSyncItem(item.getKey(), item.getKey(),
//						item.getType(), item.getState(), item.getParent());
//				super.addItem(newItem);
//			} catch (IOException ioe) {
//				Log.error(TAG_LOG,
//						"Cannot create new sync item after file renaming ", ioe);
//			}
//		}

		return ret;
	}

	/**
	 * Update an item in the local store. The item has already been received and
	 * the content written into the output stream. The purpose of this method is
	 * to simply apply the file object meta data properties to the file used to
	 * store the output stream. In particular we set the proper name and
	 * modification timestamp.
	 * 
	 * @param item
	 *            the received item
	 * @throws SyncException
	 *             if an error occurs while applying the file attributes
	 * 
	 */
	@Override
	public int updateItem(SyncItem item) throws SyncException {
		Log.trace(TAG_LOG, "updateItem");
		String key = item.getKey();

		if (Log.isLoggable(Log.DEBUG)) {
			Log.debug(TAG_LOG, "updateItem " + key);
		}

		int ret;
		FileSyncItem fsi;

		// The stream has already been written, but we may need to rename the
		// underlying file, according to the FileObject metadata
		if (item instanceof FileSyncItem) {
			fsi = (FileSyncItem) item;
			try {
//				applyFileProperties(fsi);
				if(applyFileInfo(fsi)) {
					ret = SyncSource.SUCCESS_STATUS;
				} else {
					ret = SyncSource.ERROR_STATUS;
				}
			} catch (Exception e) {
				Log.error(TAG_LOG, "updateItem Failed at applying file object properties",
						e);
//				ExSyncManager.i().notifyItemResult(getAppSource(),
//						EbenHelpers.decodeKey(key), SyncSource.STATUS_RECV_ERROR,
//						ExSyncManager.SYNC_END, item.getState());				
				ret = SyncSource.ERROR_STATUS;
			}
		} else {
			fsi = null;
			ret = SyncSource.ERROR_STATUS;
		}

		// Invoke the super method to update the tracker. We don't want this new
		// item to be considered as a change to send to the server regardless of
		// the status of this operation
//		if (SyncSource.SUCCESS_STATUS == ret) {
//			super.updateItem(item);
//		}
		return ret;
	}

	/**
	 * Delete an item from the local store.
	 * 
	 * @param key
	 *            the item key
	 * @throws SyncException
	 *             if the operation fails for any reason
	 */
	@Override
	public int deleteItem(String key) throws SyncException {
		if (Log.isLoggable(Log.DEBUG)) {
			Log.debug(TAG_LOG, "deleteItem " + key);
		}

//		String decodeKey = EbenHelpers.decodeKey(key);// new
		// String(Base64.decode(key.getBytes()));
		EdiskTracker eTracker = (EdiskTracker) tracker;
		
		String pathname =  eTracker.getkeypath(key);
		String fileName = directory + pathname;
		
		
		int ret = SyncSource.SUCCESS_STATUS;
		if(null != pathname) {
			Log.debug(TAG_LOG, "fileName : "+fileName);
			try {
				// remove download table
				String sourceName = getName();
				// ContentResolver cr = App.i().getContentResolver();
				//
				// Uri uri = Uri.withAppendedPath(
				// Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				// SyncSourceProvider.PRIFEX_DOWN+sourceName);
				eTracker.removeAlldownchildern(key, pathname);
				// int colum = cr.delete(uri, SyncSourceProvider.LUID+"=?", new
				// String[]{key});
				// Log.info(TAG_LOG,
				// "del "+sourceName+", colum: "+colum+",key: "+key);
				// remove download table end
				// ret = SyncSource.SUCCESS_STATUS;
				FileAdapter file = new FileAdapter(fileName);
				if (!file.exists()) {
					Log.info(TAG_LOG, "file has removed ,ok ");
				} else {
					// if (!file.isDirectory()) {
					// if (file.delete()) {
					String temp = String.valueOf(System.currentTimeMillis());// +fileName.substring(fileName.lastIndexOf(File.separator)+1);
					if (file.rename(tempDirectory + temp)) {
						Log.debug(TAG_LOG, "rename as delete ok !!" + fileName);
//						EbenFileLog.recordSyncLog("server del: " + fileName
//								+ ", we rename it to " + tempDirectory + temp
//								+ " ,del later");
					} else if (file.delete()) {
						Log.debug(TAG_LOG, "delete ok !!" + fileName);
//						EbenFileLog.recordSyncLog("server del: " + fileName);
					} else {
						Log.error(TAG_LOG, fileName + ", delete failed");
						ret = SyncSource.ERROR_STATUS;
					}
					// }
				}
				file.close();

			} catch (IOException ioe) {
				Log.error(TAG_LOG,
						"deleteItem, cannot delete item " + fileName, ioe);
				ret = SyncSource.ERROR_STATUS;
			}

			// Invoke the super method to update the tracker. We don't want this
			// new
			// item to be considered as a change to send to the server
			// regardless of
			// the status of this operation
			if (SyncSource.SUCCESS_STATUS == ret) {
//				ExSyncManager.i().notifyItemResult(appSource, pathname,
//						SyncSource.SUCCESS_STATUS, ExSyncManager.SYNC_END, 'D');

				// EdiskSyncSource.markDelItem(mContext,
				// appSource.getSyncSource().getName(),
				// pathname);
				// super.deleteItem(key);
//				EdiskLuidInfo(String luid, String parent, String path, String fp)
				EdiskLuidInfo iteminfo = new EdiskLuidInfo(key,null,pathname,null);
				EdiskLuidInfo.isLuidFound(mContext, getName(), key, iteminfo);
//				iteminfo.setLuid(key);
//				iteminfo.setPath(pathname);
				eTracker.removeItem(iteminfo, 'D');
			}
		} else {
			Log.debug(TAG_LOG, "has removed ,do nothing");
		}

		return ret;
	}

	@Override
	protected SyncItem getItemContent(final SyncItem item) throws SyncException {
		SourceConfig config = getConfig();
		String type = config.getType();
		// We send the item with the type of the SS
		String key = item.getKey();
		Log.trace(TAG_LOG, "key is :" + key);
		String fileName = EbenHelpers.decodeKey(key);// new
		// String(Base64.decode(key));
		Log.debug("getItemContent filename:" + fileName);
		try {
			if (fileName != null && fileName.contains("/")
					&& fileName.startsWith(directory)) {
				// item.setKey(fileName.replace(directory, ""));
				String name = fileName.substring(directory.length());
				String encodeName = EbenHelpers.encodeKey(name);// new
				// String(Base64.encode(name.getBytes()));
				Log.trace(TAG_LOG, "encode Name is " + encodeName);
				item.setKey(encodeName);

			} else {
				// item.setKey(fileName);
				String encodeName = EbenHelpers.encodeKey(fileName);// new
				// String(Base64.encode(fileName.getBytes()));
				Log.trace(TAG_LOG, "encode Name is " + encodeName);
				item.setKey(encodeName);
				fileName = directory + fileName;
			}
			// fileName = getTempFileName(fileName);
			// create a temp file to sync
//			if (!bMakeingItems) {
				String tempFile = fileName.replace(directory, tempDirectory);
				if (tempFile.contains("/")) {
					String folder = tempFile.substring(0,
							tempFile.lastIndexOf("/"));
					File fileFolder = new File(folder);
					fileFolder.mkdirs();
					fileFolder = null;
				}
				if (!AndroidConfiguration.authSyncInter) {
					if (copyFile(new File(fileName), new File(tempFile))) {
						fileName = getTempFileName(fileName);
					} else {
						throw new SyncException(SyncException.CLIENT_ERROR,
								"copy file failed");
					}
				}
				{
					String deCode = EbenHelpers.decodeKey(key);
					Log.debug(TAG_LOG, "deCode , " + deCode);
					EdiskTracker eTracker = (EdiskTracker) tracker;
//					if(!AndroidConfiguration.authSyncInter||fileName.endsWith(File.separator))
//					eTracker.updateSyncDb(key, CHUNK_SUCCESS_STATUS); // for up syncing status
					// syncing

					if (deCode.startsWith(directory)) {
						deCode = deCode.substring(directory.length());
					}
					if (!AndroidConfiguration.authSyncInter||(fileName.endsWith(File.separator) 
							&& !(item.getState()== SyncItem.STATE_UPDATED))) {
						Log.info(TAG_LOG, "state ,"+item.getState()+",compared :"+SyncItem.STATE_UPDATED);
//						ExSyncManager.i().notifyItemAction(appSource, deCode,
//								-1, ExSyncManager.SYNC_START);
					}
				}
//			}
			// create temp file end
			// item.setKey(new String(fileName.replace(directory,
			// "").getBytes(),"UTF-8"));
			FileSyncItem fsi = new FileSyncItem(fileName, item.getKey(), type,
					item.getState(), item.getParent());
			return fsi;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new SyncException(SyncException.CLIENT_ERROR,
					"Cannot create RawFileSyncItem: " + ioe.toString());
		}
	}

	public void setSupportedExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	/**
	 * Return whether a given filename is filtered by the SyncSource.
	 * 
	 * @param filename
	 * @return true if the given filename is actually filtered by the
	 *         SyncSource.
	 */
	public boolean filterFile(String name) {
		if (extensions == null || extensions.length == 0) {
			return true;
		}
		name = name.toLowerCase();
		for (int i = 0; i < extensions.length; ++i) {
			String ext = extensions[i].toLowerCase();
			if (name.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void deleteAllItems() {
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "removeAllItems");
		}
		// Scan the briefcase directory and return all keys
		try {
			FileAdapter dir = new FileAdapter(directory);
			Enumeration files = dir.list(false);
			dir.close();
			// We use the full file name as key, so we need to scan all the
			// items and prepend the directory
			while (files.hasMoreElements()) {
				String fileName = (String) files.nextElement();
				FileAdapter file = new FileAdapter(directory + fileName);
				file.delete();
				file.close();
			}

			// at the end, empty the tracker
			tracker.reset();
		} catch (Exception e) {
			throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
		}
	}

	@Override
	public void endSync() throws SyncException {
		super.endSync();
		removeTempFiles();
		beat = false;
		// StringKeyValueSQLiteStore trackerStore =
		// new StringKeyValueSQLiteStore(mContext,
		// ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
		// "edisk");

		/*
		 * Enumeration allluid = mTracker.getStatus().keys();
		 * if(allluid.hasMoreElements()){ StringBuffer sb = new StringBuffer();
		 * boolean first = true; while (allluid.hasMoreElements()) { if(!first)
		 * sb.append(","); first = false;
		 * sb.append((String)allluid.nextElement()); }
		 * NetworkUtilities.loadEdiskGuid(sb.toString(), mContext); }
		 */

	}

	// /////////////////////////////////////////////////////////////////////
	// added by jason. implements TwinDetectionSource, ResumableSource
	public void setTempDirectory(String directory) {
		tempDirectory = directory;
	}

	protected String createTempFileName(String name) throws IOException {
		try {
			StringBuffer res = new StringBuffer(tempDirectory);
			if (!tempDirectory.endsWith("/")) {
				res.append("/");
			}
			res.append(name).append(".part__");
			return res.toString();
		} catch (Exception e) {
			Log.error(TAG_LOG, "Cannot create temp file name", e);
			throw new IOException("Cannot create temp file");
		}
	}

	protected String getFileNameFromKey(String key) {
		String fileName = key.substring(key.lastIndexOf('/') + 1);
		return fileName;
	}

	public String getFileFullName(String name) {
		StringBuffer fullname = new StringBuffer();
		fullname.append(directory);
		if (!directory.endsWith("/")) {
			fullname.append("/");
		}
		fullname.append(name);
		return fullname.toString();
	}

	@Override
	public boolean readyToResume() {

		return true;
	}

	@Override
	public boolean exists(String key) throws SyncException {
		FileAdapter fa = null;
		try {
			fa = new FileAdapter(directory + key);
			return fa.exists();
		} catch (Throwable t) {
			return false;
		} finally {
			if (fa != null) {
				try {
					fa.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	@Override
	public boolean hasChangedSinceLastSync(String key, long lastSyncStartTime) {

		return false;
	}

	@Override
	/**
	 * Twin detection implementation
	 * 
	 * @param item
	 * @return the twin sync item, whose key is the LUID
	 */
	// lierbao this not called .so not debug key to base64
	public SyncItem findTwin(SyncItem item) {

		String fileName = getFileNameFromKey(item.getKey());
		String fullName = getFileFullName(fileName);

		// Does this existing in our directory?
		if (Log.isLoggable(Log.INFO)) {
			Log.info(TAG_LOG, "Checking for twin for: " + fileName);
		}
		FileAdapter fa = null;
		try {
			fa = new FileAdapter(fullName);
			if (fa.exists() /* && fa.getSize() == item.getSize() */) {// TODO:size
				// is
				// not
				// corrent.
				if (Log.isLoggable(Log.DEBUG)) {
					Log.debug(TAG_LOG, "Twin found");
				}
				item.setKey(fullName);
				return item;
			}
		} catch (Throwable t) {
			Log.error(TAG_LOG, "Cannot check for twins", t);
		} finally {
			if (fa != null) {
				try {
					fa.close();
				} catch (IOException ioe) {
				}
			}
		}
		// No twin found
		return null;
	}

	public long getPartiallyReceivedItemSize(String luid) {

		FileAdapter fa = null;
		try {
			String tempFileName = createTempFileName(getFileNameFromKey(luid));
			fa = new FileAdapter(tempFileName);
			if (!fa.exists()) {
				return -1;
			}
			return fa.getSize();
		} catch (Exception e) {
			return -1;
		} finally {
			if (fa != null) {
				try {
					fa.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public String getLuid(SyncItem item) {

		String fileName = item.getKey();

		String localFullName = getFileFullName(fileName);
		return localFullName;
	}

	/**

	 */
	static public boolean copyFile(String src, final String target) {
		if(target.contains(File.separator)) {
			String folder = target.substring(0, target.lastIndexOf(File.separator));// tt/yy
	
			File fileFolder = new File(folder);
			fileFolder.mkdirs();
			if (!fileFolder.exists()) {
				Log.info(TAG_LOG, "make temp folder failed ");
			}
			fileFolder = null;
		}
		File srcFile = new File(src);
		File targetFile = new File(target);

		return copyFile(srcFile, targetFile);
	}
/**
 * 
 * @param srcFile
 * @param targetFile
 * @return
 */
	static public boolean copyFile(File srcFile, final File targetFile) {

		boolean isSuccess = false;
		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;
		String srcMd5 = MD5Util.md5(srcFile);
		Log.info(TAG_LOG, "srcFile , " + srcFile.getAbsolutePath()
				+ ", targetFile ," + targetFile.getAbsolutePath());
//		EbenFileLog.recordSyncLog("copy file: " + "srcFile , "
//				+ srcFile.getAbsolutePath() + ", targetFile ,"
//				+ targetFile.getAbsolutePath());
		targetFile.delete();
		try {
			if (srcFile.canRead()) {
				bin = new BufferedInputStream(new FileInputStream(srcFile));
				bout = new BufferedOutputStream(
						new FileOutputStream(targetFile));
				int i = 0;
				byte[] data = new byte[1024];
				while ((i = bin.read(data)) != -1) {
					// if(operatorFlag){
					// //bout.flush();
					// bout.close();
					// bout = null;
					// targetFile.delete();
					// return false;
					// }
					bout.write(data, 0, i);
				}
				bout.flush();
				
				if(MD5Util.md5(srcFile).equalsIgnoreCase(MD5Util.md5(targetFile))) {
					targetFile.setLastModified(srcFile.lastModified());
					isSuccess = true;
				} else {
					isSuccess = false;
					targetFile.delete();
					Log.info(TAG_LOG,"copy md5 check error ");
//					EbenFileLog.recordSyncLog("copy md5 check error ");
				}
			} else {
				Log.error(TAG_LOG, "not allowed to read from this file ,"
						+ srcFile.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bin) {
					bin.close();
				}
				if (null != bout) {
					bout.close();
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return isSuccess;
	}

	// static final String TEMP_DIR = "ebenTemp";
	private String getTempFileName(String fileName) {
		// TODO Auto-generated method stub
		// String sdCardRoot =
		// Environment.getExternalStorageDirectory().toString();
		// String tempDir = sdCardRoot+"/"+TEMP_DIR+"/";
		// Create the default folder if it doesn't exist
		String tempFileName = fileName.replace(directory, tempDirectory);
		File tempFile = new File(tempFileName);

		if (tempFile.exists()) {
			tempFile = null;
			Log.info(TAG_LOG, "use temp file," + tempFileName);
			return tempFileName;
		}
		Log.info(TAG_LOG, "use original file," + fileName);
		return fileName;
	}

	/**
	 * 
	 */
	private void createTempDir() {
		// TODO Auto-generated method stub
		File dir = new File(tempDirectory);
		if (!dir.exists()) {
			Log.info(TAG_LOG, tempDirectory + "  is empty ,make it");
			dir.mkdirs();
		}
		if (!dir.exists()) {
			Log.info(TAG_LOG, tempDirectory + "  make failed");
		}
		if (!dir.canWrite()) {
			Log.info(TAG_LOG, tempDirectory + "  can't write");
		}
	}
/**
 * 
 */
	private void removeTempFiles() {

		File dir = new File(tempDirectory);
		deleteFiles(dir);

	}
/**
 * 
 * @param file
 */
	public void deleteFiles(File file) {
		// TODO Auto-generated method stub
		if (file.exists()) { // is exist
			if (file.isFile()) {
				Log.debug(TAG_LOG, "remove file ," + file.toString());// is file
				if(file.delete()) { // delete()
//					EbenFileLog.recordSyncLog("delete: "+file.toString());
				}
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteFiles(files[i]);
				}

				String folder = file.toString() + "/";
				if (!(folder.equalsIgnoreCase(tempDirectory))) {
					Log.debug(TAG_LOG, "remove folder ," + file.toString());
					if(file.delete()) {// delete directory 
//						EbenFileLog.recordSyncLog("delete folder: "+file.toString());
					}
				}
			}

		} else {
			Log.debug(TAG_LOG, "file dir not exist ");
		}
	}

	private boolean createNullFile(String dir) {
		boolean ret = false;
		String fileName = dir + "/" + TEMPFILE;

		File tempFile = new File(fileName);
		if (!tempFile.exists()) {
			File parent = new File(dir);
			long lastModifid = parent.lastModified();
			Log.debug(TAG_LOG, "create a file " + fileName);
			try {
				if (tempFile.createNewFile()) {

					Log.debug(TAG_LOG, "lastModifid is " + lastModifid
							+ ", parent " + parent.getName());
					if (0 != lastModifid) {
						if (tempFile.setLastModified(lastModifid)
								&& parent.setLastModified(lastModifid)) {
							Log.debug(TAG_LOG, "set last modified ok");
						} else {
							Log.debug(TAG_LOG, "failed to set last modified");
						}
					}
					// if(tempFile.setReadOnly()) {
					// Log.debug(TAG_LOG, "set readonly ok");
					// }
					// else {
					// Log.debug(TAG_LOG, "failed to set read only");
					// }
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ret;
	}

	// private void checkTempFile(File dir) {
	// // TODO Auto-generated method stub
	// File[] files = dir.listFiles();
	// String tempName = dir + "/"+TEMPFILE;
	//
	// if(files.length <= 0) {//null folder
	// createNullFile(dir.toString());
	// }
	// else if(files.length > 1){
	// for(int i=0;i<files.length;i++) {
	// Log.debug(TAG_LOG, "file ,"+files[i].toString());
	// Log.debug(TAG_LOG, "tempFile"+tempName);
	// if(files[i].isFile()&&files[i].toString().equalsIgnoreCase(tempName)) {
	// deleteFiles(files[i]);
	// break;
	// }
	// }
	// }
	// }

	// public void addDirectoryFilter(String dir) {
	// if (null == filterList)
	// filterList = new ArrayList<String>();
	// if (!filterList.contains(dir))
	// filterList.add(dir);
	// }

	// private boolean isInFilter(String dir) {
	// Log.info(TAG_LOG, "isInFilter : " + dir);
	// if (null == filterList)
	// return false;
	// else {
	// return filterList.contains(dir);
	// }
	// }

	private String downloadFile(String url, long hasRecv, Long size, int count,
			String fileName) {
		// TODO Auto-generated method stub
		// String fileName = url.substring(url.lastIndexOf("/"), url.length());
//    	if(ExternalEntryConst.NETWORK_OK != EbenHelpers.isNetworkAvailable()){
//    		Log.error(TAG_LOG, "network not available ");
//    		return null;
//    	}
//    	if(ConnectivityManager.TYPE_MOBILE == EbenHelpers.getNetworkType()) {
//    		ExSyncManager.i().toast3GWarning();
//    	}
		beat = true;
		heartBeat();
		Log.info(TAG_LOG, "url : " + url + ", count : " + count);
		if (null == fileName)
			fileName = String.valueOf(System.currentTimeMillis());

		String fileDir = tempDirectory;// Environment.getExternalStorageDirectory().getAbsolutePath();
		File downFile = new File(fileDir + fileName);

		if (count > 30) {
			deleteFiles(downFile);

			return null;
		}
		count++;

		long sum = hasRecv;
		RandomAccessFile oSavedFile = null;
		InputStream is = null;
		try {
			URL u = new URL(url);
			HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
			ucon.setRequestProperty("Connection", "keep-alive");
			ucon.setConnectTimeout(3 * 1000);
			ucon.setReadTimeout(3 * 1000);
			if (hasRecv > 0) {
				Log.info(TAG_LOG, "bytes=" + hasRecv + "-");
				ucon.setRequestProperty("RANGE", "bytes=" + hasRecv + "-");
			}

			is = ucon.getInputStream();

			oSavedFile = new RandomAccessFile(downFile, "rw");
			oSavedFile.seek(hasRecv);

			// OutputStream os = new BufferedOutputStream(oSavedFile);
			// OutputStream os = new BufferedOutputStream(new
			// FileOutputStream(downFile));

			int len = ucon.getContentLength();
			Log.info(TAG_LOG, fileName + " lenth: " + len);
			int hasRead = 0;

			byte[] buff = new byte[1024];
			while ((hasRead = is.read(buff)) > 0) {
				// os.write(buff, 0, hasRead);
				oSavedFile.write(buff, 0, hasRead);
				sum += hasRead;
				// Log.info(TAG_LOG, "has read : "+hasRead);
			}
			Log.info(TAG_LOG, "recv file size: " + sum);

			// os.close();
			// oSavedFile.close();
			// is.close();
			// if (sum != size) {
			// Log.info(TAG_LOG, "size not matched ,return null");
			// deleteFiles(downFile);
			// return null;
			// }
			return fileName;

		} catch (SocketTimeoutException e) {
			// TODO: handle exception
			Log.error(TAG_LOG, "e: " + e.toString() + ", has read: " + sum);

//			e.printStackTrace();
			// if(null != oSavedFile)
			if (sum > hasRecv) {
				return downloadFile(url, sum, size, 0, fileName);
			} else {
				return downloadFile(url, sum, size, count, fileName);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.error(TAG_LOG, "e: " + e.toString() + ", has read: " + sum);
			e.printStackTrace();
			deleteFiles(downFile);

			return null;
//			e.printStackTrace();
//			return downloadFile(url, sum, size, count, fileName);

		} finally {
			try {

				if (null != oSavedFile)
					oSavedFile.close();
				if (null != is)
					is.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return null;
	}

	private boolean beat = false;
	private boolean beatStart = false;
	private String jSession = null;

	public void setSourceInfo(Object info) {
		jSession = (String) info;
	}

	private void heartBeat() {
		Log.info(TAG_LOG, "heartBeat, beatStart " + beatStart + ", beat : "
				+ beat);
		if (!beatStart) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (beat) {

						try {
							URL url = new URL(jSession + "?active");
							Log.info(TAG_LOG, "url : " + jSession + "?active");
							try {
								HttpURLConnection connection = (HttpURLConnection) url
										.openConnection();
								connection.connect();
								Log.info(
										TAG_LOG,
										"response : "
												+ connection.getResponseCode());
								// connection.getResponseCode();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							Thread.sleep(5 * 60 * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					beatStart = false;
				}
			}).start();
			beatStart = true;
		}
	}

	private String getFileDurl(String json) {
		// TODO Auto-generated method stub
		try {
			JSONObject jo = new JSONObject(json);
			return jo.getString("DURL");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String getFileEtag(String json) {
		// TODO Auto-generated method stub
		try {
			JSONObject jo = new JSONObject(json);
			return jo.getString("ETAG");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private long getFileSize(String json) {
		// TODO Auto-generated method stub
		try {
			JSONObject jo = new JSONObject(json);
			return jo.getLong("SIZE");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private void add2DelList(String key) {
		// TODO Auto-generated method stub
		if (null == delList) {
			delList = new ArrayList();
		}
		Log.info(TAG_LOG, "add2DelList : " + key);
		if (!delList.contains(key)) {
			delList.add(key);
		}

		processRemovedFolder(new File(getDirectory()));
	}

	private boolean isFolderRemoved(String fileKey) {
		Log.info(TAG_LOG, "fileKey : " + fileKey);
		boolean bRet = false;
		if (null != delList) {
			for (String key : delList) {
				if (key.equalsIgnoreCase(fileKey)) {
					Log.info(TAG_LOG, "should removed this : " + fileKey);
					bRet = true;
					break;
				}
			}
		}
		return bRet;

	}

	public void processRemovedFolder(File file) {
//		EDiskHelper.removeOldFolders(directory, delList);
		// File file = new File(directory);
		// File flist[] = file.listFiles(filter);
		// Log.info(TAG_LOG, "processRemovedFolder");
		// for (File in : flist) {
		// Log.info(TAG_LOG, "in: "+in.toString());
		// if (in.isDirectory()) {
		// if(0==in.listFiles(filter).length) {
		// if (isFolderRemoved(in.toString().substring(directory.length())
		// + File.separator + TEMPFILE)) {
		// if(in.delete()) {
		// Log.info(TAG_LOG, "delete folder: "+in.toString());
		// }
		// }
		// } else {
		// processRemovedFolder(in);
		// }
		//
		// }
		// }

		// delList = null;
	}

	public void resetDelist() {
		delList = null;
	}
	/**
	 * is in sync prepare status
	 * @return
	 */
	static public boolean isSync() {
		return inPresync;
	}
	static public void setPresync(boolean isync) {
		inPresync = isync;
//		bMakeingItems = isync;
	}
	static private boolean inPresync = false;
	public static int upCount = 0;
	public static int curUpCount = 0;

	public void preSync(int syncMode, boolean resume) throws SyncException {
		// TODO Auto-generated method stub
		inPresync = true;
		netstate = EbenHelpers.isNetworkAvailable();
		// make slow sync
		if(EdiskLuidInfo.isLuidEmpty(mContext, getName())) {
			Log.error(TAG_LOG, "sync table empty should run slow sync");
			SyncMLAnchor anchor = (SyncMLAnchor) getConfig().getSyncAnchor();
			anchor.setLast(1);
		}
//		EbenFileLog.recordSyncLog("start pre sync, "+getName()+", sync mode : " +syncMode);
		String dir = getDirectory();
//		EDiskHelper.updateRenamedFils(dir, appSource);
		EdiskTracker eTracker = (EdiskTracker) tracker;
		if(syncMode == SyncSource.SELECTED_SYNC) {
			syncMode = SyncSource.INCREMENTAL_SYNC;
			Log.info(TAG_LOG, "fast sync");
			eTracker.setScanType(EdiskTracker.FASTSCAN);
		} else {
			eTracker.setScanType(EdiskTracker.ALLSCAN);
			Log.info(TAG_LOG, "all sync");
		}
		if (!EDiskHelper.isok) {
			// throw new
			// SyncException(SyncException.ERR_SYNC_SOURCE_DISABLED,"source disabled");
		}
		try {

//			bMakeingItems = true;

			beginSyncEdisk(syncMode, resume);
			uplist = new ArrayList();
			checkDelItems();
//lierbao ex nout used , do it at server
//			((EdiskTracker) tracker).mergeupdatedItems(exUpdateItems);
			// up list count
			curUpCount = 0;
			upCount = ((EdiskTracker) tracker).getAllUpCount();

			exUpdateItems = null;
			checkRenameList();
			setProgressInfo(syncMode);
			requestUpfiles();

			// the update add maybe changed ,for example :failed to upload
        	luidinfo=((EdiskTracker) tracker).getLuidinfo();
        	syncedluidinfo=((EdiskTracker) tracker).getSyncedluidinfo();// 
        	updateluidinfo = ((EdiskTracker) tracker).getUpdateluidinfo();//
        	newluidinfo = ((EdiskTracker) tracker).getNewluidinfo();//
        	deleteluidinfo = ((EdiskTracker) tracker).getDeleteluidinfo();//
        	
//			newItems = tracker.getNewItems();
//			updItems = tracker.getUpdatedItems();
//			delItems = tracker.getDeletedItems();

			// delItems = tracker.getDeletedItems();
			// Init number of changes counter
//			clientAddItemsNumber = tracker.getNewItemsCount();
//			clientReplaceItemsNumber = tracker.getUpdatedItemsCount();
//			clientDeleteItemsNumber = tracker.getDeletedItemsCount();
//
//			clientItemsNumber = clientAddItemsNumber + clientReplaceItemsNumber
//					+ clientDeleteItemsNumber;

//			bMakeingItems = false;
		} catch (SyncException syncEx) {
			inPresync = false;
			throw syncEx;
		} catch (Exception e) {
			e.printStackTrace();
			inPresync = false;
			// ExSyncManager.i().exitApp(e);
			throw new SyncException(netstate, e.getMessage());
		}
		inPresync = false;
	}
	class ParentLuidComparator implements Comparator<EdiskLuidInfo> {

		public int compare(EdiskLuidInfo name1, EdiskLuidInfo name2) {
		    // TODO Auto-generated method stub
		    String[] s1 = name1.getPath().split("/");
		    String[] s2 = name2.getPath().split("/");

//		    return (s2.length - s1.length);
		    return (s1.length - s2.length);
		}


	}
/**
 * 
 * @param syncMode
 * @param resume
 * @throws SyncException
 */
	public void beginSyncEdisk(int syncMode, boolean resume) throws SyncException {

        this.syncMode = syncMode;
//        cancel = false;
    	updateAnchor = 0;
    	newAnchor = 0;
    	deleteAnchor = 0;
        if (tracker == null) {
            throw new SyncException(SyncException.CLIENT_ERROR, "Trackable source without tracker");
        }

        // The tracker must be initialized before the source
        // as it may invoke the initXXXItems which depend on the tracker
        try {
            tracker.begin(syncMode, resume);
        } catch (TrackerException te) {
            Log.error(TAG_LOG, "Cannot track changes: " + te);
            throw new SyncException(SyncException.CLIENT_ERROR, te.toString());
        }

        allItems = null;
        newItems = null;
        updItems = null;
        delItems = null;

        // Init lists
        switch(syncMode) {
            case FULL_SYNC:
            case FULL_UPLOAD:
                // A refresh from client is like a slow here
//                allItems = getAllItemsKeys();
            	EdiskTracker etracker = (EdiskTracker) tracker;
            	etracker.getAllFilesFingerprint();
            	luidinfo=((EdiskTracker) tracker).getLuidinfo();
            	Collections.sort(luidinfo, new ParentLuidComparator());
//                allItems = applyFiltersForOutgoingItems(allItems);
                // We guarantee that the getAllItemsCount is invoked after the
                // getAllItemsKeys
                clientItemsNumber = getAllItemsCount();
                clientAddItemsNumber = 0;
                clientReplaceItemsNumber = 0;
                clientDeleteItemsNumber = 0;
                break;
            case INCREMENTAL_SYNC:
            case INCREMENTAL_UPLOAD:
                // A one way from client is like a fast here
            	luidinfo=((EdiskTracker) tracker).getLuidinfo();
            	syncedluidinfo=((EdiskTracker) tracker).getSyncedluidinfo();// 
            	updateluidinfo = ((EdiskTracker) tracker).getUpdateluidinfo();//
            	newluidinfo = ((EdiskTracker) tracker).getNewluidinfo();//
            	deleteluidinfo = ((EdiskTracker) tracker).getDeleteluidinfo();//
            	
//                newItems = tracker.getNewItems();
//                updItems = tracker.getUpdatedItems();
//                delItems = tracker.getDeletedItems();

//                // Init number of changes counter
//                clientAddItemsNumber = tracker.getNewItemsCount();
//                clientReplaceItemsNumber = tracker.getUpdatedItemsCount();
//                clientDeleteItemsNumber = tracker.getDeletedItemsCount();
//
//                clientItemsNumber = clientAddItemsNumber +
//                                    clientReplaceItemsNumber +
//                                    clientDeleteItemsNumber;
                break;
            case INCREMENTAL_DOWNLOAD:
                // No modifications to send (it's not
                // strictly necessary to reset the lists,
                // because the engine will not ask items to
                // the SyncSource, but it's good to do it)
                newItems = null;
                updItems = null;
                delItems = null;
                // Init number of changes counter
                clientItemsNumber = 0;
                clientAddItemsNumber = 0;
                clientReplaceItemsNumber = 0;
                clientDeleteItemsNumber = 0;
                break;
            case FULL_DOWNLOAD:
                // In this case, the SyncSource should
                // delete all the items in the database
                // (possibly asking the user before that)

                deleteAllItems();
                // No modifications to send.
                newItems = null;
                updItems = null;
                delItems = null;
                // Init number of changes counter
                clientItemsNumber = 0;
                clientAddItemsNumber = 0;
                clientReplaceItemsNumber = 0;
                clientDeleteItemsNumber = 0;
                break;
            case SELECTED_SYNC:
            	break;
            default:
                throw new SyncException(SyncException.SERVER_ERROR,
                                        "SyncSource "+getName()+
                                        ": invalid sync mode "+getSyncMode());
        }
    }
	boolean delCheck = false;
/**
 * this not used ,check del at tracker begin. 
 */
	private void checkDelItems() {
		// TODO Auto-generated method stub
		if (!AndroidConfiguration.authSyncInter) {
			return;
		}
		if (!delCheck) {
			return;
		}
		if(getName().equalsIgnoreCase("ephoto")) {
			Log.info(TAG_LOG,"ephoto no del protect");
			return;
		}
		if(null == deleteluidinfo || deleteluidinfo.isEmpty()){
			return;
		}
		Cursor cursor = null;
		ArrayList<String> marklist = new ArrayList<String>();
		try {
			ContentResolver cr = App.i().getApplicationContext()
					.getContentResolver();

			Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
					+ SyncSourceProvider.PRIFEX_DELETE + getName() + "/");
			String[] projection ={SyncSourceProvider.KEY_COLUMN_NAME};
			cursor = cr.query(uri, projection, null, null, null);
			if (null != cursor && cursor.getCount() > 0) {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					String key = cursor.getString(cursor
							.getColumnIndex(SyncSourceProvider.PRIFEX_DELETE));
					marklist.add(key);
				}
			}
			cursor.close();
			
			uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
					+ SyncSourceProvider.PRIFEX_RENAME + getName() + "/");
			
			String[] projection2 ={SyncSourceProvider.KEY_COLUMN_OLD};
			cursor = cr.query(uri, projection2, null, null, null);
			if (null != cursor && cursor.getCount() > 0) {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					String key = cursor.getString(cursor
							.getColumnIndex(SyncSourceProvider.KEY_COLUMN_OLD));
					marklist.add(key);
				}
			}
			cursor.close();			
			
			
			boolean isDeled = false;
			for(EdiskLuidInfo item : deleteluidinfo) {
				isDeled = false;
				for(String key:marklist) {
					if (key.endsWith(File.separator)) {
						if (item.getPath().startsWith(key)) {
							isDeled = true;
						}
					} else {
						if (key.equalsIgnoreCase(item.getPath())) {
							isDeled = true;
						}
					}
				}
				if (!isDeled) {
					
					if(isIlligalItem(item.getPath())) {
						Log.info(TAG_LOG,"ignore as delete");
//						EbenFileLog.recordSyncLog("error ,isIlligalItem : " + item);
					}  else {
//					EbenFileLog.recordSyncLog("error ,find a illegal delete file, resume deleted key : " + item);
					Log.error(TAG_LOG, "error ,resume deleted key : " + item);
//					add2update(item);
//					UpfileInfo info = new UpfileInfo(itemDel, "", 0, null);
////					uplist.add(info);
//					((EdiskTracker) tracker).removeDelItem(item);
					}
//					getConfig().setSyncAnchor(new SyncMLAnchor());
					
				}
			}

			
//			while (delItem.hasMoreElements()) {
//				String item = (String) delItem.nextElement();
//				isDeled = false;
//
////				String item = EbenHelpers.decodeKey(itemDel);
//
//				if (null != cursor && cursor.getCount() > 0) {
//					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
//							.moveToNext()) {
//						String key = cursor.getString(cursor
//								.getColumnIndex("_key"));
//						if (key.endsWith(File.separator)) {
//							if (item.startsWith(key)) {
//								isDeled = true;
//							}
//						} else {
//							if (key.equalsIgnoreCase(item)) {
//								isDeled = true;
//							}
//						}
//					}
//				}
//				if (!isDeled) {
//					
//					if(isIlligalItem(item)) {
//						Log.info(TAG_LOG,"ignore as delete");
//						EbenFileLog.recordSyncLog("error ,isIlligalItem : " + item);
//					}  else {
//					EbenFileLog.recordSyncLog("error ,find a illegal delete file, resume deleted key : " + item);
//					Log.error(TAG_LOG, "error ,resume deleted key : " + item);
//					add2update(itemDel);
//					UpfileInfo info = new UpfileInfo(itemDel, "", 0, null);
//					uplist.add(info);
//					((EdiskTracker) tracker).removeDelItem(itemDel);
//					}
////					getConfig().setSyncAnchor(new SyncMLAnchor());
//					
//				}
//
//			}
		} catch (Exception e) {
			e.printStackTrace();
			if (null != cursor)
				cursor.close();
//			ExSyncManager.i().exitApp(e);
		}
//		if (null != cursor)
//			cursor.close();
//		

	}
	
	public static boolean isIlligalItem(String item) {
//		return item.startsWith(WAVELINE)||item.startsWith(AndroidCustomization.TEMP_MYDOC)||item.contains("./")
//				||item.startsWith("theme")||item.contains("LOST.DIR")||item.contains(File.separator+WAVELINE);
		return false;
	}
	public boolean isAvailble(int syncMode, boolean resume)
			throws SyncException {
		// TODO Auto-generated method stub
//		return ExternalEntryConst.NETWORK_OK == EbenHelpers.isNetworkAvailable();
		return true;
	}

	private void updateItems() {
		Enumeration delItem = tracker.getDeletedItems();
		Enumeration updateItem = tracker.getUpdatedItems();

		Hashtable uItems = new Hashtable();

		ArrayList list = new ArrayList();
		// list.

		while (updateItem.hasMoreElements()) {
			uItems.put(updateItem.nextElement(), "");
		}

		// uItems.
	}

	private Hashtable exUpdateItems = null;// old luid maybe not used now
	private ArrayList exupdateList = null;

	/**
	 *  now this is just for delete resume 
	 * @param item
	 */
	public void add2update(EdiskLuidInfo item) {
		// TODO Auto-generated method stub
		if (null == exupdateList) {
			Log.info(TAG_LOG, "exupdateListis null ex ,new one");
			exupdateList = new ArrayList();
		}
//		exupdateList
		
//		String fileName = directory + EbenHelpers.decodeKey(item);
//		File file = new File(fileName);
//		if(fileName.endsWith(TEMPFILE)) {
//			Log.error(TAG_LOG, "add2update: .sync~ ,ignore it");
//			return;
//		}
//		
//		if (file.exists()) {
//			String md5 = null;
//			if (fileName.endsWith(File.separator) || file.isDirectory()) {
//				md5 = EdiskSyncSource.EmptyMd5;
//			} else {
//				md5 = MD5Util.md5(file);
//			}
//			if(!exUpdateItems.containsKey(item)) {
//				exUpdateItems.put(item, md5);
//				Log.info(TAG_LOG, "add2update : add file : " + fileName);
//			}
//		} else {
//			Log.error(TAG_LOG, "got a deleted file, should resume it");
//			exUpdateItems.put(item, EdiskSyncSource.EmptyMd5);
//		}
	}
	
	public boolean isDelResumeKey(String key) {
//		UpfileInfo info = new UpfileInfo("", "", 0, null);
//		uplist.add(info);
		boolean isResumed = false;
		if(null == uplist) {
			return isResumed;
		}
		for (UpfileInfo in : uplist) {
			// if (EbenHelpers.encodeKey(fileName).equals(in.luid)) {
			if (key.equalsIgnoreCase(in.luid)) {
				if((null == in.durl || "".equalsIgnoreCase(in.durl))&& 0 == in.size && "".equals(in.etag)) {
					isResumed = true;
				}else {
					isResumed = false;
				}
				break;
			}
		}
		Log.debug(TAG_LOG, "isDelResumeKey, key ,"+key+",isresume, "+isResumed);
		return isResumed;
	}
	public void addExFolder(String item) {
		((EdiskTracker) tracker).putExFolderItems(item);
	}

	public void removeUpdate(String oldName) {
		// TODO Auto-generated method stub
		if (null == exUpdateItems) {
			return;
		}	
		
		if(exUpdateItems.containsKey(oldName)) {
			Log.info(TAG_LOG, "remove update key: "+oldName);
			exUpdateItems.remove(oldName);
		}
	}
	/**
	 * set deleted item by application
	 * @param source
	 * @param itemOld
	 */
	public static void markDelItem(Context context, String source, String itemOld) {
		EdiskLuidInfo.markDelItem(context,source,itemOld);
//		try {
//			ContentResolver cr = context.getContentResolver();
//			String[] projection = { "_key", "_value" };
//			String selection = "_key=?";
//			String[] selectionArgs = { itemOld };
////			String soureUri = "content://cn.eben.provider.syncMarkStatus/"
////					+ source + "/";
////			Uri uri = Uri.parse(soureUri);
//			// lierbao changed to
//			Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
//					+ SyncSourceProvider.PRIFEX_DELETE+source + "/");
//			Cursor cur = cr.query(uri, projection, selection, selectionArgs,
//					null);
//
//			int count = cur.getCount();
//			cur.close();
//			if (0 == count) {
//				Log.info(TAG_LOG, "set to delete table : " + itemOld);
//				ContentValues values = new ContentValues();
//				values.put("_key", itemOld);
//				values.put("_value", String.valueOf(0));
//				cr.insert(uri, values);
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
