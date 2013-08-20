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
package com.msi.dns53.server.bind;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ConfigurationWriter is responsible to update the configuration file for BIND.
 */
public class ConfigurationWriter {
	
	private File file;
	private FileWriter fstream;
	private BufferedWriter bstream;
	
	/**
	 * ConfigurationWriter constructor: Initializes the internal data structure. 
	 * Developer can use this constructor to write named.conf and zone files.
	 * @param fileName Name of the file including the full path
	 */
	public ConfigurationWriter(String fileName) {		
		file = new File(fileName);
		try {
			fstream = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bstream = new BufferedWriter(fstream);
	}

	/**
	 * write method does the output writing to the specified file
	 * @param content String to be written into the file
	 */
	public void write(String content) {
		try {
			bstream.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * close method must be called after writing to the file in order to create the file
	 */
	public void close(){
		try {
			bstream.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}