package com.funambol.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.funambol.util.Base64;
import com.funambol.util.MD5;

/**
 * 
 * md5
 * 
 * @since 2009-8-7 14:46:27
 */

public class MD5Util {

    // private final static Log log = LogFactory.getLog(MD5Util.class);

    static MessageDigest md = null;

    static {

	try {

	    md = MessageDigest.getInstance("MD5");

	} catch (NoSuchAlgorithmException ne) {

	    // log.error("NoSuchAlgorithmException: md5", ne);
	    ne.printStackTrace();

	}

    }

    /**
     * 
     * 
     * 
     * @param f
     *   
     * 
     * @return 
     */
    public static final String toHex(byte data[]) {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < data.length; i++) {
	    sb.append("0123456789ABCDEF".charAt(0xf & data[i] >> 4)).append(
		    "0123456789ABCDEF".charAt(0xf & data[i]));
	}
	return sb.toString();
    }

    public static String md5(File f) {

	FileInputStream fis = null;

	try {

	    fis = new FileInputStream(f);

	    byte[] buffer = new byte[8192];

	    int length;

	    while ((length = fis.read(buffer)) != -1) {

		md.update(buffer, 0, length);

	    }

	    return new String(toHex(md.digest()));

	} catch (FileNotFoundException e) {

	    // log.error("md5 file " + f.getAbsolutePath() + " failed:"
	    // + e.getMessage());
	    e.printStackTrace();

	    return null;

	} catch (IOException e) {

	    // log.error("md5 file " + f.getAbsolutePath() + " failed:"
	    // + e.getMessage());
	    e.printStackTrace();

	    return null;

	} finally {

	    try {

		if (fis != null)
		    fis.close();

	    } catch (IOException e) {

		e.printStackTrace();

	    }

	}

    }

    /**
     * 
     *  
     * 
     * @param target
     *            
     * 
     * @return md5 value
     */

    // public static String md5(String target) {
    //
    // return DigestUtils.md5Hex(target);
    //
    // }
    public static String md5(String target) {
	byte[] input = target.getBytes();

	md.update(input, 0, input.length);

	return new String(toHex(md.digest()));

    }

    public static String md5(byte[] input) {

	md.update(input, 0, input.length);

	return new String(toHex(md.digest()));

    }
    
 
}
