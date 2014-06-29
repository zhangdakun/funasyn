package cn.eben.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.funambol.android.App;
import com.funambol.util.Log;

public class SmsUtil {
	public static final String ADDRESS = "address";
	public static final String BODY = "body";
	public static final String DATE = "date";
	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;
	public static final String MODE = "band";
	public static final String PERSON = "person";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final Uri smsUri = Uri.parse("content://sms/");
	public static final String vmgAddress = (new StringBuilder(
			String.valueOf(Environment.getExternalStorageDirectory()
					.getAbsolutePath()))).
					append("/backup/sms.vmg")
			.toString();
	static String TAG = "SmsUtil";
	
	public static long dateToTime(String s) {
//		SimpleDateFormat simpledateformat = new SimpleDateFormat(
//				"EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMddHHmmss");
		long l;
		try {
			l = simpledateformat.parse(s).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			l = System.currentTimeMillis();
		}

		return l;

	}

	private static String formatDate(long time) {
		String s;
		if (time > 0L)
			s = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date(time));
		else
			s = "";
		return s;
	}

	public static List getBackup(String vmg) {
		ArrayList arraylistVmg = new ArrayList();
		;
		BufferedReader bufferedreader = null;
		FileInputStream fileinputstream = null;

		File file = new File(vmg);
		if (!file.exists()) {
			return arraylistVmg;
		}

		try {

			fileinputstream = new FileInputStream(file);
			bufferedreader = new BufferedReader(new InputStreamReader(
					fileinputstream));
			boolean flag1;
			boolean flag2;
			SmsMessage smsmessage;
			StringBuffer stringbuffer = null;
			flag1 = false;
			flag2 = false;
			smsmessage = null;

			String s;
			while(null != (s = bufferedreader.readLine())) {

//			if (null == s) {
//				return arraylistVmg;
//			}
			if (s.contains("BEGIN:VMSG")) {
				flag1 = true;
				stringbuffer = new StringBuffer();
				smsmessage = new SmsMessage();

			} else if (flag1)
				if (s.contains("X-MESSAGE-TYPE:")) {
					int k = 1 + s.indexOf(":");
					if (s.substring(k, s.length()).equals("DELIVER"))
						smsmessage.setMESSAGE_TYPE("1");
					else if (s.substring(k, s.length()).equals("SUBMIT"))
						smsmessage.setMESSAGE_TYPE("2");
					else
						smsmessage.setMESSAGE_TYPE("2");
				} else if (s.contains("X-MA-TYPE:"))
					smsmessage.setMA_TYPE(s.substring(1 + s.indexOf(":"),
							s.length()));
				else if (s.contains("TEL:"))
					smsmessage.setTEL(s.substring(4, s.length()));
				else if (s.contains("BEGIN:VBODY"))
					flag2 = true;
				else if (s.contains("END:VBODY"))
					flag2 = false;
				else if (s.contains("END:VMSG")) {
					arraylistVmg.add(smsmessage);
					flag1 = false;
				} else if (flag2)
					if (s.contains("Date")) {
						smsmessage.setVTIME(s.substring(4, s.length()).trim());
					} else {
						stringbuffer.append(s);
						smsmessage.setVBODY(stringbuffer.toString());
					}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != fileinputstream) {
				try {
					fileinputstream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (null != bufferedreader) {
				try {
					bufferedreader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return arraylistVmg;
	}

	public static String getCurrentTime() {
		return (new SimpleDateFormat("MMM dd yyyy HH:mm:ss aa zzz"))
				.format(new Date());
	}

	public static String getDate() {
		return (new SimpleDateFormat("yyyy-MM-dd hh-mm", Locale.getDefault()))
				.format(new Date(System.currentTimeMillis()));
	}

	public static ArrayList getMessages(Context context) {
		ArrayList arraylist = new ArrayList();
		String[] _tmp = (String[]) null;
		String as[] = new String[6];
		as[0] = "_id";
		as[1] = "address";
		as[2] = "date";
		as[3] = "type";
		as[4] = "body";
		Cursor cursor = context.getContentResolver().query(smsUri, as,
				"type !=3", null, null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				SmsMessage smsmessage = new SmsMessage();
				smsmessage.setTEL(cursor.getString(1));
				smsmessage.setVTIME(formatDate(cursor.getLong(2)));
				int i = cursor.getInt(3);
				if (i == 1)
					smsmessage.setMESSAGE_TYPE("DELIVER");
				else if (i == 2)
					smsmessage.setMESSAGE_TYPE("SUBMIT");
				else
					smsmessage.setMESSAGE_TYPE("SUBMIT");
				smsmessage.setVBODY(cursor.getString(4));
				if(checkmsg(smsmessage)){
					arraylist.add(smsmessage);
				}
			}
			cursor.close();
		}
		return arraylist;
	}
	private static boolean checkmsg(SmsMessage smsmessage) {
		// TODO Auto-generated method stub
		boolean isok = false;
		
//	    private String MA_TYPE="";
//	    private String MESSAGE_TYPE="";
//	    private String TEL="";
//	    private String VBODY="";
//	    private String VTIME="";

		if(null != smsmessage.getTEL() 
				&& !"".equalsIgnoreCase(smsmessage.getTEL())  &&
		  null != smsmessage.getVBODY()
				&& !"".equalsIgnoreCase(smsmessage.getVBODY())){
			isok = true;
		}
		
		return isok;
	}

	public static boolean isSmsExist(SmsMessage smsmessage,Context context) {
		boolean isExist = false;
		
		String as[] = new String[1];
		as[0] = "_id";
//		String as1[] = new String[6];
//		as1[0] = "_id";
//		as1[1] = "address";
//		as1[2] = "date";
//		as1[3] = "type";
//		as1[4] = "body";
		String as1[] = new String[3];
		as1[0] = smsmessage.getTEL();
		as1[1] = String.valueOf(SmsUtil.dateToTime(smsmessage.getVTIME()));


		
		Cursor cursor = context.getApplicationContext().getContentResolver().query(smsUri, as,
				"type !=3 AND address = ?  AND date = ?  ", null, null);
		
		if (cursor == null || cursor.getCount() <= 0) {
			cursor.close();
			return false;
		} else {
			isExist = true;
		}
		
		
		
		return isExist;

	}
	public static boolean isSmsExist(SmsMessage smsmessage,ArrayList<SmsMessage> arraylist) {
		boolean isExist = false;
		try {
			

		if(null != arraylist && !arraylist.isEmpty()) {
			for(SmsMessage msg:arraylist) {
				if(msg.getTEL().equalsIgnoreCase(smsmessage.getTEL())
						&& msg.getVTIME().equalsIgnoreCase(smsmessage.getVTIME())
						&& msg.getVBODY().equalsIgnoreCase(smsmessage.getVBODY())) {
					isExist = true;
					break;
				}
			}
		}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return isExist;
	}
	
	public static void doRestoreVMG(Context context,String vmg) {
        List list1;
        list1 = SmsUtil.getBackup(vmg);
        if(list1.isEmpty()) {
        	Log.error("smsutils", "sms restore null");
        	return;
        }

        ArrayList list2 = getMessages(context);
        int i = 0;

        ContentValues contentvalues;
//        try
//        {
//            Uri uri = ContentUris.withAppendedId(android.provider.Telephony.Sms.Conversations.CONTENT_URI, -1L);
//            ContentResolver contentresolver = context.getContentResolver();
//            int j = contentresolver.delete(uri, null, null);
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }

        for(i=0;i<list1.size();i++) {
        	if(!checkmsg((SmsMessage)list1.get(i))) {
        		continue;
        	}
        	if(isSmsExist((SmsMessage)list1.get(i), list2)) {
        		continue;
    	}
//    	if(isSmsExist((SmsMessage)list1.get(i), context)) {
//    		Log.debug(TAG,"find same sms");
//    		break;
//    	}
	        contentvalues = new ContentValues();
	        contentvalues.put("address", ((SmsMessage)list1.get(i)).getTEL());
	        contentvalues.put("date", Long.valueOf(SmsUtil.dateToTime(((SmsMessage)list1.get(i)).getVTIME())));
	        contentvalues.put("read", Integer.valueOf(1));
	        contentvalues.put("status", Integer.valueOf(-1));
	        contentvalues.put("type", ((SmsMessage)list1.get(i)).getMESSAGE_TYPE());
	
	        contentvalues.put("body", ((SmsMessage)list1.get(i)).getVBODY());
	        context.getContentResolver().insert(SmsUtil.smsUri, contentvalues);
        }

    }
//	public static String backupSms( ) {
//		ArrayList list = SmsUtil.getMessages(App.i()
//				.getApplicationContext());
//		String filename = Contants.backUpRoot +formatDate(System.currentTimeMillis())+ "backup.vmg";
//		File file1 = new File(filename);
//		file1.getParentFile().mkdirs();
//		if(list.isEmpty()) {
//			Log.info("smsutil", "not found sms info,return a null file");
//			if(!file1.exists()) {
//				try {
//					file1.createNewFile();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			return filename;
//		}
//		FileOutputStream fileoutputstream = null;
//		OutputStreamWriter outputstreamwriter = null;
//		try {
//			fileoutputstream = new FileOutputStream(file1);
//
//			outputstreamwriter = new OutputStreamWriter(fileoutputstream,
//					"UTF-8");
//			int i = 0;
//			for(i=0;i<list.size();i++) {
//			try {
//				outputstreamwriter
//						.write((new StringBuilder(
//								"BEGIN:VMSG\nVERSION: 1.1\nX-IRMS-TYPE:MSG\nX-MESSAGE-TYPE:"))
//								.append(((SmsMessage) list.get(i))
//										.getMESSAGE_TYPE())
//								.append("\n")
//								.append("X-MA-TYPE:")
//								.append("")
//								.append("\nBEGIN:VCARD\nVERSION: 2.1\nTEL:")
//								.append(((SmsMessage) list.get(i)).getTEL())
//								.append("\nEND:VCARD\nBEGIN:VENV\nBEGIN:VBODY\nDate ")
//								.append(((SmsMessage) list.get(i)).getVTIME())
//								.append("\n")
//								.append(((SmsMessage) list.get(i)).getVBODY())
//								.append("\nEND:VBODY\nEND:VENV\nEND:VMSG\n")
//								.toString());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		} finally {
//			if(null != outputstreamwriter) {
//				try {
//					outputstreamwriter.close();
//					outputstreamwriter = null;
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		
//		return filename;
//
//	}
	
	public static String backupSms(String filename) {
		ArrayList list = SmsUtil.getMessages(App.i()
				.getApplicationContext());
//		String filename = Contants.backUpRoot +formatDate(System.currentTimeMillis())+ "backup.vmg";
		File file1 = new File(filename);
		file1.getParentFile().mkdirs();
		if(list.isEmpty()) {
			Log.info("smsutil", "not found sms info,return a null file");
			if(!file1.exists()) {
				try {
					file1.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return filename;
		}
		FileOutputStream fileoutputstream = null;
		OutputStreamWriter outputstreamwriter = null;
		try {
			fileoutputstream = new FileOutputStream(file1);

			outputstreamwriter = new OutputStreamWriter(fileoutputstream,
					"UTF-8");
			int i = 0;
			for(i=0;i<list.size();i++) {
			try {
				outputstreamwriter
						.write((new StringBuilder(
								"BEGIN:VMSG\nVERSION: 1.1\nX-IRMS-TYPE:MSG\nX-MESSAGE-TYPE:"))
								.append(((SmsMessage) list.get(i))
										.getMESSAGE_TYPE())
								.append("\n")
								.append("X-MA-TYPE:")
								.append("")
								.append("\nBEGIN:VCARD\nVERSION: 2.1\nTEL:")
								.append(((SmsMessage) list.get(i)).getTEL())
								.append("\nEND:VCARD\nBEGIN:VENV\nBEGIN:VBODY\nDate ")
								.append(((SmsMessage) list.get(i)).getVTIME())
								.append("\n")
								.append(((SmsMessage) list.get(i)).getVBODY())
								.append("\nEND:VBODY\nEND:VENV\nEND:VMSG\n")
								.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			if(null != outputstreamwriter) {
				try {
					outputstreamwriter.close();
					outputstreamwriter = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		return filename;

	}
}
