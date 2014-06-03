package cn.eben.android.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Xml;

public class CallLogUtil {
	private static XmlSerializer getXmlSerializer(Context context,
			FileWriter filewriter) throws IllegalArgumentException,
			IllegalStateException, IOException {
		XmlSerializer xmlserializer = createXmlSerializer(context);
		xmlserializer.setFeature(
				"http://xmlpull.org/v1/doc/features.html#indent-output", true);
		xmlserializer.setOutput(filewriter);
		xmlserializer.startDocument("UTF-8", Boolean.valueOf(true));
		// xmlserializer.comment((new
		// StringBuilder("File Created By ")).append(ApplicationHelper.getApplicationName()).append(" v").append(context.getString(R.string.app_version_name)).append(" on ").append((new
		// SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)).format(new
		// Date())).toString());

		xmlserializer.startTag("", getRootElementName());
		return xmlserializer;
	}

	protected static XmlSerializer createXmlSerializer(Context context) {
		return Xml.newSerializer();
	}

	protected static String getRootElementName() {
		return "calls";
	}

	private void getCallDetails(Context contoxt) {

		StringBuffer sb = new StringBuffer();
		Cursor cur = contoxt.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null,
				android.provider.CallLog.Calls.DATE + " DESC");

		int number = cur.getColumnIndex(CallLog.Calls.NUMBER);
		int duration = cur.getColumnIndex(CallLog.Calls.DURATION);
		sb.append("Call Details : \n");
		while (cur.moveToNext()) {
			String phNumber = cur.getString(number);
			String callDuration = cur.getString(duration);
			String dir = null;

			sb.append("\nPhone Number:--- " + phNumber
					+ " \nCall duration in sec :--- " + callDuration);
			sb.append("\n----------------------------------");
		}
		cur.close();
		// call.setText(sb);
	}

	public static boolean backupCalllog(Context context, String name) {
		FileWriter filewriter;
		XmlSerializer xmlserializer;
		File file1 = new File(name);
		file1.getParentFile().mkdirs();

		if (!file1.exists()) {
			try {
				file1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		try {

			filewriter = new FileWriter(file1);
			xmlserializer = getXmlSerializer(context, filewriter);

			Cursor cursor = context.getContentResolver().query(
					CallLog.Calls.CONTENT_URI, null, null, null,
					android.provider.CallLog.Calls.DATE + " DESC");

			xmlserializer.attribute("", "count",
					Integer.toString(cursor.getCount()));
			
			int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
			int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
			int date = cursor.getColumnIndex(CallLog.Calls.DATE);
			int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
			
			while(cursor.moveToNext()) {
				String phNumber = cursor.getString(number);
				String callDuration = cursor.getString(duration);
				String calldate = cursor.getString(date);
				String calltype = cursor.getString(type);
				xmlserializer.startTag("", "call");
				
				xmlserializer.attribute("", "number", phNumber);
				xmlserializer.attribute("", "duration", callDuration);
				xmlserializer.attribute("", "date", calldate);
				xmlserializer.attribute("", "type", calltype);
				
				xmlserializer.endTag("", "call");
			}
			

			// as[0] = "number";
			// as[1] = "duration";
			// as[2] = "date";
			// as[3] = "type";

			// xmlserializer.attribute("", "readable_date",
			// dateformat.format(new Date(Long.parseLong(getColumnValue(
			// cursor, "date")))));
			// xmlserializer.attribute("", "contact_name",
			// getContactName(context, cursor.getString(i)));

			
			// xmlserializer.endTag("", s);
			xmlserializer.endDocument();
			xmlserializer.flush();
			filewriter.close();
			cursor.close();
			cursor = null;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	

		

}
