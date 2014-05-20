package cn.eben.android.net.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import cn.eben.android.net.HttpJSONService;
import cn.eben.android.net.HttpJSONServiceBase;
import cn.eben.android.net.HttpJSONserviceConst;
import cn.eben.android.providers.SyncSourceProvider;

import cn.eben.android.source.edisk.EdiskSyncSource;
import cn.eben.android.source.edisk.EdiskdbStore;
import cn.eben.android.util.DirComparator;
import cn.eben.android.util.EbenHelpers;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.App;
import com.funambol.client.source.AppSyncSource;
import com.funambol.storage.StringKeyValueSQLiteStore;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncItem;
import com.funambol.sync.SyncSource;
import com.funambol.util.Base64;
import com.funambol.util.Log;

public class EDiskHelper extends HttpJSONServiceBase {

	private final static String TAG_LOG = "EDiskHelper";
	private final String KEY_OLD = "oldname";
	private final String kEY_NEW = "newname";
	private final String KEY_PAIRS = "filepairs";
	private int op = -1;
	private static final String edisk_dbname = AndroidCustomization
			.getInstance().getFunambolSQLiteDbName();
	// private static EDiskHelper instance = new EDiskHelper();

	private HashMap<String, String> fMap; // key for old file name , value for
	// new file name
	private ArrayList<String> folderList; // this folder list for server rename
	// list ,delete after rename.
	private String directory;
	private String oldPath;
	private String newPath;

	private ArrayList<String> oldList;
	private ArrayList<String> newList;

	String userName;
	String deviceId;
	private String source;
	private AppSyncSource syncSource;

	public static long syncCount = 0;
	/** if source disable isok to false */
	public static boolean isok = true;

	// public static synchronized EDiskHelper i() {
	// return instance;
	// }
	// public static void updateRenamedFils(String directory) {
	// // TODO Auto-generated method stub
	// AndroidConfiguration config =
	// App.i().getAppInitializer().getConfiguration();
	// String oldFile = config.getEdiskOldName();
	// String newFile = config.getEdiskNewName();
	// Log.info(TAG_LOG,"updateRenamedFiles ,oldfile: "+oldFile+", newFile: "+newFile);
	//
	// ExternalEntryService es = ExternalEntryService.i();
	//
	// EDiskHelper httpEdisk = new
	// EDiskHelper(HttpJSONServiceBase.OP_EDISK_SET_RENAME, directory,
	// es.getOldList(), es.getNewList());
	//
	// try {
	// httpEdisk.handler();
	//
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	public static void updateRenamedFils(String directory, AppSyncSource source) {
		// TODO Auto-generated method stub
//		isok = true;
//		AndroidConfiguration config = App.i().getAppInitializer()
//				.getConfiguration();
//		String oldFile = config.getEdiskOldName();
//		String newFile = config.getEdiskNewName();
//		Log.info(TAG_LOG, "updateRenamedFiles ,oldfile: " + oldFile
//				+ ", newFile: " + newFile);
//
//		ExSyncManager es = ExSyncManager.i();
//
//		EDiskHelper httpEdisk = new EDiskHelper(
//				HttpJSONServiceBase.OP_EDISK_SET_RENAME, directory,
//				es.getOldList(), es.getNewList(), source);
//		// httpEdisk.syncSource = source;
//
//		try {
//			httpEdisk.handler();
//
//		} catch(SyncException synce) {
//			httpEdisk.notifSyncResult();
//			throw synce;
//		}
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	public EDiskHelper(String directory) {
		// super();
		init(directory);
	}

	public EDiskHelper(int op, String directory, ArrayList<String> oldList,
			ArrayList<String> newList) {
		super();
		this.op = op;
		this.directory = directory;
		this.oldList = oldList;
		this.newList = newList;
		init();
	}

	public EDiskHelper(int op, String directory, ArrayList<String> oldList,
			ArrayList<String> newList, AppSyncSource source) {
		super();
		this.op = op;
		this.directory = directory;
		this.oldList = oldList;
		this.newList = newList;
		this.source = source.getSyncSource().getName();
		this.syncSource = source;
		syncCount = 0;
		isLocal = true;
		init();
	}

	public void init() {
		userName = App.i().getAppInitializer().getUserName();
		deviceId = App.i().getAppInitializer().getConfiguration()
				.getDeviceConfig().getDevID();
		Log.info(TAG_LOG, "init dirctory: " + directory + ", username: "
				+ userName + ", deviceId:" + deviceId);
		resetFileMap();
		if (haveRenameList()) {
			for (int i = 0; i < oldList.size(); i++) {
				oldPath = oldList.get(i);
				newPath = newList.get(i);

				if(newPath.startsWith(directory)) {
					newPath = newPath.replace(directory, "");
				}
				if(oldPath.startsWith(directory)) {
					oldPath = oldPath.replace(directory, "");
				}
				initFilesMap(oldPath, newPath);
			}
//			ExSyncManager.i().showSyncNotification(
//					App.i().getApplicationContext(), syncSource,
//					ExSyncManager.SYNC_START);
		}
	}

	private boolean haveRenameList() {
		if (oldList != null && newList != null && oldList.size() > 0
				&& newList.size() > 0 && oldList.size() == newList.size()) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("static-access")
	private void initFilesMap(String oPath, String nPath) {
		// TODO Auto-generated method stub
		if (null == oPath || null == nPath) {

			return;
		}
		if (oPath.equals("") || nPath.equals("")) {

			return;
		}
		// endwith . not as rename, 
		if(oPath.endsWith(".") || nPath.endsWith(".")) {
			Log.error(TAG_LOG, "error : old: " + oPath + ", new: " + nPath);
			return;
		}
		Log.info(TAG_LOG, "old: " + oPath + ", new: " + nPath);

		String absNew = directory + nPath;
		File newFile = new File(absNew);
		Log.info(TAG_LOG, "file :" + absNew);
		if (!newFile.exists()) {
			Log.info(TAG_LOG, "new file not exist");
			return;
		}
		if(newFile.getName().startsWith("~")) {
			Log.error(TAG_LOG, "ignore ~ file");
			return;		
		}
//		ExSyncManager.i().notifyItemAction(syncSource, newPath, -1,
//				ExSyncManager.SYNC_START);
		if (newFile.isDirectory()) {
			Log.info(TAG_LOG, "is directory :" + nPath);
			if (AndroidConfiguration.authSyncInter) {
				if(!oPath.endsWith(File.separator)) {
					oPath += File.separator;
					nPath += File.separator;
				}
				addFilePairs(oPath, nPath);
			} else {
				getAllFiles(newFile);
			}
		} else {
			Log.info(TAG_LOG, "is file ");
			addFilePairs(oPath, nPath);
		}
	}

	private void getAllFiles(File dir) {
		// TODO Auto-generated method stub
		File[] files = dir.listFiles();
		for (int x = 0; x < files.length; x++) {
			if (files[x].isDirectory()) {
				getAllFiles(files[x]);
			} else {
				String name = files[x].getAbsolutePath();
				Log.info(TAG_LOG, "file :" + name);
				// directory: /mnt/sdcard/mydoc/ , name:
				// /mnt/sdcard/mydoc/newpath/file.doc
				String newName = name.substring(directory.length()); // newname:
				// newpath/file.doc
				String oldName = oldPath + newName.substring(newPath.length()); // oldname:
				// oldpath/file.doc

				addFilePairs(oldName, newName);
			}
		}
	}

	/** for new version, count all directory files */
	private void renameAllFilesDb(File dir) {
		// TODO Auto-generated method stub
		// if(dir.isDirectory()) {
		// EdiskdbStore.reNamedbStore(App.i().getApplicationContext(), source,
		// EbenHelpers.encodeKey(oldName),
		// EbenHelpers.encodeKey(newName));
		// // renameSourceDb(oldName, newName);
		// updateStatusDb(EbenHelpers.encodeKey(oldName),
		// EbenHelpers.encodeKey(newName));
		// }
		File[] files = dir.listFiles();
		for (int x = 0; x < files.length; x++) {
			String name = files[x].getAbsolutePath();
			Log.info(TAG_LOG, "renameAllFilesDb : file :" + name);
			// directory: /mnt/sdcard/mydoc/ , name:
			// /mnt/sdcard/mydoc/newpath/file.doc
			String newName = name.substring(directory.length()); // newname:
			// newpath/file.doc
			String oldName = oldPath + newName.substring(newPath.length()); // oldname:
			// oldpath/file.doc

			if (files[x].isDirectory()) {
				oldName = oldName + File.separator;
				newName = newName + File.separator;
			}
			String on = EbenHelpers.encodeKey(oldName);
			String nn = EbenHelpers.encodeKey(newName);

			EdiskdbStore.reNamedbStore(App.i().getApplicationContext(), source,
					on, nn);
			// renameSourceDb(oldName, newName);
			updateStatusDb(on, nn);
			if (files[x].isDirectory()) {
				renameAllFilesDb(files[x]);
			} else {
				// String name = files[x].getAbsolutePath();
				// Log.info(TAG_LOG, "file :" + name);
				// // directory: /mnt/sdcard/mydoc/ , name:
				// // /mnt/sdcard/mydoc/newpath/file.doc
				// String newName = name.substring(directory.length()); //
				// newname:
				// // newpath/file.doc
				// String oldName = oldPath +
				// newName.substring(newPath.length()); // oldname:
				// // oldpath/file.doc
				//
				// EdiskdbStore.reNamedbStore(App.i().getApplicationContext(),
				// source, oldName, newName);
				// // renameSourceDb(oldName, newName);
				// updateStatusDb(oldName, newName);
			}
		}
	}

	public void init(String directory) {

		init();
		setDirectory(directory);
		Log.info(TAG_LOG, "init dirctory: " + directory + ", username: "
				+ userName + ", deviceId:" + deviceId);
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void resetFileMap() {
		fMap = null;
	}

	public void addFilePairs(String oldFile, String newFile) {
		if (oldFile.equals(newFile))
			return;
		if (null == fMap)
			fMap = new HashMap<String, String>();
		Log.info(TAG_LOG, "add oldFile: " + oldFile + ", newFile:" + newFile);
		// ExternalEntryService.notifyItemAction(newFile, -1,
		// ExternalEntryService.SYNC_START); // notify start sync
		String enOld = new String(Base64.encode(oldFile.getBytes()));
		String edNew = new String(Base64.encode(newFile.getBytes()));
		Log.info(TAG_LOG, "base64 encode :   oldFile: " + enOld + ", newFile:"
				+ edNew);
		// old luid must be in synced status list ,and new luid must not in
		// status.
		if (isValidLuid(enOld, edNew)) {
			fMap.put(enOld, edNew);
		}

	}

	// the old luid should in sync edisk db , and new Luid not in edisk db
	private boolean isValidLuid(String oldLuid, String newLuid) {
		boolean bRet = true;
		StringKeyValueSQLiteStore ediskdb = null;
		try {
			ediskdb = new StringKeyValueSQLiteStore(
					App.i().getApplicationContext(), edisk_dbname, source);
			if (null == ediskdb.get(oldLuid)) {
				Log.info(TAG_LOG, "not find old luid " + oldLuid);
				bRet = false;
			} else if (null != ediskdb.get(newLuid)) {
				Log.info(TAG_LOG, "find  find new luid , do not rename : "
						+ newLuid);
				bRet = false;
			}
			Log.info(TAG_LOG, "isValidLuid return : " + bRet);

			ediskdb.finalize();
			ediskdb = null;
		} catch (Exception e) {
			e.printStackTrace();
			if(null != ediskdb) {
				ediskdb.finalize();
			}
			throw new SyncException(223, "check db error ");
//			ExSyncManager.i().exitApp(e);
		}
		return bRet;
	}

	// private void renamedSourceDb(String )
	public void addFolder(String name) {
		if (null == folderList)
			folderList = new ArrayList<String>();
		if (!folderList.contains(name)) {
			Log.info(TAG_LOG, "addFolder :" + name);
			folderList.add(name);
		} else {
			Log.info(TAG_LOG, "folder exist :" + name);
		}
	}

	public void resetFolderList() {
		Log.info(TAG_LOG, "resetFolderList");
		folderList = null;
	}

	// {"ver":1,"username":"<userid>","device":"<deviceid>","op":"getsynclist","param":{
	// "luidsync":[
	// {"app":"notepad","luid":[{"old":"<oldluid>","new":"newluid"},]},
	// ],
	// "apps":["notepad","writer","drawer",]}

	// {"device":"G2A22EEBSA000699","username":"15201598945",
	// "ver":1,"op":"getsynclist",
	// "param":{"luidsync":[{"luid":[],"app":"edisk","apps":["edisk"]}]}}
	/** set renamed request new version */
	public JSONObject setLocalLuid() {
		JSONObject jObject = new JSONObject();
		JSONObject jLuid = new JSONObject();
		JSONArray jArray = new JSONArray();

		try {
			jObject.put("ver", JSONVERSION);
			jObject.put("username", userName);
			jObject.put("device", deviceId);
			jObject.put("op", "getsynclist");

			if (null != fMap && fMap.size() > 0) {

				Iterator<String> iter = fMap.keySet().iterator();

				while (iter.hasNext()) {
					String oldName = iter.next();
					String newName = fMap.get(oldName);
					JSONObject jo = new JSONObject();

					jo.put("old", oldName);
					jo.put("new", newName);
					jArray.put(jo);
				}
			}
			jLuid.put("luid", jArray);

			// JSONObject jLuidsync = new JSONObject();
			jLuid.put("app", source);
			// jLuidsync.put("app",source);
			JSONArray js = new JSONArray();
			js.put(jLuid);

			JSONObject jLuidsync = new JSONObject();
			jLuidsync.put("luidsync", js);
			jLuidsync.put("apps", (new JSONArray()).put(source));
			// JSONObject jPa = new JSONObject();

			jObject.put("param", jLuidsync);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jObject;
	}

	// {"data":{"source":"enote_notepad","device":"G2A22SABSA000465",
	// "username":"18701675064","filepairs":[]},"op":400}

	// set old file and new file pairs
	// this json request to server .
	public JSONObject setFileRename() {
		JSONObject jObject = new JSONObject();
		JSONObject jData = new JSONObject();
		JSONArray jArray = new JSONArray();
		try {
			jData.put("device", deviceId);
			jData.put("username", userName);
			jData.put("source", source);
			if (null != fMap && fMap.size() > 0) {

				Iterator<String> iter = fMap.keySet().iterator();

				while (iter.hasNext()) {
					String oldName = iter.next();
					String newName = fMap.get(oldName);
					JSONObject jo = new JSONObject();

					jo.put(KEY_OLD, oldName);
					jo.put(kEY_NEW, newName);
					jArray.put(jo);
				}
			}
			jData.put(KEY_PAIRS, jArray);

			jObject.put("op", HttpJSONService.OP_EDISK_SET_RENAME);
			jObject.put("data", jData);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jObject;
	}

	private void notifSyncResult() {
		if (null != newList && null != oldList) {
			for (String name : newList) {
				if(name.startsWith(directory)) {
					name = name.replace(directory, "");
				}
//				ExSyncManager.i().notifyItemAction(syncSource, name,
//						SyncSource.SUCCESS_STATUS, ExSyncManager.SYNC_END);
			}
		}
	}
	// {"code":0, "result":"ok","data":{
	// "luidsync":[
	// {"app":"notepad","faillist":["<oldluid>","<oldluid>",],"luid":[{"old":"<oldluid>","new":"newluid"},
	// ],
	// "synclist":[
	// {"app":"notepad","list":[
	// {"luid":"enote_notepad/book1/8 +HZcYrIH.note","op":"a","cver":1330944006000,
	// "size":1234, "md5":"<content-md5>"},
	// ]},

	// response:
	// {"result":"ok","data":{"synclist":[{"result":"Application disabled.","app":"enote_notepad","list":[],"code":5634}],
	// "luidsync":[{"luid":[],"app":"enote_notepad","result":"Application disabled.",
	// "code":5634,"faillist":[]}]},"code":0}
	private boolean isLocal = true;
	
	

/**
 * 	parse server renamed response ,for new version 
 * @param json server response
 */
	public void processResult(JSONObject json) {
		notifSyncResult();

		try {
			int result = json.getInt("code");
			if (0 != result)
				return;
			JSONObject data = json.getJSONObject("data");
			JSONArray luid = data.getJSONArray("luidsync");
			for (int i = 0; i < luid.length(); i++) {
				JSONObject jo = luid.getJSONObject(i);
				if (5634 == jo.getInt("code")) {
					Log.error(TAG_LOG, "source disable !!!");
					isok = false;
					return;
				}
				if (jo.getString("app").equalsIgnoreCase(source)) {
					JSONArray fail = jo.getJSONArray("faillist");
					removeFailItems(fail);
					isLocal = true;
					setLocalStatus();
					isLocal = false;
					processSyncListReslut(json);
				}
			}
			// server a d u list
			JSONArray syncLuid = data.getJSONArray("synclist");
			for (int j = 0; j < syncLuid.length(); j++) {
				JSONObject ob = syncLuid.getJSONObject(j);
				if (ob.getString("app").equalsIgnoreCase(source)) {
					JSONArray list = ob.getJSONArray("list");
					syncCount = list.length();
					if(syncCount > 0) {
//						new EbenSyncProgress(1,syncCount,1,syncSource.getSyncSource().getName(),syncSource.getName(),"",0).send();
//						//lierbao 20130608
//						EbenExsyncProgress prg = new EbenExsyncProgress(1, syncCount, 1, syncSource.getSyncSource()
//								.getName(), syncSource.getName(), "", 0);
//						prg.setstage("begin");
//						prg.send();
						
					}
					Log.info(TAG_LOG, "sync count : " + syncCount);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// set sync data base status if server ok
	// {"op":400,"data":{"status":"0","success":["<oldname1>","<oldname2>",...],"fail":["<oldname1>","<oldname2>",...],"filepairs":[]}}
	//
	/** for old interface */
	public void processSetResult(JSONObject json) {
		String status = "1"; // default set to fail rename
		Log.info(TAG_LOG, "processSetResult");
		try {
			status = json.getString("status");
			switch (Integer.parseInt(status)) {
			case 0: // for all renamed ok
				// if (null != newPath && null != oldPath) {
				// ExSyncManager.i().notifyItemAction(syncSource, newPath,
				// SyncSource.SUCCESS_STATUS, ExSyncManager.SYNC_END);
				// }
				if (null != newList && null != oldList) {
					for (String name : newList) {
						if(name.startsWith(directory)) {
							name = name.replace(directory, "");
						}
//						ExSyncManager.i().notifyItemAction(syncSource, name,
//								SyncSource.SUCCESS_STATUS,
//								ExSyncManager.SYNC_END);
					}
				}
				setLocalStatus();
				// update server renamed files
				if (AndroidConfiguration.authSyncInter) {
					processSyncListReslut(json);
				} else {
					processGetReslut(json);
				}
				break;
			case 2: // for partial success ,
				Log.info(TAG_LOG, "server renamed partial failed !!!!!  ");
				// JSONArray success = json.getJSONArray("success");
				JSONArray fail = json.getJSONArray("fail");
				removeFailItems(fail);

				setLocalStatus();
				processGetReslut(json);
				break;
			default:
//				if (null != newPath && null != oldPath)
//					ExSyncManager.i().notifyItemAction(syncSource, newPath,
//							SyncSource.ERROR_STATUS, ExSyncManager.SYNC_END);
				Log.info(TAG_LOG,
						"server renamed failed, rename will be handle in sync ,like add /new. ");
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			AndroidConfiguration config = App.i().getAppInitializer()
					.getConfiguration();
			config.saveStringKey(syncSource.getSyncSource().getName()
					+ AndroidConfiguration.KEY_MINDCLOUD_RENAME, status);
			config.commit();
			resetFileMap();
		}

	}

	/** remove renamed failed items */
	private void removeFailItems(JSONArray fail) {
		try {

			// TODO Auto-generated method stub
			for (int i = 0; i < fail.length(); i++) {
				Log.info(TAG_LOG, "fail: " + fail.getString(i));
				String oldName;
				oldName = fail.getString(i);

				if (null != fMap && fMap.size() > 0) {
					if (fMap.containsKey(oldName)) {
						fMap.remove(oldName);
						// String newName = fMap.get(oldName);
						// Log.info(TAG_LOG, "set sync Data Base oldName: "
						// + oldName + ", newName: " + newName);
						// EdiskdbStore.reNamedbStore(App.i()
						// .getApplicationContext(), oldName, newName);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** update local sync db , for renamed in both version */
	private void setLocalStatus() {
		// TODO Auto-generated method stub
		Log.info(TAG_LOG, "server renamed ok, update sync db");

		if (null != fMap && fMap.size() > 0) {
			Iterator<String> iter = fMap.keySet().iterator();

			while (iter.hasNext()) {
				String oldName = iter.next();
				String newName = fMap.get(oldName);

				Log.info(TAG_LOG, "set sync Data Base oldName: " + oldName
						+ ", newName: " + newName);
				String sname = EbenHelpers.decodeKey(newName);
				String oname = EbenHelpers.decodeKey(oldName);
				if (sname.endsWith(File.separator)
						&& oname.endsWith(File.separator)) {
					Log.info(TAG_LOG, "is a directory rename ok");
					renameDirStore(oname, sname);
					// updateStatusDirDb(oname, sname);
				} else {
					EdiskdbStore.reNamedbStore(App.i().getApplicationContext(),
							source, oldName, newName);
					// renameSourceDb(oldName, newName);
					updateStatusDb(oldName, newName);
				}

				// ExternalEntryService.notifyItemAction(EbenHelpers.decodeKey(newName),
				// SyncSource.SUCCESS_STATUS, ExternalEntryService.SYNC_END);
			}
		}
	}

	// private void updateStatusDirDb(String oldName, String newName) {
	// // TODO Auto-generated method stub
	// File dir = new File(directory + newName);
	// if (!dir.exists() || !dir.isDirectory()) {
	// Log.error(TAG_LOG, "data error !!");
	// return;
	// }
	// oldPath = oldName;
	// newPath = newName;
	// updateStatusDb(oldName, newName);
	//
	// }
	/***/
	private void renameDirStore(String oldName, String newName) {
		// TODO Auto-generated method stub

		File dir = new File(directory + newName);
		if (!dir.exists() || !dir.isDirectory()) {
			Log.error(TAG_LOG, "data error !!");
			return;
		}
		oldPath = oldName;
		newPath = newName;
		// if(dir.isDirectory()) {
		EdiskdbStore.reNamedbStore(App.i().getApplicationContext(), source,
				EbenHelpers.encodeKey(oldName), EbenHelpers.encodeKey(newName));
		// renameSourceDb(oldName, newName);
		updateStatusDb(EbenHelpers.encodeKey(oldName),
				EbenHelpers.encodeKey(newName));
		// }
		renameAllFilesDb(dir);

	}

	/** status db list for ui show */
	private void updateStatusDb(String oldName, String newName) {
		// TODO Auto-generated method stub
		ContentResolver cr = App.i().getApplicationContext()
				.getContentResolver();

		// Uri uri = Uri.parse("content://cn.eben.provider.syncStatus/edisk/");
//		Uri uri = Uri.parse("content://cn.eben.provider.syncStatus/" + source
//				+ "/");
		
//		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + SyncSourceProvider.PRIFEX_STATUS+ source
//		+ "/");		
//		String deOld = EbenHelpers.decodeKey(oldName);
//		if (null != deOld) {
//			deOld.replaceAll(SyncSourceProvider.PNT,
//					SyncSourceProvider.PNTTOKEN); // lierbao
//			cr.delete(uri, "_key = ?", new String[] { deOld });
//		}
//		Cursor cs = cr.query(uri, new String[] { "_key", "_value" },
//				"_key = ?", new String[] { EbenHelpers.decodeKey(newName) },
//				null);
//		String deNew = EbenHelpers.decodeKey(newName);
//		if (null != deNew) {
//			deNew.replaceAll(SyncStatusProvider.PNT,
//					SyncStatusProvider.PNTTOKEN); // lierbao
//			if (null == cs || 0 == cs.getCount()) {
//				ContentValues values = new ContentValues();
//
//				values.put("_key", deNew);
//				values.put("_value", "0");
//
//				cr.insert(uri, values);
//			} else {
//				ContentValues values = new ContentValues();
//
//				values.put("_key", newName);
//				values.put("_value", "0");
//				cr.update(uri, values, "_key = ?", new String[] { deNew });
//			}
//		}
//
//		if (isLocal) {
//			add2update(newName);
//		} else {
//			removeUpdate(oldName);
//		}
	}

	private void removeUpdate(String oldName) {
		// TODO Auto-generated method stub
		if(null != syncSource) {
			if(syncSource.getSyncSource() instanceof EdiskSyncSource) {
				EdiskSyncSource edisk = (EdiskSyncSource) syncSource.getSyncSource();
				edisk.removeUpdate(oldName);
			}
			
		}
	}

/** set local rename list to update list, should opration at sync ml process*/
	private void add2update(String newName) {
		// TODO Auto-generated method stub
		
//		if(null != syncSource) {
//			if(syncSource.getSyncSource() instanceof EdiskSyncSource) {
//				EdiskSyncSource edisk = (EdiskSyncSource) syncSource.getSyncSource();
//				edisk.add2update(newName);
//				edisk.addExFolder(newName);
//			}
//			
//		}

	}

	// {"code":0, "result":"ok","data":{
	// "luidsync":[
	// {"app":"notepad","faillist":["<oldluid>","<oldluid>",],"luid":[{"old":"<oldluid>","new":"newluid"},]},
	// ],
	// "synclist":[
	// {"app":"notepad","list":[
	// {"luid":"enote_notepad/book1/8 +HZcYrIH.note","op":"a","cver":1330944006000,
	// "size":1234, "md5":"<content-md5>"},
	// ]},

	// "luidsync":[{"luid":[{"new":"MDAyLw==","old":"MDAxLw=="}],"app":"edisk","faillist":[]}]},"code":0}
	// {"result":"ok","data":{"synclist":[{"app":"edisk","list":[{"luid":"MjAxMi0xMS0wNiAxNDM1MTUubG9n","cver":1352689258714,"op":"u","md5":"768FECEEF866102B7A149DD917E2A5FA","size":11041338},{"luid":"5LiA5Liq5pyJ6Laj55qE5Lmm5rOV5ZKM5raC6bim5bel5YW3LmFwaw==","cver":1352689258714,"op":"u","md5":"A8664096E7CC6C4B7AE306009680A597","size":406033}]}],
	// "luidsync":[{"luid":[{"new":"MTAwMi8=","old":"MTAwMS8="}],"app":"edisk","faillist":[]}]},"code":0}

	// "luidsync":[{"luid":[],"app":"edisk","faillist":[]}]},"code":0}
	/** set renamed list from server, for new version */
	public void processSyncListReslut(JSONObject jo) {
		

		try {
			Log.info(TAG_LOG, "processSyncListReslut :"+jo);
			JSONObject jdata = jo.getJSONObject("data");
			JSONArray jarray = jdata.getJSONArray("luidsync");

			for (int i = 0; i < jarray.length(); i++) {
				if (source.equalsIgnoreCase(jarray.getJSONObject(i).getString(
						"app"))) {
					JSONArray jLuid = jarray.getJSONObject(i).getJSONArray(
							"luid");
					for (int j = 0; j < jLuid.length(); j++) {
						JSONObject jObject = jLuid.getJSONObject(j);
						String oldName = jObject.getString("old");
						String newName = jObject.getString("new");

						String deoldName = EbenHelpers.decodeKey(oldName);
						String denewName = EbenHelpers.decodeKey(newName);
						Log.info(TAG_LOG, " de oldfile: " + deoldName
								+ ", de newfile: " + denewName);
						if (deoldName.endsWith(File.separator)
								&& denewName.endsWith(File.separator)) {
							Log.info(TAG_LOG, "it is a directory rename list");
							if (updateDirName(deoldName, denewName)) {
//								ExSyncManager.i().notifyItemResult(syncSource, EbenHelpers.decodeKey(oldName),
//										SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END,
//										SyncItem.STATE_DELETED);
								renameDirStore(deoldName, denewName);


//								ExSyncManager.i().notifyItemResult(syncSource,
//										EbenHelpers.decodeKey(newName), SyncSource.SUCCESS_STATUS,
//										ExSyncManager.SYNC_END, SyncItem.STATE_NEW);
							}
						} else {
							if (updateFileName(oldName, newName)) {
								setSyncStatus(oldName, newName);
							}
						}
						// delet key that in down ,may for foder need more care .for later care it
						String sourceName = syncSource.getSyncSource().getName();
						ContentResolver cr = App.i().getContentResolver();

						Uri uri = Uri.withAppendedPath(
								Uri.parse(SyncSourceProvider.AUTHORITY_URI),
								SyncSourceProvider.PRIFEX_DOWN+sourceName);
						
						int colum = cr.delete(uri, SyncSourceProvider.NAME+"=?", new String[]{deoldName});
						Log.info(TAG_LOG, "del "+sourceName+", colum: "+colum+",key: "+deoldName);
						// delete end

					}
				}
			}
			// JSONArray jList = jo.getJSONArray("synclist");
			// for(int i=0;i<jList.length();i++) {
			// if(source.equalsIgnoreCase(jList.getJSONObject(i).getString("app"))){
			// JSONArray jLuid = jList.getJSONObject(i).getJSONArray("list");
			// break;
			// }
			// }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** for both version */
	private void setSyncStatus(String oldName, String newName) {
		// TODO Auto-generated method stub
		if (isValidLuid(oldName, newName)) {
			EdiskdbStore.reNamedbStore(App.i().getApplicationContext(), source,
					oldName, newName);

			// renameSourceDb(oldName, newName);
			updateStatusDb(oldName, newName);
//			ExSyncManager.i().showSyncNotification(
//					App.i().getApplicationContext(), syncSource,
//					ExSyncManager.SYNC_START);
//			ExSyncManager.i().notifyItemResult(syncSource, EbenHelpers.decodeKey(oldName),
//					SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END,
//					SyncItem.STATE_DELETED);
//
//			ExSyncManager.i().notifyItemResult(syncSource,
//					EbenHelpers.decodeKey(newName), SyncSource.SUCCESS_STATUS,
//					ExSyncManager.SYNC_END, SyncItem.STATE_NEW);
		}
	}

	/** parse the renamed file list from DS server , for old versin */
	public void processGetReslut(JSONObject jo) {
		Log.info(TAG_LOG, "processGetResult");

		try {
			JSONArray jPairs = jo.getJSONArray(KEY_PAIRS);
			for (int i = 0; i < jPairs.length(); i++) {
				JSONObject jObject = jPairs.getJSONObject(i);
				String oldName = jObject.getString(KEY_OLD);
				String newName = jObject.getString(kEY_NEW);
				Log.info(TAG_LOG, "oldfile: " + oldName + ", newfile: "
						+ newName);
				if (updateFileName(oldName, newName)) {
					setSyncStatus(oldName, newName);
					// if (isValidLuid(oldName, newName)) {
					// EdiskdbStore.reNamedbStore(App.i()
					// .getApplicationContext(), source, oldName,
					// newName);
					//
					// // renameSourceDb(oldName, newName);
					// updateStatusDb(oldName, newName);
					// ExSyncManager.i().showSyncNotification(
					// App.i().getApplicationContext(), syncSource,
					// ExSyncManager.SYNC_START);
					//
					// ExSyncManager.i().notifyItemAction(syncSource, newName,
					// SyncSource.SUCCESS_STATUS, ExSyncManager.SYNC_END);
					// }

				} else {
					Log.info(TAG_LOG, "update file name failed !!");
				}
			}
			// if not, the old folder remained ,and will be upload at next sync
			// operation.
			removeOldFolders(directory, folderList);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resetFileMap();
		}

	}

	// class DirComparator implements Comparator<String> {
	//
	// public int compare(String name1, String name2) {
	// // TODO Auto-generated method stub
	// String[] s1 = name1.split("/");
	// String[] s2 = name2.split("/");
	//
	// return (s2.length - s1.length);
	// }
	//
	// }

	// for server folder rename, the client old folder should be deleted
	public static void removeOldFolders(String dir, ArrayList<String> list) {
		// TODO Auto-generated method stub
		if (null == list)
			return;
		if (list.isEmpty())
			return;
		// sort , sub dir , parent dir
		Collections.sort(list, new DirComparator());

		// for (String name : list) {
		// File folder = new File(dir + name);
		// if (folder.exists()) {
		// Log.info(TAG_LOG,
		// " folder: " + name + "dir length "
		// + folder.listFiles().length);
		// if (folder.exists() && 0 == folder.listFiles().length) {
		// Log.info(TAG_LOG, "remove folder: " + name);
		// folder.delete();
		// list.remove(name);
		// }
		// }
		// }

		Iterator<String> iter = list.iterator();

		while (iter.hasNext()) {
			String name = iter.next();
			File folder = new File(dir + name);
//			Log.info(TAG_LOG,
//					" folder: " + name + "dir length "
//							+ folder.listFiles().length);
			if (folder.exists() && 0 == folder.listFiles().length) {
				Log.info(TAG_LOG, "remove folder: " + name);
				if(folder.delete()) {
				iter.remove();
				}
			}
		}
	}

	private boolean updateDirName(String oldName, String newName) {
		boolean bRet = false;
		Log.info(TAG_LOG, "updateDirName, oldName: " + oldName + ", newName:"
				+ newName);
		String absOldName = directory + oldName;
		String absNewName = directory + newName;

		File oldFile = new File(absOldName);
		File newFile = new File(absNewName);
		if (oldFile.exists()) {
			if (newFile.exists()) {
				Log.info(TAG_LOG, "file " + absNewName
						+ " exist , rename it to conflict!!");
				if (newFile.renameTo(new File(absNewName + "conflict"))) {
					Log.info(TAG_LOG, "rename to confilict ok");
				}
			}

			String dir = absNewName.substring(0,
					absNewName.lastIndexOf(File.separator));
			dir = dir.substring(0, dir.lastIndexOf(File.separator));

			File file = new File(dir);
			if (!file.exists())
				file.mkdirs();
			if (oldFile.renameTo(new File(absNewName))) {
				Log.info(KEY_OLD, "rename " + oldName + " to " + newName);
				bRet = true;
			} else {
				Log.error(TAG_LOG, "rename failed ");
			}

		} else {
			Log.info(TAG_LOG, "file not exists :" + absOldName);
			bRet = true;
		}

		return bRet;
	}

	// after got rename list from server
	// rename files, and update sync database status.
	private boolean updateFileName(String oldName, String newName) {
		// TODO Auto-generated method stub
		boolean bRet = false;
		Log.info(TAG_LOG, "updateFileName, oldName: " + oldName + ", newName:"
				+ newName);
		// String deOld = new String(Base64.decode(oldName.getBytes()));
		// String deNew = new String(Base64.decode(newName.getBytes()));
		String deOld = EbenHelpers.decodeKey(oldName);
		String deNew = EbenHelpers.decodeKey(newName);

		Log.info(TAG_LOG, "base64 decode, oldName: " + deOld + ", newName:"
				+ deNew);

		String absOldName = directory + deOld;
		String absNewName = directory + deNew;

		Log.info(TAG_LOG, "updateFileName, oldName: " + absOldName
				+ ", newName:" + absNewName);

		File oldFile = new File(absOldName);
		File newFile = new File(absNewName);

		if (oldFile.exists()) {
			if (newFile.exists()) {
				Log.info(TAG_LOG, "file " + absNewName
						+ " exist , rename it to conflict!!");
				if (newFile.renameTo(new File(absNewName + ".conflict"))) {
					Log.info(TAG_LOG, "rename to confilict ok");
				}
			}
			Log.info(TAG_LOG, "file exists :" + absOldName);
			if (deNew.contains("/")) {
				String folder = directory
						+ deNew.substring(0, deNew.lastIndexOf("/"));// tt/yy
				File fileFolder = new File(folder);
				if (!fileFolder.exists()) {
					if (fileFolder.mkdirs()) {
						Log.info(TAG_LOG, "mkdir ok: " + folder);

					} else {
						Log.info(TAG_LOG, "mkdir failed: " + folder);
					}
				}
			}

			if (oldFile.renameTo(new File(absNewName))) {
				// cache renamed folder , it will be deleted .
				if (deOld.contains("/")) {
					String oldFolder = deOld.substring(0,
							deOld.lastIndexOf("/"));
					addFolder(oldFolder);
				}
				bRet = true;
			} else {
				Log.error(TAG_LOG, "rename failed ,do as add and del");
			}
		} else {
			Log.info(TAG_LOG, "file not exists :" + absOldName);
			bRet = true;
		}

		return bRet;
	}

	@Override
	public JSONObject processRequestJSON() throws Exception {
		// TODO Auto-generated method stub
		JSONObject jo = null;
		switch (getOp()) {
		case OP_EDISK_SET_RENAME:
			if (AndroidConfiguration.authSyncInter) {
				jo = setLocalLuid();
				Log.info(TAG_LOG, "rename: "+jo);
				// jo = setFileRename();
			} else {
				jo = setFileRename();
			}
			break;
		default:
			break;
		}
		return jo;
	}

	@Override
	public void processResponesJSON(String json) throws Exception {
		// TODO Auto-generated method stub
		Log.info(TAG_LOG, "renamed result: "+json);
		if (AndroidConfiguration.authSyncInter) {
			processResult(new JSONObject(json));
		} else {
			JSONObject jo = (new JSONObject(json)).getJSONObject("data");

			switch (getOp()) {
			case OP_EDISK_SET_RENAME:
				processSetResult(jo);
				break;
			default:
				break;
			}
		}

	}

	@Override
	public String getRequestURL() {
		// TODO Auto-generated method stub
		if (AndroidConfiguration.authSyncInter) {
			return DS_ECSI_URI;
		}
		return HttpJSONserviceConst.DS_URI;
	}

	public int getOp() {
		return op;
	}

	public void setOp(int op) {
		this.op = op;
	}

}
