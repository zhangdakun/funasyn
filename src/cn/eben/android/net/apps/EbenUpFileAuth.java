package cn.eben.android.net.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.client.source.AppSyncSource;
import com.funambol.sync.SyncException;
import com.funambol.sync.SyncSource;
import com.funambol.util.Log;


import cn.eben.android.net.HttpJSONServiceBase;
import cn.eben.android.source.edisk.EbenUpFile;
import cn.eben.android.source.edisk.EdiskSyncSource;
import cn.eben.android.source.edisk.UpfileInfo;

import cn.eben.android.util.DiskMemory;
import cn.eben.android.util.EbenFileLog;
import cn.eben.android.util.EbenHelpers;
import cn.eben.android.util.MD5Util;

public class EbenUpFileAuth extends HttpJSONServiceBase {
	private final String TAG_LOG = "EbenUpFileAuth";
	private ArrayList<UpfileInfo> fileList;
	private JSONArray jsonList = null;
	private String source;
	private String directory;
	private String tempdirectory;
	private AppSyncSource appSource;
	private Hashtable<String, Object> upList = null; // for up list up file list
	
	class FileUri {
		ArrayList<String> urlList;
		String uri;
		
	}
	private void addupfileList(String path,ArrayList<String> urlList,String uri) {
		if(null ==  upList) {
			upList = new Hashtable();
		}
		FileUri upuri = new FileUri();
		upuri.uri = uri;
		upuri.urlList = urlList;
		upList.put(path, upuri);
	}
	/**
	 * begin up file 
	 */
	private void beginUPfile() {
		
		if(null == upList || upList.isEmpty()) {
			return;
		}
		EdiskSyncSource.upCount = upList.size();// sum of up files
		EdiskSyncSource.curUpCount = 0;
		Log.info(TAG_LOG,"beginUPfile count : "+EdiskSyncSource.upCount);
		for (Iterator it = upList.keySet().iterator(); it.hasNext();) {

			String luid = (String) it.next();
			FileUri fUri = (FileUri) upList.get(luid);
			
			upfileContent(luid,fUri);

//			hm.put(key, value);
		}

	
	}

	private void upfileContent(String luid, FileUri fUri) {
		// TODO Auto-generated method stub
		//lierbao
		String fileName = luid;//EbenHelpers.decodeKey(luid);
		
		String uri = fUri.uri;
		ArrayList<String> urlList = fUri.urlList;
		Log.info(TAG_LOG,"upfileContent");
		boolean ok = false;
		if (EdiskSyncSource.copyFile(directory + fileName,
				tempdirectory + fileName)) {
			String localMd5 = MD5Util.md5(new File(tempdirectory + fileName));
			String fileMd5 = getTag(luid);
//			int retry = 0;
			if(null == fileMd5 || !fileMd5.equalsIgnoreCase(localMd5)) {
				Log.error(TAG_LOG,"error !!! md5 not match ,some error in copy or md5 calcualte");
				Log.error(TAG_LOG,"fileMd5 : "+fileMd5+", localMd5: "+localMd5);
//				if(DiskMemory.freeMemory(directory) <= EdiskSyncSource.MAX_FILE_SIZE) {
//					ExSyncManager.i().toastInfo(R.string.mindcloud_localmemory_full);
//
//					throw new SyncException(ExternalEntryConst.LOCAL_CLIENT_FULL_ERROR,"local disk full");
//
//				}
				syncStart(source);
				return;
			
			}
//			ExSyncManager.i().notifyItemAction(appSource, luid/*EbenHelpers.decodeKey(luid)*/, -1, ExSyncManager.SYNC_START);
			if(luid.contains(File.separator)) {
				String folder = luid.substring(0, luid.lastIndexOf(File.separator)+1);
//				ExSyncManager.i().notifyItemAction(appSource, folder, -1, ExSyncManager.SYNC_START);
			}
			EdiskSyncSource.curUpCount++;
			
//			EbenExsyncProgress prg = new EbenExsyncProgress(1, EdiskSyncSource.upCount, EdiskSyncSource.curUpCount, appSource.getSyncSource()
//					.getName(), appSource.getName(), /*EbenHelpers.decodeKey(luid)*/luid, 0);
//			prg.setstage("up");
//			prg.setItemSize(String.valueOf(getSize(luid)));
//			prg.send();
			ok = EbenUpFile.upfile(urlList,
					tempdirectory + fileName, fileMd5);
			if (ok) {
				setSuccessList(luid, uri);
				if(new File(tempdirectory + fileName).delete())  {
					Log.info(TAG_LOG, "remove file : " +tempdirectory + fileName);
					EbenFileLog.recordSyncLog("delete : "+tempdirectory + fileName);
				}else {
					Log.error(TAG_LOG, "temp file remove error : "+tempdirectory + fileName);
				}
			}
			int itemStatus = SyncSource.SUCCESS_STATUS;
			if(!ok) {
				itemStatus = SyncSource.ERROR_STATUS;
//				failcount ++;
			}
//			ExSyncManager.i().notifyItemAction(appSource, /*EbenHelpers.decodeKey(luid)*/luid, itemStatus, ExSyncManager.SYNC_END);	
			if(luid.contains(File.separator)) {
				String folder = luid.substring(0, luid.lastIndexOf(File.separator)+1);
//				ExSyncManager.i().notifyItemAction(appSource, folder,itemStatus, ExSyncManager.SYNC_END);
			}
			
			
			if(!ok/* && failcount>3*/) {
				throw new SyncException(111,"upfile error");
			}
		} else {
			if(DiskMemory.freeMemory(directory) <= EdiskSyncSource.MAX_FILE_SIZE) {
//				ExSyncManager.i().toastInfo(R.string.mindcloud_localmemory_full);
//				ExSyncManager.i().exitApp(new Exception("local disk full"));
				throw new SyncException(112,"local disk full");
//				return;
			}
		}

	}
	public EbenUpFileAuth(String source, String directory, String tempdirectory, AppSyncSource appSource) {
		this.source = source;
		this.directory = directory;
		this.tempdirectory = tempdirectory;
		this.appSource = appSource;
	}

	public void clear() {
		jsonList = null;
	}
	
	public boolean hasItems() {
		boolean bRet = false;
		if(null == jsonList) {
			
			bRet = false;
		} else {
			if(jsonList.length() > 0) {
				bRet = true;
			}
		}
		return bRet;
		
	}
	
/**
 * set need up file list
 * @param luid : file luid
 * @param size file size
 * @param md5 file finger printer md5
 * @param parts large file spite to parts
 * @param parent 
 * @param path 
 */
	public void setList(String luid, Long size, String md5, int parts, String path, String parent) {
		if (null == fileList)
			fileList = new ArrayList<UpfileInfo>();

		UpfileInfo info = new UpfileInfo(luid, md5, size, null,path,parent);
		Log.debug(TAG_LOG,"setList : luid : "+luid+", md5: "+md5);
//		if(fileList.contains(luid))
		fileList.add(info);
		if (null == jsonList) {
			jsonList = new JSONArray();
		}

		JSONObject jo = new JSONObject();
		try {
//			jo.put("luid", luid);
			//lierbao
//			jo.put("luid", path);
			jo.put("luid", EbenHelpers.encodeKey(path));
			jo.put("size", size);
			jo.put("md5", md5);
			jo.put("parts", parts);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jsonList.put(jo);
	}
	public ArrayList<UpfileInfo> getFileList() {
	return fileList;
}
public void setFileList(ArrayList<UpfileInfo> fileList) {
	this.fileList = fileList;
}
	/**
	 * set file list to null, 
	 */
	public void resetFileList() {
		fileList = null;
	}

	// {"ver":1,"username":"<userid>","device":"<deviceid>","op":"upfileauth","param":[
	// {"app":"notepad","list":[{"luid":"<LUID>","size":1234,

//	{"ver":1,"username":"<userid>","device":"<deviceid>","op":"upfileauth","param":[

	@Override
	public JSONObject processRequestJSON() throws Exception {
		// TODO Auto-generated method stub
		AppInitializer initializer = App.i().getAppInitializer();
		String username = initializer.getUserName();
		String devId = initializer.getConfiguration().getDeviceConfig()
				.getDevID();
		JSONObject jo = new JSONObject();
		jo.put("ver", JSONVERSION);

		jo.put("username", username);
		jo.put("device", devId);
		jo.put("op", OP_EBEN_UPFILE);

		JSONObject jApp = new JSONObject();
		jApp.put("app", source);

		jApp.put("list", jsonList);

		if(null == devId || "".equals(devId)
				|| null == username || "".equals(username)
				|| null == jsonList|| "".equals(jsonList)) {
			throw new SyncException(10000,"null devid or username");
		}
		
		// JSONObject japps = new JSONObject();

		JSONArray jArrayApp = new JSONArray();
		jArrayApp.put(jApp);

		jo.put("param", jArrayApp);

		return jo;
	}

	// {"result":"ok","data":[{"app":"edisk","list":[{"luid":"MTIzLy5zeW5jfg==","result":"ok","uurl":"aHR0cDovL3N0b3JhZ2UuYWxpeXVuLmNvbS9lZGlzay8xNTgxMDM4ODg4Mi9EQzkzMzg5MkU3NUVBREI1RTZBMENBMjRBRjJCMUUwMy0wP0V4cGlyZXM9MTM0ODk3NjM0NyZPU1NBY2Nlc3NLZXlJZD1na3J0YWdjemFscmM1dXd2ajQzbm56anUmU2lnbmF0dXJlPTBwM0huTkZtNk4xNEVYd3hrenh3Y0ViZzlMOCUzRA=="}]}],
	// "code":0}

	// {"result":"ok","data":
	// [{"app":"edisk","list":[{"luid":"SU1HXzIwMTIxMDI5XzE2MzI0NC5qcGc=","result":"fileexist",
	// "uurl":"","uri":"OSS://product-test/15810388882/7CDA3AFD2442682AEBB81D244E2D304F-F55A7"},
	// {"luid":"SU1HXzIwMTIxMDI3XzE0MzcyNC5qcGc=","result":"fileexist","uurl":"",
	// "uri":"OSS://product-test/15810388882/B5C164086FF70F3BED102566CF406FA9-17EBE"},
	// {"luid":"MjAxMi0xMS0wNiAxNDM1MTUubG9n","result":"fileexist","uurl":"",
	// "uri":"OSS://product-test/15810388882/768FECEEF866102B7A149DD917E2A5FA-A87A3A"},
	// {"luid":"MTM1MTMxNTIyODE1Mi5qcGc=","result":"fileexist","uurl":"",
	// "uri":"OSS://product-test/15810388882/8804521134EE6DD8DEFE80EF42816D5F-1657A"},
	// {"luid":"SU1HXzIwMTIxMDI5XzE2MzI0NyAtIOWJr+acrC5qcGc=","result":"fileexist","uurl":"",
	// "uri":"OSS://product-test/15810388882/02B7EECC157AE82F57BBADAFFE16BD4B-D88C"},
	// {"luid":"SU1HXzIwMTIxMDI5XzE2MzI0Ny5qcGc=","result":"fileexist","uurl":"",
	// "uri":"OSS://product-test/15810388882/02B7EECC157AE82F57BBADAFFE16BD4B-D88C"},
	// {"luid":"5LiA5Liq5pyJ6Laj55qE5Lmm5rOV5ZKM5raC6bim5bel5YW3LmFwaw==","result":"fileexist",
	// "uurl":"","uri":"OSS://product-test/15810388882/A8664096E7CC6C4B7AE306009680A597-63211"}]}],
	// "code":0}

	

	@Override
	public void processResponesJSON(String json) throws Exception {
		// TODO Auto-generated method stub
		Log.debug(TAG_LOG, "processResponesJSON : " + json);

		JSONObject jo = new JSONObject(json);
		if (0 != jo.getLong("code")) {
			Log.error(TAG_LOG, "error code!!");
			throw new SyncException(0, "upfile err");// for backup should throw ,have to send all backup file ok
//			return;
		}
		int failcount = 0;
		JSONArray jData = jo.getJSONArray("data");

		for (int i = 0; i < jData.length(); i++) {
			String app = jData.getJSONObject(i).getString("app");
			JSONArray list = jData.getJSONObject(i).getJSONArray("list");

			for (int j = 0; j < list.length(); j++) {
				String result = list.getJSONObject(j).getString("result");
				String luid = list.getJSONObject(j).getString("luid");  // this luid is a path
				luid = EbenHelpers.decodeKey(luid);
				int code = list.getJSONObject(j).getInt("code");

				String uri = list.getJSONObject(j).getString("uri");
//				ExSyncManager.i().notifyItemAction(appSource, EbenHelpers.decodeKey(luid), -1, ExSyncManager.SYNC_START);
				boolean ok = true;
				if (5636 == code || "File already exists.".equalsIgnoreCase(result)) {
					setSuccessList(luid, uri);
				} else {
					JSONArray uurl = list.getJSONObject(j).getJSONArray("uurl");
					String url=null;
					ArrayList<String> urlList = new ArrayList<String>();
					for(int k=0;k<uurl.length();k++) {
						int part = uurl.getJSONObject(k).getInt("part");
//						if(k != part) {
//							Log.error(TAG_LOG,"error!! parse part ");
//							ExSyncManager.exitApp(null);
//							return;
//						}

						url = uurl.getJSONObject(k).getString("url");
						urlList.add(EbenHelpers.decodeKey(url));
					}
//					String url = list.getJSONObject(j).getString("uurl");
					
					if (!"".equals(luid) && !"".equals(url)) {
						addupfileList(luid,urlList,uri);
					}
				}

			}
		}
		
		beginUPfile();
	}

	private ArrayList<UpfileInfo> successList;

	public ArrayList<UpfileInfo> getSuccessList() {
		if(null == successList) {
			successList = new ArrayList<UpfileInfo>();
		}
		return successList;
	}

//	public void setSuccessList(ArrayList<UpfileInfo> successList) {
//		if (null == successList) {
//			successList = new ArrayList<UpfileInfo>();
//		}
//		this.successList = successList;
//	}
/**
 * 
 */
	public void resetSuccessList() {
		successList = null;
	}
	private void setSuccessList(String path, String uri) {
		// TODO Auto-generated method stub
		if (null == successList) {
			successList = new ArrayList<UpfileInfo>();
		}
		for (UpfileInfo info : fileList) {
			if (path.equals(info.path)) {
				successList
						.add(new UpfileInfo(info.luid, info.etag, info.size, uri,path,info.parent));
				break;
			}
		}
	}

	private long getSize(String path) {
		// TODO Auto-generated method stub
		if (null == successList) {
			successList = new ArrayList<UpfileInfo>();
		}
		for (UpfileInfo info : fileList) {
			if (path.equals(info.path)) {
				return info.size;
			}
		}
		return 0L;
	}
	
	private String getTag(String path) {
		// TODO Auto-generated method stub
		if (null == fileList) {
			fileList = new ArrayList<UpfileInfo>();
		}
		for (UpfileInfo info : fileList) {
			if (path.equals(info.path)) {
				return info.etag;

			}
		}
		return null;
	}
	
	@Override
	public String getRequestURL() {
		// TODO Auto-generated method stub
		// return EbenConst.HTTP_HOST + "/ecsi/device";
		// return "http://42.120.48.113/ecsi/device";
		// return "http://42.121.96.124/funambol/ecsi/device";
		return DS_ECSI_URI;
	}
	
    private void syncStart(String appName) {
        // TODO Auto-generated method stub
//    	Context mContext = App.i().getApplicationContext();
//        Log.info(TAG_LOG, "syncStart, "+appName);
//        Intent intent = new Intent();
//        intent.setAction(ExternalEntryConst.ACTION_START_SYNC);
//        Bundle bl = new Bundle();
//        bl.putString(ExternalEntryConst.KEY_SYNC_ORIGPKG, mContext.getPackageName());
//        bl.putString(ExternalEntryConst.KEY_SYNC_SRCNAME, appName);
//        bl.putString(ExternalEntryConst.KEY_SYNC_TYPE, ExternalEntryConst.APP_SYNC);
//        
//        intent.putExtras(bl);
//        
//        mContext.sendBroadcast(intent);
    }

}
