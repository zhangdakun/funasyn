package cn.eben.android.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import android.os.Environment;
//import android.os.SystemProperties;

import com.funambol.util.Log;
import cn.eben.android.util.CMDExecute;

public class SystemInfo {
	private static final String TAG_LOG = "SystemInfo";

	public String getCPUSerial() {

		String result = "";
		String cpuAddress = "0000000000000000";
		CMDExecute cmdexe = new CMDExecute();
		try {
		    String[] args = { "/system/bin/cat", "/persist/.sn.info" };
			result = cmdexe.run(args, "/system/bin/");
			if(null != result) {

			    cpuAddress = result.substring(0, cpuAddress.length());
				if(cpuAddress.contains("persist")) {
					Log.error(TAG_LOG, "can not read .sn.info,");
					cpuAddress = EbenHelpers.getDevid();
					
				}

				

			}
			
		} catch (IOException ex) {
			Log.error(TAG_LOG, "failed to get cpuinfo!");
		}
		return cpuAddress;	  
	}
	
	public static String getManufactory() {
		String man = null;
		man = "Eben";
		return man;
	}
	 public static String readFileByLines() {
         String TF_PATH = Environment.getExternalStorageDirectory().getPath();
            String DB_PATH = TF_PATH + "/eben/";
            String fileName = DB_PATH+"sn.txt";
            File file = new File(fileName);
            BufferedReader reader = null;
            StringBuffer allStr = new StringBuffer();
            try {

                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                int line = 1;
                while ((tempString = reader.readLine()) != null) {

//                  System.out.println("line " + line + ": " + tempString);
                    allStr.append(tempString);
                    line++;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
            return allStr.toString();
        }
	    static File getDirectory(String variableName, String defaultPath) {	    	
//	    	String serial = null;
//	    	try {
//	    	    Class<?> c = Class.forName("android.os.SystemProperties");
//	    	    Method get = c.getMethod("get", String.class);
//	    	    serial = (String) get.invoke(c, "ro.serialno");
//	    	}
//	    	catch (Exception ignored) {
//
//	    	}
	    	String path = null;
	    	try {
	    	    Class<?> c = Class.forName("android.os.Environment");
	    	    Method get = c.getMethod("getMydocStorage");
	    	    path = (String) get.invoke(c).toString();
	    	}
	    	catch (Exception ignored) {
	    		ignored.printStackTrace();
	    	}
	    	if(null == path) {
	         path = System.getenv(variableName);
	    	}
	        return new File(path);
	      }
	    public static File getMydocStorage() {
//	    	Environment.getMydocStorage().toString();
	        return EXTERNAL_MY_DOC;
	      }
	    private static final File EXTERNAL_MY_DOC = getDirectory("EXTERNAL_MYDOC", "/mnt/sdcard");
}
