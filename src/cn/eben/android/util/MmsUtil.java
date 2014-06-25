package cn.eben.android.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.ParseException;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;


public class MmsUtil {

	public static final Uri mmsUri = Uri.parse("content://mms/");
	private static int totalNum;
	private static int curNum;
	public static String TAG = "MmsUtil";
	private static int mmsBoxType;

	public static final Uri mmsInboxUri = Uri.parse("content://mms//inbox");
	public static final Uri mmsSendboxUri = Uri.parse("content://mms//sentbox");

	public String backupMms(Context context,String filename) {
		// ArrayList list = SmsUtil.getMessages(App.getInstance()
		// .getApplicationContext());
//		int mmsInboxSize = getMmsCount(context, "content://mms/inbox");
//		int mmsOutboxSize = getMmsCount(context, "content://mms/sent");
//
//		totalNum = mmsInboxSize + mmsOutboxSize;
//		curNum = 0;
		Log.d(TAG, "backupMms");
//		String filename = Contants.backUpRoot
//				+ SmsUtil.formatDate(System.currentTimeMillis()) + "backup.vmg";
		File file1 = new File(filename);
		file1.getParentFile().mkdirs();

		// if (list.isEmpty()) {
		Log.i("MmsUtil", "not found mms info,return a null file");
		if (!file1.exists()) {
			try {
				file1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// return filename;
		// }
		try {

			OutputStreamWriter outputstreamwriter;
			outputstreamwriter = new OutputStreamWriter(new FileOutputStream(
					file1), "utf-8");

			// for inbox
			try {
				String inbox = "content://mms/inbox";
				String selection = getMmsSelection(inbox);

				Cursor cursor = context.getContentResolver().query(
						Uri.parse(inbox), null, selection, null, "date desc");

				if (cursor != null && cursor.getCount() > 0) {
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
							.moveToNext()) {

						initParams();
						processResults(context, cursor);

						StringBuffer stringbuffer = new StringBuffer("");
						stringbuffer.append("BEGIN:VMSG\r\n");
						stringbuffer.append("VERSION:1.1\r\n");
						makeMmsTelString(true, stringbuffer);
						makeMmsBodyString(true, "X-BOX:INBOX", stringbuffer);
						makeMmsPartString(stringbuffer);
						stringbuffer.append("END:VMSG\r\n");

						String s1 = stringbuffer.toString();

						outputstreamwriter.write(s1);
					}

				}
				cursor.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			try {
				String sentbox = "content://mms/sent";
				String selection = getMmsSelection(sentbox);
				Cursor cursor = context.getContentResolver().query(
						Uri.parse(sentbox), null, selection, null, "date desc");
				if (cursor != null && cursor.getCount() > 0) {
					while (cursor.moveToNext()) {

						processResults(context, cursor);

						StringBuffer stringbuffer = new StringBuffer("");
						stringbuffer.append("BEGIN:VMSG\r\n");
						stringbuffer.append("VERSION:1.1\r\n");
						makeMmsTelString(false, stringbuffer);
						makeMmsBodyString(false, "X-BOX:SENDBOX", stringbuffer);
						makeMmsPartString(stringbuffer);
						stringbuffer.append("END:VMSG\r\n");

						String s1 = stringbuffer.toString();

						outputstreamwriter.write(s1);
					}

					cursor.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			outputstreamwriter.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return filename;
	}

	private void makeMmsPartString(StringBuffer stringbuffer) {
		int i = partsNumber;
		int j = 0;
		do {
			if (j >= i)
				return;
			stringbuffer.append("BEGIN:VPART\r\n");
			appendItem(stringbuffer, "X-MMS-PART-SEQ:", partSeq[j]);
			appendItem(stringbuffer, "X-MMS-PART-CONTENT-TYPE:",
					partContentType[j]);
			appendItem(stringbuffer, "X-MMS-PART-NAME:", partName[j]);
			appendItem(stringbuffer, "X-MMS-PART-CHARSET:", partCharset[j]);
			appendItem(stringbuffer, "X-MMS-PART-CONTENT-DISPOSITION:",
					partContentDisposition[j]);
			appendItem(stringbuffer, "X-MMS-PART-FILENAME:", partFileName[j]);
			appendItem(stringbuffer, "X-MMS-PART-CONTENT-ID:", partContentID[j]);
			appendItem(stringbuffer, "X-MMS-PART-CONTENT-LOCATION:",
					partContentLocation[j]);
			appendItem(stringbuffer, "X-MMS-PART-CT-START:", partCTStart[j]);
			appendItem(stringbuffer, "X-MMS-PART-CT-TYPE:", partCTType[j]);
			appendItem(stringbuffer, "X-MMS-PART-DATA:", partData[j]);
			partData[j] = null;
			appendItem(stringbuffer, "X-MMS-PART-TEXT:", partText[j]);
			stringbuffer.append("END:VPART\r\n");
			j++;
		} while (true);
	}

	public static int getBackupCount(Context context) {
		return getMmsCount(context, "content://mms/inbox")
				+ getMmsCount(context, "content://mms/sent");
	}

	public static int getMmsCount(Context context, String s) {
		int i = 0;
		String s1;
		Cursor cursor;
		s1 = getMmsSelection(s);
		cursor = null;
		cursor = context.getContentResolver().query(Uri.parse(s), null, s1,
				null, "date desc");

		if (null != cursor) {
			i = cursor.getCount();
		}
		if (cursor != null)
			cursor.close();

		return i;

	}

	static String getMmsSelection(String box) {

		String selection = null;
		if (box.equals("content://mms/inbox")) {
			selection = "m_type = 132 OR m_type = 130 OR m_type = 160";
		} else if (box.equals("content://mms/sent")) {//content://mms/sentbox
			selection = "m_type = 128";
		} else if (box.equals("content://mms/drafts")) {
			selection = null;
		}
		return selection;

	}

	public static String export1MmsVmsgStr(int i) {
		// if(i == 0)
		// mmsInboxSize =
		// getMmsIDArray("content://mms/inbox");
		// if(i == mmsInboxSize)
		// getMmsIDArray("content://mms/sent");
		// boolean flag;
		return null;
	}

	private String mmsID;
	private String mmsSubject;
	private String mmsContentType;
	private String mmsContentLocation;
	private String mmsMsgCls;
	private String mmsVersion;
	private String mmsSize;
	private String mmsTransID;
	private int mmsRead;
	private int mmsLocked;

	private String mmsDate;
	private String mmsExpiry;
	private String mmsPriority;
	private String mmsMsgType;
	private String mmsReadReport;
	private String mmsDelReport;
	private String mmsSubjectCharset;
	private int partsNumber;
	private String[] partSeq;
	private String[] partContentType;
	private String[] partName;
	private String[] partCharset;
	private String[] partContentDisposition;
	private String[] partFileName;
	private String[] partContentID;
	private String[] partContentLocation;
	private String[] partCTStart;
	private String[] partCTType;
	private String[] partData;
	private String[] partText;
	private int toPhoneNumberCounts;
	private String[] toPhoneNumber;
	private String fromPhoneNumber;
	private String messageSubID;

	private void initParams() {
		mmsID = null;
		mmsSubject = null;
		mmsContentType = null;
		mmsContentLocation = null;
		mmsMsgCls = null;
		mmsVersion = null;
		mmsSize = null;
		mmsTransID = null;
		mmsRead = 0;
		mmsLocked = 0;

		mmsDate = null;
		mmsExpiry = null;
		mmsPriority = null;
		mmsMsgType = null;
		mmsReadReport = null;
		mmsDelReport = null;
		mmsSubjectCharset = null;
		partsNumber = 0;
		partSeq = null;
		partContentType = null;
		partName = null;
		partCharset = null;
		partContentDisposition = null;
		partFileName = null;
		partContentID = null;
		partContentLocation = null;
		partCTStart = null;
		partCTType = null;
		partData = null;
		partText = null;
		toPhoneNumberCounts = 0;
		toPhoneNumber = null;
		fromPhoneNumber = null;
		messageSubID = null;
	}
	private boolean processResults(Context context, Cursor cursor) {
		// if(!cursor.moveToFirst())
		// return false;
		try {
			formatMmsParam(cursor);

			if (!getMmsPartContent(context, mmsID))
				return false;
			getMmsPhoneNumber(context, mmsID);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean getMmsPhoneNumber(Context context, String mmsID2) {
		String as[];
		boolean flag = false;
		as = new String[1];
		as[0] = "address";
		flag = false;
		Cursor cursor = context.getContentResolver().query(
				Uri.parse((new StringBuilder("content://mms/")).append(mmsID2)
						.append("/addr").toString()), as, "type =151", null,
				null);
		if (cursor != null) {
			toPhoneNumberCounts = cursor.getCount();
			if (cursor.moveToFirst()) {
				toPhoneNumber = new String[toPhoneNumberCounts];
				int i = 0;
				do {
					toPhoneNumber[i] = cursor.getString(cursor
							.getColumnIndex("address"));
					i++;
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		Cursor cursor1 = context.getContentResolver().query(
				Uri.parse((new StringBuilder("content://mms/")).append(mmsID2)
						.append("/addr").toString()), as, "type =137", null,
				null);
		if (cursor1 != null) {
			if (cursor1.moveToFirst())
				fromPhoneNumber = cursor1.getString(cursor1
						.getColumnIndex("address"));
			cursor1.close();
		}
		flag = true;

		return flag;


	}

	private void formatMmsParam(Cursor cursor)
			throws UnsupportedEncodingException {
		mmsID = cursor.getString(cursor.getColumnIndex("_id"));
		mmsSubject = cursor.getString(cursor.getColumnIndex("sub"));
		mmsContentType = cursor.getString(cursor.getColumnIndex("ct_t"));
		mmsContentLocation = cursor.getString(cursor.getColumnIndex("ct_l"));
		mmsMsgCls = cursor.getString(cursor.getColumnIndex("m_cls"));
		mmsVersion = cursor.getString(cursor.getColumnIndex("v"));
		mmsSize = cursor.getString(cursor.getColumnIndex("m_size"));
		mmsTransID = cursor.getString(cursor.getColumnIndex("tr_id"));
		mmsRead = Integer.parseInt(cursor.getString(cursor
				.getColumnIndex("read")));
		mmsLocked = Integer.parseInt(cursor.getString(cursor
				.getColumnIndex("locked")));
		mmsPriority = getMmsPriorityStr(cursor.getString(cursor
				.getColumnIndex("pri")));
		mmsMsgType = getMmsMsgTypeStr(cursor.getString(cursor
				.getColumnIndex("m_type")));
		mmsReadReport = getMmsInt(cursor.getString(cursor.getColumnIndex("rr")));
		mmsDelReport = getMmsInt(cursor.getString(cursor
				.getColumnIndex("d_rpt")));
		java.sql.Date date = new java.sql.Date(1000L * Long.parseLong(cursor
				.getString(cursor.getColumnIndex("date")).trim()));
		SimpleDateFormat simpledateformat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		mmsDate = simpledateformat.format(date);
		if (cursor.getString(cursor.getColumnIndex("exp")) != null)
			mmsExpiry = simpledateformat.format(new java.sql.Date(1000L * Long
					.parseLong(cursor.getString(cursor.getColumnIndex("exp"))
							.trim())));
		if (cursor.getString(cursor.getColumnIndex("sub_cs")) != null)
			mmsSubjectCharset = CharacterSets
					.getMimeName(Integer.parseInt(cursor.getString(cursor
							.getColumnIndex("sub_cs"))));
		// messageSubID = CommonFunctions.getCardID(cursor, context);
	}

	public boolean isEmpty(String s) {
		boolean flag;
		if (s == null || s != null && s.length() == 0)
			flag = true;
		else
			flag = false;
		return flag;
	}

	String getMmsPriorityStr(String s) {
		String s1 = null;
		if (!isEmpty(s)) {

			int i = Integer.parseInt(s);
			switch (i) {
			case 128:
				s1 = "low";
				break;

			case 129:
				s1 = "normal";
				break;

			case 130:
				s1 = "high";
				break;
			}
		}

		return s1;
	}

	String getMmsMsgTypeStr(String s) {
		String s1;
		if (s == null) {
			s1 = null;
		} else {
			int i = Integer.parseInt(s);
			String as[] = new String[34];
			as[0] = "SEND_REQ";
			as[1] = "SEND_CONF";
			as[2] = "NOTIFICATION_IND";
			as[3] = "NOTIFYRESP_IND";
			as[4] = "RETRIEVE_CONF";
			as[5] = "ACKNOWLEDGE_IND";
			as[6] = "DELIVERY_IND";
			as[7] = "READ_REC_IND";
			as[8] = "READ_ORIG_IND";
			as[9] = "FORWARD_REQ";
			as[10] = "FORWARD_CONF";
			as[11] = "MBOX_STORE_REQ";
			as[12] = "MBOX_STORE_CONF";
			as[13] = "MBOX_VIEW_REQ";
			as[14] = "MBOX_VIEW_CONF";
			as[15] = "MBOX_UPLOAD_REQ";
			as[16] = "MBOX_UPLOAD_CONF";
			as[17] = "MBOX_DELETE_REQ";
			as[18] = "MBOX_DELETE_CONF";
			as[19] = "MBOX_DESCR";
			as[20] = "DELETE_REQ";
			as[21] = "DELETE_CONF";
			as[22] = "CANCEL_REQ";
			as[23] = "CANCEL_CONF";
			as[32] = "PUSH_SI";
			as[33] = "PUSH_SL";
			Integer ainteger[] = new Integer[34];
			ainteger[0] = Integer.valueOf(128);
			ainteger[1] = Integer.valueOf(129);
			ainteger[2] = Integer.valueOf(130);
			ainteger[3] = Integer.valueOf(131);
			ainteger[4] = Integer.valueOf(132);
			ainteger[5] = Integer.valueOf(133);
			ainteger[6] = Integer.valueOf(134);
			ainteger[7] = Integer.valueOf(135);
			ainteger[8] = Integer.valueOf(136);
			ainteger[9] = Integer.valueOf(137);
			ainteger[10] = Integer.valueOf(138);
			ainteger[11] = Integer.valueOf(139);
			ainteger[12] = Integer.valueOf(140);
			ainteger[13] = Integer.valueOf(141);
			ainteger[14] = Integer.valueOf(142);
			ainteger[15] = Integer.valueOf(143);
			ainteger[16] = Integer.valueOf(144);
			ainteger[17] = Integer.valueOf(145);
			ainteger[18] = Integer.valueOf(146);
			ainteger[19] = Integer.valueOf(147);
			ainteger[20] = Integer.valueOf(148);
			ainteger[21] = Integer.valueOf(149);
			ainteger[22] = Integer.valueOf(150);
			ainteger[23] = Integer.valueOf(151);
			ainteger[24] = Integer.valueOf(152);
			ainteger[25] = Integer.valueOf(153);
			ainteger[26] = Integer.valueOf(154);
			ainteger[27] = Integer.valueOf(155);
			ainteger[28] = Integer.valueOf(156);
			ainteger[29] = Integer.valueOf(157);
			ainteger[30] = Integer.valueOf(158);
			ainteger[31] = Integer.valueOf(159);
			ainteger[32] = Integer.valueOf(160);
			ainteger[33] = Integer.valueOf(161);
			if (i < ainteger[0].intValue()
					|| i > ainteger[-1 + ainteger.length].intValue())
				s1 = null;
			else
				s1 = as[i - ainteger[0].intValue()];
		}
		return s1;
	}

	String getMmsInt(String s) {
		String s1 = null;
		if (s != null) {

			switch (Integer.parseInt(s)) {
			case 128:
				s1 = "1";
				break;

			case 129:
				s1 = "0";
				break;
			}
		}

		return s1;

	}

	boolean getMmsPartContent(Context context, String s) {
		boolean flag;
		Cursor cursor;
		flag = false;
		cursor = null;
		cursor = context.getContentResolver().query(
				Uri.parse((new StringBuilder("content://mms/")).append(s)
						.append("/part").toString()), null, null, null, null);
		if (cursor == null)
			return false;
		partsNumber = cursor.getCount();
		if (cursor.moveToFirst()) {
			partSeq = new String[partsNumber];
			partContentType = new String[partsNumber];
			partName = new String[partsNumber];
			partCharset = new String[partsNumber];
			partContentDisposition = new String[partsNumber];
			partFileName = new String[partsNumber];
			partContentID = new String[partsNumber];
			partContentLocation = new String[partsNumber];
			partCTStart = new String[partsNumber];
			partCTType = new String[partsNumber];
			partData = new String[partsNumber];
			String as[] = new String[partsNumber];
			partText = new String[partsNumber];
			int i = 0;
			do {
				partSeq[i] = cursor.getString(cursor.getColumnIndex("seq"));
				partContentType[i] = cursor.getString(cursor
						.getColumnIndex("ct"));
				partName[i] = cursor.getString(cursor.getColumnIndex("name"));
				partCharset[i] = cursor.getString(cursor
						.getColumnIndex("chset"));
				partContentDisposition[i] = cursor.getString(cursor
						.getColumnIndex("cd"));
				partFileName[i] = cursor.getString(cursor.getColumnIndex("fn"));
				partContentID[i] = cursor.getString(cursor
						.getColumnIndex("cid"));
				partContentLocation[i] = cursor.getString(cursor
						.getColumnIndex("cl"));
				partCTStart[i] = cursor.getString(cursor
						.getColumnIndex("ctt_s"));
				partCTType[i] = cursor
						.getString(cursor.getColumnIndex("ctt_t"));
				as[i] = cursor.getString(cursor.getColumnIndex("_data"));
				partText[i] = cursor.getString(cursor.getColumnIndex("text"));
				String s1 = cursor.getString(cursor.getColumnIndex("_id"));
				if (as[i] != null && as[i].length() != 0) {
					Log.d("MessageMms", (new StringBuilder(" length = "))
							.append(as[i].length()).toString());
					try {
						getMmsData(context, i, s1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		flag = true;
		if (cursor != null)
			cursor.close();

		return flag;

	}

	boolean getMmsData(Context context, int i, String s) throws Exception {
		ByteArrayOutputStream bytearrayoutputstream;
		InputStream inputstream;
		Exception exception;
		IOException ioexception2;
		Uri uri;
		byte abyte0[];
		int j;
		try {
			bytearrayoutputstream = new ByteArrayOutputStream();
		} catch (Exception exception1) {
			throw exception1;
		}
		inputstream = null;
		uri = Uri.parse((new StringBuilder("content://mms/part/")).append(s)
				.toString());
		inputstream = context.getContentResolver().openInputStream(uri);
		abyte0 = new byte[256];

		while (-1 != (j = inputstream.read(abyte0))) {
			// partData[i] =
			// bytesToB64String(bytearrayoutputstream.toByteArray());
			bytearrayoutputstream.write(abyte0, 0, j);
		}

		partData[i] = bytesToB64String(bytearrayoutputstream.toByteArray());

		bytearrayoutputstream.close();

		return true;
	}

	public static final String bytesToB64String(byte abyte0[]) {
		String s;
		if (abyte0 == null)
			s = null;
		else
			s = Base64.encodeToString(abyte0, 0);
		return s;
	}

	private void makeMmsTelString(boolean flag, StringBuffer stringbuffer) {
		if (flag) {

			if (checkString(fromPhoneNumber)) {
				stringbuffer.append("BEGIN:VCARD\r\n");
				stringbuffer.append("TEL:");
				stringbuffer.append(fromPhoneNumber);
				stringbuffer.append("\r\n");
				stringbuffer.append("END:VCARD\r\n");
			}
		} else {
			if (toPhoneNumber != null && toPhoneNumber.length > 0) {
				int i = 0;
				while (i < toPhoneNumber.length) {
					if (checkString(toPhoneNumber[i])) {
						stringbuffer.append("BEGIN:VCARD\r\n");
						stringbuffer.append("TEL:");
						stringbuffer.append(toPhoneNumber[i]);
						stringbuffer.append("\r\n");
						stringbuffer.append("END:VCARD\r\n");
					}
					i++;
				}
			}
		}
	}

	private void makeMmsBodyString(boolean flag, String s,
			StringBuffer stringbuffer) {
		stringbuffer.append("BEGIN:VBODY\r\n");
		stringbuffer.append(s);
		stringbuffer.append("\r\n");
		if (flag) {
			if (mmsRead != 0)
				stringbuffer.append("X-READ:READ\r\n");
			else
				stringbuffer.append("X-READ:UNREAD\r\n");
		}
		// if(checkString(messageSubID))
		// {
		// stringbuffer.append("X-SIMID:");
		// stringbuffer.append(messageSubID);
		// stringbuffer.append("\r\n");
		// } else
		{
			stringbuffer.append("X-SIMID:0\r\n");
		}
		if (mmsLocked == 0)
			stringbuffer.append("X-LOCKED:UNLOCKED\r\n");
		else
			stringbuffer.append("X-LOCKED:LOCKED\r\n");
		stringbuffer.append("X-TYPE:MMS\r\n");
		appendItem(stringbuffer, "Date:", mmsDate);
		makeMmsXMmsString(stringbuffer);
		appendItemWithQP(stringbuffer, "Subject", mmsSubject);
		stringbuffer.append("END:VBODY\r\n");
	}

	public boolean appendItemWithQP(StringBuffer stringbuffer, String s,
			String s1) {
		boolean flag;
		if (stringbuffer != null && s != null && checkString(s1)) {
			stringbuffer.append((new StringBuilder(String
					.valueOf(makeUtf8QPStr(s, s1)))).append("\r\n").toString());
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}

	public String makeUtf8QPStr(String s, String s1) {
		return encodeQuotedPrintable((new StringBuilder(String.valueOf(s)))
				.append(";").toString(), s1);
	}

	private String encodeQuotedPrintable(String s, String s1) {
//		if (s1 != null && s1.length() != 0)
//			return null;

		String s2 = s;

		StringBuilder stringbuilder;
		int j;
		int k;
		int i = 0;
		stringbuilder = new StringBuilder(256);
		if (s != null) {
			stringbuilder.append(s);
			i = s.length();
		}
		stringbuilder.append("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:");
		j = 0;
		k = i + "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:".length();
		if (s1 == null || s1.length() == 0)
			return  stringbuilder.toString();
		// (byte[])null;
		byte abyte1[] = null;
		try {
			abyte1 = s1.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte abyte0[] = abyte1;
		// _L5:
		// if(j < abyte0.length)
		// break MISSING_BLOCK_LABEL_110;
		// s2 = stringbuilder.toString();
		// if(true) goto _L4; else goto _L3
		// _L3:
		// UnsupportedEncodingException unsupportedencodingexception;
		// unsupportedencodingexception;
		// abyte0 = s1.getBytes();
		// goto _L5
		while (j < abyte0.length) {
			stringbuilder.append("=");
			stringbuilder.append(forDigit0x((0xf0 & abyte0[j]) >> 4));
			stringbuilder.append(forDigit0x(0xf & abyte0[j]));
			j++;
			if ((k += 3) >= 67 && j < abyte0.length) {
				stringbuilder.append("=\r\n");
				k = 0;
			}
		}

		s2 = stringbuilder.toString();

		return s2;
		// goto _L5
	}

	private static char forDigit0x(int i) {
		char c = Character.forDigit(i, 16);
		if ('a' <= c && c <= 'z')
			c += '\uFFE0';
		return c;
	}

	public static boolean checkString(String s) {
		boolean flag;
		if (s != null && s.length() > 0)
			flag = true;
		else
			flag = false;
		return flag;
	}

	public boolean appendItem(StringBuffer stringbuffer, String s, String s1) {
		boolean flag;
		if (stringbuffer != null && s != null && checkString(s1)) {
			stringbuffer.append(s);
			stringbuffer.append(s1);
			stringbuffer.append("\r\n");
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}

	private void makeMmsXMmsString(StringBuffer stringbuffer) {
		appendItem(stringbuffer, "X-MMS-CONTENT-TYPE:", mmsContentType);
		appendItem(stringbuffer, "X-MMS-CONTENT-LOCATION:", mmsContentLocation);
		appendItem(stringbuffer, "X-MMS-EXPIRY:", mmsExpiry);
		appendItem(stringbuffer, "X-MMS-MSG-CLASS:", mmsMsgCls);
		appendItem(stringbuffer, "X-MMS-MESSAGE-TYPE:", mmsMsgType);
		appendItem(stringbuffer, "X-MMS-VERSION:", mmsVersion);
		appendItem(stringbuffer, "X-MMS-MESSAGE-SIZE:", mmsSize);
		appendItem(stringbuffer, "X-MMS-PRIORITY:", mmsPriority);
		appendItem(stringbuffer, "X-MMS-TRAN-ID:", mmsTransID);
		// appendItem(stringbuffer, "X-MMS-CC:", cc);
		// appendItem(stringbuffer, "X-MMS-BCC:", bcc);
		appendItem(stringbuffer, "X-MMS-READ-REPORT:", mmsReadReport);
		appendItem(stringbuffer, "X-MMS-DEL-REPORT:", mmsDelReport);
	}

	public void restoreMms(Context context, String name) {
		
		File file = new File(name);
		if(!file.exists()) {
			Log.e(TAG, "restoreMms, file not exist ");
			
			return ;
		}
		FileInputStream fileinputstream = null;
		try {
			

		fileinputstream = new FileInputStream(file);
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(fileinputstream, Charset.defaultCharset()));
		importMmsByVmsg(context,bufferedreader);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		if(null != fileinputstream){
			try {
				fileinputstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	private int importMmsByVmsg(Context context,BufferedReader bufferedreader)
    {
        int i = 8193;
        String s;
        try {
			while ((s = bufferedreader.readLine()) != null) {
			
			if(s != null && i == 8193) {
				if("BEGIN:VMSG".equals(s)) {
			        LinkedList linkedlist;
			        String s1;
			        linkedlist = new LinkedList();
			        s1 = bufferedreader.readLine();
			       while(s1 != null && !"END:VMSG".equals(s1)) {
//                   String s2;
			           linkedlist.add(s1);
			           s1 = bufferedreader.readLine();
//                   s1 = s2;
			        } 
			       initParams();
			       import1Mms(context, linkedlist);
				} 
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return i;
//          goto _L1
//_L5:
//        if(i == 8193)
//            reporter.updateProcessStatus(this);
//          goto _L2
//_L14:
//        if(!isCancel()) goto _L4; else goto _L3
//_L3:
//        i = 8195;
//          goto _L5
//_L4:
//        if(!"BEGIN:VMSG".equals(s)) goto _L7; else goto _L6
//_L6:
//        LinkedList linkedlist;
//        String s1;
//        reporter.updateProcessStatus(this);
//        linkedlist = new LinkedList();
//        s1 = bufferedreader.readLine();
//_L13:
//        if(s1 != null && !"END:VMSG".equals(s1)) goto _L9; else goto _L8
//_L8:
//        if(i != 8193) goto _L5; else goto _L10
//_L10:
//        i = VMsg.import1Mms(linkedlist);
//        curNum = 1 + curNum;
//_L7:
//        s = bufferedreader.readLine();
//          goto _L1
//_L9:
//        if(!isCancel()) goto _L12; else goto _L11
//_L11:
//        i = 8195;
//          goto _L8
//_L12:
//        String s2;
//        linkedlist.add(s1);
//        s2 = bufferedreader.readLine();
//        s1 = s2;
//          goto _L13
//        Exception exception;
//        exception;
//        Logging.e(exception.getMessage());
//        i = 8194;
//          goto _L2
//_L1:
//        if(s != null && i == 8193) goto _L14; else goto _L5
//_L2:
//        return i;
    }
	
    public int import1Mms(Context context ,List list)
    {
        String s = getItemDataByBeginStr(list, "X-TYPE:");
        int i = 0;
        if(s != null && !s.equals("MMS"))
        {
            i = 8194;// not mms ,in fact it is have to be mms
        } else
        {
//            MessageMmsInterface messagemmsinterface = new MessageMmsInterface();
            boolean flag = true;
            String s1 = getItemDataByBeginStr(list, "X-BOX:");
            if(s1 == null)
                s1 = getItemDataByBeginStr(list, "X-IRMC-BOX:");
            mmsBoxType = 1;
            if(s1 != null && (s1.equals("SENTBOX") || s1.equals("SENDBOX")))
            {
                flag = false;
                mmsBoxType = 2;
            }
            getMmsTelString(flag, list);
            getMmsBodyString(flag, list);
            getMmsPartString( list);
            try {
				i = AddMmsItem(context);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return i;
    }
    int getMmsMsgTypeInt(String s)
    {
        int i = 0;
        if(s== null){
          return i;
        }

        if(s.matches("SEND_REQ"))
            i = 128;
        else
        if(s.matches("SEND_CONF"))
            i = 129;
        else
        if(s.matches("NOTIFICATION_IND"))
            i = 130;
        else
        if(s.matches("NOTIFYRESP_IND"))
            i = 131;
        else
        if(s.matches("RETRIEVE_CONF"))
            i = 132;
        else
        if(s.matches("ACKNOWLEDGE_IND"))
            i = 133;
        else
        if(s.matches("DELIVERY_IND"))
            i = 134;
        else
        if(s.matches("READ_REC_IND"))
            i = 135;
        else
        if(s.matches("READ_ORIG_IND"))
            i = 136;
        else
        if(s.matches("FORWARD_REQ"))
            i = 137;
        else
        if(s.matches("FORWARD_CONF"))
            i = 138;
        else
        if(s.matches("MBOX_STORE_REQ"))
            i = 139;
        else
        if(s.matches("MBOX_STORE_CONF"))
            i = 140;
        else
        if(s.matches("MBOX_VIEW_REQ"))
            i = 141;
        else
        if(s.matches("MBOX_VIEW_CONF"))
            i = 142;
        else
        if(s.matches("MBOX_UPLOAD_REQ"))
            i = 143;
        else
        if(s.matches("MBOX_UPLOAD_CONF"))
            i = 144;
        else
        if(s.matches("MBOX_DELETE_REQ"))
            i = 145;
        else
        if(s.matches("MBOX_DELETE_CONF"))
            i = 146;
        else
        if(s.matches("MBOX_DESCR"))
            i = 147;
        else
        if(s.matches("DELETE_REQ"))
            i = 148;
        else
        if(s.matches("DELETE_CONF"))
            i = 149;
        else
        if(s.matches("CANCEL_REQ"))
            i = 150;
        else
        if(s.matches("CANCEL_CONF"))
            i = 151;
        else
        if(s.matches("PUSH_SI"))
            i = 160;
        else
        if(s.matches("PUSH_SL"))
            i = 161;

        return i;
    }    
    private String getCheckSelectionStr()
            throws java.text.ParseException
        {
            String s = (new StringBuilder(String.valueOf((new StringBuilder(String.valueOf((new StringBuilder(String.valueOf((new StringBuilder(String.valueOf(""))).append("m_type ='").append(getMmsMsgTypeInt(mmsMsgType)).append("'").toString()))).append(" AND ").toString()))).append("date =").append(String.valueOf((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(mmsDate).getTime() / 1000L)).append(" AND ").toString()))).append("msg_box = '").append(String.valueOf(mmsBoxType)).append("'").toString();
            if(mmsTransID != null)
                s = (new StringBuilder(String.valueOf((new StringBuilder(String.valueOf(s))).append(" AND ").toString()))).append("tr_id = '").append(mmsTransID).append("'").toString();
            return s;
        }
    
    public boolean checkMmsIsExist(Context context)
    {
        Cursor cursor = null;
        String s = null;
		try {
			s = getCheckSelectionStr();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        cursor = context.getContentResolver().query(Uri.parse("content://mms/"), null, s, null, null);
        if(cursor == null) {//goto _L2; else goto _L1
        	cursor.close();
        	return false;
        }
//_L1:
        int i = cursor.getCount();
        
        if( i <= 0) {
        	cursor.close();
        	return false;
        }
        cursor.close();
        
        return true;
        
        
//        for (cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext())  {
//        	String s1 = cursor.getString(cursor.getColumnIndex("_id"));
//        }
        
        
//        return false;
        	 
//        if(i != 0) goto _L3; else goto _L2
//_L2:
//        boolean flag;
//        if(cursor != null)
//            cursor.close();
//        flag = false;
//_L6:
//        return flag;
//_L3:
//        boolean flag1 = cursor.moveToNext();
//        if(flag1) goto _L5; else goto _L4
//_L4:
//        if(cursor != null)
//            cursor.close();
//_L11:
//        flag = false;
//          goto _L6
//_L5:
//        boolean flag2 = false;
//        String s1;
//        int j;
//        s1 = cursor.getString(cursor.getColumnIndex("_id"));
//        j = 0;
//_L12:
//        if(j < toPhoneNumberCounts) goto _L8; else goto _L7
//_L7:
//        if(flag2) goto _L3; else goto _L9
//_L9:
//        boolean flag3 = checkByFromPhoneNumber(s1);
//        if(flag3) goto _L3; else goto _L10
//_L10:
//        if(cursor != null)
//            cursor.close();
//        flag = true;
//          goto _L6
//_L8:
//        Cursor cursor1;
//        String s2 = (new StringBuilder("address = '")).append(toPhoneNumber[j]).append("' AND type = '").append("151").append("'").toString();
//        cursor1 = context.getContentResolver().query(Uri.parse((new StringBuilder("content://mms/")).append(s1).append("/addr").toString()), null, s2, null, null);
//        if(cursor1 == null)
//        {
//            flag2 = true;
//            break MISSING_BLOCK_LABEL_313;
//        }
//        if(cursor1.getCount() != 0)
//            break MISSING_BLOCK_LABEL_290;
//        flag2 = true;
//        cursor1.close();
//          goto _L7
//        Exception exception1;
//        exception1;
//        exception1.printStackTrace();
//        if(cursor != null)
//            cursor.close();
//          goto _L11
//        cursor1.close();
//        break MISSING_BLOCK_LABEL_313;
//        Exception exception;
//        exception;
//        if(cursor != null)
//            cursor.close();
//        throw exception;
//        j++;
//          goto _L12
    }
    public int AddMmsItem(Context context) throws Exception
    {
    	if(checkMmsIsExist(context))
    		return 8193;
//        if(!ApplicationConfig.getInstance().isIncremental() || !checkMmsIsExist()) goto _L2; else goto _L1
//_L1:
//        int i = 8193;
//_L3:
//        return i;
//_L2:
        String s = null;
        int j;
        s = getSmsId(context);
        ContentValues contentvalues = createContentValues(context,s);
        Uri uri = context.getContentResolver().insert(Uri.parse("content://mms/"), contentvalues);
        if(uri != null)
        {
            ContentValues contentvalues1 = new ContentValues();
            contentvalues1.put("date", String.valueOf((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(mmsDate).getTime() / 1000L));
            context.getContentResolver().update(uri, contentvalues1, null, null);
        }
        j = checkMmsUri(context,uri);
//        i = j;
        deleteSmsForMms(context, s);
        
        return j;
//          goto _L3
//        SQLiteFullException sqlitefullexception;
//        sqlitefullexception;
//        Log.d("MessageMms", sqlitefullexception.getMessage());
//        DeleteSmsForMms(s);
//        i = 8196;
//          goto _L3
//        Exception exception1;
//        exception1;
//        if(exception1 == null)
//            break MISSING_BLOCK_LABEL_195;
//        Log.d("MessageMms", (new StringBuilder()).append(exception1.getMessage()).toString());
//        DeleteSmsForMms(s);
//        i = 8194;
//          goto _L3
//        Exception exception;
//        exception;
//        DeleteSmsForMms(s);
//        throw exception;
    }
    public Boolean deleteSmsForMms(Context context,String s)
    {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://sms/");
        ContentResolver contentresolver = context.getContentResolver();
        String as[] = new String[1];
        as[0] = "thread_id";
        cursor = contentresolver.query(uri, as, (new StringBuilder("_id = ")).append(s).toString(), null, null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
        	 context.getContentResolver().delete(Uri.parse((new StringBuilder("content://sms/")).append(s).toString()), null, null);
        }
        
        cursor.close();
        return true;
//        if(cursor != null && cursor.moveToFirst()) goto _L2; else goto _L1
//_L1:
//        Boolean boolean2 = Boolean.valueOf(false);
//        Boolean boolean1;
//        boolean1 = boolean2;
//        closeCursor(cursor);
//_L3:
//        return boolean1;
//_L2:
//        Boolean boolean3;
//        do
//            context.getContentResolver().delete(Uri.parse((new StringBuilder("content://sms/")).append(s).toString()), null, null);
//        while(cursor.moveToNext());
//        boolean3 = Boolean.valueOf(true);
//        boolean1 = boolean3;
//        closeCursor(cursor);
//          goto _L3
//        Exception exception1;
//        exception1;
//        Log.d("MessageSms", exception1.getMessage());
//        closeCursor(cursor);
//        boolean1 = Boolean.valueOf(false);
//          goto _L3
//        Exception exception;
//        exception;
//        closeCursor(cursor);
//        throw exception;
    }
    private int checkMmsUri(Context context,Uri uri)
            throws Exception
        {
            char c = '\u2002';//8194
            if(uri == null) {

            return c;
            }

            if(!addMmsPhoneNumber(context,uri))
                c = '\u2004';//8196
            else
            if(addMmsPartContent(context,uri))
                c = '\u2001';//8193

            
            return c;
        }

    public byte[] B64StringTobytes(String s)
    {
        byte abyte0[];
        if(s == null)
            abyte0 = null;
        else
            abyte0 = Base64.decode(s, 0);
        return abyte0;
    }
    
    boolean addMmsData(Context context, Uri uri, int i)
            throws Exception
        {
            long l = ContentUris.parseId(uri);
            ByteArrayInputStream bytearrayinputstream = null;;
            OutputStream outputstream;
//            Exception exception;
//            IOException ioexception1;
//            IOException ioexception2;
            Uri uri1;
            byte abyte0[];
            int j;
            try
            {
                bytearrayinputstream = new ByteArrayInputStream(B64StringTobytes(partData[i]));
            }
            catch(SQLiteFullException sqlitefullexception)
            {
//                Log.d("MessageMms", sqlitefullexception.getMessage());
//                throw sqlitefullexception;
                sqlitefullexception.printStackTrace();
                
            }
            catch(Exception exception1)
            {
//                throw exception1;
            	exception1.printStackTrace();
            }
            try {
				

            outputstream = null;
            uri1 = Uri.parse((new StringBuilder("content://mms/part/")).append(l).toString());
            outputstream = context.getContentResolver().openOutputStream(uri1);
            abyte0 = new byte[256];
            
            while((j = bytearrayinputstream.read(abyte0)) > 0) {
            	outputstream.write(abyte0, 0, j);
            }
            
            outputstream.close();
            bytearrayinputstream.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
            return true;
//    _L1:
//            j = bytearrayinputstream.read(abyte0);
//            if(j != -1)
//                break MISSING_BLOCK_LABEL_107;
//            if(bytearrayinputstream == null)
//                break MISSING_BLOCK_LABEL_95;
//            bytearrayinputstream.close();
//            if(outputstream == null)
//                break MISSING_BLOCK_LABEL_105;
//            outputstream.close();
//            return true;
//            outputstream.write(abyte0, 0, j);
//              goto _L1
//            ioexception2;
//            throw ioexception2;
//            exception;
//            if(bytearrayinputstream == null)
//                break MISSING_BLOCK_LABEL_137;
//            bytearrayinputstream.close();
//            if(outputstream == null)
//                break MISSING_BLOCK_LABEL_147;
//            outputstream.close();
//            throw exception;
//            ioexception1;
//            throw ioexception1;
//            IOException ioexception;
//            ioexception;
//            throw ioexception;
//            IOException ioexception4;
//            ioexception4;
//            throw ioexception4;
//            IOException ioexception3;
//            ioexception3;
//            throw ioexception3;
        }
    
    boolean addMmsPartContent(Context context , Uri uri)
            throws Exception
        {
            boolean flag = true;
            long l = ContentUris.parseId(uri);
            boolean flag1;
            if(partsNumber <= 0)
            {
                flag1 = true;
            } else
            {
                int i = 0;
                do
                {
                    try
                    {
                        if(i >= partsNumber)
                            break;
                        ContentValues contentvalues = new ContentValues();
                        contentvalues.put("seq", partSeq[i]);
                        contentvalues.put("ct", partContentType[i]);
                        contentvalues.put("name", partName[i]);
                        contentvalues.put("chset", partCharset[i]);
                        contentvalues.put("cd", partContentDisposition[i]);
                        contentvalues.put("fn", partFileName[i]);
                        contentvalues.put("cid", partContentID[i]);
                        contentvalues.put("cl", partContentLocation[i]);
                        contentvalues.put("ctt_s", partCTStart[i]);
                        contentvalues.put("ctt_t", partCTType[i]);
                        contentvalues.put("text", partText[i]);
                        Uri uri1 = context.getContentResolver().insert(Uri.parse((new StringBuilder("content://mms/")).append(l).append("/part").toString()), contentvalues);
                        if(uri1 == null)
                        {
                            flag = false;
                            break;
                        }
                        if(partData[i] != null && partData[i].length()>0)
                            addMmsData(context, uri1, i);
                    }
                    catch(SQLiteFullException sqlitefullexception)
                    {
//                        Log.d("MessageMms", sqlitefullexception.getMessage());
                        sqlitefullexception.printStackTrace();
                       
//                        throw sqlitefullexception;
                    }
                    catch(Exception exception)
                    {
//                        Log.d("MessageMms", exception.getMessage());
                    	exception.printStackTrace();
//                        throw exception;
                    }
                    i++;
                } while(true);
                flag1 = flag;
            }
            return flag1;
        }
    
    
    boolean addMmsPhoneNumber(Context context, Uri uri)
            throws Exception
        {
            boolean flag = true;
            long l = ContentUris.parseId(uri);

            if(toPhoneNumberCounts <= 0 && fromPhoneNumber == null)
            {
                flag = true;
            } else
            {
                int i = 0;
                do
                {
                    Uri uri1;
                    try
                    {
                    	if(toPhoneNumberCounts <= 0 || null != toPhoneNumber) {
                    		break;
                    	}
//                        if(i >= toPhoneNumberCounts)
//                        {
//                            if(flag && fromPhoneNumber != null)
//                            {
//                                ContentValues contentvalues1 = new ContentValues();
//                                contentvalues1.put("address", fromPhoneNumber);
//                                contentvalues1.put("type", "137");
//                                if(context.getContentResolver().insert(Uri.parse((new StringBuilder("content://mms/")).append(l).append("/addr").toString()), contentvalues1) == null)
//                                    flag = false;
//                            }
//                            break;
//                        }
                        ContentValues contentvalues = new ContentValues();
                        contentvalues.put("address", toPhoneNumber[i]);
                        contentvalues.put("type", "151");
                        uri1 = context.getContentResolver().insert(Uri.parse((new StringBuilder("content://mms/")).append(l).append("/addr").toString()), contentvalues);
                    }
                    catch(SQLiteFullException e)
                    {
//                        Log.d("MessageMms", "SQLiteFullException:" +e.getMessage());
                    	e.printStackTrace();
                        throw e;
                    }
                    catch(Exception e)
                    {
//                        Log.d("MessageMms", "exception : "+e.getMessage());
                    	e.printStackTrace();
                        throw e;
                    }
                    if(uri1 == null)
                        flag = false;
                    i++;
                } while( i< toPhoneNumberCounts);
//                flag1 = flag;
                

                    if(flag && fromPhoneNumber != null)
                    {
                        ContentValues contentvalues1 = new ContentValues();
                        contentvalues1.put("address", fromPhoneNumber);
                        contentvalues1.put("type", "137");
                        if(context.getContentResolver().insert(Uri.parse((new StringBuilder("content://mms/")).append(l).append("/addr").toString()), contentvalues1) == null)
                            flag = false;
                    }

                
            }
            return flag;
        }
    
    private String getThreadIdFromCursor(Cursor cursor)
    {
        String s;
        if(cursor == null || !cursor.moveToFirst())
            s = null;
        else
            s = cursor.getString(cursor.getColumnIndex("thread_id"));
        return s;
    }
    public String getSmsThreadIdForMms(String s, Context context)
    {
        Cursor cursor = null;
        String s3;
        String s2 = (new StringBuilder("_id = ")).append(s).toString();
        ContentResolver contentresolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms/");
        String as[] = new String[1];
        as[0] = "thread_id";
        cursor = contentresolver.query(uri, as, s2, null, null);
        s3 = getThreadIdFromCursor(cursor);
        String s1;
        s1 = s3;
        
        cursor.close();
        return s1;
//        closeCursor(cursor);
//_L2:
//        return s1;
//        Exception exception1;
//        exception1;
//        Log.d("MessageSms", exception1.getMessage());
//        closeCursor(cursor);
//        s1 = null;
//        if(true) goto _L2; else goto _L1
//_L1:
//        Exception exception;
//        exception;
//        closeCursor(cursor);
//        throw exception;
    }
    
    
    int getMmsPriorityInt(String s)
    {
        int i = 128;
        if(s == null) {
        	return i;
        }

        if(s.matches("low"))
            i = 128;
        else
        if(s.matches("normal"))
            i = 129;
        else
        if(s.matches("high"))
            i = 130;

        return i;
    }
    
    
    String getMmsIntStr(String s)
    {
        String s1 = null;
        if(s == null) {
        	return s1;
        }

        if(s.matches("1"))
            s1 = "128";
        else
        if(s.matches("0"))
            s1 = "129";


        return s1;
    }
    
    
    private ContentValues createContentValues(Context context, String s)
            throws Exception, ParseException, UnsupportedEncodingException
        {
            String s1 = getSmsThreadIdForMms(s,context);
            if(s1 == null)
                throw new Exception("can not create get threadId");
            ContentValues contentvalues = new ContentValues();
            contentvalues.put("thread_id", s1);
            contentvalues.put("date", String.valueOf((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(mmsDate).getTime() / 1000L));
            contentvalues.put("read", String.valueOf(mmsRead));
            if(mmsSubject != null)
                contentvalues.put("sub", String.valueOf(mmsSubject));
            if(mmsSubjectCharset != null)
                contentvalues.put("sub_cs", mmsSubjectCharset);
            contentvalues.put("ct_t", mmsContentType);
            contentvalues.put("ct_l", mmsContentLocation);
            if(mmsExpiry != null)
                contentvalues.put("exp", String.valueOf((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(mmsExpiry).getTime() / 1000L));
            contentvalues.put("m_cls", mmsMsgCls);
            contentvalues.put("m_type", Integer.valueOf(getMmsMsgTypeInt(mmsMsgType)));
            contentvalues.put("v", mmsVersion);
            contentvalues.put("m_size", mmsSize);
            if(mmsPriority != null)
                contentvalues.put("pri", Integer.valueOf(getMmsPriorityInt(mmsPriority)));
            contentvalues.put("tr_id", mmsTransID);
            contentvalues.put("locked", String.valueOf(mmsLocked));
            contentvalues.put("rr", getMmsIntStr(mmsReadReport));
            contentvalues.put("d_rpt", getMmsIntStr(mmsDelReport));
            contentvalues.put("msg_box", String.valueOf(mmsBoxType));
            contentvalues.put("seen", "1");
            contentvalues.put("_id", "-1");
//            messageSubID = CommonFunctions.getSimID(messageSubID, context);
//            if(bDoubleCard)
//                contentvalues.put("sub_id", messageSubID);
//            if(bHasSimId)
//                contentvalues.put("sim_id", String.valueOf(messageSubID));
//            if(bHasSimIndex)
//            {
//                contentvalues.put("sim_index", String.valueOf(messageSubID));
//                contentvalues.put("rev_date", String.valueOf((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(mmsDate).getTime()));
//            }
            return contentvalues;
        }
    
    public String createSmsItemForMms(Context context,String messagePhoneNumber, int messageType)
    {
        String s = null;
  
        ContentValues contentvalues = new ContentValues();
        contentvalues.put("address", messagePhoneNumber);
        contentvalues.put("type", String.valueOf(messageType));
        contentvalues.put("body", "");
        contentvalues.put("read", "1");
        Uri uri = context.getContentResolver().insert(Uri.parse("content://sms/"), contentvalues);
        if(uri == null)
            return null;
        s = String.valueOf(ContentUris.parseId(uri));


        return s;

    }
    String addSmsForMms(Context context)
    {
//    	messageType = 1;
    	String messagePhoneNumber;
    	switch (mmsBoxType) {
		case 1:
			messagePhoneNumber = fromPhoneNumber;
			break;

		default:
			if(null != toPhoneNumber)
			messagePhoneNumber = toPhoneNumber[0];
			else 
				messagePhoneNumber = "1";//for test
			break;
		}
    	
    	return createSmsItemForMms(context,messagePhoneNumber,1);
//        mmsBoxType;
//        JVM INSTR tableswitch 1 3: default 44
//    //                   1 65
//    //                   2 88
//    //                   3 88;
//           goto _L1 _L2 _L3 _L3
//_L1:
//        String s;
//        if(messagesmsinterface.messagePhoneNumber == null)
//            messagesmsinterface.messageBody = "test";
//        s = messagesmsinterface.createSmsItemForMms();
//_L5:
//        return s;
//_L2:
//        if(fromPhoneNumber != null)
//            break; /* Loop/switch isn't completed */
//        s = null;
//        if(true) goto _L5; else goto _L4
//_L4:
//        messagesmsinterface.messagePhoneNumber = fromPhoneNumber;
//        continue; /* Loop/switch isn't completed */
//_L3:
//        if(toPhoneNumber != null)
//            messagesmsinterface.messagePhoneNumber = toPhoneNumber[0];
//        if(true) goto _L1; else goto _L6
//_L6:
    }
    
    
    private String getSmsId(Context context)
            throws Exception
        {
            String s = addSmsForMms(context);
            if(s == null)
                throw new Exception("create sms failed");
            else
                return s;
        }
    private void getMmsPartString(List list)
    {
        LinkedList linkedlist = new LinkedList();
        boolean flag = false;
        LinkedList linkedlist1 = null;
        Iterator iterator = list.iterator();
        
        while(iterator.hasNext()) {
        	String s = (String)iterator.next();
            if(s.equals("BEGIN:VPART"))
            {
                flag = true;
                linkedlist1 = new LinkedList();
            } else
            if(s.equals("END:VPART"))
            {
                flag = false;
                linkedlist.add(linkedlist1);
            } else
            if(flag && linkedlist1 != null)
                linkedlist1.add(s);

        }
        
        partsNumber = linkedlist.size();
        String s;
        if(partsNumber >= 1)
        {
            partSeq = new String[partsNumber];
            partContentType = new String[partsNumber];
            partName = new String[partsNumber];
            partCharset = new String[partsNumber];
            partContentDisposition = new String[partsNumber];
            partFileName = new String[partsNumber];
            partContentID = new String[partsNumber];
            partContentLocation = new String[partsNumber];
            partCTStart = new String[partsNumber];
            partCTType = new String[partsNumber];
            partData = new String[partsNumber];
            partText = new String[partsNumber];
            int i = 0;
            while(i < partsNumber) 
            {
                List list1 = (List)linkedlist.get(i);
                partSeq[i] = getItemDataByBeginStr(list1, "X-MMS-PART-SEQ:");
                partContentType[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-TYPE:");
                partName[i] = getItemDataByBeginStr(list1, "X-MMS-PART-NAME:");
                partCharset[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CHARSET:");
                partContentDisposition[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-DISPOSITION:");
                if(partContentDisposition[i] == null)
                    partContentDisposition[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT_DISPOSITION:");
                partFileName[i] = getItemDataByBeginStr(list1, "X-MMS-PART-FILENAME:");
                partContentID[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-ID:");
                partContentLocation[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-LOCATION:");
                partCTStart[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CT-START:");
                partCTType[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CT-TYPE:");
                partData[i] = getMmsPartEndData(list1, "X-MMS-PART-DATA:");
                partText[i] = getMmsPartEndData(list1, "X-MMS-PART-TEXT:");
                i++;
            }
          }
            
//        do
//        {
//            if(!iterator.hasNext())
//            {
//                partsNumber = linkedlist.size();
//                String s;
//                if(partsNumber >= 1)
//                {
//                    partSeq = new String[partsNumber];
//                    partContentType = new String[partsNumber];
//                    partName = new String[partsNumber];
//                    partCharset = new String[partsNumber];
//                    partContentDisposition = new String[partsNumber];
//                    partFileName = new String[partsNumber];
//                    partContentID = new String[partsNumber];
//                    partContentLocation = new String[partsNumber];
//                    partCTStart = new String[partsNumber];
//                    partCTType = new String[partsNumber];
//                    partData = new String[partsNumber];
//                    partText = new String[partsNumber];
//                    int i = 0;
//                    while(i < partsNumber) 
//                    {
//                        List list1 = (List)linkedlist.get(i);
//                        partSeq[i] = getItemDataByBeginStr(list1, "X-MMS-PART-SEQ:");
//                        partContentType[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-TYPE:");
//                        partName[i] = getItemDataByBeginStr(list1, "X-MMS-PART-NAME:");
//                        partCharset[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CHARSET:");
//                        partContentDisposition[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-DISPOSITION:");
//                        if(partContentDisposition[i] == null)
//                            partContentDisposition[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT_DISPOSITION:");
//                        partFileName[i] = getItemDataByBeginStr(list1, "X-MMS-PART-FILENAME:");
//                        partContentID[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-ID:");
//                        partContentLocation[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CONTENT-LOCATION:");
//                        partCTStart[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CT-START:");
//                        partCTType[i] = getItemDataByBeginStr(list1, "X-MMS-PART-CT-TYPE:");
//                        partData[i] = getMmsPartEndData(list1, "X-MMS-PART-DATA:");
//                        partText[i] = getMmsPartEndData(list1, "X-MMS-PART-TEXT:");
//                        i++;
//                    }
//                }
//                return;
//            }
//            s = (String)iterator.next();
//            if(s.equals("BEGIN:VPART"))
//            {
//                flag = true;
//                linkedlist1 = new LinkedList();
//            } else
//            if(s.equals("END:VPART"))
//            {
//                flag = false;
//                linkedlist.add(linkedlist1);
//            } else
//            if(flag && linkedlist1 != null)
//                linkedlist1.add(s);
//        } while(true);
    } 
    
    
    private static String getMmsPartEndData(List list, String s)
    {
        boolean flag = false;
        StringBuffer stringbuffer = new StringBuffer("");
        if(s.startsWith("X-MMS-"))
            s = s.substring("X-MMS".length());
        Iterator iterator = list.iterator();
        
        while(iterator.hasNext()) {
        	String s1 = (String)iterator.next();
            if(flag)
            {
                stringbuffer.append(s1);
            } else
            {
                int i = s1.indexOf(s);
                if(-1 != i)
                {
                    stringbuffer.append(s1.substring(i + s.length()));
                    flag = true;
                }
            }
        }
        String s2 = stringbuffer.toString();
        
        return s2;
//        do

//            if(!iterator.hasNext())
//            {
//                String s2 = stringbuffer.toString();
//                String s1;
//                int i;
//                if(!flag)
//                    s2 = null;
//                return s2;
//            }
//            s1 = (String)iterator.next();
//            if(flag)
//            {
//                stringbuffer.append(s1);
//            } else
//            {
//                i = s1.indexOf(s);
//                if(-1 != i)
//                {
//                    stringbuffer.append(s1.substring(i + s.length()));
//                    flag = true;
//                }
//            }
//        } while(true);
    }
    
    private void getMmsXMmsString( List list)
    {
    	Log.d(TAG, "getMmsXMmsString");
        mmsContentType = getItemDataByBeginStr(list, "X-MMS-CONTENT-TYPE:");
        mmsContentLocation = getItemDataByBeginStr(list, "X-MMS-CONTENT-LOCATION:");
        mmsExpiry = getItemDataByBeginStr(list, "X-MMS-EXPIRY:");
        mmsMsgCls = getItemDataByBeginStr(list, "X-MMS-MSG-CLASS:");
        mmsMsgType = getItemDataByBeginStr(list, "X-MMS-MESSAGE-TYPE:");
        mmsVersion = getItemDataByBeginStr(list, "X-MMS-VERSION:");
        mmsSize = getItemDataByBeginStr(list, "X-MMS-MESSAGE-SIZE:");
        mmsPriority = getItemDataByBeginStr(list, "X-MMS-PRIORITY:");
        mmsTransID = getItemDataByBeginStr(list, "X-MMS-TRAN-ID:");
//        cc = getItemDataByBeginStr(list, "X-MMS-CC:");
//        bcc = getItemDataByBeginStr(list, "X-MMS-BCC:");
        mmsReadReport = getItemDataByBeginStr(list, "X-MMS-READ-REPORT:");
        mmsDelReport = getItemDataByBeginStr(list, "X-MMS-DEL-REPORT:");
    }
    
    
    private void getMmsBodyString(boolean flag, List list)
    {
        if(flag)
        {
            mmsRead = 0;
            String s1 = getItemDataByBeginStr(list, "X-READ:");
            if(s1 == null)
                s1 = getItemDataByBeginStr(list, "X-STATUS:");
            if(s1 != null && s1.equals("READ"))
                mmsRead = 1;
        }
        messageSubID = getItemDataByBeginStr(list, "X-SIMID:");
        if(messageSubID == null)
            messageSubID = getItemDataByBeginStr(list, "X-CARD:");
        mmsLocked = 0;
        String s = getItemDataByBeginStr(list, "X-LOCKED:");
        if(s != null && s.equals("LOCKED"))
            mmsLocked = 1;
        mmsDate = getItemDataByBeginStr(list, "Date:");
        getMmsXMmsString(list);
        mmsSubject = getUtf8QPStr(list, "Subject");
        if(mmsSubject == null)
           mmsSubject = getItemDataByBeginStr(list, "X-TITLE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:");
        if(mmsSubject != null)
           mmsSubjectCharset = "106";
    }
    public final String decodeQuotedPrintable(String s)
    {
        String s1 = null;
        if(null == s)
        	return s1;
//_L2:
        byte abyte0[];
        ByteArrayOutputStream bytearrayoutputstream;
        int i;
        abyte0 = s.getBytes();
        if(abyte0 == null)
        	return s1;
//            continue; /* Loop/switch isn't completed */
        bytearrayoutputstream = new ByteArrayOutputStream();
        i = 0;
//_L4:
        byte byte0;
        int k;
        int l;
        
        while(i<abyte0.length) {
        	byte0 = abyte0[i];
        	 if(byte0 != 61) {
        		 break;
        	 }
        	 
             int j = i + 1;
//             try
             {
                 k = Character.digit((char)abyte0[j], 16);
                 i = j + 1;
                 l = Character.digit((char)abyte0[i], 16);
                 if(k != -1 && l != -1) {
//                     break label0;
                     char c = (char)(l + (k << 4));
                     bytearrayoutputstream.write(c);
                 }

             }
             i++;
             
        }
        s1 = bytearrayoutputstream.toString();
        return s1;
//        if(i >= abyte0.length)
//        {
//            s1 = bytearrayoutputstream.toString();
//        } else
//        {
////label0:
//            {
//                byte0 = abyte0[i];
//                if(byte0 != 61)
//                    break MISSING_BLOCK_LABEL_146;
//                int j = i + 1;
//                try
//                {
//                    k = Character.digit((char)abyte0[j], 16);
//                    i = j + 1;
//                    l = Character.digit((char)abyte0[i], 16);
//                    if(k != -1 && l != -1){
////                        break label0;
//                        char c = (char)(l + (k << 4));
//                        bytearrayoutputstream.write(c);
//                    }
////                    Logging.e("Invalid quoted-printable encoding");
//                    
//                    
//                }
//                catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception)
//                {
////                    Logging.e("Invalid quoted-printable encoding");
//                	arrayindexoutofboundsexception.printStackTrace();
//                }
//            }
//        }
//        if(true) goto _L1; else goto _L3
//_L3:
//        char c = (char)(l + (k << 4));
//        bytearrayoutputstream.write(c);
//_L5:
//        i++;
//          goto _L4
//        bytearrayoutputstream.write(byte0);
//          goto _L5
    }
    public String getUtf8QPStr(List list, String s)
    {
        String s1;
        boolean flag;
        int i;
        s1 = null;
        flag = false;
        i = 0;
//_L3:
//        if(i < list.size()) goto _L2; else goto _L1
//_L1:
        while(i<list.size()) {
            String s2 = (String)list.get(i);
            if(flag)
            {
                if(!s1.endsWith("=")) {
//                    continue; /* Loop/switch isn't completed */
//                    s1 = (new StringBuilder(String.valueOf(s1))).append(s2).toString();
//                    list.remove(i);
//                    i--;
                	break;
                }
                s1 = (new StringBuilder(String.valueOf(s1.substring(0, -1 + s1.length())))).append(s2).toString();
                list.remove(i);
                i--;
            } else
            if(-1 != s2.indexOf(s))
            {
                int j = s2.indexOf("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:");
                if(j < 0)
                    j = s2.indexOf("CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:");
                if(-1 != j)
                {
                    s1 = s2.substring(j + "CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:".length());
                    flag = true;
                    list.remove(i);
                    i--;
                }
            }
            i++;
        }
        String s3 = s1;
        if(flag && s1 != null && s1.length() > 0)
            s3 = decodeQuotedPrintable(s1);
        return s3;
//_L2:
//        String s2 = (String)list.get(i);
//        if(flag)
//        {
//            if(!s1.endsWith("="))
//                continue; /* Loop/switch isn't completed */
//            s1 = (new StringBuilder(String.valueOf(s1.substring(0, -1 + s1.length())))).append(s2).toString();
//            list.remove(i);
//            i--;
//        } else
//        if(-1 != s2.indexOf(s))
//        {
//            int j = s2.indexOf("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:");
//            if(j < 0)
//                j = s2.indexOf("CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:");
//            if(-1 != j)
//            {
//                s1 = s2.substring(j + "CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:".length());
//                flag = true;
//                list.remove(i);
//                i--;
//            }
//        }
//        i++;
//          goto _L3
//        if(!s2.startsWith("=")) goto _L1; else goto _L4
//_L4:
//        s1 = (new StringBuilder(String.valueOf(s1))).append(s2).toString();
//        list.remove(i);
//        i--;
//        break MISSING_BLOCK_LABEL_114;
    }
    
    
    private void getMmsTelString(boolean flag, List list)
    {
    	if(flag) {
    		fromPhoneNumber = getItemDataByBeginStr(list, "TEL:");
    	       if(fromPhoneNumber == null)
    	            fromPhoneNumber = getItemDataByBeginStr(list, "FROMTEL:");
    	} else {
            String s;
            LinkedList linkedlist;
            s = getItemDataByBeginStr(list, "TEL:");
            if(s == null)
                s = getItemDataByBeginStr(list, "TOTEL:");
            linkedlist = new LinkedList();
            
            while(s != null) {
                linkedlist.add(s);
                s = getItemDataByBeginStr(list, "TEL:");
                if(s == null)
                    s = getItemDataByBeginStr(list, "TOTEL:");
            }
            
            if(linkedlist.size() > 0) {
                int i;
                Iterator iterator;
                toPhoneNumberCounts = linkedlist.size();
                toPhoneNumber = new String[linkedlist.size()];
                i = 0;
                iterator = linkedlist.iterator();	
                
                while (iterator.hasNext()) {
//					type type = (type) iterator.nextElement();
					 String s1 = (String)iterator.next();

				        toPhoneNumber[i] = s1;
				        i++;
				        
				}
            }
    	}
//        if(!flag) goto _L2; else goto _L1
//_L1:
//        fromPhoneNumber = getItemDataByBeginStr(list, "TEL:");
//        if(fromPhoneNumber == null)
//            fromPhoneNumber = getItemDataByBeginStr(list, "FROMTEL:");
//_L6:
//        return;
//_L2:
//        String s;
//        LinkedList linkedlist;
//        s = getItemDataByBeginStr(list, "TEL:");
//        if(s == null)
//            s = getItemDataByBeginStr(list, "TOTEL:");
//        linkedlist = new LinkedList();
//_L7:
//        if(s != null) goto _L4; else goto _L3
//_L3:
//        if(linkedlist.size() <= 0) goto _L6; else goto _L5
//_L5:
//        int i;
//        Iterator iterator;
//        toPhoneNumber = new String[linkedlist.size()];
//        i = 0;
//        iterator = linkedlist.iterator();
//_L8:
//        if(iterator.hasNext())
//            break MISSING_BLOCK_LABEL_155;
//        toPhoneNumberCounts = linkedlist.size();
//          goto _L6
//_L4:
//        linkedlist.add(s);
//        s = getItemDataByBeginStr(list, "TEL:");
//        if(s == null)
//            s = getItemDataByBeginStr(list, "TOTEL:");
//          goto _L7
//        String s1 = (String)iterator.next();
//        String as[] = toPhoneNumber;
//        int j = i + 1;
//        as[i] = s1;
//        i = j;
//          goto _L8
    }
    public static String getItemDataByBeginStr(List list, String s)
    {
//        String s1;
        int i;
//        s1 = null;
//        if(s.startsWith("X-MMS-"))
//            s1 = (new StringBuilder("X-ZTE-MMS-")).append(s.substring("X-MMS-".length())).toString();
        i = 0;
        
        while(i < list.size()) {
        	String s2 = (String)list.get(i);
            if(s2.startsWith(s))
            {
            	String s3 = s2.substring(s.length());
                list.remove(i);
//                continue; /* Loop/switch isn't completed */
                return s3;
            }
            i++;
        }
//_L6:
//        if(i < list.size()) goto _L2; else goto _L1
//_L1:
//        String s3 = null;
//_L4:
//        return s3;
//_L2:
//        String s2 = (String)list.get(i);
//        if(s2.startsWith(s))
//        {
//            s3 = s2.substring(s.length());
//            list.remove(i);
//            continue; /* Loop/switch isn't completed */
//        }
//        if(s1 == null || !s2.startsWith(s1))
//            break; /* Loop/switch isn't completed */
//        s3 = s2.substring(s1.length());
//        list.remove(i);
//        if(true) goto _L4; else goto _L3
//_L3:
//        i++;
//        if(true) goto _L6; else goto _L5
//_L5:
        return null;
    }
    
}
