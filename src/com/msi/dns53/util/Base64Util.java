package com.msi.dns53.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import com.msi.tough.core.Appctx;

public class Base64Util {
	private final static Logger logger =  Appctx.getLogger(Base64Util.class.getName());
	
	public static String encode(String original){
		logger.debug("Encoding the string: " + original);
		byte[] encoded = Base64.encodeBase64(original.getBytes());
		String result = new String(encoded);
		logger.debug("Encoded String: " + result);
		return result;
	}
}
