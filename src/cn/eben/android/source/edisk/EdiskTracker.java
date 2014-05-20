package cn.eben.android.source.edisk;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.eben.android.net.apps.EDiskHelper;
import cn.eben.android.net.apps.EbenUpFileAuth;
import cn.eben.android.providers.SyncSourceProvider;
import cn.eben.android.source.edisk.EdiskSyncSource.FileSyncItem;
import cn.eben.android.util.EbenFileLog;
import cn.eben.android.util.EbenHelpers;
import cn.eben.android.util.MD5Util;
import cn.eben.androidsync.R;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.android.source.AndroidChangesTracker;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncSource;
import com.funambol.sync.client.CacheTracker;
import com.funambol.sync.client.TrackerException;
import com.funambol.util.Log;

public class EdiskTracker extends CacheTracker implements AndroidChangesTracker {

	private static final String TAG_LOG = "EdiskTracker";

	private final Context mContext;

	private EbenUpFileAuth upFileQuery;// = new EbenUpFileAuth(ss.getName());
	private boolean fileRelation = false;
	
	private ArrayList<EdiskLuidInfo> luidinfo;// = new ArrayList<Luidinfo>();// scan from file os
	private ArrayList<EdiskLuidInfo> syncedluidinfo;// 
	private ArrayList<EdiskLuidInfo> updateluidinfo;//
	private ArrayList<EdiskLuidInfo> newluidinfo;//
	private ArrayList<EdiskLuidInfo> deleteluidinfo;//
	
	public static String FASTSCAN = "fastscan";
	public static String ALLSCAN = "allscan";
	
	private String scanType = ALLSCAN;
	
	
	
	public String getScanType() {
		Log.debug(TAG_LOG, "getScanType : "+scanType);
		return scanType;
	}

	public void setScanType(String scanType) {
		Log.debug(TAG_LOG, "setScanType : "+scanType);
		this.scanType = scanType;
	}

	public ArrayList<EdiskLuidInfo> getSyncedluidinfo() {
		return syncedluidinfo;
	}

	public ArrayList<EdiskLuidInfo> getUpdateluidinfo() {
		return updateluidinfo;
	}

	public ArrayList<EdiskLuidInfo> getNewluidinfo() {
		return newluidinfo;
	}

	public ArrayList<EdiskLuidInfo> getDeleteluidinfo() {
		return deleteluidinfo;
	}

	public ArrayList<EdiskLuidInfo> getLuidinfo() {
		return luidinfo;
	}

	public void setLuidinfo(ArrayList<EdiskLuidInfo> luidinfo) {
		this.luidinfo = luidinfo;
	}

	//	class Luidinfo  {
//		String luid;
//		String parent;
//		String path;
//		String fp;
//	}
	public void setFileRelation(boolean fileRelation) {
		this.fileRelation = fileRelation;
	}

	public EdiskTracker(Context context, StringKeyValueStore keyStore) {
		super(keyStore);
		this.mContext = context;
	}
/**
 * 
 * @return
 */
	public StringKeyValueStore getStatus() {
		return status;
	}
	/**
	 * 
	 * @param context
	 * @param app
	 */
	private void getSyncedLuidList(Context context, String app) {
		syncedluidinfo = new ArrayList<EdiskLuidInfo>();

		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		Cursor cr = resolver.query(uri, null, null, null, null);

		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				// public final static String KEY_COLUMN_NAME = "_key";
				// protected final static String VALUE_COLUMN_NAME = "_value";

				// public final static String PATH_COLUMN_NAME = "_path";
				// public final static String PARENT_COLUMN_NAME = "_parent";
				String luid = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
				String fp = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));
				String path = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				String parent = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PARENT_COLUMN_NAME));
				String synced = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.SYNC_COLUMN_NAME));
				EdiskLuidInfo info = new EdiskLuidInfo(luid, parent, path, fp);
				info.setissynced(synced);
				syncedluidinfo.add(info);

			}
		}
	}
	private void resetSyncedLuidList(Context context, String app) {
		syncedluidinfo = new ArrayList<EdiskLuidInfo>();
		Log.info(TAG_LOG, "resetSyncedLuidList : "+app);
		
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		resolver.delete(uri, null, null);

	}
	/**
	 * system status sync run
	 */
	private void sendExProgress(long needsync, String key) {
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		
//		new EbenSyncProgress(1,needsync,1,ss.getName(),ess.getAppSource().getName(),"",0).send();
		// show at start
//		EbenExsyncProgress prg = new EbenExsyncProgress(1, 1, 1, ss
//				.getName(), ess.getAppSource().getName(), "", 0);
//		prg.setstage("begin");
//		prg.send();
		
	}
//	this.isSync = isSync;
//	this.needSync = needSync;
//	this.curSync = curSync;
//	this.sourcName = sourc;
//	this.itemName = itemName;
//	this.appName = appName;
//	this.errcode = errcode;
	@Override
	public void begin(int syncMode, boolean reset) throws TrackerException {
		// TODO Auto-generated method stub
//		super.begin(syncMode, reset);
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "begin, syncMode is " + syncMode);
		}
		EdiskSyncSource ess = (EdiskSyncSource) ss;
//		syncMode == SyncSource.SELECTED_SYNC
		this.syncMode = syncMode;

//		newItems = new Hashtable();
//		updatedItems = new Hashtable();
//		deletedItems = new Hashtable();
		
		updateluidinfo = new ArrayList<EdiskLuidInfo>();
		newluidinfo = new ArrayList<EdiskLuidInfo>();
		deleteluidinfo = new ArrayList<EdiskLuidInfo>();
//		getSyncedLuidList(mContext, ss.getName()) ;
		
		if (syncMode == SyncSource.INCREMENTAL_SYNC
				|| syncMode == SyncSource.INCREMENTAL_UPLOAD) {
			long needsync = EDiskHelper.syncCount;
			Hashtable snapshot;
			try {
				getSyncedLuidList(mContext, ss.getName()) ;
				Collections.sort(syncedluidinfo, new ParentLuidComparator());
				snapshot = getAllFilesFingerprint();
			} catch (SyncException e) {
				Log.error(TAG_LOG, "Cannot compute fingerprint for items ", e);
				throw new TrackerException(e.toString());
			}

			// Initialize the status by loading its content
			try {
//				this.status.load();
//				getSyncedLuidList(mContext, ss.getName()) ;
				boolean isfound = false;
//				syncedluidinfo = new Vector<EdiskLuidInfo>();
				// get new and update list
				for(EdiskLuidInfo info:luidinfo) {
					isfound = false;
					for (EdiskLuidInfo syncedinf : syncedluidinfo) {
						if (null != syncedinf.getissynced()
								&& syncedinf.getissynced().equals(EdiskLuidInfo.SYNCED)) {
//							isfound = true;
							if (null != info&& null != info.getLuid()&&info.getLuid()
									.equalsIgnoreCase(syncedinf.getLuid())) {
								if (!info.getFp().equalsIgnoreCase(
										syncedinf.getFp())) {

									info.setUpdateOp("edit");
									Log.debug(TAG_LOG, "find a update item : "
											+ syncedinf.toString()+" --> "+info.toString());
									
									EdiskLuidInfo.saveChangedItems(mContext, SyncSourceProvider.PRIFEX_UPDATE, ss.getName(), info);
									EdiskLuidInfo.add(updateluidinfo,newluidinfo,null,info,false);
//									updateluidinfo.add(info);
								} else if (!info.getParent().equalsIgnoreCase(
										syncedinf.getParent())) {
									info.setUpdateOp("move");
									Log.debug(TAG_LOG, "parent not same ,find a move item : "
											+ syncedinf.toString()+" --> "+info.toString());
									
									EdiskLuidInfo.add(updateluidinfo,newluidinfo,null,info,false);
//									updateluidinfo.add(info);
								} else if (!info.getPath().equalsIgnoreCase(
										syncedinf.getPath())) {

									info.setUpdateOp("rename");
									Log.debug(TAG_LOG, "find a rename item : "
											+ syncedinf.toString()+" --> "+info.toString());
									EdiskLuidInfo.add(updateluidinfo,newluidinfo,null,info,false);
//									updateluidinfo.add(info);
								} else {
//									isfound = false;
									Log.debug(TAG_LOG,
											"synced : " + info.getPath());
								}
								isfound = true;
								break;
							}
						}
					}
					if (!isfound) {
						EdiskLuidInfo.saveChangedItems(mContext, SyncSourceProvider.PRIFEX_NEW, ss.getName(), info);
						EdiskLuidInfo.add(newluidinfo,updateluidinfo,null,info,false);
//						newluidinfo.add(info);
					}
				}
				// end get update and new list
				// get delete item list
				ArrayList<String> delList = EdiskLuidInfo.getNeedsyncItems(SyncSourceProvider.PRIFEX_DELETE, ss.getName());
				isfound = true;
				if(ALLSCAN.equals(getScanType())) {
					for (EdiskLuidInfo syncedinf : syncedluidinfo) {
						isfound = false;
						if (null != syncedinf.getissynced()
								&& syncedinf.getissynced().equals(
										EdiskLuidInfo.SYNCED)) {
							for (EdiskLuidInfo info : luidinfo) {
								if (syncedinf.getLuid().equalsIgnoreCase(
										info.getLuid())) {
									isfound = true;
									break;
								}
							}
						}
						if(!isfound) {
							// find a deleted item, should check it 
							if(!syncedinf.getissynced().equalsIgnoreCase(EdiskLuidInfo.SYNCED)) {
								continue;
							}
							if(EdiskLuidInfo.findDelItem(mContext, ss.getName(), syncedinf.getPath())) {
								if(syncedinf.getPath().endsWith(File.separator)) {
									
								}
								EdiskLuidInfo.saveChangedItems(mContext, SyncSourceProvider.PRIFEX_DELETE, ss.getName(), syncedinf);
								EdiskLuidInfo.add(deleteluidinfo,newluidinfo,updateluidinfo,syncedinf,true);

								removeAlldownchildern(syncedinf.getLuid(),syncedinf.getPath());
							} else {
								if(EdiskLuidInfo.checkSamePathLuid(mContext, ss.getName(), syncedinf.getPath())) {
									Log.info(TAG_LOG, "find same path at diff luid,will do as new");
//									throw new TrackerException("error ,find same path at diff luid");
								} else {
//									EdiskLuidInfo(String luid, String parent, String path, String fp)
									if(EdiskSyncSource.isIlligalItem(syncedinf.getPath())) {
										Log.info(TAG_LOG,"ignore as delete");
									} else {
								EdiskLuidInfo fakeinfo = new EdiskLuidInfo(syncedinf.getLuid(),
										syncedinf.getParent(),syncedinf.getPath(),"");
								fakeinfo.setUpdateOp("edit");

								Log.info(TAG_LOG, "find a resume key : "+fakeinfo.toString());
								EdiskLuidInfo.add(updateluidinfo,newluidinfo,updateluidinfo,fakeinfo,false);
									}

								}
							}
							
						}
						
					}
				} else {
					
					for(String delitem:delList) {
						isfound = false;
						for(EdiskLuidInfo syncedinf:syncedluidinfo) {
							if(syncedinf.getPath().equalsIgnoreCase(delitem)) {
								isfound = true;
								File file = new File(ess.directory+delitem);
								if(!file.exists()){
//									deleteluidinfo.add(syncedinf);
									if(null != renameHt){
										for (Iterator it = renameHt.keySet().iterator(); it
												.hasNext();) {

											String olditem = (String) it.next();
											String newitem = (String) renameHt.get(olditem);

											if(delitem.startsWith(olditem)){
												Log.info(TAG_LOG, "not del : a rename :"+syncedinf.toString());
												EdiskLuidInfo.removeDelItem(mContext, ss.getName(), delitem);
												continue;
											}
										}
									}
										

									
									EdiskLuidInfo.add(deleteluidinfo,newluidinfo,updateluidinfo,syncedinf,true);
								}
							}
						}
						if(!isfound) {
							Log.debug(TAG_LOG, "has deleted : "+delitem);
							EdiskLuidInfo.removeDelItem(mContext, ss.getName(), delitem);
						}
					}
				}
				// end get delete item list

			} catch (Exception e) {

				Log.error(TAG_LOG, "Cannot load tracker status, error !!!");
				e.printStackTrace();
				throw new TrackerException(e.toString());

			}



		} else if (syncMode == SyncSource.FULL_SYNC
				|| syncMode == SyncSource.FULL_UPLOAD
				|| syncMode == SyncSource.FULL_DOWNLOAD) {
			// Reset the status when performing a slow sync
			try {
//				status.reset();
//				Collections.sort(luidinfo, new ParentLuidComparator());

				resetSyncedLuidList(mContext, ss.getName());
			} catch (Exception ex) {
				Log.error(TAG_LOG, "Cannot reset status", ex);
				throw new TrackerException("Cannot reset status");
			}
		} else if (syncMode == SyncSource.SELECTED_SYNC) {
//			newItems = new Hashtable();
//			updatedItems = new Hashtable();
//			deletedItems = new Hashtable();
//			String name = ss.getName();
//			String 
			
//			String directory = ess.getDirectory();
//			Log.info(TAG_LOG,"a seleted mode : ");
//			long needsync = EDiskHelper.syncCount;
//			try {
//				this.status.load();
//			} catch (Exception e) {
//
//				Log.error(TAG_LOG, "Cannot load tracker status, error !!!");
//				e.printStackTrace();
//				throw new TrackerException(e.toString());
//			}
//			ArrayList<String> newsync = getsyncItems(SyncSourceProvider.PRIFEX_NEW, name);
//			ArrayList<String> updatesync = getsyncItems(SyncSourceProvider.PRIFEX_UPDATE, name);
//			ArrayList<String> delsync = getsyncItems(SyncSourceProvider.PRIFEX_DELETE, name);
//			for (String newKey : newsync) {
//				setUpdateNewItems(newKey,SyncSourceProvider.PRIFEX_NEW);
//			}
//			for (String updateKey : updatesync) {
//				setUpdateNewItems(updateKey,SyncSourceProvider.PRIFEX_UPDATE);
//			}
//			for (String delKey : delsync) {
//				File delfile = new File(directory+delKey);
//				if(delfile.exists()) {
//					removeSyncItem(SyncSourceProvider.PRIFEX_DELETE,
//							delKey);
////					cont;	
//				} else {
//					try {
//						String enkey = EbenHelpers.encodeKey(delKey);
//						String fp = status.get(enkey);
//						if(null != fp) {
//							deletedItems.put(enkey, fp);
//						} else {
//							removeSyncItem(SyncSourceProvider.PRIFEX_DELETE,
//									delKey);							
//						}
//						
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
////				setUpdateNewItems(updateKey);
////				deletedItems.put(oldKey, (String) status.get(oldKey));
////				removeDownPending(EbenHelpers.decodeKey(oldKey));
//			}
				
//			SyncSourceProvider.PRIFEX_NEW,app);
			
		}
		Collections.sort(updateluidinfo, new ParentLuidComparator());
		Collections.sort(newluidinfo, new ParentLuidComparator());
		
		resetFolderChecked();
//		if (fileRelation) {
//			
//			setExItems(newItems);
//			setExItems(updatedItems);
//			setExItems(deletedItems);
//			resetFolderChecked();
//			
//			setExFolderItems(deletedItems);
//		}
//		setExFolderItems(newItems);
//		setExFolderItems(updatedItems);
	}
	/**
	 * lierbao for select sync item set
	 * @param newKey
	 * @param table
	 */
	void setUpdateNewItems(String newKey,String table){
		String name = ss.getName();
//		String 
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		String directory = ess.getDirectory();
		try {
			String eKey = EbenHelpers.encodeKey(newKey);
			if (status.get(eKey) == null) {
				if (Log.isLoggable(Log.INFO)) {
					Log.info(TAG_LOG, "Found a new item with key: "
							+ newKey);
				}
//				needsync++;
//				sendExProgress(needsync, "");
				File syncFile = new File(directory + newKey);
				if (!syncFile.exists()
						|| syncFile.length() > EdiskSyncSource.MAX_FILE_SIZE) {
					// remove this item
					removeSyncItem(table,
							newKey);
					return;
				}
				String fp = MD5Util.md5(syncFile);
				Log.debug(TAG_LOG, "fp: " + fp);
				newItems.put(eKey, fp);
				removeDownPending(EbenHelpers.decodeKey(newKey));
			} else {
				File syncFile = new File(directory + newKey);
				if (!syncFile.exists()
						|| syncFile.length() > EdiskSyncSource.MAX_FILE_SIZE) {
					// remove this item
					removeSyncItem(table,
							newKey);
					return;
				}
				String fp = MD5Util.md5(syncFile);
				if(fp.equalsIgnoreCase(status.get(eKey))) {
					// has synced 
					removeSyncItem(table,
							newKey);
					return;
				} else {
//					needsync++;
//					sendExProgress(needsync,"");
					updatedItems.put(newKey, fp);
					removeDownPending(EbenHelpers.decodeKey(newKey));							
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	void removeSyncItem(String table,String name) {
		String sourceName = ss.getName();
		ContentResolver cr = App.i().getContentResolver();

		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				table+sourceName);
		
		if(cr.delete(uri, "_key=?", new String[]{name}) >0) {
			Log.info(TAG_LOG, "remove "+table+",  "+name);
		}	
	}
	/**
	 * 
	 * @param list
	 * @param table
	 * @param app
	 */
	ArrayList getsyncItems(String table,String app) {
		ArrayList existList = new ArrayList();
		ContentResolver cr = App.i().getApplicationContext()
				.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ table+app + "/");

		Cursor cur = cr.query(uri, null, null, null, null);
		
		if(cur.getCount()>0) {
			for (cur.moveToFirst(); !cur.isAfterLast(); cur
					.moveToNext()) {

				String key = cur.getString(cur
						.getColumnIndex("_key"));
				if(!existList.contains(key)) {
					existList.add(key);
				}

			}
		}
						
		cur.close();

		return existList;
	}
	/**
	 * remove the down pending item after file downloaded 
	 * @param name
	 */
	public void removeDownPending(String name) {
		String sourceName = ss.getName();
		ContentResolver cr = App.i().getContentResolver();

		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				SyncSourceProvider.PRIFEX_DOWN+sourceName);
		
		if(cr.delete(uri, SyncSourceProvider.NAME+"=?", new String[]{name}) >0) {
			Log.info(TAG_LOG, "removeDownPending : "+name);
		}
	}
	/**
	 * file changed, should update folder to server
	 * @param ht Add Delete Replaced items list 
	 */
	public void setExFolderItems(Hashtable ht) {
		// TODO Auto-generated method stub
		for (Iterator it = ht.keySet().iterator(); it.hasNext();) {

			String key = (String) it.next();
			putExFolderItems(key);
		}
	}

	/**
	 * 
	 * @param key
	 */
	public void putExFolderItems(String key) {
		// TODO Auto-generated method stub
//		String name = EbenHelpers.decodeKey(key);
//		EdiskSyncSource ess = (EdiskSyncSource) ss;
//		String directory = ess.getDirectory();
//		while(name.contains(File.separator)) {
//			
//			name = name.substring(0,name.lastIndexOf(File.separator));
//			if(isFolderChecked(name)) {
//				return ;// child checked ,parent must checked ,so can return
//			}
//			setFolderChecked(name);
//			File file = new File(directory+name);
//			if(file.exists() && file.isDirectory()) {
//				String folerItem = EbenHelpers.encodeKey(name+File.separator);
//
//					ess.add2update(folerItem);
//
//			}
//		}
	}
	/** set notepad file to update list in same directory
	 * 
	 * @param ht a hashtable
	 */
	private void setExItems(Hashtable ht) {
		for (Iterator it = ht.keySet().iterator(); it.hasNext();) {

			String key = (String) it.next();
			putExItems(key);
		}
	}
	private ArrayList folderChecked;
	private void setFolderChecked(String folder) {
		if(null == folderChecked) {
			folderChecked = new ArrayList();
		}
		if(!folderChecked.contains(folder))  {
			folderChecked.add(folder);
		}
			
	}
	private boolean isFolderChecked(String folder) {
		if(null != folderChecked && folderChecked.contains(folder))
			return true;
		
		return false;
	}
	public void resetFolderChecked() {
		Log.info(TAG_LOG, "resetFolderChecked");
		folderChecked = null;
	}
	/** 
	 * put item to extra list to send to server
	 * @param item 
	 */
	private void putExItems(String item) {

//		String name = EbenHelpers.decodeKey(item);
//		if(name.contains(File.separator)) {
//			EdiskSyncSource ess = (EdiskSyncSource) ss;
//			String directory = ess.getDirectory();
//			name = directory + name.substring(0, name.lastIndexOf(File.separator));
//			
//			if(isFolderChecked(name)) {
//				Log.info(TAG_LOG,"has checked : "+name);
//				return ;
//			}
//			Log.info(TAG_LOG, "putExItems : name directory : "+name);
//			setFolderChecked(name);
//			File file = new File(name);
//			if(!name.endsWith(".")&&file.exists() && file.isDirectory()) {
//				File[] list = file.listFiles(new EdiskFileFilter(ExternalEntryConst.APP_EDISK));
//				for(File subFile : list) {
////					File subFile = new File(sub);
//					if(subFile.exists()&&!subFile.isDirectory()) {
////						Log.info(TAG_LOG, "sub : "+sub);
//						String subItem = EbenHelpers.encodeKey(subFile.toString().substring(directory.length()));
//						Log.debug(TAG_LOG, "subItem : "+subItem);
//						if(!checkItemExist(subItem)) {
//							ess.add2update(subItem);
//						}
//					}
//				}
//			} else {
//				Log.error(TAG_LOG, "error !! not directory or not exist");
//			}
//		}
				
	}
	/**
	 * check item  in list already
	 * @param item a key
	 * @return
	 */
	private boolean checkItemExist(String item) {
		
		return (newItems.containsKey(item)||updatedItems.containsKey(item)||deletedItems.containsKey(item));

	}

	@Override
	protected String computeFingerprint(SyncItem item) {
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "computeFingerprint");
		}

		FileSyncItem fsi = (FileSyncItem) item;
		String fp = null;
		File file = new File(fsi.getFileName());
		if(file.isDirectory()||0 == file.length()) {
			fp = MD5Util.md5("");
		}
		else {
			fp = MD5Util.md5(new File(fsi.getFileName()));
		}

		Log.debug(TAG_LOG, "fp : md5 -- " + fp);
		return fp;
	}

	public String computeFingerprint(String name) {
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "computeFingerprint");
		}

		String fp = null;
		File file = new File(name);
		if(!file.exists()) {
			Log.debug(TAG_LOG,"file not exit fp null");
			return null;
			
		}
		if(file.isDirectory()||0 == file.length()) {
			fp = MD5Util.md5("");
		}
		else {
			fp = MD5Util.md5(file);
		}

		Log.debug(TAG_LOG, "fp : md5 -- " + fp);
		return fp;
	}
	public void getAll(File dir, int level, ArrayList resultList,FileFilter filter) {
//		if(ExternalEntryConst.NETWORK_OK != EdiskSyncSource.netstate) {
//			throw new SyncException(EdiskSyncSource.netstate,"network not available");
//		}
		
		level++;
		
		File[] files = dir.listFiles(filter);
		for (int x = 0; x < files.length; x++) {
//			String name = dir.getAbsolutePath() + "/" + files[x].getName();
			String name = files[x].getAbsolutePath();

			Log.debug(TAG_LOG, "name : " + name);
			if(files[x].getName().startsWith(EdiskSyncSource.WAVELINE)) {
				Log.info(TAG_LOG, "ignore file : "+files[x].getName());
				continue;
			}
			if (files[x].isDirectory()) {

				// String a = File.separator;
				String dirItem = name + File.separator;
				Log.debug(TAG_LOG, "dir Item : " + dirItem);
				// resultList.addElement(EbenHelpers.encodeKey(dirItem));
				resultList.add(dirItem);

				getAll(files[x], level, resultList, filter);
			} else {

				// Log.info(TAG_LOG,
				// "dir.getAbsolutePath() is "+dir.getAbsolutePath()+", name is "+files[x].getName());
				// resultVector.addElement(name.replace(directory, ""));
				if (files[x].getName().contains(".aux")) {
					continue;
				}
				if (AndroidConfiguration.authSyncInter) {
//					if (files[x].getName().contains(EdiskSyncSource.TEMPFILE)) {
//						Log.error(TAG_LOG, "scan files ,find .sync~ file, remove it");
//						deleteItem(EbenHelpers.encodeKey(name));
//						continue;
//					}					
				}

				if (0 == EdiskSyncSource.MAX_FILE_SIZE || EbenHelpers.getFileSizes(files[x]) < EdiskSyncSource.MAX_FILE_SIZE) {
					resultList.add(name);
				} else {
					Log.info(TAG_LOG,
							"dir.getAbsolutePath() is " + dir.getAbsolutePath()
									+ ", name is " + files[x].getName());

//					ExSyncManager.i().toastInfo(mContext.getString(R.string.mindcloud_large_file, files[x].getName()));

					Log.error(TAG_LOG, "file is too large, ignore it ");
				}
			}
		}
	}	
	
	private  ArrayList getAllItemsKeys(String directory,FileFilter filter) throws SyncException {
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "getAllItemsKeys");
		}

		ArrayList keys = new ArrayList();
		try {
			getAll(new File(directory), 0, keys,filter);
		} catch (Exception e) {
			Log.error(TAG_LOG, "Cannot get list of files", e);
			throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
		}
		return keys;
	}
	
	class ParentFirstComparator implements Comparator<String> {

		public int compare(String name1, String name2) {
		    // TODO Auto-generated method stub
		    String[] s1 = name1.split("/");
		    String[] s2 = name2.split("/");

//		    return (s2.length - s1.length);
		    return (s1.length - s2.length);
		}

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
	
	@Override
	public Hashtable getAllFilesFingerprint() throws SyncException {
		// TODO Auto-generated method stub
		EdiskSyncSource ess = (EdiskSyncSource) ss;
//		Enumeration allItemsKeys = ess.getAllItemsKeys();
		String directory = ess.getDirectory();
		ArrayList<String> fileList = null;
//		public static String FASTSCAN = "fastscan";
//		public static String ALLSCAN = "allscan";
		renameHt = null;
		if(ALLSCAN.equals(getScanType())){
			fileList = getAllItemsKeys(directory,ess.getfileFileter());
		} else {
			fileList = getSelectItems();
		}
		Collections.sort(fileList, new ParentFirstComparator());
		Hashtable snapshot = new Hashtable();
		luidinfo = new ArrayList<EdiskLuidInfo>();
		
		for(String path :fileList) {
			String fp ;
//			String fileName;
			if(ALLSCAN.equals(getScanType())) {
//				fileName = path;
				fp = computeFingerprint(path);
				path = path.substring(directory.length());
			} else {
//				fileName = directory+path;
				fp = computeFingerprint(directory+path);
			}
			if(null == fp) {
				Log.error(TAG_LOG, "got a error file : file not exist,"+path);
				if(null != renameHt&&renameHt.contains(path)){
//					public EdiskLuidInfo(String luid, String parent, String path, String fp)
					EdiskLuidInfo delInfo = new EdiskLuidInfo(null,null,null,null);
					for (Iterator it = renameHt.keySet().iterator(); it
							.hasNext();) {

						String key = (String) it.next();
						String value = (String) renameHt.get(key);

						if(value.equalsIgnoreCase(path)) {
							delInfo.setPath(key);
							delInfo.setFp("");
							EdiskLuidInfo.saveChangedItems(mContext, SyncSourceProvider.PRIFEX_DELETE,
									ss.getName(), delInfo);
						}
					
					}

				}
				EdiskLuidInfo.clearItems(mContext,ss.getName(),path);
				continue;
			}
			String parent = EdiskLuidInfo.getfileParentluid(luidinfo, path);
			if(null == parent) {
				Log.info(TAG_LOG, "parent null ,try to find at syncedluidinfo");
				parent = EdiskLuidInfo.getfileParentluid(syncedluidinfo, path);
				if(null == parent) {
				Log.error(TAG_LOG, "got a error file ,parent not found : "+path);
				EdiskLuidInfo.clearItems(mContext,ss.getName(),path);
				continue;	
				}
			}
			
//			EdiskLuidInfo.createLuid(mContext, ss.getName());
//			snapshot.put(luid, fp);
//			EdiskLuidInfo(String luid, String parent, String path, String fp)
			EdiskLuidInfo info  = new EdiskLuidInfo(null,parent,path,fp);
			info.setFp(fp);
			info.setPath(path);
			info.setParent(parent);
			
			String luid = null;
			if(null != renameHt){
				String old=null;
				if (renameHt.contains(path)) {
					for (Iterator it = renameHt.keySet().iterator(); it
							.hasNext();) {

						String key = (String) it.next();
						String value = (String) renameHt.get(key);

						if(value.equalsIgnoreCase(path)) {
							old = key;
						}
					}
//					String old = (String) renameHt.g;
					Log.debug(TAG_LOG, "a renamed key: "+old+" --> "+path);
					if(null == old) {
						Log.error(TAG_LOG, "find old got null  : "+path);
//						continue;
					}  else {
//					renameHt.get(path)
					luid = EdiskLuidInfo.getExistluid(mContext, ss.getName(), old, info);
						if(null == luid) {
							Log.error(TAG_LOG, "find a not sync old item : "+path);
							EdiskLuidInfo.removeRenameItem(mContext,ss.getName(),old);

						} else {
							// make a bookinfo update
//							if(ss.getName().equalsIgnoreCase(ExternalEntryConst.APP_ENOTE)) {
//								if(old.endsWith(File.separator)) {
////									File up = new File(directory+path+".bookinfo");
//									String upfp = computeFingerprint(directory+path+".bookinfo"); 
//									if(null != upfp ) {
//										EdiskLuidInfo upinfo  = new EdiskLuidInfo(null,luid,path+".bookinfo",upfp);
//										String upluid = EdiskLuidInfo.getExistluid(mContext, ss.getName(), old+".bookinfo", upinfo);
//										if(null != upluid) {
//											Log.info(TAG_LOG, "update .bookinfo ,"+upinfo.toString());
//											upinfo.setLuid(upluid);
//											luidinfo.add(upinfo);
//										}
//									}
//								}
//							}
						}
					}
				}
			}
			if(null == luid){
				luid = EdiskLuidInfo.getluid(mContext, ss.getName(),path ,info);
			}
//			info.setLuid(luid);
			luidinfo.add(info);
		}

		
//		while (allItemsKeys.hasMoreElements()) {
//			String key = (String) allItemsKeys.nextElement();
////			SyncItem item = new SyncItem(key);
////			item = getItemContent(item);
//			// Compute the fingerprint for this item
//			if (Log.isLoggable(Log.TRACE)) {
////				Log.trace(TAG_LOG, "Computing fingerprint for " + item.getKey());
//			}
////			String fp = computeFingerprint(item);
//			String fp = computeFingerprint(key);
//			if (Log.isLoggable(Log.TRACE)) {
//				Log.trace(TAG_LOG, "Fingerprint is: " + fp);
//			}
//			
//			// write luid fp path parent to table
//			
//			String luid = getLuid(key);
//			// Store the fingerprint for this item
//			if(null != fp) {
////			snapshot.put(item.getKey(), fp);
//			}
//		}

		return snapshot;
	}
	/**
	 * key: for old ,value for new
	 */
	private Hashtable renameHt ;
	/**
	 * 
	 * @return
	 */
	private ArrayList<String> getSelectItems() {
		// TODO Auto-generated method stub
		ArrayList<String> keys = new ArrayList<String>();
		
		Log.debug(TAG_LOG, "getSelectItems");

//		ArrayList<String> newsync = getsyncItems(SyncSourceProvider.PRIFEX_NEW, name);
//		ArrayList<String> updatesync = getsyncItems(SyncSourceProvider.PRIFEX_UPDATE, name);
//		ArrayList<String> delsync = getsyncItems(SyncSourceProvider.PRIFEX_DELETE, name);

		keys.addAll(EdiskLuidInfo.getNeedsyncItems(SyncSourceProvider.PRIFEX_NEW, ss.getName()));
		keys.addAll(EdiskLuidInfo.getNeedsyncItems(SyncSourceProvider.PRIFEX_UPDATE, ss.getName()));
		
		renameHt = EdiskLuidInfo.getRenamedItems(SyncSourceProvider.PRIFEX_RENAME, ss.getName());
		keys.addAll(renameHt.values());
//		Hashtable ht = new Hashtable();
//		keys.addAll(ht.);
//		Collections.sort(keys, new ParentFirstComparator());
		
		return keys;
	}

private String getLuid(String key) {
		// TODO Auto-generated method stub
		return "";
	}
	//	public static final String AUTHORITY = "content://cn.eben.provider.syncStatus";
	public static final String AUTHORITY_STATUS = SyncSourceProvider.AUTHORITY_URI;

	// public static final Uri AUTHORITY_URI =
	// Uri.withAppendedPath(Uri.parse("content://" + AUTHORITY), "edisk");
	//
	@Override
	protected void setItemStatus(String key, int itemStatus)
			throws com.funambol.sync.client.TrackerException {

		EdiskSyncSource eSource = (EdiskSyncSource) ss;
		boolean isfound = false;
		if (isSuccess(itemStatus)
				&& (syncMode == SyncSource.FULL_SYNC || syncMode == SyncSource.FULL_UPLOAD)) {
			for (EdiskLuidInfo info : luidinfo) {
				if (key.equals(info.getLuid())) {
					isfound = true;
					// update luid to db
					info.setissynced(EdiskLuidInfo.SYNCED);
					EdiskLuidInfo
							.updateSyncedItemResult(mContext, ss.getName(), info);
					updateSyncDb(mContext, ss.getName(), info);
//				ExSyncManager.i().notifyItemAction(eSource.getAppSource(),
//				info.getPath(), itemStatus, ExSyncManager.SYNC_END);

				EbenFileLog.recordSyncLog("upinfo: " + info.getPath()
						+ " --> cloud ");
				eSource.deleteFiles(new File(eSource.tempDirectory + info.getPath()));
				
				
				break;
				}

			}
			if(!isfound) {
				Log.error(TAG_LOG, "setItemStatus not find key: "+key);
			}
		}
		else if (isSuccess(itemStatus)) {
			for (EdiskLuidInfo info : updateluidinfo) {
				if (key.equals(info.getLuid())) {
					// update luid to db
					isfound = true;
					info.setissynced(EdiskLuidInfo.SYNCED);
					EdiskLuidInfo
							.updateSyncedItemResult(mContext, ss.getName(), info);
					updateSyncDb(mContext, ss.getName(), info);
//				ExSyncManager.i().notifyItemAction(eSource.getAppSource(),
//				info.getPath(), itemStatus, ExSyncManager.SYNC_END);

				EbenFileLog.recordSyncLog("upinfo: " + info.getPath()
						+ " --> cloud ");
				eSource.deleteFiles(new File(eSource.tempDirectory + info.getPath()));
				
				break;
				}
			}
			
			if(!isfound) {
				for (EdiskLuidInfo info : newluidinfo) {
					if (key.equals(info.getLuid())) {
						// update luid to db
						isfound = true;
						info.setissynced(EdiskLuidInfo.SYNCED);
						EdiskLuidInfo
								.updateSyncedItemResult(mContext, ss.getName(), info);
						updateSyncDb(mContext, ss.getName(), info);
//					ExSyncManager.i().notifyItemAction(eSource.getAppSource(),
//					info.getPath(), itemStatus, ExSyncManager.SYNC_END);

					EbenFileLog.recordSyncLog("upinfo: " + info.getPath()
							+ " --> cloud ");
					eSource.deleteFiles(new File(eSource.tempDirectory + info.getPath()));
					
					}
			}
				}
			
			
			if (!isfound) {
				for (EdiskLuidInfo info : deleteluidinfo) {
					if (key.equals(info.getLuid())) {
						// delete
						isfound = true;
//						EdiskLuidInfo.deleteyncedItem(mContext, ss.getName(),
//								info);
						updateStatusinfo(ss.getName(),info.getPath(),SyncItem.STATE_DELETED);
						EdiskLuidInfo.deleteyncedItemChild(mContext,ss.getName(),info,syncedluidinfo);
//						ExSyncManager.i().notifyItemResult(eSource.getAppSource(),
//								info.getPath(), itemStatus, ExSyncManager.SYNC_END,' ');// not 'D' just for server del
//						
						break;
					}

				}
			}
		}
//		if (isSuccess(itemStatus)
//				&& (syncMode == SyncSource.FULL_SYNC || syncMode == SyncSource.FULL_UPLOAD)) {
////			for(EdiskLuidInfo info: luidinfo) {
////				if(key.equals(info.getLuid())) {
////					// update luid to db
////					info.setissynced(EdiskLuidInfo.SYNCED);
////					EdiskLuidInfo.updateSyncedItem(mContext, ss.getName(), info);
////				}
////			}
//
//		} else if (isSuccess(itemStatus)
//				&& itemStatus != SyncSource.CHUNK_SUCCESS_STATUS) {
//			// We must update the fingerprint store with the value of the
//			// fingerprint at the last sync
//			try {
//				
////				boolean isfound = false;
////				for(EdiskLuidInfo info:newluidinfo) {
////					if(key.equals(info.getLuid())) {
////						// update 
////						info.setissynced(EdiskLuidInfo.SYNCED);
////						EdiskLuidInfo.updateSyncedItem(mContext, ss.getName(), info);
////						
////						isfound = true;
////						break;
////					}
////						
////				}
////				if(!isfound) {
////					for(EdiskLuidInfo info:updateluidinfo) {
////						if(key.equals(info.getLuid())) {
////							// update 
////							isfound = true;
////							EdiskLuidInfo.updateSyncedItem(mContext, ss.getName(), info);
////							break;
////						}
////							
////					}				
////				}
////				if(!isfound) {
////					for(EdiskLuidInfo info:deleteluidinfo) {
////						if(key.equals(info.getLuid())) {
////							// delete  
////							isfound = true;
////							EdiskLuidInfo.deleteyncedItem(mContext, ss.getName(), info);
////							break;
////						}
////							
////					}				
////				}				
//
//
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new TrackerException(e.toString());
//			}
//		} else {
//			// On error we do not change the fp so the change will
//			// be reconsidered at the next sync
//		}
		if (Log.isLoggable(Log.TRACE)) {
			Log.trace(TAG_LOG, "status set for item: " + key);
		}
		
//		updateSyncDb(key, itemStatus);
//		EdiskSyncSource eSource = (EdiskSyncSource) ss;
//		String decode = EbenHelpers.decodeKey(key);
		if (null != deletedItems && deletedItems.get(key) != null) {
//			ExSyncManager.i().notifyItemResult(eSource.getAppSource(),
//					decode, itemStatus, ExSyncManager.SYNC_END,'D');
		}
//		else if(decode.endsWith(File.separator)) {
////			ExSyncManager.i().notifyItemAction(eSource.getAppSource(),
////					decode, itemStatus, ExSyncManager.SYNC_END);
//		}

		Log.debug(TAG_LOG, "setItemStatus ,key is " + key + ", itemStatus is "
				+ itemStatus);

	}

	private void updateSyncDb(Context context, String app, EdiskLuidInfo info) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				SyncSourceProvider.PRIFEX_STATUS + app);
		String  name = info.getPath();
		if (null != name) {
			name = name.replaceAll(SyncSourceProvider.PNT,
					SyncSourceProvider.PNTTOKEN); // lierbao
		}

		String[] projection = { "_key", "_value" };
		String selection = "_key = ?";
		String[] args = { name };

		boolean isNew = true;
		Cursor c = cr.query(uri, projection, selection, args, null);
		if (null != c && c.getCount() > 0) {
			isNew = false;
		}
		Log.debug(TAG_LOG, "isNew " + name + ", " + isNew);
		
		if (null != c) {
			c.close();
		}
		int itemStatus = 6;
		if(info.getissynced().equals(EdiskLuidInfo.SYNCED)) {
			itemStatus = SyncSource.STATUS_SUCCESS;
		}
		ContentValues cv = new ContentValues();
		cv.put("_key", name);
		cv.put("_value", itemStatus);

		if (isNew) {
			cr.insert(uri, cv);
		} else {
			cr.update(uri, cv, selection, args);
		}

	}

/**
 *  for update  ui status db 
 * @param key  luid
 * @param itemStatus   chunked 213 for syncing
 */
//	public void updateSyncDb(String key, int itemStatus) {
//		// TODO Auto-generated method stub
//		//lierbao
////		if(true)
////			return;
//		
//		String deCode = EdiskLuidInfo.getLuidPath(mContext, ss.getName(), key);
//		if(SyncSource.STATUS_SUCCESS != itemStatus) {
//			itemStatus = SyncStatusService.SYNCITEM_ST_UNSYNC;
//		}
//		Log.debug(TAG_LOG, "updateSyncDb , deCode, " + deCode + ", itemStatus, "
//				+ itemStatus);
//		ContentResolver cr = App.i().getContentResolver();
//
//		String sourceName = ss.getName();
//		Log.debug(TAG_LOG, "source name is : " + sourceName);
//
//		if (ss instanceof EdiskSyncSource) {
//			EdiskSyncSource eSource = (EdiskSyncSource) ss;
//
//			if (deCode.startsWith(eSource.getDirectory())) {
//				deCode = deCode.substring(eSource.getDirectory().length());
//			}
//		}
//		if (null != deCode)
//			deCode = deCode.replaceAll(SyncSourceProvider.PNT,
//					SyncSourceProvider.PNTTOKEN); // lierbao
//		Log.debug(TAG_LOG, "decode file name : " + deCode);
//
////		Uri uri = Uri.withAppendedPath(Uri.parse(AUTHORITY), sourceName);
////		Uri uri = Uri.parse(EdiskTracker.AUTHORITY_STATUS+sourceName);
//		Uri uri = Uri.withAppendedPath(Uri.parse(SyncSourceProvider.AUTHORITY_URI), SyncSourceProvider.PRIFEX_STATUS+sourceName);
//		
//		String[] projection = { "_key", "_value" };
//		String selection = "_key = ?";
//		String[] args = { deCode };
//
//		boolean isNew = true;
//		Cursor c = cr.query(uri, projection, selection, args, null);
//		if (null != c && c.getCount() > 0) {
//			isNew = false;
//		}
//		Log.debug(TAG_LOG, "isNew " + deCode + ", " + isNew);
//
//		if (null != c)
//			c.close();
//		ContentValues cv = new ContentValues();
//		cv.put("_key", deCode);
//		cv.put("_value", itemStatus);
//
//		if (isSuccess(itemStatus)
//				&& (syncMode == SyncSource.FULL_SYNC || syncMode == SyncSource.FULL_UPLOAD)) {
//			if (isNew) {
//				cr.insert(uri, cv);
//			} else {
//				cr.update(uri, cv, selection, args);
//			}
//		} else if (isSuccess(itemStatus)) {
//			// fingerprint at the last sync
//			if (newItems.get(key) != null) {
//				// This is a new item
//				if (isNew) {
//					cr.insert(uri, cv);
//				} else {
//					cr.update(uri, cv, selection, args);
//				}
//				
//			} else if (updatedItems.get(key) != null) {
//				if (isNew) {
//					cr.insert(uri, cv);
//				} else {
//					cr.update(uri, cv, selection, args);
//				}
//				
//			} else if (deletedItems.get(key) != null) {
//				// Update the fingerprint
//				EbenFileLog.recordSyncLog("delete server :" + deCode + " --> server ");
//				if (!isNew)
//					cr.delete(uri, selection, args);
//			}
//		}
//		if (null != c)
//			c.close();
//	}

	@Override
	public boolean removeItem(SyncItem item) throws TrackerException {
		// TODO Auto-generated method stub

		Log.debug(TAG_LOG,
				"removeItem " + item.getKey() + ", " + item.getState());
	
		// for local interface status
		String sourceName = ss.getName();
		Log.info(TAG_LOG, "source name is : " + sourceName);

		String key = item.getKey();		
		String pathname = getkeypath(key);
		updateStatusinfo(sourceName,pathname,item.getState());

		// notify item status
		// String deCode = EbenHelpers.decodeKey(key);
//		EdiskSyncSource eSource = (EdiskSyncSource) ss;
//		ExSyncManager.i().notifyItemResult(eSource.getAppSource(),
//				EbenHelpers.decodeKey(key), SyncSource.STATUS_SUCCESS,
//				ExSyncManager.SYNC_END, item.getState());
//		return updateSyncedInfo(item);
		return super.removeItem(item);

	}
	/**
	 * 
	 * @param item
	 * @param state
	 * @return
	 */
	private boolean updateSyncedInfo(EdiskLuidInfo item, char state) {
		// TODO Auto-generated method stub
		Log.debug(TAG_LOG,
				"updateSyncedInfo, parent, " + item.getParent() + ", " + item.getLuid());
		String fp;
		boolean res = true;
		try {
			switch (state) {
			case SyncItem.STATE_NEW:

			case SyncItem.STATE_UPDATED:
				item.setissynced(EdiskLuidInfo.SYNCED);
				EdiskLuidInfo.updateSyncedItem(mContext, ss.getName(), item);
				break;
			case SyncItem.STATE_DELETED:
//				if(item.getPath().endsWith(File.separator)) {
//				for(EdiskLuidInfo info: syncedluidinfo) {
//					if(info.getPath().startsWith(item.getPath())) {
//						EdiskLuidInfo.deleteyncedItem(mContext, ss.getName(), info);
//					}
//				}
//				} else {
//					EdiskLuidInfo.deleteyncedItem(mContext, ss.getName(), item);
//					
//				}
				EdiskLuidInfo.deleteyncedItemChild(mContext,ss.getName(),item,syncedluidinfo);
				break;
			default:
				Log.error(TAG_LOG, "Cache Tracker cannot remove item");
				res = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TrackerException(e.toString());
		}
		return res;
	}
	/**
	 * 
	 * @param sourceName
	 * @param pathname
	 * @param state
	 */
	private void updateStatusinfo(String sourceName, String pathname,char state) {
		// TODO Auto-generated method stub
		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				SyncSourceProvider.PRIFEX_STATUS + sourceName);

		// String deCode = EbenHelpers.decodeKey(key);
		// if (null != deCode)
		pathname = pathname.replaceAll(SyncSourceProvider.PNT,
				SyncSourceProvider.PNTTOKEN); // lierbao
		ContentResolver cr = App.i().getContentResolver();

		String[] projection = { "_key", "_value" };
		String selection = "_key = ?";
		String[] args = { pathname };

		boolean isNew = true;
		Cursor c = cr.query(uri, projection, selection, args, null);
		if (null != c && c.getCount() > 0) {
			isNew = false;
		}
		Log.info(TAG_LOG, "isNew " + pathname + ", " + isNew);
		if (null != c)
			c.close();
		ContentValues cv = new ContentValues();
		cv.put("_key", pathname);
		cv.put("_value", "0");

		switch (state) {
		case SyncItem.STATE_NEW:
			try {
				if (isNew) {
					cr.insert(uri, cv);
				} else {
					cr.update(uri, cv, selection, args);
				}
				;
			} catch (SyncException ex) {
				throw new TrackerException(ex.toString());
			}
			break;
		case SyncItem.STATE_UPDATED:
			if (isNew) {
				cr.insert(uri, cv);
			} else {
				cr.update(uri, cv, selection, args);
			}
			;
			break;
		case SyncItem.STATE_DELETED:
			if (!isNew)
				cr.delete(uri, selection, args);
			break;
		default:
			Log.error(TAG_LOG, "Cache Tracker cannot remove item");

		}
	}

	public boolean hasChanges() {
		boolean result = false;
		begin(SyncSource.INCREMENTAL_SYNC, false);
		result |= getNewItemsCount() > 0;
		result |= getUpdatedItemsCount() > 0;
		result |= getDeletedItemsCount() > 0;
		end();
		return result;
	}
	ArrayList<UpfileInfo> uplist;
	ArrayList<UpfileInfo> localList;
	public ArrayList<UpfileInfo> getUplist() {
		return uplist;
	}
/**
 * 
 * @throws SyncException
 */
	public void upFileQuery() throws SyncException {
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		String directory = ess.getDirectory();
		String temp = ess.getTempDirectory();
//		String fileName;
		boolean hasChanged = false;
		localList= new ArrayList();
		uplist = new ArrayList();
		upFileQuery = new EbenUpFileAuth(ss.getName(), directory, temp,ess.getAppSource());
		ArrayList<EdiskLuidInfo> newTable = new ArrayList<EdiskLuidInfo>();
		ArrayList<EdiskLuidInfo> updateTable = new ArrayList<EdiskLuidInfo>();
		int count = 0;
		Log.debug(TAG_LOG,"upFileQuery");
		
		upItems(newluidinfo,newTable, updateTable);
		upItems(updateluidinfo,newTable, updateTable);

		newluidinfo = newTable;
		updateluidinfo = updateTable;		
		uplist.addAll(localList);
		
	}
	/**
	 * 
	 * @throws SyncException
	 */
	public void upAllFileQuery() throws SyncException {
		Log.debug(TAG_LOG,"upAllFileQuery");
		
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		String directory = ess.getDirectory();
		String temp = ess.getTempDirectory();
//		String fileName;
		boolean hasChanged = false;
		localList= new ArrayList();
		uplist = new ArrayList();
		
		upFileQuery = new EbenUpFileAuth(ss.getName(), directory, temp,ess.getAppSource());
		ArrayList<EdiskLuidInfo> newTable = new ArrayList<EdiskLuidInfo>();
		ArrayList<EdiskLuidInfo> updateTable = new ArrayList<EdiskLuidInfo>();
		int count = 0;
		
		upItems(luidinfo,null, updateTable);

		luidinfo = updateTable;
		
		uplist.addAll(localList);
		
	}
	/**
	 * request to upfile auth and upfile 
	 * @param changedluid
	 * @param newTable 
	 * @param updateTable 
	 * @param isnew 
	 * @throws Exception 
	 */
	private void upItems(ArrayList<EdiskLuidInfo> changedluid, ArrayList<EdiskLuidInfo> newTable, ArrayList<EdiskLuidInfo> updateTable) throws SyncException {
		// TODO Auto-generated method stub
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		String directory = ess.getDirectory();
		String temp = ess.getTempDirectory();
		int count = 0;
		if (!changedluid.isEmpty()) {
//			for (Iterator it = changedluid.entrySet().iterator(); it.hasNext();) 
			for(int i=0 ; i<changedluid.size();i++)
			{
//				Map.Entry entry = (Map.Entry) it.next();
//				String luid = (String) entry.getKey();
//				String md5 = (String) entry.getValue();
				EdiskLuidInfo inf = changedluid.get(i);
				String luid = inf.getLuid();
				String md5 = inf.getFp();
				String path = inf.getPath();
				String parent = inf.getParent();

//				String fileName = directory + EbenHelpers.decodeKey(luid);
//				String path =  EdiskLuidInfo.getLuidPath(luidinfo, luid);
				String fileName = directory +path;
				Log.debug(TAG_LOG, "upItems: fileName  " + fileName
						+ ",  : " + inf.toString());

				File file = new File(fileName);
				if (!file.exists()) {
					// a  new item
					if(null == inf.getUpdateOp()) {
						Log.error(TAG_LOG, "file not exists : do nothing  " + fileName);
						continue;
					}
					// not exist will resume from server
				}
				boolean emptyFile = false;
				if("".equalsIgnoreCase(md5)||!file.exists()||file.isDirectory() || 
						0 == file.length()||EdiskSyncSource.EmptyMd5.equalsIgnoreCase(md5)) {
					UpfileInfo item = new UpfileInfo(luid, md5, 0, "",path,parent);
					

					if(null == inf.getUpdateOp()&&  null != newTable) {

						newTable.add(inf);
					} else {
						updateTable.add(inf);
					}
					Log.debug(TAG_LOG, "add to loclist "+item.toString());
					localList.add(item);
//					if(it.hasNext()) 
					if(i<changedluid.size()-1)
					{
						continue;
					}else {
						emptyFile = true;
					}
				}
				if(!emptyFile) {
					long size = file.length();
					int parts = 0;
					if(size > EbenUpFile.PARTSIZE) {
						parts = (int) (size/EbenUpFile.PARTSIZE +1);
					}
//					upFileQuery.setList(luid, file.length(), md5, parts,path,parent);
					//lierbao
					upFileQuery.setList(luid, file.length(), md5, parts,path,parent);
					count++;
				}
//				hasChanged = true;
				if(count > EdiskSyncSource.MaxItemCount||i>=changedluid.size()-1) {
					count = 0;
//					hasChanged = false;
					if (upFileQuery.hasItems()) {
						try {
							upFileQuery.handler();
						} catch (SyncException syncex) {
							EbenFileLog.recordSyncLog("upFileQuery Exception: "+syncex.toString());
							throw syncex;
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							EbenFileLog.recordSyncLog("upFileQuery Exception: "+e.toString());
							throw new SyncException(0, e.getMessage());
						}
					}


					
					if (null != upFileQuery.getSuccessList() && upFileQuery.getSuccessList().size() > 0) {
						for (UpfileInfo info : upFileQuery.getSuccessList()) {
//							if (newItems.containsKey(info.luid)) {
//								newTable.put(info.luid, info.etag);
//							}
//							if (updatedItems.containsKey(info.luid)) {
//								updateTable.put(info.luid, info.etag);
//							}
							EdiskLuidInfo myinfo = containLuid(newluidinfo,info.luid);
							if(null != myinfo){
								Log.debug(TAG_LOG, "add to newtable,"+info.toString());
								newTable.add(myinfo);
							} else {
								myinfo = containLuid(updateluidinfo,info.luid);
								if(null != myinfo) {
									Log.debug(TAG_LOG, "add to updatetable,"+info.toString());
									updateTable.add(myinfo);
								} else {
									// for slow 201 sync
									myinfo = containLuid(luidinfo,info.luid);
									if(null != myinfo) {
										Log.debug(TAG_LOG, "201 ,add to updatetable,"+info.toString());
										updateTable.add(myinfo);
									}
								}
							}

						}
					}	
					uplist.addAll( upFileQuery.getSuccessList());
					upFileQuery = new EbenUpFileAuth(ss.getName(), directory, temp,ess.getAppSource());
					
				}
				
			}
			
			
		} else {
			Log.info(TAG_LOG, "no new items");
		}		
	}
/**
 * 
 * @param luidinfo
 * @param luid
 * @return
 */
	private EdiskLuidInfo containLuid( ArrayList<EdiskLuidInfo> luidinfo,String luid) {
		EdiskLuidInfo info = null;
		if(null == luidinfo) {
			return info;
		}
		
		for(EdiskLuidInfo inf: luidinfo) {
			if(inf.getLuid().equalsIgnoreCase(luid)) {
				info = inf;
				break;
			}
		}
		
		return info;
	}
	public void checkRenameList() throws TrackerException {
		if(AndroidConfiguration.authSyncInter)
			return ;
		EdiskSyncSource ess = (EdiskSyncSource) ss;
		String directory = ess.getDirectory();
		String fileName;
		Log.info(TAG_LOG, "checkRenameList");
		if (SyncSource.INCREMENTAL_SYNC != ss.getSyncMode()) {
			Log.info(TAG_LOG, "not fast ,return");
			return;
		}
		AndroidConfiguration config = App.i().getAppInitializer()
				.getConfiguration();
		String status = config.loadStringKey(ss.getName()
				+ AndroidConfiguration.KEY_MINDCLOUD_RENAME, "1");
		Log.info(TAG_LOG, "have renamed : " + status);
		if (!"0".equals(status)) {
			Log.info(TAG_LOG, "have renamed false, not check : " + status);
			return;
		}

		if (!deletedItems.isEmpty() && !newItems.isEmpty()) {
			boolean rename = false;
			ArrayList<String> oldList = new ArrayList<String>();
			ArrayList<String> newList = new ArrayList<String>();
			for (Iterator it = deletedItems.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String delLuid = (String) entry.getKey();
				String delfp = (String) entry.getValue();

				for (Iterator itNew = newItems.entrySet().iterator(); itNew
						.hasNext();) {
					Map.Entry entryNew = (Map.Entry) itNew.next();
					String newLuid = (String) entryNew.getKey();
					String newfp = (String) entryNew.getValue();

					if (newfp.equals(delfp)) {

						if (!newList.contains(newLuid)) {
							String del = EbenHelpers.decodeKey(delLuid);
							String add = EbenHelpers.decodeKey(newLuid);
							if (!del.contains(EdiskSyncSource.TEMPFILE)
									&& !add.contains(EdiskSyncSource.TEMPFILE)) {
								oldList.add(del);
								newList.add(add);

								rename = true;
							}

						}
					}
				}

			}

			if (rename) {
//				EbenSyncCmd.syncStart(ss.getName(), oldList, newList,
//						AppSyncService.MIN_INTERVAL);
				Log.error(TAG_LOG, "find rename item, should throw exception");
				throw new TrackerException("find rename list");
			}
		}

	}
	/**
	 * add the external items to sync item .  for page changed update book
	 * @param items item list
	 */
	public void mergeupdatedItems(Hashtable items) {
		Log.info(TAG_LOG, "mergeupdatedItems");
		if(null != items) {
			for (Iterator it = items.keySet().iterator(); it.hasNext();) {

				String key = (String) it.next();

				if(checkItemExist(key)) {
					Log.error(TAG_LOG, "find a same key : "+key);
					it.remove();
//					items.remove(key);
				}
			}
			updatedItems.putAll(items);
		}
	}
	/**
	 * get count for up list
	 * @return
	 */
	public int getAllUpCount() {
		
		// lierba temp
		return 0;
//		return getUpCount(newItems)+getUpCount(updatedItems);
	}
	public int getUpCount(Hashtable ht) {
		int count = 0;
//		if (null != ht) {
//			Iterator<String> iter = ht.keySet().iterator();
//
//			EdiskSyncSource ess = (EdiskSyncSource) ss;
//			String directory = ess.getDirectory();
//			while (iter.hasNext()) {
//
////				String name = EbenHelpers.decodeKey((String)iter.next());
//				String name = EdiskLuidInfo.getLuidPath(luidinfo, (String)iter.next());
//				File file = new File(directory + name);
//
//				if (!file.isDirectory() && file.length() > 0) {
//					count++;
//				}
//			}
//		}
		return count;
	}
	/**
	 *  for item resume
	 * @param item
	 */
	public void removeDelItem(String item) {
		deletedItems.remove(item);
	}

	public void resetNewItems() {
		Log.info(TAG_LOG, "reset newItems");
		newItems = new Hashtable();
	}

	public String getkeypath(String key) {
		// TODO Auto-generated method stub
		return EdiskLuidInfo.getLuidPath(mContext,ss.getName(), key);
	}
	/**
	 * 
	 */
	public boolean removeItem(EdiskLuidInfo item,char state) throws TrackerException {
		// TODO Auto-generated method stub

//		Log.debug(TAG_LOG,
//				"removeItem " + item.getKey() + ", " + item.getState());
	
		// for local interface status
		String sourceName = ss.getName();
//		Log.info(TAG_LOG, "source name is : " + sourceName);

//		String key = item.getKey();		
//		String pathname = getkeypath(key);
		updateStatusinfo(sourceName,item.getPath(),state);

		// notify item status
		// String deCode = EbenHelpers.decodeKey(key);
//		EdiskSyncSource eSource = (EdiskSyncSource) ss;
//		ExSyncManager.i().notifyItemResult(eSource.getAppSource(),
//				EbenHelpers.decodeKey(key), SyncSource.STATUS_SUCCESS,
//				ExSyncManager.SYNC_END, item.getState());
		return updateSyncedInfo(item,state);
//		return super.removeItem(item);

	}

	/**
	 * 
	 * @param pathname
	 * @param pathname2 
	 */
	public void removeAlldownchildern(String luid, String pathname) {
		// TODO Auto-generated method stub
		ContentResolver cr = mContext.getContentResolver();
		Uri uri = Uri.withAppendedPath(
				Uri.parse(SyncSourceProvider.AUTHORITY_URI),
				SyncSourceProvider.PRIFEX_DOWN + ss.getName());
		String[] projection = {SyncSourceProvider.NAME};
		if (pathname.endsWith(File.separator)) {
			Cursor cursor = cr.query(uri, projection, null, null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

				String path = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.NAME));
				if(path.startsWith(pathname)) {
					int colum = cr.delete(uri, SyncSourceProvider.NAME + "=?",
							new String[] { path });
					Log.debug(TAG_LOG, "del child" + ss.getName() + ", colum: " + colum
							+ ",pathname: " + path);
				}
			}
			cursor.close();
			
		} else {

			int colum = cr.delete(uri, SyncSourceProvider.NAME + "=?",
					new String[] { pathname });
			Log.debug(TAG_LOG, "del " + ss.getName() + ", colum: " + colum
					+ ",pathname: " + pathname);
		}
	}
}
