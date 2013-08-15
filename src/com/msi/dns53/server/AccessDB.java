package com.msi.dns53.server;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.msi.dns53.model.DNS53HostedZone;
import com.msi.dns53.model.DNS53ResourceRecord;
import com.msi.tough.query.ErrorResponse;

public interface AccessDB {
	
	public void loadDriver() throws ErrorResponse;
	
	public void closeConnection() throws ErrorResponse;
	
	public List<String> createHostedZone(Session session, String domainName, String callerRef, String comment, long acid) throws ErrorResponse;
	
	public String[] getHostedZone(String zoneID) throws ErrorResponse;
	
	public void deleteHostedZone(String tableName, String zoneName, String callerRef) throws ErrorResponse;
	
	public ListHostedZonesResult listHostedZones(String marker_tableName, int maxItems, long accId) throws ErrorResponse;
	
	public boolean recordExists(Session session, String tableName, String name, String rdtype, String value) throws ErrorResponse;
	
	public void addResourceRecord(String zoneID, String name, long ttl, String rdtype, String rdata, long accId) throws ErrorResponse;
	
	public void addRecord(String tableName, String name, long ttl, String rdtype, String rdata) throws ErrorResponse;
	
	public void deleteResourceRecord(String zoneID, String name, long ttl, String rdtype, String rdata, long accId) throws ErrorResponse;
	
	public void deleteRecord(String tableName, String name, long ttl, String rdtype, String rdata) throws ErrorResponse;
	
	public List<DNS53ResourceRecord> listResourceRecords(Session sess, String zoneId, String tableName, String value,
			String type, String sid, long ttl, long weight) throws ErrorResponse;

	public String getTableName(String zoneID, long accId) throws ErrorResponse;
	
	public String getZoneName(String zoneId) throws ErrorResponse;
	
	public List<DNS53HostedZone> getAllHostedZones();
	
	public String[] getChange(String changeID) throws ErrorResponse;

	public void updateChanges() throws SQLException;
	
	public boolean pendingChangesExist() throws SQLException;
	
	public void addChangeRecord(String changeID, String status, String submitTime, String tableName, String request) throws ErrorResponse;

	public String getZoneId(String zoneName) throws ErrorResponse;

}
