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

public class NamedConfGenerator {
	private static String default_ = 
			"options {\n" +
			"	directory \"/var/named\";\n" +
			"	version \"aegjiae320adf2q30asdfjq12q30gfe\";\n" +
			"};\n" +
			"logging {\n" +
			"	channel named_log{\n" +
			"		file \"/var/log/named.log\" versions 3 size 2m;\n" +
			"		severity info;\n" +
			"		print-severity yes;\n" +
			"		print-time yes;\n" +
			"		print-category yes;\n" +
			"	};\n" +
			"	category default {\n" +
			"		named_log;\n" +
			"	};\n" +
			"};\n";
	
	public static String getDefaultNamedConf(){
		return default_;
	}
	
	public static String generateZoneConf(String domainName, String tableName, String hostName, String userName, String password){
		String result = 
				"zone \"" + domainName + "\" {\n" +
				"	type master;\n" +
				"	notify yes;\n" +
				"	database \"mysqldb bind " + tableName + " " + hostName + " " + userName + " " + password + "\";\n" +
				"};\n";
		return result;
	}
}
