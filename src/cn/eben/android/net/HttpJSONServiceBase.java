package cn.eben.android.net;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.DateFormat;
//import cn.eben.android.externalEntry.ExSyncManager;
//import cn.eben.android.externalEntry.ExternalEntryConst;
import cn.eben.android.util.EbenHelpers;
import cn.eben.android.util.MD5Util;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.App;
import com.funambol.sync.SyncException;
import com.funambol.util.Base64;
import com.funambol.util.Log;

public abstract class HttpJSONServiceBase implements HttpJSONserviceConst {

	private final static String TAG = "HttpServiceBase";

	private final static int RETRY_TIMES = 3;
//	private int retry = 0;
	boolean authenFailed = false;

	public synchronized void handler() throws Exception {
//		ExternalEntryConst.NETWORK_OK == EbenHelpers.isNetworkAvailable()

		for (int retry = 0; retry < RETRY_TIMES; retry++) {
			int net = EbenHelpers.isNetworkAvailable();		
//			if (ExternalEntryConst.NETWORK_OK != net) {
////				return;
////				ExSyncManager.i().exitApp(new Exception("network not availabe in http"));
//				throw new SyncException(net,"network notAvailable");
////				return;
//			}
			HttpClient hc = CustomerHttpClient.getHttpClient();
			HttpPost request = getHttpPost();

			if(null == request) {
				authenFailed = true;
				Log.error(TAG,"null request");
				Thread.sleep(5*1000);
				continue;
			}
			HttpResponse response = null;
			HttpEntity entity = null;
			try {
				response = hc.execute(request);

				entity = response.getEntity();
				



				String result = null;
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (entity != null) {

						result = EntityUtils.toString(entity, "UTF-8");
						Log.debug(TAG, "retry : " + retry+ ", response: " + result);// need check
						// authority
						// failed,
						if (authorityFailed(response,result) && retry < RETRY_TIMES - 1) {
							// set authority true;
							authenFailed = true;
							continue;
						}
						authenFailed = false;

						processResponesJSON(result);
						return;
					}
				}

			}catch(SyncException e) {
				throw e;
			}
			catch (JSONException e) {
				Log.error(TAG, "json format is invalid!");
				e.printStackTrace();
				throw e;
			} catch(ConnectTimeoutException e) {
				e.printStackTrace();
				throw new SyncException(111,"http connect timeout");
				// getsynclist too much cause time out ,next sync will do as add and delete
//				if(EbenHelpers.getWifiRssi() < -85) {
//					throw new SyncException(ExternalEntryConst.NETWORK_TIMEOUT,"http connect timeout");
//				}
//				if (retry < RETRY_TIMES - 1) {
//					continue;
//				} else {
//					throw new SyncException(ExternalEntryConst.NETWORK_TIMEOUT,"http connect timeout");
//				}
			}
			catch (Exception e) {
				e.printStackTrace();
				if (retry < RETRY_TIMES - 1) {
					continue;
				} else {
					throw e;
				}
			} finally {

			}
			break;
		}

	}

	private boolean isNetworkAvailable() {
		// TODO Auto-generated method stub
//		return EbenHelpers.isNetworkAvailable();
//		return (ExternalEntryConst.NETWORK_OK == EbenHelpers.isNetworkAvailable());
		return true;
//		WifiManager wf = (WifiManager) App.i().getApplicationContext()
//				.getSystemService(Context.WIFI_SERVICE);
//
////		if (EbenHelpers.isWifiOnly()) { // set
////			Log.info(TAG, "wlan only");
////			if (!wf.isWifiEnabled()) {
////				Log.info(TAG, "wlan not enable ,exit");
////				return false;
////			}
////		}
////		return true;
	}

	private boolean authorityFailed(HttpResponse response, String result) {
		// TODO Auto-generated method stub
		// {"op":1001,"data":{"apps":
		// {"code":103,"result":"Get key failed."}
		if (AndroidConfiguration.authSyncInter) {
			try {
				JSONObject jo = new JSONObject(result);
				int code = jo.getInt("code");				
				Log.info(TAG, "code : " +code);
				if(102==code) {
					
					Header[] headers = response.getHeaders("Date");
					Log.info(TAG,"date headers + "+headers+", length "+headers.length);
					for (int i = 0; i < headers.length; i++) {
						String rDate = headers[i].getValue();
//						SimpleDateFormat datef = new SimpleDateFormat();
						long time = Date.parse(rDate);
						long localt = new Date().getTime();
						Log.info(TAG, "server time : "+time+", curtime : "+localt);
						if(Math.abs(time - localt)>15*16*1000L) {
							Log.error(TAG, "date has expired");
							throw new SyncException(5434, "Date has expired");
						}
					}
				}
				return 0 != code;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
//		if (!result.contains("\"op\":") && !result.contains("\"code\":0")) {
//			return true;
//		}
		return false;
	}

	// {"code":103,"result":"Get key failed."}
	private HttpPost getHttpPost() {
		// TODO Auto-generated method stub
		HttpPost request = new HttpPost(getRequestURL());
		JSONObject jo;
		try {
			jo = processRequestJSON();
			String json = jo.toString();
			json = json.replaceAll("\\\\", ""); //lierbao
			request.setEntity(new StringEntity(json, "UTF-8"));
			if (AndroidConfiguration.authSyncInter) {
				if (!EbenAuthen.isChecked() || authenFailed) {
					EbenAuthen.requestAuthen();
				}

				request.addHeader("X-ECLIENT-VERSION", "1.1");
				request.addHeader("x-ebuild-version", "8804");
				String contentMD5 = MD5Util.md5(json);
				request.addHeader("Content-MD5", contentMD5);
				Log.debug(TAG, "md5 : " + contentMD5);

				String contentType = "application/json";
				request.addHeader("Content-Type", contentType);

				SimpleDateFormat dateformat1 = new SimpleDateFormat(
						"E, dd MMM yyyy HH:mm:ss z", Locale.US);
				dateformat1.setTimeZone(new SimpleTimeZone(0, "GMT"));

				// Sun, 06 Nov 1994 08:49:37 GMT ; RFC 822, updated by RFC 1123

				// String date = dateformat1.format(new Date());
				//
				// Log.info(TAG, "date: " + date);

				Calendar calendar1 = Calendar.getInstance();
				TimeZone tztz = TimeZone.getTimeZone("GMT");
				calendar1.setTimeZone(tztz);

				String date = dateformat1.format(calendar1.getTime());
				Log.debug(TAG, "date: " + date);

				request.addHeader("Date", date);

				String tokenVersion = EbenAuthen.getTokenVersion();
				if(null == tokenVersion) {
					return null;
				}
				request.addHeader("X-ECSI-TokenVer", tokenVersion);

				String oriString = request.getMethod().trim() + "\n"
						+ contentMD5 + "\n" + contentType + "\n" + date + "\n"
						+ tokenVersion;
				String userName = App.i().getAppInitializer().getUserName();
				String token = getDesToken(EbenAuthen.getToken(),
						reverseString(userName).substring(0, 8));

				String signatureString = null;
				try {
					signatureString = getSignature(oriString, token);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String ecsiString = "ECSI " + userName + ":" + signatureString;
				Log.debug(TAG, "ecsiString : " + ecsiString);
				request.addHeader("Authorization", ecsiString);
			}
			Log.debug(TAG, "send: " + json);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return request;
	}

	private static final String HMAC_SHA1 = "HmacSHA1";

	public static String getSignature(String data, String key) throws Exception {
		Log.debug(TAG, " data: " + data + ", key: " + key);
		byte[] keyBytes = key.toLowerCase().getBytes("UTF-8");
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
		return new String(Base64.encode(rawHmac), "UTF-8");
	}

	private static String getDesToken(String encToken, String strKey) {
		Cipher cipher = null;
		String token = null;
		try {
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(strKey.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(dks);
			cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, sr);

			byte[] byteFina = cipher
					.doFinal(Base64.decode(encToken.getBytes()));
			token = new String(byteFina, "UTF-8");
		} catch (Exception e) {
			return null;
		} finally {
			cipher = null;
		}
		return token;
	}

	private String reverseString(String s) { // revert string
		String newStr = "";
		int len = s.length();
		for (int i = len - 1; i >= 0; i--) {
			newStr += s.charAt(i);
		}
		return newStr;
	}

	public abstract JSONObject processRequestJSON() throws Exception;

	public abstract void processResponesJSON(String json) throws Exception;

	public abstract String getRequestURL();

}
