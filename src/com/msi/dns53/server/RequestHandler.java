package com.msi.dns53.server;

import java.util.UUID;

import com.msi.tough.query.ErrorResponse;

/**
 * @author Daniel Kim (dkim@momentumsi.com)
 *
 *	RequestHandler is a Java class that handles update requests. All the requests are reproduced into an object-oriented data structure.
 *	All of its functions are static.
 */
public class RequestHandler {

	public static void checkType(String type) throws ErrorResponse {
		boolean error = true;
		if(type == null){
			error = false; 
		}
		else{
			error = !DNS53Constants.RESOURCERECORDSETS_TYPES.contains(type);
		}

		if(error){
			throw DNS53Faults.InvalidArgument("ResourceRecordSet type, " + type + ", is not a valid. Valid ResourceRecordSet type " +
					"values are A, AAAA, CNAME, MX, NS, PTR, SOA, SPF, SRV, and TXT.");
		}
	}

	public static String writeChange(AccessMySQL sqlaccess, String status, String submitTime, String tableName, String request)
			throws ErrorResponse {
		String changeID = "C" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
		sqlaccess.addChangeRecord(changeID, status, submitTime, tableName, request);
		return changeID;
	}

}
