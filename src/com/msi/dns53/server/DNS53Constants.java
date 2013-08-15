package com.msi.dns53.server;

import java.util.Arrays;
import java.util.Collection;

public final class DNS53Constants {
	public static final String ACTION = "Action";
	public static final String CALLERREFERENCE = "CallerReference";
	public static final String CHANGE = "Change";
	public static final String CHANGEBATCH = "ChangeBatch";
	public static final String CHANGEINFO = "ChangeInfo";
	public static final String CHANGERESOURCERECORDSETS = "ChangeResourceRecordSets";
	public static final String CHANGERESOURCERECORDSETSRESPONSE = "ChangeResourceRecordSetsResponse";
	public static final String CHANGERESOURCERECORDSETSREQUEST = "ChangeResourceRecordSetsRequest";
	public static final String CHANGES = "Changes";
	public static final String COMMENT = "Comment";
	public static final String CONFIG = "Config";
	public static final String CREATEHOSTEDZONE = "CreateHostedZone";
	public static final String CREATEHOSTEDZONERESPONSE = "CreateHostedZoneResponse";
	public static final String CREATEHOSTEDZONEREQUEST = "CreateHostedZoneRequest";
	public static final String DELEGATIONSET = "DelegationSet";
	public static final String DELETEHOSTEDZONE = "DeleteHostedZone";
	public static final String DELETEHOSTEDZONERESPONSE = "DeleteHostedZoneResponse";
	public static final String DNS53VERSION = "2012-02-29";
	public static final String GETCHANGE = "GetChange";
	public static final String GETCHANGERESPONSE = "GetChangeResponse";
	public static final String GETHOSTEDZONE = "GetHostedZone";
	public static final String GETHOSTEDZONERESPONSE = "GetHostedZoneResponse";
	public static final String HOSTEDZONE = "HostedZone";
	public static final String HOSTEDZONES = "HostedZones";
	public static final String HOSTEDZONECONFIG = "HostedZoneConfig";
	public static final String ID = "Id";
	public static final String ISTRUNCATED = "IsTruncated";
	public static final String LOCATION_VALUE = "United States";
	public static final String LISTHOSTEDZONES = "ListHostedZones";
	public static final String LISTHOSTEDZONESRESPONSE = "ListHostedZonesResponse";
	public static final String LISTRESOURCERECORDSETS = "ListResourceRecordSets";
	public static final String LISTRESOURCERECORDSETSRESPONSE = "ListResourceRecordSetsResponse";
	public static final String MARKER = "Marker";
	public static final String MAXITEMS = "MaxItems";
	public static final String NAME = "Name";
	public static final String NAMESERVER = "NameServer";
	public static final String NAMESERVERS = "NameServers";
	public static final String NEXTMARKER = "NextMarker";
	public static final String NEXTRECORDIDENTIFIER = "NextRecordIdentifier";
	public static final String NEXTRECORDNAME = "NextRecordName";
	public static final String NEXTRECORDTYPE = "NextRecordType";
	public static final String PENDING = "PENDING";
	public static final String RESOURCERECORD = "ResourceRecord";
	public static final String RESOURCERECORDS = "ResourceRecords";
	public static final String RESOURCERECORDSET = "ResourceRecordSet";
	public static final String RESOURCERECORDSETS = "ResourceRecordSets";
	public static final String RESOURCERECORDSETCOUNT = "ResourceRecordSetCount";
	public static final String RRSET = "rrset";
	public static final String SETIDENTIFIER = "SetIdentifier";
	public static final String STATUS = "Status";
	public static final String SUBMITTEDAT = "SubmittedAt";
	public static final String TTL = "TTL";
	public static final String TYPE = "Type";
	public static final String VALUE = "Value";
	public static final String WEIGHT = "Weight";
	public static final String XMLNS = "xmlns";
	public static final String XMLNS_VALUE = "https://route53.amazonaws.com/doc/2012-02-29/";
	public static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		
	public static final Collection<String> RESOURCERECORDSETS_TYPES = 
			Arrays.asList(new String[] { "A", "AAAA", "CNAME", "MX", "NS", "PTR", "SOA", "SPF", "SRV", "TXT" });
	
	
}
