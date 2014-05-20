package cn.eben.android.net;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import cn.eben.android.EbenConst;

import cn.eben.android.source.edisk.EdiskdbStore;
import cn.eben.android.util.EbenConstantsUtil;

import com.funambol.android.AndroidCustomization;
import com.funambol.android.App;
import com.funambol.android.AppInitializer;
import com.funambol.storage.StringKeyValuePair;

final public class NetworkUtilities {

    private final static String TAG = "NetworkUtilities";
    
    private final static int REGISTRATION_TIMEOUT_MS = 100 * 1000;
    
    private final static int EBEN_REGISTRATION_TIMEOUT_MS = 120 * 1000;

    private static final String BASE_URL = EbenConst.HTTP_HOST + "funambol/PassportSevlet";

    private static final String  WEBPORTAL_SEC_LOCK_URI  = EbenConst.HTTP_HOST + "t4portal/sec/pushlock";
    
    private static final String WEB_CENTER_URI = "http://passport.eben.cn/userapp/userinfoterminalapi/";
    
    private static final int NUM_RETRY = 3;
	private static HttpClient sHttpClient = null;
	private static HttpGet request = null;
	/**

	 */
	public static final String KEY_LAN_STAT = "dhcp.eth0.eth0_stat";
	/**
	 *
	 */
	public static final String LAN_STAT_CONNECTED = "connected";
        
    private static HttpClient getDefaultHttpClient() {
        HttpClient sHttpClient = new DefaultHttpClient();
        final HttpParams params = sHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT_MS);
        return sHttpClient;
    }
    
    private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
        if (hrb != null) {
            hrb.abort();
        }
        if (httpclient != null) {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static void abortConnection(){
        if (request != null) {
        	request.abort();
        }
        if (sHttpClient != null) {
        	sHttpClient.getConnectionManager().shutdown();
        }
    }
    
    
    private static void setParams(ArrayList<NameValuePair> params, String type){
        AppInitializer initializer = App.i().getAppInitializer();
        params.add(new BasicNameValuePair("op", type));
        params.add(new BasicNameValuePair("username", initializer.getUserName()));
        params.add(new BasicNameValuePair("passwd", initializer.getPassword()));
        params.add(new BasicNameValuePair("device", App.i().getAppInitializer().getConfiguration().getDeviceConfig().getDevID()));
    
    }
    
    public static boolean saveSecuritySetting(String key){
        
        final HttpResponse resp;
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        setParams(params, "-1");
        params.add(new BasicNameValuePair("key", key));
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        
        
        final HttpPost post = new HttpPost(WEBPORTAL_SEC_LOCK_URI);
        post.addHeader(entity.getContentType());
        
        post.setEntity(entity);
        HttpClient sHttpClient = getDefaultHttpClient();
        try {
            resp = sHttpClient.execute(post);
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Header[] sheader = resp.getHeaders("ebensec");
                if(sheader != null && sheader.length == 1){
                    return "1".equals(sheader[0].getValue()) ? true : false;
                }
            }
        }
        catch (final IOException e) {
            Log.v(TAG, "IOException \r\n" + e.getMessage());

        }
        finally{
            abortConnection(post, sHttpClient);
        }
        return false;
    }
    
    private static long mreNameTime;
    private static String moldName;
    private static String mnewName;
    private static String mediskDirectory = "/.edisk/";
    
    public static void loadEdiskRename(Context context) throws JSONException{
        final Context scontext = context;
        EdiskdbStore ediskdb = new EdiskdbStore(scontext);
        Enumeration ediskGuids = ediskdb.keyValuePairs();
        
        if(ediskGuids.hasMoreElements()){
            
            JSONArray jArray = new JSONArray();
            int number = 0;
            while (ediskGuids.hasMoreElements()) {
                StringKeyValuePair keyValue = (StringKeyValuePair) ediskGuids.nextElement();
                JSONObject jObject = new JSONObject();
                jObject.put("guid", keyValue.getValue());
                jObject.put("luid", keyValue.getKey());
                jArray.put(number++, jObject);                
            }
            
            final HttpResponse resp;
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            setParams(params, "500");
//            params.add(new BasicNameValuePair("device", App.i().getAppInitializer().getConfiguration().getDeviceConfig().getDevID()));
            params.add(new BasicNameValuePair("guidluids", jArray.toString()));
            HttpEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(params, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
            final HttpPost post = new HttpPost(BASE_URL);
            post.addHeader(entity.getContentType());
            
            post.setEntity(entity);
            HttpClient sHttpClient = getDefaultHttpClient();
            try {
                resp = sHttpClient.execute(post);
                
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    final String response = EntityUtils.toString(resp.getEntity(), "utf-8");
                    final JSONArray users = new JSONArray(response);
                    int lenth = users.length();
                    
                    for(int i=0; i<lenth; i++){
                        JSONObject jobject = users.getJSONObject(i);
                        String newName = jobject.getString("luid");
                        String guid = jobject.getString("guid");
                        String oldName = ediskdb.get(guid);
                        if(oldName != null){
                            EdiskdbStore.reNamedbStore(scontext, oldName, newName);
//                            ediskdb.update(oldName, newName);

                            mreNameTime = System.currentTimeMillis();
                            moldName = oldName;
                            mnewName = newName;
                            String sdCardRoot = Environment.getExternalStorageDirectory().toString();
                            File newFile = new File(sdCardRoot + mediskDirectory + newName);
                            File fileFolder = new File(newFile.getParent());
                            fileFolder.mkdirs();
                            File oldFile = new File(sdCardRoot + mediskDirectory + oldName);
                            oldFile.renameTo(newFile);
                        }

                    }

                }
            }
            catch (final IOException e) {
                Log.v(TAG, "IOException \r\n" + e.getMessage());

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally{
                abortConnection(post, sHttpClient);
            }

        }
        ediskdb.save();
    }
    
    public static void loadEdiskGuid(String luids, Context context){      
        final Context scontext = context;
        final HttpResponse resp;
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        setParams(params, "501");
//        params.add(new BasicNameValuePair("device", App.i().getAppInitializer().getConfiguration().getDeviceConfig().getDevID()));
        params.add(new BasicNameValuePair("luids", luids.toString()));
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        final HttpPost post = new HttpPost(BASE_URL);
        post.addHeader(entity.getContentType());
        
        post.setEntity(entity);
        HttpClient sHttpClient = getDefaultHttpClient();
        try {
            resp = sHttpClient.execute(post);
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final String response = EntityUtils.toString(resp.getEntity(), "utf-8");
                final JSONArray users = new JSONArray(response);
                int lenth = users.length();
                EdiskdbStore ediskdb = new EdiskdbStore(scontext);
                ediskdb.reset();
                for(int i=0; i<lenth; i++){
                    JSONObject jobject = users.getJSONObject(i);
                    ediskdb.add(jobject.getString("luid"), jobject.getString("guid"));
                }
                ediskdb.save();

            }
        }
        catch (final IOException e) {
            Log.v(TAG, "IOException \r\n" + e.getMessage());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally{
            abortConnection(post, sHttpClient);
        }

        
    }
    
    
    public static boolean ediskReName(String newName, String oldName, String type){
        if(mreNameTime > 0 && (System.currentTimeMillis() - mreNameTime  > 3000) && newName.equals(mnewName) && oldName.equals(moldName)) return false;
        final HttpResponse resp;
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        setParams(params, "400");
        params.add(new BasicNameValuePair("newpath", newName));
        params.add(new BasicNameValuePair("oldpath", oldName));
        params.add(new BasicNameValuePair("type", type));
//        params.add(new BasicNameValuePair("device", App.i().getAppInitializer().getConfiguration().getDeviceConfig().getDevID()));
        
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        final HttpPost post = new HttpPost(BASE_URL);
        post.addHeader(entity.getContentType());
        
        post.setEntity(entity);
        HttpClient sHttpClient = getDefaultHttpClient();
        try {
            resp = sHttpClient.execute(post);
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

               return true;
            }
        }
        catch (final IOException e) {
            Log.v(TAG, "IOException \r\n" + e.getMessage());

        }
        finally{
            abortConnection(post, sHttpClient);
        }
        
        return false;
    }
    
//    public static List<AndroidCustomization.ActivationDeviceInfo> loadActivationDevices(){
//        final HttpResponse resp;
//        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//        setParams(params, "300");
//        HttpEntity entity = null;
//        try {
//            entity = new UrlEncodedFormEntity(params, "UTF-8");
//        } catch (final UnsupportedEncodingException e) {
//            throw new AssertionError(e);
//        }
//        final HttpPost post = new HttpPost(BASE_URL);
//        post.addHeader(entity.getContentType());
//        
//        post.setEntity(entity);
//        HttpClient sHttpClient = getDefaultHttpClient();        
//        try {
//            resp = sHttpClient.execute(post);
//            
//            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                final String response = EntityUtils.toString(resp.getEntity(), "utf-8");
//                final JSONArray users = new JSONArray(response);
//                int lenth = users.length();
//                final ArrayList<AndroidCustomization.ActivationDeviceInfo> usersList = new ArrayList<AndroidCustomization.ActivationDeviceInfo>();
//                for(int i=0; i<lenth; i++){
//                    usersList.add(AndroidCustomization.ActivationDeviceInfo.valueOf(users.getJSONObject(i)));
//                }
//
//                return usersList;
//            }
//        }
//        catch (final IOException e) {
//            Log.v(TAG, "IOException \r\n" + e.getMessage());
//
//        }catch (org.json.JSONException e) {
//            Log.v(TAG, "JSONException \r\n" + e.getMessage());
//        }
//        finally{
//            abortConnection(post, sHttpClient);
//        }
//
//
//        return null;
//    }
       
    
    /**

     */
//    public static SourceCapacityEntity getAllCapacityAndNotePadNumAndPercent(String appCode){
//    	HttpResponse response = null;
//    	String result = null;
//    	HttpPost request = null;
//    	HttpClient sHttpClient = null;
//    	JSONObject jsonObject;
//    	String notepadUseCapacity = EbenConstantsUtil.DEFAULT_CAPACITY;
//    	String ediskUseCapacity = EbenConstantsUtil.DEFAULT_CAPACITY;
//    	String totalCapacity = EbenConstantsUtil.DEFAULT_TOTAL_CAPACITY;
//    	int iNotepadCount = EbenConstantsUtil.DEFAULT_NOTEPAD_COUNT;
//    	
//        try {
//        	request = new HttpPost(BASE_URL);
//        	
//        	final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//            setParams(params, "601");
//            params.add(new BasicNameValuePair("type", appCode));
//            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
//            request.addHeader(entity.getContentType());
//            
//            request.setEntity(entity);
//        	sHttpClient = getDefaultHttpClient();
//			response = sHttpClient.execute(request);
//			if(null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//				result = EntityUtils.toString(response.getEntity(), "utf-8");
//			    if(null != result){				 
//				   jsonObject = new JSONObject(result);
//				   iNotepadCount = jsonObject.getInt("notepadUsedCount");
//				   notepadUseCapacity = jsonObject.getString("notepadUsedStorage");
//				   ediskUseCapacity = jsonObject.getString("ediskUsedStorage");
//				   totalCapacity = jsonObject.getString("count");
//				   return new SourceCapacityEntity(notepadUseCapacity, ediskUseCapacity, totalCapacity, iNotepadCount);						   	
//				 }
//			}
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch(JSONException e){
//			e.printStackTrace();
//		}
//		finally{
//		    abortConnection(request, sHttpClient);
//		}
//		return null;
//    }
//    
    
    public static String getUserTrueName(String userName, String password, String CPUSerial){
    	HttpResponse response = null;
    	String result = null;
    	JSONObject jsonObject;
    	String trueNameString = "";
    	
    	String WEB_URL = WEB_CENTER_URI + "?username="+userName+"&passwd="+password+"&snum="+CPUSerial;
    	request = new HttpGet(WEB_URL);
    	sHttpClient = getDefaultHttpClient();
    	try{
        	for(int i = 0; i < NUM_RETRY; i++){
        		try {
        			response = sHttpClient.execute(request);
        			if(null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
        				result = EntityUtils.toString(response.getEntity(), "utf-8");
        			    if(null != result){				 
        				   jsonObject = new JSONObject(result);
        				   Log.e("---------", "result=== "+result.toString());
        				   trueNameString =  jsonObject.getString("truename");
        				   return trueNameString;
        				 }
        			}
        		} catch (IOException e) {
                    if(i<NUM_RETRY-1) {
                        continue;
                    } else {
                        // Forward the exception
                        throw e;
                    }
        		} 
            	break;
        	}
    	}catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(JSONException e){
			e.printStackTrace();
		}
		finally{
		    abortConnection(request, sHttpClient);
		}        
		return trueNameString;
    }
    
    public static boolean isNetworkAvailable(Context context) {

    	ConnectivityManager cwjManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
        if(null == info || !info.isAvailable()) {
        	return false;
        }
        return true;
    }    
}
