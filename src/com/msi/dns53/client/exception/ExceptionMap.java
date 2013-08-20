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
package com.msi.dns53.client.exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ExceptionMap {
	
	private static ExceptionMap instance;
	private static String PROPFILE = "exception_map.properties";
	
	private Map<String, String> map;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ExceptionMap(String fileLocation) throws NullPointerException{
		Properties props = new Properties();
		try {
			props.load(this.getClass().getResourceAsStream(fileLocation));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("ExceptionMap log: props.size() = " + props.size());
		this.map = new HashMap<String, String>((Map) props);
	}
	
	public static ExceptionMap getExceptionMap() throws NullPointerException{
		if(instance == null){
			instance = new ExceptionMap(PROPFILE);
		}
		return instance;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
}
