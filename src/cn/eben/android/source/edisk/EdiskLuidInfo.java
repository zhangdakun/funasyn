package cn.eben.android.source.edisk;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


import cn.eben.android.providers.SyncSourceProvider;

import com.funambol.android.App;
import com.funambol.util.Log;

public class EdiskLuidInfo {
	public static final String TAG = "EdiskLuidInfo";
	public static final String ROOT = "0";
	private String luid;
	private String parent;
	// should a relative path
	private String path;
	private String fp;
	private String fullname;

	private String issynced;
	
	private String updateOp;

	public static String SYNCED = "OK";
	public static String SYNC_NOTUP = "NOTUP";
	public static String SYNC_NOTDOWN = "NOTDOWN";

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("luid: ").append(luid).append(",parent: ").append(parent)
				.append(",path: ").append(path).append(",fp: ").append(fp)
				.append(",issynced, ").append(issynced).append(", updaeOp , ").append(updateOp);

		return sb.toString();

	}

	
//	public EdiskLuidInfo() {
//		// super();
//		this.luid = null;
//		this.parent = null;
//		this.fp = null;
//		this.issynced = null;
//		this.path = null;
//
//		// TODO Auto-generated constructor stub
//	}

	public String getUpdateOp() {
		return updateOp;
	}


	public void setUpdateOp(String updateOp) {
		this.updateOp = updateOp;
	}


	public EdiskLuidInfo(String luid, String parent, String path, String fp) {
		this.luid = luid;
		this.parent = parent;
		this.path = path;
		this.fp = fp;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public void setissynced(String synced) {
		this.issynced = synced;
	}

	public String getissynced() {
		return issynced;
	}

	public String getLuid() {
		return luid;
	}

	public void setLuid(String luid) {
		this.luid = luid;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFp() {
		return fp;
	}

	public void setFp(String fp) {
		this.fp = fp;
	}

	/**
	 * 
	 * @param vector
	 * @param luid
	 * @return
	 */
	// public static String getLuidPath(ArrayList<EdiskLuidInfo> vector,String
	// luid) {
	// String path = null;
	//
	// for(EdiskLuidInfo info:vector) {
	// if(info.getLuid().equalsIgnoreCase(luid)) {
	// path = info.getPath();
	// }
	// }
	//
	// return path;
	//
	// }
	/**
	 * 
	 * @param vector
	 * @param luid
	 * @return
	 */
	public static String getLuidPath(Context context, String app, String luid) {
		String path = null;

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		String[] selectionArgs = { luid };

		Cursor cr = resolver.query(uri, null, selection, selectionArgs, null);

		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				path = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				break;
			}
		}
		cr.close();
		// for(EdiskLuidInfo info:vector) {
		// if(info.getLuid().equalsIgnoreCase(luid)) {
		// path = info.getPath();
		// }
		// }

		return path;

	}

	/**
	 * 
	 * @param vector
	 * @param luid
	 * @return
	 */
	public static String getLuidFullPath(Context context, String app,
			String luid) {
		String path = "";

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		// String[] selectionArgs = {luid};
		String[] projection = { SyncSourceProvider.PATH_COLUMN_NAME,
				SyncSourceProvider.PARENT_COLUMN_NAME };
		String parent = luid;
		String fullname = "";
		do {
			String[] selectionArgs = { parent };
			Cursor cr = resolver.query(uri, projection, selection,
					selectionArgs, null);

			if (null != cr && cr.getCount() > 0) {
				if (cr.moveToFirst()) {
					path = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
					parent = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.PARENT_COLUMN_NAME));
					fullname = path + fullname;
				}
			} else {
				Log.error(TAG, "error !!, not found full name," + luid);

				break;
			}
			cr.close();
		} while (!ROOT.equals(parent));

		// for(EdiskLuidInfo info:vector) {
		// if(info.getLuid().equalsIgnoreCase(luid)) {
		// path = info.getPath();
		// }
		// }

		return fullname;

	}

	/**
	 * 
	 * @param vector
	 * @param luid
	 * @return
	 */
	// public static String getParentluid(ArrayList<EdiskLuidInfo> vector,String
	// luid) {
	// String parent = null;
	//
	// for(EdiskLuidInfo info:vector) {
	// if(info.getLuid().equalsIgnoreCase(luid)) {
	// parent = info.getParent();
	// }
	// }
	//
	// return parent;
	//
	// }
	/**
	 * 
	 * @param luidinfo
	 * @param name
	 *            : a relative path
	 * @return
	 */
	public static String getfileParentluid(ArrayList<EdiskLuidInfo> luidinfo,
			String name) {
		String parent = null;
//		Log.debug(TAG, "getfileParentluid : " + name);
		// luidList.
		if (name.endsWith(File.separator)) {
			name = name.substring(0, name.length() - 1);
		}
		if (name.contains(File.separator)) {
			// if()
			String parentName = name.substring(0,
					name.lastIndexOf(File.separator) + 1);
			for (EdiskLuidInfo info : luidinfo) {
				if (parentName.equalsIgnoreCase(info.getPath())) {
					parent = info.luid;
				}
			}
			if (null == parent) {
				Log.error(TAG, "not find parent something error");
			}
		} else {
			parent = ROOT;
		}
		Log.debug(TAG, "getfileParentluid : " + name+", parent luid: " + parent);
		return parent;

	}

	/**
	 * 
	 * @param luidinfo
	 * @param name
	 * @return
	 */
//	public static String getParentluid(ArrayList<EdiskLuidInfo> luidinfo,
//			String name) {
//		String parent = ROOT;
//		Log.info(TAG, "getParentluid : " + name);
//		// luidList.
//		String item = name;
//		if (!item.contains(File.separator)) {
//			return parent;
//		}
//		if (item.endsWith(File.separator)) {
//			if (!item.contains(File.separator)) {
//				return parent;
//			}
//		}
//		item = name;
//		do {
//			String key = item.substring(0, item.indexOf(File.separator) + 1);
//			item = item.substring(key.length(), item.length());
//
//			for (EdiskLuidInfo info : luidinfo) {
//				if (key.equalsIgnoreCase(info.getPath())
//						&& parent.equalsIgnoreCase(info.getParent())) {
//					parent = info.luid;
//				}
//			}
//		} while (!item.contains(File.separator)
//				|| (item.endsWith(File.separator) && !item
//						.contains(File.separator)));
//
//		Log.info(TAG, "parent luid: " + parent);
//		return parent;
//
//	}

	public static boolean deleteyncedItem(Context context, String app,
			EdiskLuidInfo info) {
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		String[] selectionArgs = { info.getLuid() };

		// Cursor cr = resolver.query(uri, null, selection, selectionArgs,
		// null);
		resolver.delete(uri, selection, selectionArgs);

		return true;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param item
	 * @param syncedluidinfo
	 * @return
	 */
	public static boolean deleteyncedItemChild(Context context, String app,
			EdiskLuidInfo item, ArrayList<EdiskLuidInfo> syncedluidinfo) {
		Log.debug(TAG, "deleteyncedItemChild : "+item.toString());
		if(item.getParent().equalsIgnoreCase(item.getLuid())) {
			Log.error(TAG, "got a error key ,"+item.toString());
			EdiskLuidInfo.deleteyncedItem(context, app, item);
			return true;
		}
		if (item.getPath().endsWith(File.separator)) {
			for (EdiskLuidInfo info : syncedluidinfo) {
//				if (info.getPath().startsWith(item.getPath())) {
//					EdiskLuidInfo.deleteyncedItem(context, app, info);
//				}
				if(info.getParent().equalsIgnoreCase(item.getLuid())) {
//					EdiskLuidInfo child = new EdiskLuidInfo();
//					child.setLuid(item.getLuid());
//					child.setParent(item.getParent());
//					
					deleteyncedItemChild(context,app,info,syncedluidinfo);
				}
			}
		}
		
		EdiskLuidInfo.deleteyncedItem(context, app, item);

		

		return true;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param info
	 * @return
	 */
	public static boolean updateSyncedItem(Context context, String app,
			EdiskLuidInfo info) {
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		String[] selectionArgs = { info.getLuid() };
		
		Log.debug(TAG, "updateSyncedItem, "+info.toString());
		if(null == info.getLuid() ||
				null == info.getParent()||
				null == info.getPath() ||
				null == info.getFp()) {
			
			Log.error(TAG, "error in update table ,some value null ,"+info.toString());
		
			return false;
		}
		
		Cursor cr = resolver.query(uri, null, selection, selectionArgs, null);
		boolean isnew = true;
		if (null != cr && cr.getCount() > 0) {
			isnew = false;
		}
		// public final static String KEY_COLUMN_NAME = "_key";
		// public final static String VALUE_COLUMN_NAME = "_value";
		//
		// public final static String PATH_COLUMN_NAME = "_path";
		// public final static String PARENT_COLUMN_NAME = "_parent";
		//
		// public final static String SYNC_COLUMN_NAME = "_issync";
		ContentValues values = new ContentValues();
		if (null != info.getLuid()) {
			values.put(SyncSourceProvider.KEY_COLUMN_NAME, info.getLuid());
		}
		
		if (null != info.getFp()) {
			values.put(SyncSourceProvider.VALUE_COLUMN_NAME, info.getFp());
		}
		if (null != info.getPath()) {
			values.put(SyncSourceProvider.PATH_COLUMN_NAME, info.getPath());
		}
		if (null != info.getParent()) {
			values.put(SyncSourceProvider.PARENT_COLUMN_NAME, info.getParent());
		}
		if (null != info.getissynced()) {
			values.put(SyncSourceProvider.SYNC_COLUMN_NAME, info.getissynced());
		}
		if (isnew) {

			resolver.insert(uri, values);
		} else {
			resolver.update(uri, values, selection, selectionArgs);
		}
		cr.close();
		return true;

	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param name
	 * @return
	 */
	public static String getluid(Context context, String app, String name,
			ArrayList<EdiskLuidInfo> luidList) {
		String luid = null;

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME,
				SyncSourceProvider.PATH_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");

		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				String path = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				if (name.equalsIgnoreCase(path)) {
					luid = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
					break;
				}
			}
		}
		// cr.moveToLast();
		long syncedMax = 0L;
		long localMax = 0L;
		if (null == luid) {
			// luid = buildLuid(context,app);
			if (null != cr && cr.getCount() > 0) {
				String lastid = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
				Log.info(TAG, "max id is " + lastid);

				// luid = String.valueOf(Long.valueOf(lastid)+1);
				syncedMax = Long.valueOf(lastid);

				// }
				// else {
				// luid = String.valueOf(Long.valueOf(0)+1);
				// }
			}
			Log.info(TAG, "luidlist size : " + luidList.size());
			if (luidList.size() > 0) {
				localMax = Long.valueOf(luidList.get(luidList.size() - 1)
						.getLuid());
			}
			Log.info(TAG, "syncedMax : " + syncedMax + ", localMax : "
					+ localMax);
			long myluid = Math.max(syncedMax, localMax) + 1;
			luid = String.valueOf(myluid);

			ContentValues cv = new ContentValues();
			cv.put(SyncSourceProvider.PATH_COLUMN_NAME, name);
			cv.put(SyncSourceProvider.KEY_COLUMN_NAME, luid);

			resolver.insert(uri, cv);
		}
		cr.close();
		Log.debug(TAG, "getluid 2,luid is " + luid + " name : " + name);
		return luid;
	}

	private static String buildLuid(Context context, String app) {
		// TODO Auto-generated method stub
		return null;
	}

	// public static String getfileParentluid(Context context, String app,String
	// name) {
	// String luid = null;
	// ContentResolver resolver = context.getContentResolver();
	// Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
	// + SyncSourceProvider.PRIFEX_LUID+app + "/");
	// String selection = "_key = ?";
	// String[] selectionArgs = {luid};
	//
	// Cursor cr = resolver.query(uri, null, selection, selectionArgs, null);
	//
	// return luid;
	// }
	/**
	 * 
	 * @param context
	 * @param app
	 * @return
	 */
	public static String createLuid(Context context, String app) {
		// TODO Auto-generated method stub
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		// resolver.query(uri, projection, "MAX(COLUMN_NAME)", null, sortOrder)
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME };
		String luid = null;

		// check if this has synced
		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");

		if (null != cr && cr.getCount() > 0 && cr.moveToFirst()) {

			String lastid = cr.getString(cr
					.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));

			long myluid = Long.valueOf(lastid) + 1;
			luid = String.valueOf(myluid);

		} else {

			luid = "1000000000000";
		}
		Log.debug(TAG, "createLuid : " + luid);
		return luid;
		// Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");
		// if (null != cr && cr.getCount() > 0) {
		// for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
		// String fp = cr.getString(cr
		// .getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));
		// if (eTag.equalsIgnoreCase(fp)) {
		// path = cr
		// .getString(cr
		// .getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
		//
		// break;
		// }
		// }
		// }
		// cr.close();

		// Cursor cr = resolver.query(uri,projection , "MAX(_key)", null, null);
		// int count = cr.getCount();
		// if(count >1) {
		// Log.error(TAG,"error ,find same luid when query max");
		// EbenFileLog.recordSyncLog("error ,find same luid at query max");
		// }
		// if(null != cr && count > 0) {
		// String maxluid = cr.getString(cr
		// .getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
		// Log.info(TAG, "find max luid is "+maxluid);
		// return String.valueOf(Long.parseLong(maxluid)+1);
		// }
		//
		//
		// return "1";
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param luid
	 * @param itemInfo
	 * @return
	 */
	public static boolean isLuidFound(Context context, String app, String luid,
			EdiskLuidInfo itemInfo) {
		// TODO Auto-generated method stub
		boolean isfound = false;
		// itemInfo = new EdiskLuidInfo();

		if (ROOT.equals(luid)) {
			return true;
		}
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		String[] selectionArgs = { luid };
		String[] projection = { SyncSourceProvider.PATH_COLUMN_NAME,
				SyncSourceProvider.VALUE_COLUMN_NAME,
				SyncSourceProvider.PARENT_COLUMN_NAME,
				SyncSourceProvider.SYNC_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, selection, selectionArgs,
				null);
		int count = cr.getCount();
		if (count > 1) {
			Log.error(TAG, "error ,find same luid " + luid + ", count " + count);

		}
		if (null != cr && cr.moveToFirst()) {

			itemInfo.setLuid(luid);
			itemInfo.setPath(cr.getString(cr
					.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME)));

			itemInfo.setFp(cr.getString(cr
					.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME)));
			itemInfo.setParent(cr.getString(cr
					.getColumnIndex(SyncSourceProvider.PARENT_COLUMN_NAME)));

			itemInfo.setissynced(cr.getString(cr
					.getColumnIndex(SyncSourceProvider.SYNC_COLUMN_NAME)));
			isfound = true;
		}

		return isfound;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param eTag
	 * @return
	 */
	public static String checkItemExist(Context context, String app, String eTag) {
		// TODO Auto-generated method stub
		String path = null;
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		String[] projection = { SyncSourceProvider.VALUE_COLUMN_NAME,
				SyncSourceProvider.PATH_COLUMN_NAME };
		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");
		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				String fp = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));
				if (eTag.equalsIgnoreCase(fp)) {
					path = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));

					break;
				}
			}
		}
		cr.close();
		return path;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param name
	 * @param info
	 * @return
	 */
	public static String getExistluid(Context context, String app, String name,
			EdiskLuidInfo info) {
		String luid = null;

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME,
				SyncSourceProvider.PATH_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");

		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				String path = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				if (name.equalsIgnoreCase(path)) {
					luid = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
					break;
				}
			}
		}

		cr.close();
		Log.debug(TAG, "getExistluid ,luid is " + luid + " name : " + name);
		info.setLuid(luid);
		return luid;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param name
	 * @return
	 */
	public static boolean isLuidEmpty(Context context, String app) {
		boolean empty = true;

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");

		if (null != cr && cr.getCount() > 0) {
			empty = false;
		}

		cr.close();
		Log.info(TAG, "isLuidEmpty ,empty is "+app+", " + empty);

		return empty;
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param name
	 * @param info
	 * @return
	 */
	public static String getluid(Context context, String app, String name,
			EdiskLuidInfo info) {
		String luid = null;

		// check if this has synced
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME,
				SyncSourceProvider.PATH_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, null, null, "_key DESC");

		if (null != cr && cr.getCount() > 0) {
			for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
				String path = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				if (name.equalsIgnoreCase(path)) {
					luid = cr
							.getString(cr
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
					break;
				}
			}
		} else {
			info.setLuid("1000000000000");
			info.setissynced(EdiskLuidInfo.SYNC_NOTUP);
			updateSyncedItem(context, app, info);
			return "1000000000000";
		}

		long syncedMax = 0L;
		// long localMax = 0L;
		if (null == luid) {
			if (cr.moveToFirst()) {
				String lastid = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
				Log.debug(TAG, "getluid, max id is " + lastid);

				syncedMax = Long.valueOf(lastid);

				long myluid = syncedMax + 1;
				luid = String.valueOf(myluid);
				info.setLuid(luid);
				info.setissynced(EdiskLuidInfo.SYNC_NOTUP);
				updateSyncedItem(context, app, info);
			}

		}
		cr.close();
		Log.debug(TAG, "getluid ,luid is " + luid + " name : " + name);
		info.setLuid(luid);
		return luid;
	}

	/**
	 * 
	 * @param context
	 * @param table
	 * @param app
	 * @param info
	 */
	public static void saveChangedItems(Context context, String table, String app,EdiskLuidInfo info) {
		ContentResolver cr = context
				.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + table + app
				+ "/");
		if(null == info.getPath() || null== info.getFp()) {
			return;
		}
//		Log.debug(TAG,"saveChangedItems, tale, "+table+", path, "+info.getPath()+",fp, "+info.getFp());
		Log.debug(TAG,"saveChangedItems, tale, "+table+", luidinfo, "+info.toString());
		
		if (table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_NEW) ||
				table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_UPDATE)) {
			Cursor cur = cr.query(uri,
					new String[] { SyncSourceProvider.VALUE_COLUMN_NAME },
					SyncSourceProvider.KEY_COLUMN_NAME + "=?",
					new String[] { info.getPath() }, null);
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				String fp = cur.getString(cur
						.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));
				if (!fp.equalsIgnoreCase(info.getFp())) {
					ContentValues values = new ContentValues();
					values.put(SyncSourceProvider.KEY_COLUMN_NAME,
							info.getPath());
					values.put(SyncSourceProvider.VALUE_COLUMN_NAME,
							info.getFp());
					cr.update(uri, values, SyncSourceProvider.KEY_COLUMN_NAME
							+ "=?", new String[] { info.getPath() });
				}
			} else {
				ContentValues values = new ContentValues();
				values.put(SyncSourceProvider.KEY_COLUMN_NAME, info.getPath());
				values.put(SyncSourceProvider.VALUE_COLUMN_NAME, info.getFp());
				cr.insert(uri, values);
			}
			cur.close();

		} else if(table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_DELETE)) {
			Cursor cur = cr.query(uri,
					new String[] { SyncSourceProvider.VALUE_COLUMN_NAME },
					SyncSourceProvider.KEY_COLUMN_NAME + "=?",
					new String[] { info.getPath() }, null);
			if (!(cur.getCount() > 0)) {
//				cur.moveToFirst();
				ContentValues values = new ContentValues();
				values.put(SyncSourceProvider.KEY_COLUMN_NAME, info.getPath());
				values.put(SyncSourceProvider.VALUE_COLUMN_NAME, "0");
				cr.insert(uri, values);
			}
			cur.close();
		} else if(table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_ERROR)) {
			
		}
		
		
	}
	/**
	 * 
	 * @param list
	 * @param table
	 * @param app
	 */
	public static void saveSelecteItems(ArrayList list, String table, String app) {
//		String directory = FileHelpers.getFilePath(app) + File.separator;
//
//		Log.debug(TAG,"saveSelecteItems, "+app);
//		ArrayList existList = new ArrayList();
//		ContentResolver cr = App.i().getApplicationContext()
//				.getContentResolver();
//		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + table + app
//				+ "/");
//		String[] sel = { "_key" };
//		Cursor cur = cr.query(uri, sel, null, null, null);
//
//		if (cur.getCount() > 0) {
//			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
//
//				String key = cur.getString(cur.getColumnIndex("_key"));
//				if (!existList.contains(key)) {
//					existList.add(key);
//				}
//
//			}
//		}
//
//		cur.close();
//		for (Iterator iter = list.iterator(); iter.hasNext();) {
//			// String item = EbenHelpers.encodeKey((String) iter.next());
//
//			String item = (String) iter.next();
//			Log.debug(TAG, "item: " + item);
//			ContentValues values = new ContentValues();
//			values.put("_key", item);
//			String fp = EdiskSyncSource.EmptyMd5;
//			if (!item.endsWith(File.separator)) {
//				File file = new File(directory + item);
//				if (file.exists()) {
//					if (file.length() > 0) {
//						if(file.length() >= EdiskSyncSource.MAX_FILE_SIZE) {
//							iter.remove();
//							continue;
//						}
//						fp = MD5Util.md5(file);
//					}
//				}
//			}
//			values.put("_value", fp);
//			if (!existList.contains(item)) {
//				cr.insert(uri, values);
//
//			}
//
//		}

	}

	/**
	 * 
	 * @param listnew
	 * @param listold
	 * @param table
	 * @param app
	 */
	public static void saveRenamedItems(ArrayList listnew, ArrayList listold,
			String table, String app) {
		ContentResolver cr = App.i().getApplicationContext()
				.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + table + app
				+ "/");
		// String[] sel = {"KEY_COLUMN_NEW"};
		// Cursor cur = cr.query(uri, sel, null, null, null);
		//
		// if(cur.getCount()>0) {
		// for (cur.moveToFirst(); !cur.isAfterLast(); cur
		// .moveToNext()) {
		//
		// String key = cur.getString(cur
		// .getColumnIndex("_key"));
		// if(!existList.contains(key)) {
		// existList.add(key);
		// }
		//
		// }
		// }

		// cur.close();
//		String photo = "/mnt/sdcard/DCIM/Camera/";
//		String dir = FileHelpers.getFilePath(app);
//		Log.debug(TAG,"app ,"+app+", dir ,"+dir);
//		for (int i = 0; i < listnew.size(); i++) {
//			String newname = (String) listnew.get(i);
//			String oldname = (String) listold.get(i);
//			boolean isfolder = false;
//			if(new File(newname).isDirectory()) {
//				isfolder = true;
//			}
//			Log.info(TAG, "saveRenamedItems org, oldname : "+oldname+", newname : "+newname);
//			if(newname.startsWith(dir)&&oldname.startsWith(dir)) {
//				newname = newname.substring(dir.length()+1);
//				oldname = oldname.substring(dir.length()+1);
//			} else if(newname.startsWith(photo)&&oldname.startsWith(photo)) {
//				newname = newname.substring(photo.length());
//				oldname = oldname.substring(photo.length());
//			}
//			if (!newname.endsWith(File.separator)) {
//				File file = new File(FileHelpers.getFilePath(app)
//						+ File.separator + newname);
//				if(!file.exists()) {
//					Log.error(TAG, "error ,saveRenamedItems ,new file not exist, oldname : "+oldname+", newname : "+newname);
//					EbenFileLog.recordSyncLog("saveRenamedItems ,new file not exist, oldname : "+oldname+", newname : "+newname);
//					if(oldname.startsWith(dir)) {
//						oldname = oldname.substring(dir.length()+1);
//					}
//					if(isfolder) {
//						markDelItem(App.i().getApplicationContext(),  app,oldname+File.separator);
//					}
//					else  {
//						markDelItem(App.i().getApplicationContext(),  app,oldname);
//					}
//					continue;
//				}
//				if(file.length() >= EdiskSyncSource.MAX_FILE_SIZE) {
//					Log.error(TAG, "over 50M file,igore");
//					
//					markDelItem(App.i().getApplicationContext(),  app,oldname);
//					continue;
//				}
//				if (file.isDirectory()) {
//					newname = newname + File.separator;
//					oldname = oldname + File.separator;
//				}
//				file.setLastModified(System.currentTimeMillis());
//			} else {
//				File file = new File(FileHelpers.getFilePath(app)
//						+ File.separator + newname);
//				file.setLastModified(System.currentTimeMillis());
//			}
//			ContentValues values = new ContentValues();
//
//			Log.debug(TAG, "saveRenamedItems, oldname : "+oldname+", newname : "+newname);
//			values.put(SyncSourceProvider.KEY_COLUMN_NEW, newname);
//			values.put(SyncSourceProvider.KEY_COLUMN_OLD, oldname);
//			cr.insert(uri, values);
//			
//			markDelItem(App.i().getApplicationContext(),  app,oldname);
////			if(app.equalsIgnoreCase(ExternalEntryConst.APP_ENOTE)) {
////				if(newname.endsWith(File.separator))  {
////					File bookinfo = new File(dir+File.separator+newname+".bookinfo");
////					if(bookinfo.exists())  {
////						Log.debug(TAG, "add bookinfo to update table") ;
////							ArrayList up = new ArrayList();
////							up.add(newname+".bookinfo");
////							saveSelecteItems(up,SyncSourceProvider.PRIFEX_UPDATE,app);
////						}
////					}
////				
////			}
//			
//		}

	}
	/**
	 * 
	 * @param context
	 * @param source
	 * @param itemOld
	 */
	public static void markDelItem(Context context, String source, String itemOld) {
		try {
			ContentResolver cr = context.getContentResolver();
			String[] projection = { "_key", "_value" };
			String selection = "_key=?";
			String[] selectionArgs = { itemOld };
//			String soureUri = "content://cn.eben.provider.syncMarkStatus/"
//					+ source + "/";
//			Uri uri = Uri.parse(soureUri);
			// lierbao changed to
			Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
					+ SyncSourceProvider.PRIFEX_DELETE+source + "/");
			Cursor cur = cr.query(uri, projection, selection, selectionArgs,
					null);

			int count = cur.getCount();
			cur.close();
			if (0 == count) {
				Log.info(TAG, "set to delete table : " + itemOld);
				ContentValues values = new ContentValues();
				values.put("_key", itemOld);
				values.put("_value", String.valueOf(0));
				cr.insert(uri, values);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param table
	 * @param app
	 * @return
	 */
	public static ArrayList getNeedsyncItems(String table, String app) {
//		String directory = FileHelpers.getFilePath(app) + File.separator;
//		
		ArrayList<String> existList = new ArrayList<String>();
//		ContentResolver cr = App.i().getApplicationContext()
//				.getContentResolver();
//		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + table + app
//				+ "/");
//
//		String[] projection = { "_key" };
//		Cursor cur = cr.query(uri, projection, null, null, null);
//
//		if (cur.getCount() > 0) {
//			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
//
//				String key = cur.getString(cur.getColumnIndex("_key"));
//			
//				
//				if(new File(directory+key).length() >= EdiskSyncSource.MAX_FILE_SIZE) {
//					Log.error(TAG, "find a large file ,remove it"+key);
//					String[] selectionArgs = { key };
//					cr.delete(uri,  "_key=?",
//							selectionArgs);
//					continue;
//				}
//				if (!existList.contains(key)) {
//					existList.add(key);
//				}
//
//			}
//		}
//
//		cur.close();
//
		return existList;
	}

	private Hashtable renamedHt;

	/**
	 * 
	 * @param table
	 * @param app
	 * @return
	 */
	public static Hashtable getRenamedItems(String table, String app) {
		Hashtable ht = new Hashtable();
		ArrayList<String> existList = new ArrayList<String>();
		ContentResolver cr = App.i().getApplicationContext()
				.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI + table + app
				+ "/");

		String[] projection = { SyncSourceProvider.KEY_COLUMN_NEW,
				SyncSourceProvider.KEY_COLUMN_OLD };
		// public final static String KEY_COLUMN_NEW = "_keynew";
		// public final static String KEY_COLUMN_OLD = "_keyold";
		Cursor cur = cr.query(uri, projection, null, null, null);

		if (cur.getCount() > 0) {
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

				String newitem = cur.getString(cur
						.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NEW));
				String olditem = cur.getString(cur
						.getColumnIndex(SyncSourceProvider.KEY_COLUMN_OLD));
				if (null == newitem || null == olditem) {
					String[] selectionArgs = { newitem };
					cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NEW + "=?",
							selectionArgs);
					continue;
				}
				ht.put(olditem, newitem);
			}
		}

		cur.close();

		return ht;
	}

	/**
	 * 
	 * @param mContext
	 * @param name
	 */
	public static void resetStatusChanges(Context context, String app,String table) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		Uri luidUri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");

		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ table + app + "/");
		Log.debug(TAG, "resetStatusChanges, " + app+", table :"+table);
		
		if (table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_UPDATE)
				|| table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_NEW)) {
			Cursor cur = cr.query(uri, null, null, null, null);
			if (null != cur && cur.getCount() > 0) {
				for (cur.moveToFirst(); !cur.isLast(); cur.moveToNext()) {
					String itemPath = cur
							.getString(cur
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
					String itemFp = cur
							.getString(cur
									.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));

					Cursor cursor = cr.query(luidUri, new String[] {
							SyncSourceProvider.VALUE_COLUMN_NAME,
							SyncSourceProvider.SYNC_COLUMN_NAME },
							SyncSourceProvider.PATH_COLUMN_NAME + "=?",
							new String[] { itemPath }, null);
					if (null != cursor && cursor.getCount() > 0) {
						cursor.moveToFirst();
						String luidFp = cursor
								.getString(cursor
										.getColumnIndex(SyncSourceProvider.VALUE_COLUMN_NAME));
						String luidsynced = cursor
								.getString(cursor
										.getColumnIndex(SyncSourceProvider.SYNC_COLUMN_NAME));

						if (luidFp.equalsIgnoreCase(itemFp)
								&& luidsynced
										.equalsIgnoreCase(EdiskLuidInfo.SYNCED)) {
							cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NAME
									+ "=?", new String[] { itemPath });
							Log.info(TAG, "resetStatusChanges table: "+table+", delete, "+itemPath);

						}
					}
					cursor.close();

				}
			}
			cur.close();
		} else if (table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_DELETE)) {

			Cursor cur = cr.query(uri,
					new String[] { SyncSourceProvider.KEY_COLUMN_NAME }, null,
					null, null);
			if (null != cur && cur.getCount() > 0) {
				for (cur.moveToFirst(); !cur.isLast(); cur.moveToNext()) {
					String itemPath = cur
							.getString(cur
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
					
					Cursor cursor = cr
							.query(luidUri,
									new String[] { SyncSourceProvider.SYNC_COLUMN_NAME },
									SyncSourceProvider.PATH_COLUMN_NAME + "=?",
									new String[] { itemPath }, null);
					if (null != cursor && cursor.getCount() > 0) {
						cursor.moveToFirst();
						String luidsynced = cursor
								.getString(cursor
										.getColumnIndex(SyncSourceProvider.SYNC_COLUMN_NAME));

						if (!luidsynced.equalsIgnoreCase(EdiskLuidInfo.SYNCED)) {
							cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NAME
									+ "=?", new String[] { itemPath });
							Log.info(TAG, "resetStatusChanges table: "+table+", delete, "+itemPath);
							
						}

					} else {
						cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NAME
								+ "=?", new String[] { itemPath });
					}
					cursor.close();
				} 
			}
			cur.close();
		} else if (table.equalsIgnoreCase(SyncSourceProvider.PRIFEX_RENAME)) {
			Cursor cur = cr.query(uri,
					new String[] { SyncSourceProvider.KEY_COLUMN_OLD }, null,
					null, null);
			if (null != cur && cur.getCount() > 0) {
				for (cur.moveToFirst(); !cur.isLast(); cur.moveToNext()) {
					String olditemPath = cur
							.getString(cur
									.getColumnIndex(SyncSourceProvider.KEY_COLUMN_OLD));
					Cursor cursor = cr
							.query(luidUri,
									new String[] { SyncSourceProvider.SYNC_COLUMN_NAME },
									SyncSourceProvider.PATH_COLUMN_NAME + "=?",
									new String[] { olditemPath }, null);
					
					if (null != cursor && cursor.getCount() > 0) {
						cursor.moveToFirst();
						String luidsynced = cursor
								.getString(cursor
										.getColumnIndex(SyncSourceProvider.SYNC_COLUMN_NAME));
						if (!luidsynced.equalsIgnoreCase(EdiskLuidInfo.SYNCED)) {
							cr.delete(uri, SyncSourceProvider.KEY_COLUMN_OLD
									+ "=?", new String[] { olditemPath });
							Log.info(TAG, "resetStatusChanges table: "+table+", delete, "+olditemPath);
							
						}
					} else {
						cr.delete(uri, SyncSourceProvider.KEY_COLUMN_OLD
								+ "=?", new String[] { olditemPath });
						Log.info(TAG, "delete, "+olditemPath);
					}
					cursor.close();
				}
			}
			cur.close();
		} else if (SyncSourceProvider.PRIFEX_DOWN.equalsIgnoreCase(table)) {
			Cursor cur = cr.query(uri, new String[]{SyncSourceProvider.NAME}, null, null, null);
			if(!(cur.getCount()>0)) {
				
				int count = cr.delete(luidUri, SyncSourceProvider.SYNC_COLUMN_NAME+"=?", new String[]{EdiskLuidInfo.SYNC_NOTDOWN});
				int upcount = cr.delete(luidUri, SyncSourceProvider.SYNC_COLUMN_NAME+"=?", new String[]{EdiskLuidInfo.SYNC_NOTUP});
				Log.debug(TAG, "not down item, reset notdown UP items at luid table, NOT DOWN "+count+", notup : "+upcount);
			}
			cur.close();
		}
//		else if ()
//		String[] projection = { SyncSourceProvider.KEY_COLUMN_NEW,
//				SyncSourceProvider.KEY_COLUMN_OLD };
//		// public final static String KEY_COLUMN_NEW = "_keynew";
//		// public final static String KEY_COLUMN_OLD = "_keyold";
//		String[] selectionArgs = { item };
//		cr.delete(uri, "_key=?", selectionArgs);
//
//		uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
//				+ SyncSourceProvider.PRIFEX_UPDATE + app + "/");
//		cr.delete(uri, "_key=?", selectionArgs);
//
//		uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
//				+ SyncSourceProvider.PRIFEX_RENAME + app + "/");
//		cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NEW + "=?", selectionArgs);

	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param item
	 */
	public static void clearItems(Context context, String app, String item) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_NEW + app + "/");

		String[] projection = { SyncSourceProvider.KEY_COLUMN_NEW,
				SyncSourceProvider.KEY_COLUMN_OLD };
		// public final static String KEY_COLUMN_NEW = "_keynew";
		// public final static String KEY_COLUMN_OLD = "_keyold";
		String[] selectionArgs = { item };
		cr.delete(uri, "_key=?", selectionArgs);

		uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_UPDATE + app + "/");
		cr.delete(uri, "_key=?", selectionArgs);

		uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_RENAME + app + "/");
		cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NEW + "=?", selectionArgs);

	}

	/**
	 * 
	 * @param mContext
	 * @param name
	 * @param old
	 */
	public static void removeRenameItem(Context context, String app, String old) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_RENAME + app + "/");
		String[] selectionArgs = { old };
		cr.delete(uri, SyncSourceProvider.KEY_COLUMN_OLD + "=?", selectionArgs);
		
		// SET TO DELETE
		uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_DELETE + app + "/");
		Cursor cur = cr.query(uri,
				new String[] { SyncSourceProvider.VALUE_COLUMN_NAME },
				SyncSourceProvider.KEY_COLUMN_NAME + "=?",
				new String[] { old }, null);
		if (!(cur.getCount() > 0)) {
//			cur.moveToFirst();
			ContentValues values = new ContentValues();
			values.put(SyncSourceProvider.KEY_COLUMN_NAME, old);
			values.put(SyncSourceProvider.VALUE_COLUMN_NAME, "0");
			cr.insert(uri, values);
		}
		cur.close();
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param old
	 */
	public static void removeDelItem(Context context, String app, String del) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_DELETE + app + "/");
		String[] selectionArgs = { del };
		cr.delete(uri, SyncSourceProvider.KEY_COLUMN_NAME + "=?", selectionArgs);
	}

	/**
	 * 
	 * @param context
	 * @param app
	 * @param del
	 */
	public static boolean findDelItem(Context context, String app, String del) {
		// TODO Auto-generated method stub
		boolean isfound = false;
		if(app.equalsIgnoreCase("ephoto")) {
			Log.info(TAG,"ephoto no del protect");
			return true;
		}
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_DELETE + app + "/");
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME };
		// String selection = SyncSourceProvider.KEY_COLUMN_NAME
		// String[] selectionArgs = {del};
		Cursor cursor = cr.query(uri, projection, null, null, null);

		if (null != cursor && cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				String key = cursor.getString(cursor.getColumnIndex("_key"));
				if (key.endsWith(File.separator)) {
					if (del.startsWith(key)) {
						isfound = true;
					}
				} else {
					if (key.equalsIgnoreCase(del)) {
						isfound = true;
					}
				}
			}
		}
		cursor.close();

		return isfound;
	}

	/**
	 * 
	 * @param mContext
	 * @param name
	 * @param info
	 */
	public static void updateSyncedItemResult(Context context, String app,
			EdiskLuidInfo info) {
		// TODO Auto-generated method stub

		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = "_key = ?";
		String[] selectionArgs = { info.getLuid() };
		String[] projection = { SyncSourceProvider.PATH_COLUMN_NAME };
		
		Log.debug(TAG, "updateSyncedItemResult, "+info.toString());
		Cursor cr = resolver.query(uri, projection, selection, selectionArgs,
				null);

		boolean isnew = true;
		if (null != cr && cr.getCount() > 0) {
			isnew = false;

			if (null != info.getPath()
					&& info.getPath().endsWith(File.separator)) {
				cr.moveToFirst();
				String syncedpath = cr.getString(cr
						.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
				if (!info.getPath().equalsIgnoreCase(syncedpath)) {
					Log.info(TAG, "got a folder renamed, " + syncedpath
							+ " --> " + info.getPath());
					replaceParentPath(context, app, syncedpath, info.getPath());
				}

			}

		}

		// public final static String KEY_COLUMN_NAME = "_key";
		// public final static String VALUE_COLUMN_NAME = "_value";
		//
		// public final static String PATH_COLUMN_NAME = "_path";
		// public final static String PARENT_COLUMN_NAME = "_parent";
		//
		// public final static String SYNC_COLUMN_NAME = "_issync";
		ContentValues values = new ContentValues();
		if (null != info.getLuid()) {
			values.put(SyncSourceProvider.KEY_COLUMN_NAME, info.getLuid());
		}
		if (null != info.getFp()) {
			values.put(SyncSourceProvider.VALUE_COLUMN_NAME, info.getFp());
		}
		if (null != info.getPath()) {
			values.put(SyncSourceProvider.PATH_COLUMN_NAME, info.getPath());
		}
		if (null != info.getParent()) {
			values.put(SyncSourceProvider.PARENT_COLUMN_NAME, info.getParent());
		}
		if (null != info.getissynced()) {
			values.put(SyncSourceProvider.SYNC_COLUMN_NAME, info.getissynced());
		}
		if (isnew) {

			resolver.insert(uri, values);
		} else {
			resolver.update(uri, values, selection, selectionArgs);
		}

		cr.close();
		// return true;
	}

	private static void replaceParentPath(Context context, String app,
			String syncedpath, String newpath) {
		// TODO Auto-generated method stub

		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = SyncSourceProvider.PATH_COLUMN_NAME + " = ?";
		// String[] selectionArgs = {info.getLuid()};
		String[] projection = { SyncSourceProvider.PATH_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, null, null, null);

		for (cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
			String itempath = cr.getString(cr
					.getColumnIndex(SyncSourceProvider.PATH_COLUMN_NAME));
			if (itempath.startsWith(syncedpath)) {

				String replacepath = newpath
						+ itempath.substring(syncedpath.length());
				ContentValues values = new ContentValues();
				values.put(SyncSourceProvider.PATH_COLUMN_NAME, replacepath);
				String[] selectionArgs = { itempath };
				resolver.update(uri, values, selection, selectionArgs);

			}
			// break;
		}

	}
	
	public static boolean checkSamePathLuid(Context context, String app,
			String path) {
		// TODO Auto-generated method stub
		boolean found = false;
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(SyncSourceProvider.AUTHORITY_URI
				+ SyncSourceProvider.PRIFEX_LUID + app + "/");
		String selection = SyncSourceProvider.PATH_COLUMN_NAME + " = ?";
		 String[] selectionArgs = {path};
		String[] projection = { SyncSourceProvider.KEY_COLUMN_NAME };

		Cursor cr = resolver.query(uri, projection, selection, selectionArgs, null);
		if(cr.getCount()  > 1) {
			found = true;

			cr.moveToFirst();
			String luid = cr.getString(cr
					.getColumnIndex(SyncSourceProvider.KEY_COLUMN_NAME));
			
			resolver.delete(uri, SyncSourceProvider.KEY_COLUMN_NAME+"=?", new String[]{luid});
			
			Log.error(TAG, "error ,find same path at diff lui ,"+path+",count ,"+cr.getCount()+",luid ,"+luid);
//			EbenFileLog.recordSyncLog("error ,find same path at diff lui ,"+path+",count ,"+cr.getCount()+",remove ,"+luid);
			
		}
		
		cr.close();
		return found;

	}

	public static void add(ArrayList<EdiskLuidInfo> luidinfo,ArrayList<EdiskLuidInfo> exluidinfo,ArrayList<EdiskLuidInfo> exluidinfo2,
			EdiskLuidInfo info, boolean isFolderCheck) {
		// TODO Auto-generated method stub
		if(null == luidinfo) {
			return;
		}
		
		for(EdiskLuidInfo ediskinfo:luidinfo) {
			if(isFolderCheck) {
				if(ediskinfo.getPath().endsWith(File.separator)&& info.getPath().startsWith(ediskinfo.getPath()) ){
					Log.info(TAG, "find a parent at delete table : "+info.toString());
					return;
				}
			}  
			if(ediskinfo.getLuid().equalsIgnoreCase(info.getLuid())
					|| ediskinfo.getPath().equalsIgnoreCase(info.getPath())){
				Log.info(TAG, "has put to table ,"+info.toString());
				return;
			}
		}
		
		if(null != exluidinfo) {
			for(EdiskLuidInfo ediskinfo:exluidinfo) {
				if(ediskinfo.getLuid().equalsIgnoreCase(info.getLuid())
						|| ediskinfo.getPath().equalsIgnoreCase(info.getPath())){
					Log.info(TAG, "has exist to ex table ,"+info.toString());
					return;
				}
			}			
		}

		if(null != exluidinfo2) {
			for(EdiskLuidInfo ediskinfo:exluidinfo2) {
				if(ediskinfo.getLuid().equalsIgnoreCase(info.getLuid())
						|| ediskinfo.getPath().equalsIgnoreCase(info.getPath())){
					Log.info(TAG, "has exist to ex2 table ,"+info.toString());
					return;
				}
			}			
		}
		luidinfo.add(info);
	}
}
