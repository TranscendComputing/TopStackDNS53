package com.msi.dns53.server.query;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.model.DNS53ResourceRecord;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.server.RequestHandler;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.dns53.util.ResourceRecordEntity;
import com.msi.tough.core.Appctx;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;

public class ChangeResourceRecordSets extends AbstractHeaderAction<ChangeResourceRecordSetsResult>{
	private final static Logger logger =  Appctx.getLogger(ChangeResourceRecordSets.class.getName());

	@Override
	public String marshall(
			MarshallStruct<ChangeResourceRecordSetsResult> input,
			HttpServletResponse resp) throws Exception {
		ChangeResourceRecordSetsResult result = input.getMainObject();
		ChangeInfo ci = result.getChangeInfo();
		XMLNode response = new XMLNode(DNS53Constants.CHANGERESOURCERECORDSETSRESPONSE);
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
		if(ci != null){
			DNS53QueryUtil.marshallChangeInfo(ci, response);
		}
		return response.toString();
	}

	public ChangeResourceRecordSetsRequest unmarshall(HttpServletRequest req) throws IOException, SAXException{
		final ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
		SlashObject so = new SlashObject(req.getPathInfo());
		List<String> paths = so.getList();
		if(paths.size() != 4){
			throw DNS53Faults.InternalError();
		}
		String zoneId = paths.get(2);
		request.setHostedZoneId(zoneId);

		final ChangeBatch batch = new ChangeBatch();
		StringBuilder stringBuilder = new StringBuilder(1000);
		Scanner scanner = new Scanner(req.getInputStream());
		while (scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine());
		}
		String body = stringBuilder.toString();
		logger.debug("XML Body Content: " + body);

		// TODO handler below parses basic syntax only; latency, weight, and alias record sets cannot be parsed at the moment
		DefaultHandler handler = new DefaultHandler(){
			private boolean changeResourceRecordSetsRequest = false;
			private boolean changeBatch = false;
			private boolean changes = false;
			private boolean change = false;
			private boolean resourceRecordSet = false;
			private boolean resourceRecords = false;
			private boolean resourceRecord = false;
			private CharArrayWriter contents = new CharArrayWriter();
			private List<Change> cList;
			private Change c;
			ResourceRecordSet rrSet;
			Collection<ResourceRecord> rrs;
			ResourceRecord record;

			public void startElement(String uri, String localName,String nodeName, 
					Attributes attributes) throws SAXException {
				contents.reset();
				if(!uri.equals(DNS53Constants.XMLNS_VALUE)){
					throw DNS53Faults.InvalidInput("The XML you provided did not have the correct namespace.");
				}
				if(nodeName.equals(DNS53Constants.CHANGERESOURCERECORDSETSREQUEST)){
					changeResourceRecordSetsRequest = true;
				}
				if(nodeName.equals(DNS53Constants.CHANGEBATCH)){
					changeBatch = true;
				}
				if(nodeName.equals(DNS53Constants.CHANGES)){
					changes = true;
					cList = new LinkedList<Change>();
				}
				if(nodeName.equals(DNS53Constants.CHANGE)){
					change = true;
					c = new Change();
				}
				if(nodeName.equals(DNS53Constants.RESOURCERECORDSET)){
					resourceRecordSet = true;
					rrSet = new ResourceRecordSet();
				}
				if(nodeName.equals(DNS53Constants.RESOURCERECORDS)){
					resourceRecords = true;
					rrs = new LinkedList<ResourceRecord>();
				}
				if(nodeName.equals(DNS53Constants.RESOURCERECORD)){
					resourceRecord = true;
					record = new ResourceRecord();
				}
			}

			public void endElement(String uri, String localName,
					String nodeName) throws SAXException {
				// closing xml node
				if(changeResourceRecordSetsRequest && localName.equals( DNS53Constants.CREATEHOSTEDZONEREQUEST )){
					changeResourceRecordSetsRequest = false;
				}
				if(changeResourceRecordSetsRequest && changeBatch && localName.equals( DNS53Constants.CHANGEBATCH )){
					changeBatch = false;
					request.setChangeBatch(batch);
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && localName.equals( DNS53Constants.CHANGES )){
					changes = false;
					batch.setChanges(cList);
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && localName.equals( DNS53Constants.CHANGE )){
					change = false;
					cList.add(c);
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change 
						&& localName.equals( DNS53Constants.RESOURCERECORDSET )){
					resourceRecordSet = false;
					c.setResourceRecordSet(rrSet);
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change 
						&& localName.equals( DNS53Constants.RESOURCERECORDS )){
					resourceRecords = false;
					rrSet.setResourceRecords(rrs);
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change
						&& resourceRecords && localName.equals( DNS53Constants.RESOURCERECORD )){
					resourceRecord = false;
					rrs.add(record);
				}

				// pulling xml node value
				if(changeResourceRecordSetsRequest && changeBatch && localName.equals( DNS53Constants.COMMENT )){
					batch.setComment(contents.toString());
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change 
						&& localName.equals( DNS53Constants.ACTION )){
					c.setAction(contents.toString());
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& localName.equals( DNS53Constants.NAME )){
					rrSet.setName(contents.toString());
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& localName.equals( DNS53Constants.TYPE )){
					rrSet.setType(contents.toString());
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& localName.equals( DNS53Constants.SETIDENTIFIER )){
					rrSet.setSetIdentifier(contents.toString());
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& localName.equals( DNS53Constants.TTL )){
					try{
						rrSet.setTTL(Long.valueOf(contents.toString()));	
					}catch(NumberFormatException e){
						logger.debug("Problem parsing TTL. It must be a numeric value!");
						throw DNS53Faults.InvalidArgument("TTL must be a numeric value within range.");
					}
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& localName.equals( DNS53Constants.WEIGHT )){
					try{
						rrSet.setWeight(Long.valueOf(contents.toString()));
					}catch(NumberFormatException e){
						logger.debug("Problem parsing Weight. It must be a numeric value!");
						throw DNS53Faults.InvalidArgument("Weight must be a numeric value within range.");
					}
				}
				if(changeResourceRecordSetsRequest && changeBatch && changes && change && resourceRecordSet 
						&& resourceRecords && resourceRecord && localName.equals( DNS53Constants.VALUE )){
					record.setValue(contents.toString());
				}
			}
			public void characters(char ch[], int start, int length) throws SAXException {
				this.contents.write( ch, start, length );
			}
		};

		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler( handler );
		xr.parse( new InputSource(new StringReader(body)));
		return request;
	}

	@Override
	public ChangeResourceRecordSetsResult process0(Session session,
			HttpServletRequest req, HttpServletResponse resp,
			Map<String, String[]> map) throws Exception {
		ChangeResourceRecordSetsRequest request = unmarshall(req);
		return changeResourceRecordSets(session, request);
	}

	private ChangeResourceRecordSetsResult changeResourceRecordSets(Session s, ChangeResourceRecordSetsRequest request) throws ErrorResponse{
		Date submittedAt = new Date();
		ChangeResourceRecordSetsResult result = new ChangeResourceRecordSetsResult();
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		String tableName = sqlaccess.getTableName(request.getHostedZoneId(), this.getAccountId());
		if(tableName.equals("FAILED")){
			throw DNS53Faults.NoSuchHostedZone(request.getHostedZoneId());
		}
		String zoneName = sqlaccess.getZoneName(request.getHostedZoneId());
		//LinkedList<RRRequest> rrrs = request.getRRRequests(); 
		List<Change> changes = request.getChangeBatch().getChanges();
		//check if the ChangeBatch is valid
		for(Change tmp : changes){
			String name = tmp.getResourceRecordSet().getName();
			String nameDot = name;
			if(name.charAt(name.length() - 1) != '.'){
				nameDot += ".";
			}else{
				name = name.substring(0, name.length() - 1);
			}
			long weight = -1;
			if(tmp.getResourceRecordSet().getWeight() != null){
				weight = tmp.getResourceRecordSet().getWeight();
				if(weight < 0 || weight > 225){
					throw DNS53Faults.InvalidArgument("Weight of a resource record must be between 0 and 255.");
				}				
			}
			if(tmp.getResourceRecordSet().getType() == null){
				throw DNS53Faults.MissingRequiredParameter("Type parameter is required for each resource record set change request.");
			}
			if(tmp.getResourceRecordSet().getTTL() == null){
				throw DNS53Faults.MissingRequiredParameter("TTL parameter is required for each resource record set change request " +
						"except for the change requests to modify alias resource record sets.");
			}
			if(tmp.getAction().equals("CREATE")){
				for(ResourceRecord rr : tmp.getResourceRecordSet().getResourceRecords()){
					boolean recordExists = sqlaccess.recordExists(s, request.getHostedZoneId(), nameDot, 
							tmp.getResourceRecordSet().getType(), rr.getValue());
					DNS53ResourceRecord newRecord = new DNS53ResourceRecord();
					newRecord.setZoneId(request.getHostedZoneId());
					newRecord.setZoneName(zoneName);
					newRecord.setName(nameDot);
					newRecord.setTtl(tmp.getResourceRecordSet().getTTL());
					newRecord.setRdata(rr.getValue());
					if(tmp.getResourceRecordSet().getWeight() != null){
						logger.debug("Request to create a weighted resource record set: " + tmp.getResourceRecordSet().toString());
						if(recordExists){
							if(sqlaccess.recordExists(s, request.getHostedZoneId(), nameDot,
									tmp.getResourceRecordSet().getType(), rr.getValue())){
								throw DNS53Faults.InvalidChangeBatch(
										"Tried to create a weighted resource record set, " + nameDot 
										+ " " + tmp.getResourceRecordSet().getType() + ", but it already exists.");
							}
						}
						if(tmp.getResourceRecordSet().getSetIdentifier() == null){
							throw DNS53Faults.MissingRequiredParameter("SetIdentifier is required for weighted resource" +
									"record set.");
						}
						newRecord.setSid(tmp.getResourceRecordSet().getSetIdentifier());
						if(!(tmp.getResourceRecordSet().getType().equals("A") || tmp.getResourceRecordSet().getType().equals("AAAA")
								|| tmp.getResourceRecordSet().getType().equals("CNAME")
								|| tmp.getResourceRecordSet().getType().equals("TXT"))){
							throw DNS53Faults.InvalidArgument("Weighted resource record set must be A, AAAA, CNAME, or TXT type.");
						}
						newRecord.setRdtype(tmp.getResourceRecordSet().getType());
						newRecord.setWeight(weight);
						for(int i = 0; i < weight; ++i){
							sqlaccess.addResourceRecord(request.getHostedZoneId(), name,
									tmp.getResourceRecordSet().getTTL(), tmp.getResourceRecordSet().getType(),
									rr.getValue(), this.getAccountId());
						}
					}
					else{
						if(recordExists){
							throw DNS53Faults.InvalidChangeBatch(
									"Tried to create resource record set, " + name 
									+ " " + tmp.getResourceRecordSet().getType() + ", but it already exists.");	
						}
						if(!DNS53Constants.RESOURCERECORDSETS_TYPES.contains(tmp.getResourceRecordSet().getType())){
							throw DNS53Faults.InvalidArgument("Resource record type is not recognized.");
						}
						newRecord.setRdtype(tmp.getResourceRecordSet().getType());
						sqlaccess.addResourceRecord(request.getHostedZoneId(), name,
								tmp.getResourceRecordSet().getTTL(), tmp.getResourceRecordSet().getType(),
								rr.getValue(), this.getAccountId());
					}
					s.save(newRecord);
				}
			}
			else if(tmp.getAction().equals("DELETE")){
				for(ResourceRecord rr : tmp.getResourceRecordSet().getResourceRecords()){
					boolean recordExists = sqlaccess.recordExists(s, request.getHostedZoneId(), nameDot, 
							tmp.getResourceRecordSet().getType(), rr.getValue());
					if(!recordExists){
						throw DNS53Faults.InvalidChangeBatch(
								"Tried to delete resource record set " + name
								+ " " + tmp.getResourceRecordSet().getType() + ", but it was not found.");
					}
					List<DNS53ResourceRecord> recordList = ResourceRecordEntity.selectResourceRecords(s, request.getHostedZoneId(),
							nameDot, rr.getValue(), tmp.getResourceRecordSet().getType(), 
							tmp.getResourceRecordSet().getSetIdentifier(), tmp.getResourceRecordSet().getTTL(),
							weight);
					
					if(recordList == null || recordList.size() == 0){
						throw DNS53Faults.InvalidChangeBatch(
								"Tried to delete resource record set " + name 
								+ " " + tmp.getResourceRecordSet().getType() + ", but it was not found with " +
										"weight = " + tmp.getResourceRecordSet().getWeight() + " and " 
								+ "SetIdentifier = " + tmp.getResourceRecordSet().getSetIdentifier() + ".");
					}
					if(recordList.size() > 1){
						logger.debug("Too many results being returned for DELETE change request. Either duplicate entries exist, NS records are selected for deletion, or other issue. Throwing InternalError!");
						throw DNS53Faults.InternalError();
					}
					DNS53ResourceRecord currentRecord = recordList.get(0);
					
					if(tmp.getResourceRecordSet().getWeight() != null){
						if(tmp.getResourceRecordSet().getSetIdentifier() == null){
							throw DNS53Faults.MissingRequiredParameter("SetIdentifier is required for weighted resource" +
									"record set.");
						}
						List<DNS53ResourceRecord> existing = sqlaccess.listResourceRecords(tableName, tmp.getResourceRecordSet().getType(), 
								name, rr.getValue());
						logger.debug("Existing weight of the record resource set is " + existing.size() + ". DELETE is requested" +
								" with weight, " + tmp.getResourceRecordSet().getWeight());
						if(tmp.getResourceRecordSet().getWeight() != Integer.valueOf(existing.size()).longValue() ||
								tmp.getResourceRecordSet().getWeight() != currentRecord.getWeight()){
							throw DNS53Faults.InvalidArgument("Weighted resource record set was found with matching name and value(s). " +
									"However, the value of weight is different. Existing resource record set has the weight of " 
									+ existing.size() + ".");
						}
					}else{
						if(tmp.getResourceRecordSet().getSetIdentifier() != null){
							throw DNS53Faults.MissingRequiredParameter("SetIdentifier was received, however, weight was not " +
									"passed along. Both of these parameters are required for change request against weighted " +
									"resource record sets.");
						}
						if(currentRecord.getWeight() != -1){
							throw DNS53Faults.InvalidArgument("Weighted resource record set was found with matching name and value(s). " +
									"However, the request did not pass weight parameter.");
						}
					}
					s.delete(currentRecord);
					sqlaccess.deleteResourceRecord(request.getHostedZoneId(), name,
							tmp.getResourceRecordSet().getTTL(), tmp.getResourceRecordSet().getType(),
							rr.getValue(), this.getAccountId());
				}
			}
		}
		String changeID = RequestHandler.writeChange(sqlaccess, "INSYNC", submittedAt.toString(), "RRSET", "RRSET");
		ChangeInfo ci = new ChangeInfo();
		ci.setId(changeID);
		ci.setStatus("INSYNC");
		ci.setSubmittedAt(submittedAt);
		result.setChangeInfo(ci);

		return result;
	}
}
