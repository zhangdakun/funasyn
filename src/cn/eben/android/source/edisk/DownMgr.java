package cn.eben.android.source.edisk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.eben.android.net.apps.EbenDownFileAuth;
import cn.eben.android.providers.SyncSourceProvider;
import cn.eben.android.util.EbenFileLog;
import cn.eben.android.util.EbenHelpers;
import cn.eben.android.util.MD5Util;

import com.funambol.android.App;
import com.funambol.client.source.AppSyncSource;
import com.funambol.platform.FileAdapter;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncItem;
import com.funambol.util.Log;

public class DownMgr {
	
	public final String TAG = "downMgr";
	class downInfo {
		String name;
		String etag;
		String size;
		String modified;
		String url;
		String luid;
		String parent;
	}

	public static int downCount = 0;
	public static int curDownCount = 0;
	public static String ERROR = "ERROR";
	public static String TIMEOUT = "TIMEOUT";

	/**
	 * down files
	 * 
	 * @param appSource
	 */
	public void startDown(AppSyncSource appSource) {
		if(/*APP_CONTACTS*/"contacts".equals(appSource.getSyncSource().getName())) {
			EdiskSyncSource.setPresync(false);

//			bdSyncStatus(App.i().getApplicationContext(), appSource,
//					ExternalEntryConst.SYNC_END, 0);
			return;
		}
		boolean isok = true;
		int errorcode = 0;
		try {
			EdiskSyncSource.setPresync(true);
			int net = EbenHelpers.isNetworkAvailable();
			if (0 != net) {
//				bdSyncStatus(App.i().getApplicationContext(), appSource,
//						ExternalEntryConst.SYNC_END, net);
				EdiskSyncSource.setPresync(false);
				return;
			}

			downCount = 0;
			curDownCount = 0;

			String sourceName = appSource.getSyncSource().getName();
			Log.info(TAG, "source : " + sourceName);

			ContentResolver cr = App.i().getContentResolver();

			// Uri uri = Uri.withAppendedPath(
			// Uri.parse("content://cn.eben.provider.syncSource/"),
			// SyncSourceProvider.PRIFEX_DOWN+sourceName);
			// Uri uri =
			// Uri.parse(SyncSourceProvider.AUTHORITY_URI+SyncSourceProvider.PRIFEX_DOWN+sourceName);

			Uri uri = Uri.withAppendedPath(
					Uri.parse(SyncSourceProvider.AUTHORITY_URI),
					SyncSourceProvider.PRIFEX_DOWN + sourceName);

			// String[] projection = { SyncSourceProvider.NAME };
			// String selection = SyncSourceProvider.NAME+" = ?";
			// String[] args = { name };
			ArrayList<downInfo> downList = new ArrayList();
			boolean isNew = true;
			Cursor cursor = cr.query(uri, null, null, null, null);
			if (null != cursor && cursor.getCount() > 0) {
				isNew = false;
			}
			while (cursor.moveToNext()) {
				downInfo downinfo = new downInfo();
				downinfo.name = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.NAME));
				downinfo.etag = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.ETAG));
				downinfo.size = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.SIZE));
				downinfo.modified = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.MODIFY));
				downinfo.url = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.URI));

				downinfo.luid = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.LUID));
				downinfo.parent = cursor.getString(cursor
						.getColumnIndex(SyncSourceProvider.PARENT));

				Log.info(TAG, "name : " + downinfo.name + ",etag: "
						+ downinfo.etag + ",size: " + downinfo.size
						+ ",modified : " + downinfo.modified + ", url: "
						+ downinfo.url);
				if (Long.valueOf(downinfo.size) > 0) {
					downCount++;
				}
				downList.add(downinfo);

			}

			cursor.close();

			downItems(appSource, downList);
		} catch (SyncException e) {
			// e.printStackTrace();
			// EbenFileLog.recordSyncLog("exception : "+e.getMessage());
			// Intent intent = getStatusIntent(SYNC_END, new Date().getTime());
			// s
			// intent.putExtra(KEY_SYNC_ERROR, e.getCode());
			// App.i().getApplicationContext().sendBroadcast(intent);
			// isok = false;
			errorcode = e.getCode();
			EdiskSyncSource.setPresync(false);
			EbenFileLog.recordSyncLog("startDown : " + e.toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// EbenFileLog.recordSyncLog("exception : "+e.getMessage());
			// Intent intent = getStatusIntent(SYNC_END, new Date().getTime());
			// intent.putExtra(KEY_SYNC_ERROR, SYNC_FAIL);
			// App.i().getApplicationContext().sendBroadcast(intent);
			// isok = false;
			errorcode = 1;
			EdiskSyncSource.setPresync(false);
			EbenFileLog.recordSyncLog("startDown : " + e.toString());
		}
		EdiskSyncSource.setPresync(false);

//		bdSyncStatus(App.i().getApplicationContext(), appSource,
//				ExternalEntryConst.SYNC_END, errorcode);

	}

	private static int OK = 0;
	private static int SOCKET_ERROR = 1;
	private static int GENERAL_ERROR = 10;
	private static int ETAG_ERROR = 11;
	private static int UNKNOWN = 255;
	private static int NOTEXIST = 5892;

	// File not exists.","code":5892
	private void downItems(AppSyncSource appSource, ArrayList<downInfo> downList)
			throws Exception {
		// TODO Auto-generated method stub
		if (null == downList)
			return;
		if (downList.isEmpty())
			return;
		for (downInfo downinfo : downList) {
			int result = applyDown(appSource, downinfo);
			if (OK == result || UNKNOWN == result) {
				if (appSource.getSyncSource() instanceof EdiskSyncSource) {
					EdiskSyncSource edisk = (EdiskSyncSource) appSource
							.getSyncSource();
					EdiskTracker eTrack = (EdiskTracker) edisk.getTracker();
					// SyncItem item = new
					// SyncItem(EbenHelpers.encodeKey(downinfo.name));
					// SyncItem item = new SyncItem(downinfo.name);
					EdiskLuidInfo iteminfo = new EdiskLuidInfo(downinfo.luid, downinfo.parent, downinfo.name, downinfo.etag);
//					iteminfo.setLuid(downinfo.luid);
//					iteminfo.setPath(downinfo.name);
					// item.setState('U');
					if (eTrack.removeItem(iteminfo, SyncItem.STATE_UPDATED)) {
						String sourceName = appSource.getSyncSource().getName();
						ContentResolver cr = App.i().getContentResolver();

						// Uri uri =
						// Uri.parse(SyncSourceProvider.AUTHORITY_URI+SyncSourceProvider.PRIFEX_DOWN+sourceName);
						Uri uri = Uri.withAppendedPath(
								Uri.parse(SyncSourceProvider.AUTHORITY_URI),
								SyncSourceProvider.PRIFEX_DOWN + sourceName);
						cr.delete(uri, SyncSourceProvider.NAME + "=?",
								new String[] { downinfo.name });
					}

				}
			} else if (NOTEXIST == result) {
				Log.error(TAG, "find not find at server,just remove it ");
				String sourceName = appSource.getSyncSource().getName();
				ContentResolver cr = App.i().getContentResolver();

				// Uri uri =
				// Uri.parse(SyncSourceProvider.AUTHORITY_URI+SyncSourceProvider.PRIFEX_DOWN+sourceName);
				Uri uri = Uri.withAppendedPath(
						Uri.parse(SyncSourceProvider.AUTHORITY_URI),
						SyncSourceProvider.PRIFEX_DOWN + sourceName);
				cr.delete(uri, SyncSourceProvider.NAME + "=?",
						new String[] { downinfo.name });
			}
		}

	}

	private String downloadFile(String url, long hasRecv, Long size, int count,
			String fileName, String fileDir) {
		// TODO Auto-generated method stub
		// String fileName = url.substring(url.lastIndexOf("/"), url.length());
		int net = EbenHelpers.isNetworkAvailable();
		if (0 != net) {
			Log.error(TAG, "network not available ");
			throw new SyncException(net, "net work not ok");
			// return null;
		}
//		if (ConnectivityManager.TYPE_MOBILE == EbenHelpers.getNetworkType()) {
//			ExSyncManager.i().toast3GWarning();
//		}

		Log.info(TAG, "url : " + url + ", count : " + count);
		if (null == fileName)
			fileName = String.valueOf(System.currentTimeMillis());

		File downFile = new File(fileDir + fileName);

		if (count > 30) {
			downFile.delete();
			throw new SyncException(2, "net work timeout");
			// return TIMEOUT;
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
				Log.info(TAG, "bytes=" + hasRecv + "-");
				ucon.setRequestProperty("RANGE", "bytes=" + hasRecv + "-");
			}

			is = ucon.getInputStream();

			oSavedFile = new RandomAccessFile(downFile, "rw");
			oSavedFile.seek(hasRecv);

			// OutputStream os = new BufferedOutputStream(oSavedFile);
			// OutputStream os = new BufferedOutputStream(new
			// FileOutputStream(downFile));

			int len = ucon.getContentLength();
			Log.info(TAG, fileName + " lenth: " + len);
			int hasRead = 0;

			byte[] buff = new byte[1024];
			while ((hasRead = is.read(buff)) > 0) {
				// os.write(buff, 0, hasRead);
				oSavedFile.write(buff, 0, hasRead);
				sum += hasRead;
				// Log.info(TAG_LOG, "has read : "+hasRead);
			}
			Log.info(TAG, "recv file size: " + sum);

			return fileName;

		} catch (SocketTimeoutException e) {
			// TODO: handle exception
			Log.error(TAG, "e: " + e.toString() + ", has read: " + sum);

			// e.printStackTrace();
			// if(null != oSavedFile)
			if (sum > hasRecv) {
				return downloadFile(url, sum, size, 0, fileName, fileDir);
			} else {
				return downloadFile(url, sum, size, count, fileName, fileDir);
			}
		} catch (MalformedURLException e) {
			// e.printStackTrace();
			downFile.delete();
			return ERROR;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.error(TAG, "e: " + e.toString() + ", has read: " + sum);
			e.printStackTrace();
			downFile.delete();

			return ERROR;

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

	private int applyDown(AppSyncSource appSource, downInfo downinfo)
			throws Exception {
		// TODO Auto-generated method stub
		int code = OK;
		try {
			
//		ExSyncManager.i().notifyItemResult(appSource, downinfo.name,
//				SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_START, 'U');
		if (downinfo.name.contains(File.separator)) {
			String folder = downinfo.name.substring(0,
					downinfo.name.lastIndexOf(File.separator) + 1);
//			ExSyncManager.i().notifyItemResult(appSource, folder, -1,
//					ExSyncManager.SYNC_START,'U');
		}
		curDownCount++;
//		EbenExsyncProgress prg = new EbenExsyncProgress(1, downCount,
//				curDownCount, appSource.getSyncSource().getName(),
//				appSource.getName(), downinfo.name, 0);
//		prg.setstage("down");
//		prg.setItemSize(String.valueOf(downinfo.size));
//		prg.send();

//		updateSyncDb(appSource, downinfo.name, SyncSource.CHUNK_SUCCESS_STATUS);
		String directory = ((EdiskSyncSource) appSource.getSyncSource())
				.getDirectory();
		String tempDirectory = ((EdiskSyncSource) appSource.getSyncSource())
				.getTempDirectory();

		boolean hasExist = false;
		FileAdapter file = null;
		Context mContext = App.i().getApplicationContext();
		if (Long.valueOf(downinfo.size) > 0) {
			String url = downinfo.url;
			String eTag = downinfo.etag;
			Log.info(TAG, "luid is : " + url + ", eTag: " + eTag);
			if (null != url && null != eTag) {
				// String syncedLuid = EdiskdbStore.checkItemExist(mContext,
				// appSource.getSyncSource().getName(), eTag);
				String existfile = EdiskLuidInfo.checkItemExist(mContext,
						appSource.getSyncSource().getName(), eTag);

				String fileName = null;
				if (null != existfile&&!"".equalsIgnoreCase(fileName)) {
					// String existfile = EbenHelpers.decodeKey(existfile);
					Log.info(TAG, "this file exist ,just copy file not down: "
							+ existfile);
					File localFile = new File(directory + existfile);
					if (localFile.exists()) {
						String localTag = MD5Util.md5(localFile);
						Log.info(TAG, "localTag : " + localTag);
						if (!eTag.equalsIgnoreCase(localTag)) {
							Log.error(TAG, "eTag not match, to download");
							// throw new IOException("eTag not match");
						} else {
							if (existfile.equalsIgnoreCase(downinfo.name)) {
								Log.info(TAG,
										"the same file has exist , do nothing");
								// file.close();
								// file = new FileAdapter(localFile);
								hasExist = true;
								fileName = existfile;
								// return null;
								// return file;
							} else
							// just copy file not down
							if (EdiskSyncSource.copyFile(directory + existfile,
									tempDirectory + existfile)) {
								if (!MD5Util.md5(
										new File(tempDirectory + existfile))
										.equalsIgnoreCase(eTag)) {
									throw new IOException(
											"copy error !!, eTag not match");
								}
								Log.info(TAG, "etag check ok, copy ok");
								fileName = existfile;
							}
						}
					}
				}
				// file name null need down this
				if (null == fileName && !hasExist) {
					EbenDownFileAuth df = new EbenDownFileAuth(url);
					try {
						df.handler();
					} catch (SyncException syncException) { //SyncException
						Log.error(TAG, "sync exception");
						syncException.printStackTrace();
						throw syncException;
					}catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (OK != df.getLastErr()) {
						code = df.getLastErr();
						return df.getLastErr();
					}
					Hashtable<String, String> list = df.getDownList();
					Iterator iter = list.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry entry = (Map.Entry) iter.next();
						String key = (String) entry.getKey();
						if (key.equalsIgnoreCase(url)) {
							url = (String) entry.getValue();
						}

					}

					fileName = downloadFile(url, 0,
							Long.valueOf(downinfo.size), 0, null, tempDirectory);
					if (null != fileName) {
						if (ERROR.equalsIgnoreCase(fileName)) {
							code = UNKNOWN;
							return UNKNOWN;
						}
						if (TIMEOUT.equalsIgnoreCase(fileName)) {
							code = SOCKET_ERROR;
							return SOCKET_ERROR;
						}
						String localTag = MD5Util.md5(new File(tempDirectory
								+ fileName));
						Log.info(TAG, "localTag : " + localTag);
						// if (!eTag.equals(localTag)) {
						if (!eTag.equalsIgnoreCase(localTag)) {
							Log.error(TAG, "eTag not match");
							// throw new IOException("eTag not match");
							code = ETAG_ERROR;
							return ETAG_ERROR;
						} else {
							Log.info(TAG, "etag check ok");
						}
					}
				}
				Log.info(TAG, "down file is : " + fileName);
				if (null != fileName) {

					Log.info(TAG, "reset file");
					file = new FileAdapter(tempDirectory + fileName);
				} else {
					// throw new IOException("down load file failed");
					code = GENERAL_ERROR;
					return GENERAL_ERROR;
				}
			}
		} else { // it is a folder or a file content length 0
			if (!downinfo.name.endsWith(File.separator)) {// a file no content
				// ,directory
				final String temp = String.valueOf(System.currentTimeMillis())
						+ "nullFile.temp00";//
				File tempFile = new File(tempDirectory + temp);
				if (!tempFile.exists()) {
					if (tempFile.createNewFile()) {
						Log.info(TAG, "null file");
						file = new FileAdapter(tempDirectory + temp);
					}
				}
			}
		}
		if (downinfo.name != null && !hasExist
				&& (null != file || Long.valueOf(downinfo.size) == 0)) {
			boolean bMk = false;
			if (downinfo.name.endsWith(File.separator)) {
				Log.info(TAG, "a folder : " + downinfo.name);
				File fileFolder = new File(directory + downinfo.name);
				bMk = fileFolder.mkdirs();
				if (bMk) {
					Log.info(TAG, " mkdir ok");
					EbenFileLog.recordSyncLog("down: cloud --> " + directory
							+ downinfo.name);
				} else {
					Log.info(TAG, "failed to mkdir");
				}
			} else if (downinfo.name.contains("/")) {
				// if(newName.)
				String folder = downinfo.name.substring(0,
						downinfo.name.lastIndexOf("/"));// tt/yy
				String tempNewFileName = downinfo.name.substring(
						downinfo.name.lastIndexOf("/"), downinfo.name.length());// /host.txt
				Log.info(TAG, "folder is " + folder + ", tempNewFileName is "
						+ tempNewFileName);
				File fileFolder = new File(directory + folder);
				bMk = fileFolder.mkdirs();
				if (bMk) {
					Log.info(TAG, " mkdir ok");
				} else {
					Log.info(TAG, "failed to mkdir");
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
					Log.info(TAG, "rename is ok : " + nFileName);
					EbenFileLog.recordSyncLog("down: cloud --> " + nFileName);
					new File(backup).delete();
				} else {
					new File(backup).renameTo(new File(nFileName));
					// throw new IOException("rename failed");
					EbenFileLog
							.recordSyncLog("down: rename failed in local -- "
									+ nFileName);
					Log.error(TAG, "rename failed");
					return GENERAL_ERROR;
				}
			} else {
				if (file.rename(directory + downinfo.name)) {
					Log.info(TAG, "rename ok " + directory + downinfo.name);
					EbenFileLog.recordSyncLog("down: cloud --> " + directory
							+ downinfo.name);
				} else {
					if (new File(directory + downinfo.name).isDirectory()) {
						EbenFileLog.recordSyncLog("down a wrong key "
								+ downinfo.name);
						Log.error(TAG, "down a wrong key " + downinfo.name);
					} else {
						code = GENERAL_ERROR;
						throw new IOException(downinfo.name + " rename failed ");
					}
				}
			}

		}

		// Apply the modified date if present
		FileAdapter newFile = new FileAdapter(directory + downinfo.name);
		if (newFile != null && newFile.exists()) {
			if (newFile.isSetLastModifiedSupported()
					&& downinfo.modified != null) {
				if (newFile.setLastModified(Long.valueOf(downinfo.modified))) {
					Log.info(TAG, "set LastModified ok");
					EbenFileLog.recordSyncLog("down: cloud --> " + directory
							+ downinfo.name + ", set lastModified local : "
							+ downinfo.modified);
				} else {
					Log.error(TAG, "failed to set last modified");
				}
			}
			newFile.close();
		} else {
			Log.error(TAG,
					"error!! , not exist, maybe same file in local ,but error in process");
			code = GENERAL_ERROR;
			return GENERAL_ERROR;
		}
//		ExSyncManager.i().notifyItemResult(appSource, downinfo.name,
//				SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END, 'U');
//		if (downinfo.name.contains(File.separator)) {
//			String folder = downinfo.name.substring(0,
//					downinfo.name.lastIndexOf(File.separator) + 1);
//			ExSyncManager.i().notifyItemAction(appSource, folder,
//					SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END);
//		}
		}  catch (SyncException syncException) { //SyncException

			throw syncException;
		}catch(Exception e) {
//			Log.error(TAG, e.printStackTrace();
			Log.error(TAG, e.toString());
//			e.printStackTrace();
			
		} finally{
//			int status;
//			if(code == OK) {
//				status = SyncSource.STATUS_SUCCESS;
//			} else {
//				
//			}
//			ExSyncManager.i().notifyItemResult(appSource, downinfo.name,
//					SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END, 'U');
			if (downinfo.name.contains(File.separator)) {
				String folder = downinfo.name.substring(0,
						downinfo.name.lastIndexOf(File.separator) + 1);
//				ExSyncManager.i().notifyItemResult(appSource, folder,
//						SyncSource.STATUS_SUCCESS, ExSyncManager.SYNC_END,'U');
			}
		}
		return OK;
	}
}
