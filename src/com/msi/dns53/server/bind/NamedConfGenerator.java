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
