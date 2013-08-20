/*
 * TopStack (c) Copyright 2012-2013 Transcend Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
