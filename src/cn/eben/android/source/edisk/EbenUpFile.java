package cn.eben.android.source.edisk;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpPut;

import android.net.ConnectivityManager;

import cn.eben.android.util.EbenFileLog;
import cn.eben.android.util.EbenHelpers;

import com.funambol.sync.SyncException;
import com.funambol.util.Log;

public class EbenUpFile {
	public static final String TAG_LOG = "EbenUpFile";
	public static int MAXBUF = 512;
	public static int RETRY = 3;

	// public static void upfile(String url, String fileName) {
	// HttpClient hc = CustomerHttpClient.getHttpClient();
	// HttpPut request = getHttpPut(url);
	//
	// request.
	// }
	private static int uploadId = 10000; // test multi upload

	private static void initiateMultipartUpload(String url) {
		Log.info(TAG_LOG, "initiateMultipartUpload");
		HttpURLConnection ucon;
		try {
			ucon = (HttpURLConnection) new URL(url).openConnection();

			ucon.setRequestProperty("Connection", "keep-alive");
			ucon.setConnectTimeout(3 * 1000);
			ucon.setReadTimeout(3 * 1000);

			ucon.setRequestProperty("uploads", "");
			ucon.connect();

			getResponse(ucon);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean completeMultipartUpload(String url, String eTag) {
		Log.info(TAG_LOG, "completeMultipartUpload, url: " + url);
		
		HttpURLConnection ucon;
		OutputStream os = null;
		DataOutputStream dos = null;
		try {
			ucon = (HttpURLConnection) new URL(url).openConnection();

			ucon.setRequestProperty("Connection", "keep-alive");
			ucon.setConnectTimeout(3 * 1000);
			ucon.setReadTimeout(3 * 1000);

			ucon.setRequestProperty("uploadId", String.valueOf(uploadId));
			ucon.setRequestMethod("POST");
			ucon.setRequestProperty("Content-Type", "");
			// ucon.connect();

			os = ucon.getOutputStream();
			dos = new DataOutputStream(os);
			String content = getcomplete();
			dos.write(content.getBytes());

			if (200 != getResponse(ucon)) {
				return false;
			}
//			String respEtag = ucon.getHeaderField("ETag");
//			if(null == respEtag) {
//				Log.error(TAG_LOG, "error !! null response etag ");
//				return false; 
//			}
//			if (!respEtag.replaceAll("\"", "").equalsIgnoreCase(eTag)) {
//				Log.info(TAG_LOG, "error!! etag , local etag:  " + eTag
//						+ ", server eTag : " + respEtag);
//				return false;// o for error.
//			}
			
//			if (respEtag.replaceAll("\"", "").equalsIgnoreCase(
//					EdiskSyncSource.EmptyMd5)) {
////				Log.error(TAG_LOG, "upfile error");
////				return -1;
//			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			if(null != os) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(null != dos) {
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	/**
	 * 
	 * @return
	 */
	private static String getcomplete() {
		// TODO Auto-generated method stub
		StringBuilder buf = new StringBuilder();
		formatStartTag(buf, "CompleteMultipartUpload");
		if (null != multiList)
			for (UpfileInfo info : multiList) {
				formatStartTag(buf, "Part");
				formatCompleteTag(buf, "PartNumber", info.luid);
				formatCompleteTag(buf, "ETag", info.etag);
				formatEndTag(buf, "Part");
			}
		formatEndTag(buf, "CompleteMultipartUpload");
		return buf.toString();
	}

	// private static boolean uploadPart(String url, String fileName) {
	// Log.info(TAG_LOG, "uploadPart");
	// // for(int i=1; i<4;i++) {
	// // upfile(url, fileName,i);
	// // }
	// return upfile(url, fileName,0, true);
	// }
	// public static boolean upfile(String url, String fileName) {
	// Log.info(TAG_LOG,"upfile");
	// // initiateMultipartUpload(url);
	// return uploadPart( url, fileName);
	// // completeMultipartUpload(url);
	//
	// }
	// class MultiUp {
	// String partNumber;
	// String ETag;
	// }
	static ArrayList<UpfileInfo> multiList = null;
	public static long PARTSIZE = 6 * 1024 * 1024;

	public static boolean upfile(ArrayList<String> urlList, String fileName,
			String eTag) {
		Log.info(TAG_LOG, "upfile");
//		int net = EbenHelpers.isNetworkAvailable();
//		if (ExternalEntryConst.NETWORK_OK != net) {
//			Log.error(TAG_LOG, "network not available ");
//			throw new SyncException(net ,"net not available");
////			return false;
//		}
//    	if(ConnectivityManager.TYPE_MOBILE == EbenHelpers.getNetworkType()) {
//    		ExSyncManager.i().toast3GWarning();
//    	}
		int count = urlList.size();
		File file = new File(fileName);
		long size = file.length();
		size = size > PARTSIZE ? PARTSIZE : size;
		Log.info(TAG_LOG, "upfile : count " + count+", size: "+size);
		if (2 == count) {
			Log.error(TAG_LOG, "some error !!! part can not be count 2 !!! ");
		}
		if (count > 2) {
			long hasup = 0;
			long curup = 0;
			boolean bAll = false;
			multiList = new ArrayList();
			for (int i = 0; i < count - 1; i++) {
				if (i == count - 2) {
					bAll = true;
				}
				for (int j = 0; j < RETRY; j++) {
					Log.info(TAG_LOG, "to up file ,rety :"+j);
					curup = upfile(urlList.get(i), fileName, hasup, bAll, true,
							eTag);
					if (curup > 0) {
//						return false;
						break;
					}
					else if(j==(RETRY-1) ){
						return false;
					}
				}
				hasup += curup;
			}
			
			for (int j = 0; j < RETRY; j++) {
				Log.info(TAG_LOG, "to up complete load ,rety :"+j);
				boolean ok = completeMultipartUpload(urlList.get(count - 1),eTag);
				if(ok) {
					return ok;
				} else if(j==(RETRY-1) ){
					return false;
				}
			}
//			return completeMultipartUpload(urlList.get(count - 1),eTag);
		} else {
			for (int j = 0; j < RETRY; j++) {
				 
				Log.info(TAG_LOG, "to up file ,rety :"+j);
				long curup = upfile(urlList.get(0), fileName, 0, true, false, eTag);
				if (curup > 0) {
//					return false;
					return true;
				}
				else if(j==(RETRY-1) ){
					return false;
				}
			}			
//			return upfile(urlList.get(0), fileName, 0, true, false, eTag) > 0;
		}
		return false;
		// initiateMultipartUpload(url);
		// return uploadPart( url, fileName);
		// completeMultipartUpload(url);
		// return true;

	}

	// public static boolean upfile(String url, String fileName, int offset) {
	//
	// }
	public static long upfile(String url, String fileName, long offset,
			boolean allwrite, boolean multi, String eTag) {
		// url =
		// "http://storage.aliyun.com/edisk/15810388882/6B4B440EFC602B92B56AA92AA64BDA8C-B?Expires=1352354849&OSSAccessKeyId=gkrtagczalrc5uwvj43nnzju&Signature=sqYzrHddPBG8Jr3rM3xQU81WkyM%3D";
		Log.info(TAG_LOG, "upfile, " + fileName + ", url: " + url
				+ "  part offset  " + offset);
		URL u;
		RandomAccessFile raf = null;
		BufferedOutputStream bos = null;
		OutputStream os = null;
		try {
			File file = new File(fileName);
			long filesize = file.length();
			long size = filesize > PARTSIZE ? PARTSIZE : filesize;
			u = new URL(url);

			HttpURLConnection ucon = (HttpURLConnection) u.openConnection();

			ucon.setRequestProperty("Connection", "keep-alive");
			ucon.setConnectTimeout(3 * 1000);
			ucon.setReadTimeout(3 * 1000);
			ucon.setRequestMethod("PUT");

			ucon.setRequestProperty("Content-Type", "");

			ucon.setDoOutput(true);
			// ucon.setDoInput(true);
			ucon.setUseCaches(false);

//			ucon.connect();

			os = ucon.getOutputStream();

			raf = new RandomAccessFile(file, "rw");
			raf.seek(offset);

			bos = new BufferedOutputStream(os, (int) (size));
			byte[] tempbytes = new byte[MAXBUF];
			int byteread;
			long sum = 0;
			long sum2 = 0;
			// while ((byteread = in.read(tempbytes)) != -1) {
			while ((byteread = raf.read(tempbytes)) != -1) {
				sum += byteread;
				sum2 += byteread;

				bos.write(tempbytes, 0, byteread);

				if (sum2 > 1024 * 1024) {
					sum2 = 0;
					Log.info(TAG_LOG, "wait ");
					// os.flush();
					// Thread.sleep(60*1000);//for mem full issue debug
				}
				if (!allwrite && sum >= PARTSIZE) {
					Log.info(TAG_LOG, "part up , size :  " + sum);
					break;
				}

			}
			
//			ucon.setRequestProperty("Content-Length",String.valueOf(sum));
			// os.close();
			 bos.close();
			 bos = null;
			if (200 != getResponse(ucon)) {
				return -1;
			}
			String part = url.substring(url.length() - 1);
			String respEtag = ucon.getHeaderField("ETag");
			if(null == respEtag) {
				Log.error(TAG_LOG, "error !! null response etag ");
			}
			if (respEtag.replaceAll("\"", "").equalsIgnoreCase(
					EdiskSyncSource.EmptyMd5)) {
//				Log.error(TAG_LOG, "upfile error");
//				return -1;
			}
			if (multi) {
				setMultiUpInfo(part, respEtag);
			} else {
				if (!respEtag.replaceAll("\"", "").equalsIgnoreCase(eTag)) {
					Log.info(TAG_LOG, "error!! etag , local etag:  " + eTag
							+ ", server eTag : " + respEtag);
					return -1;// o for error.
				}
			}
			Log.info(TAG_LOG, "write ok ,write : " + sum + ", file size : "
					+ size);
//			EbenFileLog
//					.recordSyncLog("upfile : " + fileName + " --> oss cloud");
			// in.close();
			// raf.close();
			if (size > 1024 * 1024) {
				Log.info(TAG_LOG, "file size : " + size);
				// Thread.sleep(1000);
			}
			return sum;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.error(TAG_LOG, "err !! " + e.toString());
//			EbenFileLog.recordSyncLog("upfile error : " + fileName
//					+ " --> oss cloud !! " + e.toString());
			e.printStackTrace();

		} finally {
			try {
				if (null != os)
					os.close();
			} catch (Exception e) {
				Log.error(TAG_LOG, "close os err !! " + e.toString());
				e.printStackTrace();
			}			
			try {
				if (null != raf)
					raf.close();
			} catch (Exception e) {
				Log.error(TAG_LOG, "close raf err !! " + e.toString());
				e.printStackTrace();
			}
			try {
				if (null != bos)
					bos.close();
			} catch (Exception e) {
				Log.error(TAG_LOG, "close bos err !! " + e.toString());
				e.printStackTrace();
			}
		}
		return -1;
	}

	private static void setMultiUpInfo(String part, String etag) {
		// TODO Auto-generated method stub
		Log.info(TAG_LOG, "part: " + part + ", etag : " + etag);
		if (null == multiList) {
			multiList = new ArrayList();
		}
		// UpfileInfo info = new UpfileInfo(part,etag,0,null);
		multiList.add(new UpfileInfo(part, etag, 0, null));

	}

	private static HttpPut getHttpPut(String url) {
		// TODO Auto-generated method stub
		HttpPut request = new HttpPut(url);

		return request;
	}

	private static int getResponse(HttpURLConnection ucon) throws IOException {
		// TODO Auto-generated method stub
		int resp = ucon.getResponseCode();
		Log.info(TAG_LOG, "resp is , " + resp);
		if (resp != 200) {
			InputStream erris = ucon.getErrorStream();
			// Log.info(TAG_LOG, IOUtils.toString(conn.getInputStream()));
			byte[] temp = new byte[MAXBUF];
			int read = 0;
			while ((read = erris.read(temp)) != -1) {
				Log.info(TAG_LOG, "err response: " + new String(temp, 0, read));

			}

		} else {
			// InputStream is = ucon.getInputStream();
			Log.info(TAG_LOG, "contentLength: " + ucon.getContentLength());
			Log.info(TAG_LOG, ucon.getHeaderField("ETag"));
			// byte[] temp = new byte[MAXBUF];
			// int read = 0;
			// while ((read = is.read(temp)) != -1) {
			// Log.info(TAG_LOG, " response: "
			// + new String(temp, 0, read));
			//
			// }

			// InputStream erris = ucon.getErrorStream();
			// // Log.info(TAG_LOG, IOUtils.toString(conn.getInputStream()));
			// byte[] temp2 = new byte[MAXBUF];
			// int read2 = 0;
			// while ((read2 = erris.read(temp2)) != -1) {
			// Log.info(TAG_LOG, "err response: "
			// + new String(temp, 0, read2));
			//
			// }

			Log.info(TAG_LOG, "response message: " + ucon.getResponseMessage());
		}
		// Log.info(TAG_LOG,"response message: "+ucon.getResponseMessage());
		return resp;
	}

	private static void formatCompleteTag(StringBuilder buf, String tagName,
			String tagValue) {
		buf.append("<").append(tagName).append(">").append(tagValue)
				.append("</").append(tagName).append(">").append("\n");
	}

	private static void formatStartTag(StringBuilder buf, String tagName) {
		buf.append("<").append(tagName).append(">").append("\n");
	}

	private static void formatEndTag(StringBuilder buf, String tagName) {
		buf.append("</").append(tagName).append(">").append("\n");
	}
	// private static void XmlFileCreator(List<JokeBean> data){
	// File newxmlfile = new
	// File(Environment.getExternalStorageDirectory()+"/new.xml");
	// try{
	// if(!newxmlfile.exists())
	// newxmlfile.createNewFile();
	// }catch(IOException e){
	// Log.error("IOException", "exception in createNewFile() method");
	// }
	// //we have to bind the new file with a FileOutputStream
	// FileOutputStream fileos = null;
	// try{
	// fileos = new FileOutputStream(newxmlfile);
	// }catch(FileNotFoundException e){
	// Log.error("FileNotFoundException", "can't create FileOutputStream");
	// }
	// //we create a XmlSerializer in order to write xml data
	// XmlSerializer serializer = Xml.newSerializer();
	// try {
	// //we set the FileOutputStream as output for the serializer, using UTF-8
	// encoding
	// serializer.setOutput(fileos, "UTF-8");
	// //Write <?xml declaration with encoding (if encoding not null) and
	// standalone flag (if standalone not null)
	// serializer.startDocument(null, Boolean.valueOf(true));
	// //set indentation option
	// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
	// true);
	// //start a tag called "root"
	// serializer.startTag(null, "jokes");
	// for(JokeBean joke:data){
	// serializer.startTag(null, "joke");
	// //i indent code just to have a view similar to xml-tree
	// serializer.startTag(null, "id");
	// serializer.text(joke.getId());
	// serializer.endTag(null, "id");
	//
	// serializer.startTag(null, "title");
	// serializer.text(joke.getTitle());
	// //set an attribute called "attribute" with a "value" for <child2>
	// //serializer.attribute(null, "attribute", "value");
	// serializer.endTag(null, "title");
	// serializer.startTag(null, "text");
	// //write some text inside <text>
	// serializer.text(joke.getText());
	// serializer.endTag(null, "text");
	//
	// serializer.endTag(null, "joke");
	// }
	// serializer.endTag(null, "jokes");
	// serializer.endDocument();
	// //write xml data into the FileOutputStream
	// serializer.flush();
	// //finally we close the file stream
	// fileos.close();
	// } catch (Exception e) {
	// Log.error("Exception","error occurred while creating xml file");
	// }
	// }

}
