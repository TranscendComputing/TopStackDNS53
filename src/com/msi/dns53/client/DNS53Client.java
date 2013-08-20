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
package com.msi.dns53.client;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.DelegationSet;
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest;
import com.amazonaws.services.route53.model.DeleteHostedZoneResult;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.GetChangeResult;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.GetHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.msi.dns53.client.exception.ExceptionMap;
import com.msi.dns53.client.model.ChangeResourceRecordSetsResponsePOJO;
import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO;
import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.DelegationSetPOJO;
import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.HostedZonePOJO;
import com.msi.dns53.client.model.DeleteHostedZoneResponsePOJO;
import com.msi.dns53.client.model.ErrorResponsePOJO;
import com.msi.dns53.client.model.GetChangeResponsePOJO;
import com.msi.dns53.client.model.GetHostedZoneResponsePOJO;
import com.msi.dns53.client.model.ListHostedZonesResponsePOJO;
import com.msi.dns53.client.model.ListResourceRecordSetsResponsePOJO;
import com.msi.dns53.client.model.ListResourceRecordSetsResponsePOJO.ResourceRecordPOJO;
import com.msi.dns53.client.model.ListResourceRecordSetsResponsePOJO.ResourceRecordSetPOJO;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DNS53Client {
	private String serverURL;
	private String changeURL;
	private String accessKey;
	private String secretKey;	// not used now, but later this may be used to calculate the Signature, etc.

	public DNS53Client(String serverUrl, String changeUrl, String accessKey, String secretKey) {
		this.serverURL = serverUrl;
		this.changeURL = changeUrl;
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@SuppressWarnings("unchecked")
	public void exceptionMapper(ClientResponse response, String resultXml) throws AmazonServiceException{
		ErrorResponsePOJO er = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(ErrorResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			er = (ErrorResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new AmazonClientException("There was a problem parsing the error response xml with JAXB.", e);
		}
		if(er == null || er.getError() == null || er.getRequestId() == null || er.getError().getCode() == null ||
				er.getError().getMessage() == null || er.getError().getType() == null){
			throw new AmazonClientException("Error response xml did not contain expected elements although it is well formed.");
		}

		String errCode = er.getError().getCode();
		Class<AmazonServiceException> clazz = null;
		Constructor<AmazonServiceException> c = null;
		AmazonServiceException exception = null;
		try {
			String clazzName = ExceptionMap.getExceptionMap().getMap().get(errCode);
			clazz = (Class<AmazonServiceException>) Class.forName(clazzName);
			c = (Constructor<AmazonServiceException>) clazz.getConstructor(String.class);
			exception = (AmazonServiceException) c.newInstance(new Object[]{ er.getError().getMessage() });
		} catch (NullPointerException e) {
			exception = new AmazonServiceException(er.getError().getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AmazonClientException("Client could not determine the type of the error response.");
		}
		if(exception == null){
			throw new AmazonClientException("Client encountered a problem while it was mapping the error response.");
		}
		exception.setErrorCode(er.getError().getCode());
		ErrorType et = ErrorType.Unknown;
		if("Sender".equals(er.getError().getType())){
			et = ErrorType.Service;
		}
		exception.setErrorType(et);
		exception.setRequestId(er.getRequestId());
		exception.setStatusCode(response.getStatus());
		exception.setServiceName("DNS53");
		throw exception;
	}
	
	public CreateHostedZoneResult createHostedZone(CreateHostedZoneRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);

		String entity = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<CreateHostedZoneRequest xmlns=\"https://route53.amazonaws.com/doc/2012-02-29/\">" +
				"<Name>" + req.getName() + "</Name>" +
				"<CallerReference>" + req.getCallerReference() + "</CallerReference>";
		if(req.getHostedZoneConfig() != null && req.getHostedZoneConfig().getComment() != null){
			entity += "<HostedZoneConfig>" +
					"<Comment>" + req.getHostedZoneConfig().getComment() + "</Comment>" +
					"</HostedZoneConfig>";
		}
		entity += "</CreateHostedZoneRequest>";

		ClientResponse response = r
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.type(MediaType.APPLICATION_XML_TYPE)
						.accept(MediaType.TEXT_XML)
						.entity(entity)
						.post(ClientResponse.class);

		String resultXml = response.getEntity(String.class);
		if(response.getStatus() > 299 || response.getStatus() < 200){
			exceptionMapper(response, resultXml);
		}
		
		CreateHostedZoneResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(CreateHostedZoneResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (CreateHostedZoneResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		CreateHostedZoneResult result = new CreateHostedZoneResult();
		if(interResult.getHostedZone() != null){
			HostedZone hostedZone = new HostedZone();
			hostedZone.setId(interResult.getHostedZone().getId());
			hostedZone.setName(interResult.getHostedZone().getName());
			hostedZone.setCallerReference(interResult.getHostedZone().getCallerReference());
			if(interResult.getHostedZone().getConfig() != null){
				HostedZoneConfig config = new HostedZoneConfig();
				config.setComment(interResult.getHostedZone().getConfig().getComment());
				hostedZone.setConfig(config);	
			}
			result.setHostedZone(hostedZone);	
		}
		if(interResult.getChangeInfo() != null){
			ChangeInfo changeInfo = new ChangeInfo();
			changeInfo.setId(interResult.getChangeInfo().getId());
			changeInfo.setStatus(interResult.getChangeInfo().getStatus());
			changeInfo.setSubmittedAt(interResult.getChangeInfo().getSubmittedAt());
			changeInfo.setComment(interResult.getChangeInfo().getComment());
			result.setChangeInfo(changeInfo);
		}
		if(interResult.getDelegationSet() != null){
			DelegationSet dSet = new DelegationSet();
			dSet.setNameServers(interResult.getDelegationSet().getNameServers());
			result.setDelegationSet(dSet);
		}
		return result;
	}

	public GetHostedZoneResult getHostedZone(GetHostedZoneRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);
		ClientResponse response = r
				.path(req.getId())
				.type(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.TEXT_XML)
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.get(ClientResponse.class);

		String resultXml = response.getEntity(String.class);
		if(response.getStatus() != 200){
			exceptionMapper(response, resultXml);
		}
		
		
		GetHostedZoneResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(GetHostedZoneResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (GetHostedZoneResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		GetHostedZoneResult result = new GetHostedZoneResult();
		if(interResult.getHostedZone() != null){
			HostedZone hz = new HostedZone();
			hz.setCallerReference(interResult.getHostedZone().getCallerReference());
			hz.setId(interResult.getHostedZone().getId());
			hz.setName(interResult.getHostedZone().getName());
			hz.setResourceRecordSetCount(interResult.getHostedZone().getResourceRecordSetCount());
			if(interResult.getHostedZone().getConfig() != null){
				HostedZoneConfig config = new HostedZoneConfig();
				config.setComment(interResult.getHostedZone().getConfig().getComment());
				hz.setConfig(config);
			}
			result.setHostedZone(hz);
		}
		if(interResult.getDelegationSet() != null){
			DelegationSetPOJO ds = interResult.getDelegationSet();
			DelegationSet ds_ = new DelegationSet();
			if(ds.getNameServers() != null){
				ds_.setNameServers(ds.getNameServers());				
			}
			result.setDelegationSet(ds_);
		}
		return result;
	}

	public DeleteHostedZoneResult deleteHostedZone(DeleteHostedZoneRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);
		ClientResponse response = r
				.path(req.getId())
				.type(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.TEXT_XML)
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.delete(ClientResponse.class);

		String resultXml = response.getEntity(String.class);
		if(response.getStatus() != 200){
			exceptionMapper(response, resultXml);
		}
		
		DeleteHostedZoneResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(DeleteHostedZoneResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (DeleteHostedZoneResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		DeleteHostedZoneResult result = new DeleteHostedZoneResult();
		if(interResult.getChangeInfo() != null){
			ChangeInfo ci = new ChangeInfo();
			if(interResult.getChangeInfo().getId() != null){
				ci.setId(interResult.getChangeInfo().getId());
			}
			if(interResult.getChangeInfo().getStatus() != null){
				ci.setStatus(interResult.getChangeInfo().getStatus());
			}
			if(interResult.getChangeInfo().getSubmittedAt() != null){
				ci.setSubmittedAt(interResult.getChangeInfo().getSubmittedAt());
			}
			if(interResult.getChangeInfo().getComment() != null){
				ci.setComment(interResult.getChangeInfo().getComment());
			}
			result.setChangeInfo(ci);
		}
		return result;
	}

	public ListHostedZonesResult listHostedZones(ListHostedZonesRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);
		MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
		if(req.getMarker() != null){
			paramMap.add("marker", req.getMarker());
		}
		if(req.getMaxItems() != null){
			paramMap.add("maxitems", req.getMaxItems());	
		}
				
		ClientResponse response = r
				.queryParams(paramMap)
				.type(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.TEXT_XML)
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.get(ClientResponse.class);

		String resultXml = response.getEntity(String.class);
		if(response.getStatus() != 200){
			exceptionMapper(response, resultXml);
		}
		
		ListHostedZonesResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(ListHostedZonesResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (ListHostedZonesResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		ListHostedZonesResult result = new ListHostedZonesResult();
		if(interResult.getHostedZones() != null){
			Collection<HostedZone> hzs = new LinkedList<HostedZone>();
			for(HostedZonePOJO hz : interResult.getHostedZones()){
				HostedZone temp = new HostedZone();
				temp.setCallerReference(hz.getCallerReference());
				temp.setId(hz.getId());
				temp.setName(hz.getName());
				temp.setResourceRecordSetCount(hz.getResourceRecordSetCount());
				if(hz.getConfig() != null){
					HostedZoneConfig config = new HostedZoneConfig();
					if(hz.getConfig().getComment() != null){
						config.setComment(hz.getConfig().getComment());
					}
					temp.setConfig(config);	
				}
				hzs.add(temp);
			}
			result.setHostedZones(hzs);
		}
		if(interResult.getMarker() != null){
			result.setMarker(interResult.getMarker());
		}
		if(interResult.getMaxItems() != null){
			result.setMaxItems(interResult.getMaxItems());
		}
		if(interResult.getNextMarker() != null){
			result.setNextMarker(interResult.getNextMarker());
		}
		return result;
	}

	public ChangeResourceRecordSetsResult changeResourceRecordSets(ChangeResourceRecordSetsRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);		

		String entity = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ChangeResourceRecordSetsRequest xmlns=\"https://route53.amazonaws.com/doc/2012-02-29/\">";
		if(req.getChangeBatch() != null){
			entity += "<ChangeBatch>";
			if(req.getChangeBatch().getComment() != null){
				entity += "<Comment>" + req.getChangeBatch().getComment() + "</Comment>";
			}
			List<Change> changes = req.getChangeBatch().getChanges();
			if(changes != null && changes.size() > 0){
				entity += "<Changes>";
				for(Change change : changes){
					entity += "<Change>";
					entity += "<Action>" + change.getAction() + "</Action>";
					if(change.getResourceRecordSet() != null){
						entity += "<ResourceRecordSet>";
						entity += "<Name>" + change.getResourceRecordSet().getName() + "</Name>";
						entity += "<Type>" + change.getResourceRecordSet().getType() + "</Type>";
						entity += "<TTL>" + change.getResourceRecordSet().getTTL()+ "</TTL>";
						List<ResourceRecord> records = change.getResourceRecordSet().getResourceRecords();
						if(records != null && records.size() > 0){
							entity += "<ResourceRecords>";
							for(ResourceRecord record : records){
								entity += "<ResourceRecord><Value>" + record.getValue() + "</Value></ResourceRecord>";
							}
							entity += "</ResourceRecords>";
						}
						entity += "</ResourceRecordSet>";
					}					
					entity += "</Change>";
				}
				entity += "</Changes>";
			}
			entity += "</ChangeBatch>";
		}				
		entity += "</ChangeResourceRecordSetsRequest>";

		ClientResponse response = r
				.path(req.getHostedZoneId() + "/rrset")
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.type(MediaType.APPLICATION_XML_TYPE)
						.accept(MediaType.TEXT_XML)
						.entity(entity)
						.post(ClientResponse.class);


		String resultXml = response.getEntity(String.class);
		if(response.getStatus() != 200){
			exceptionMapper(response, resultXml);
		}
		
		ChangeResourceRecordSetsResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(ChangeResourceRecordSetsResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (ChangeResourceRecordSetsResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		ChangeResourceRecordSetsResult result = new ChangeResourceRecordSetsResult();
		if(interResult.getChangeInfo() != null){
			ChangeInfo ci = new ChangeInfo();
			ci.setComment(interResult.getChangeInfo().getComment());
			ci.setId(interResult.getChangeInfo().getId());
			ci.setStatus(interResult.getChangeInfo().getStatus());
			ci.setSubmittedAt(interResult.getChangeInfo().getSubmittedAt());
			result.setChangeInfo(ci);
		}

		return result;
	}

	public ListResourceRecordSetsResult listResourceRecordSets(ListResourceRecordSetsRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.serverURL);
		MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
		if(req.getStartRecordName() != null){
			paramMap.add("name", req.getStartRecordName());
		}
		if(req.getStartRecordType() != null){
			paramMap.add("type", req.getStartRecordType());
		}
		if(req.getStartRecordIdentifier() != null){
			paramMap.add("identifier", req.getStartRecordIdentifier());
		}
		if(req.getMaxItems() != null){
			paramMap.add("maxitems", req.getMaxItems());	
		}
		
		ClientResponse response = r
				.path(req.getHostedZoneId() + "/rrset")
				.queryParams(paramMap)
				.type(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.TEXT_XML)
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.get(ClientResponse.class);

		String resultXml = response.getEntity(String.class);
		if(response.getStatus() != 200){
			exceptionMapper(response, resultXml);
		}
		
		
		
		
		ListResourceRecordSetsResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(ListResourceRecordSetsResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (ListResourceRecordSetsResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		ListResourceRecordSetsResult result = new ListResourceRecordSetsResult();
		result.setMaxItems(interResult.getMaxItems());
		result.setIsTruncated(interResult.isTruncated());
		if(interResult.getResourceRecordSets() != null){
			Collection<ResourceRecordSet> rrSets = new LinkedList<ResourceRecordSet>();
			for(ResourceRecordSetPOJO p : interResult.getResourceRecordSets()){
				ResourceRecordSet temp = new ResourceRecordSet();
				temp.setName(p.getName());
				temp.setSetIdentifier(p.getSetIdentifier());
				temp.setTTL(p.getTTL());
				temp.setType(p.getType());
				temp.setWeight(p.getWeight());
				if(p.getResourceRecords() != null){
					Collection<ResourceRecord> resourceRecords = new LinkedList<ResourceRecord>();
					for(ResourceRecordPOJO record : p.getResourceRecords()){
						ResourceRecord newRec = new ResourceRecord();
						newRec.setValue(record.getValue());
						resourceRecords.add(newRec);
					}
					temp.setResourceRecords(resourceRecords);
				}
				rrSets.add(temp);
			}
			result.setResourceRecordSets(rrSets);
		}

		return result;
	}

	public GetChangeResult getChange(GetChangeRequest req) throws AmazonServiceException, AmazonClientException{
		Client c = Client.create();
		WebResource r = c.resource(this.changeURL);
		String resultXml = r
				.path(req.getId())
				.type(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.TEXT_XML)
				.header("X-Amzn-Authorization",
						"AWS3 AWSAccessKeyId=" + this.accessKey + "," +
								"Algorithm=HmacSHA256," +
								"SignedHeaders=Host;X-Amz-Date," +
						"Signature=THISISANEXAMPLESIGNATURE=")
						.get(String.class);

		GetChangeResponsePOJO interResult = null;
		try {
			StringReader reader = new StringReader(resultXml);
			JAXBContext context = JAXBContext.newInstance(GetChangeResponsePOJO.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			interResult = (GetChangeResponsePOJO) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
		if(interResult == null){
			return null;
		}

		GetChangeResult result = new GetChangeResult();
		if(interResult.getChangeInfo() != null){
			ChangeInfo ci = new ChangeInfo();
			ci.setComment(interResult.getChangeInfo().getComment());
			ci.setId(interResult.getChangeInfo().getId());
			ci.setStatus(interResult.getChangeInfo().getStatus());
			ci.setSubmittedAt(interResult.getChangeInfo().getSubmittedAt());
			result.setChangeInfo(ci);
		}
		return result;
	}
}
