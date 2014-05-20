package cn.eben.android.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.funambol.util.Base64;



public class ContentConvertUtil {


	
	 /**
		 * Convert byte[] string to 
		 * 
		 * @param src
		 * @return
		 */
	public static String bytesTo16HexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return "";
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public byte[] hexToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		if (src == null || "".equals(src)|| 0 == src.length) {
			return "";
		}
		return new String(Base64.encode(gZip(src)));

//		return new String(httpCodec.upEnc(src));
		
//		//Log.d("TestJni", "HttpCodec enc len:" + src.length + ", src:" + src);
//		byte[] data = httpCodec.upEnc(src);
//		if(data != null) {
//			//Log.d("TestJni", "enc data len:" + data.length);
//			return new String(data);
//		} else
//			return "";
	}

	/**

	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[]  hexStringToBytes(String hexString) {
		if (hexString == null || "".equals(hexString) || hexString.length() == 0) {
			return new byte[0];
		}
		return unGZip(Base64.decode(hexString));

//		return httpCodec.downDec(hexString.getBytes());
//		//Log.d("TestJni", "HttpCodec dec len:" + src.length + ", src:" + new String(src));
//		byte[] data = httpCodec.downDec(hexString.getBytes());
//		if(data != null) {
//			//Log.d("TestJni", "dec data len:" + data.length);
//			return data;
//		} else
//			return new byte[0];
	}

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/***************************************************************************
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] gZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***************************************************************************


	 * @param data
	 * @return
	 */
	public static byte[] unGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***************************************************************************

	 * @param data
	 * @return
	 */
	public static byte[] zip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(bos);
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(data.length);
			zip.putNextEntry(entry);
			zip.write(data);
			zip.closeEntry();
			zip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***************************************************************************


	 * @param data
	 * @return
	 */
	public static byte[] unZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ZipInputStream zip = new ZipInputStream(bis);
			while (zip.getNextEntry() != null) {
				byte[] buf = new byte[1024];
				int num = -1;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((num = zip.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, num);
				}
				b = baos.toByteArray();
				baos.flush();
				baos.close();
			}
			zip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
	
    public static String replaceAll(String s, String src, String tgt) {
        StringBuffer sb = new StringBuffer();
        src = '"' + src + '"';
        int pos = s.indexOf(src);

        int start = 0;
        while(pos != -1) {

            String portion = s.substring(start, pos);
            sb.append(portion);
            sb.append(tgt);

            if (pos + src.length() < s.length()) {
                // Get ready for another round
                start = pos + src.length();
                pos = s.indexOf(src, pos + src.length());
            } else {
                pos = -1;
                start = s.length();
            }
        }

        // Append the last chunk
        if (start < s.length()) {
            sb.append(s.substring(start));
        }

        return sb.toString();
    }

}
