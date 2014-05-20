package cn.eben.android.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder;

import com.funambol.util.Log;



public class CMDExecute {
	private static final String TAG_LOG = "CMDExecute";
	
	public synchronized String run ( String [] cmd,String workdirectory)
		throws IOException {
		
		StringBuilder result = new StringBuilder();
	
		try {
			ProcessBuilder builder = new ProcessBuilder( cmd );		
			if ( workdirectory != null )
				builder.directory ( new File ( workdirectory ) ) ;
				
			builder.redirectErrorStream (true) ;
			Process process = builder.start ( ) ;	
			InputStream in = process.getInputStream( ) ;		
			
			byte[] re = new byte[1024] ;
			
			int len = 0;
			while((len = in.read(re)) != -1) {		
				result.append(new String(re,0,len));			
			}
			
		    in.close();
		
		} catch (Exception e) {
			Log.error(TAG_LOG,"failed to execute the cmd: " + cmd +" " +workdirectory);
			
		}
		
		return result.toString() ;
	}
}
