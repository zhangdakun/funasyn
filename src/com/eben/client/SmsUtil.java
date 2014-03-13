package com.eben.client;

import java.util.ArrayList;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsUtil {
	public static void sendSMS(Context ctx, String phoneNumber, String message) {
		sendSMS(ctx, phoneNumber, message, null);
	}

	/**
	 * 
	 * 
	 * @param ctx
	 *        
	 * @param phoneNumber
	 *           
	 * @param message
	 *            
	 * @param toastText
	 *           
	 */
	public static void sendSMS(Context ctx, String phoneNumber, String message,
			String toastText) {
		Log.d("sms", "sending sms");

		if (!checkMessage(ctx, phoneNumber, message)) {
			return;
		}

		SmsManager smsMgr = SmsManager.getDefault();

		if (message.length() > 70) {
			ArrayList<String> msgs = smsMgr.divideMessage(message);
			for (String msg : msgs) {
				smsMgr.sendTextMessage(phoneNumber, null, msg, null, null);
			}
		} else {
			smsMgr.sendTextMessage(phoneNumber, null, message, null, null);
		}

//		if (toastText == null) {
//			UIUtil.showMessage(ctx, "forward sms to " + phoneNumber);
//		} else {
//			UIUtil.showMessage(ctx, toastText);
//		}
	}

	private static boolean checkMessage(Context ctx, String phoneNumber,
			String message) {
		// if(StringUtils.isEmptyTrim(phoneNumber)){
		// UIUtil.showMessage(ctx, R.string.error_forward_phone_is_empty);
		// Log.d("sms", "Forward Failure! Forward phone is empty!");
		// return false;
		// }
		//
		// if(StringUtils.isEmptyTrim(message)){
		// UIUtil.showMessage(ctx, R.string.error_forward_message_is_empty);
		// return false;
		// }

		return true;
	}
}
