package cn.eben.android.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
	public static String getString(String str) {
		if (str == null || "".equals(str)) {
			return "";
		}
		return str;
	}
	
	public static String time2String(String time) {
		String tempTime = "";
		if (time != null && !"".equals(time)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date tempDate = sdf.parse(time);
				tempTime = tempDate.getTime() + "";
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tempTime;
	}
}
