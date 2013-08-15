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