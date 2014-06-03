package cn.eben.android.util;

import java.io.File;
import java.io.FileFilter;

import android.provider.SyncStateContract.Constants;
import android.provider.SyncStateContract.Helpers;
import cn.eben.android.source.edisk.EdiskSyncSource;

public class EdiskFileFilter implements FileFilter {
	private String source;
	String temp;

	public EdiskFileFilter(String source,String temp) {
		super();
		this.source = source;
		this.temp=temp;
	}

	public boolean accept(File pathname) {
//		boolean bAccept = true;
		if(!pathname.exists()) {
			return false;
		}
		if(pathname.getPath().contains("LOST.DIR")) {
			return false;
		}
		if(pathname.getName().startsWith(EdiskSyncSource.WAVELINE)
					||pathname.getPath().contains(File.separator+EdiskSyncSource.WAVELINE) ){
			return false;
		}
		if (pathname.isDirectory()) {
			// for (int nCnt = 0; nCnt < Constants.ItemPathArray.length; nCnt++)
			// {
			// if (Constants.OFFICE_FLAG.equals(object)
			// Helpers.isCheckFile(pathname.getPath(),
			// Constants.ItemPathArray[nCnt])) {
			// return false;
			// }
			// } {
			if(pathname.toString().contains(temp)) {
				return false;
			}
			
		}else {
			if(pathname.toString().endsWith(".mp4")) {
				return false;
			}
		}
		
		
		return true;
	}

}
