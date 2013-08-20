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
package com.msi.dns53.server.query;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.DelegationSet;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.server.RequestHandler;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.tough.core.Appctx;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;

public class CreateHostedZone extends AbstractHeaderAction<CreateHostedZoneResult>{
	private final static Logger logger =  Appctx.getLogger(CreateHostedZone.class.getName());

	@Override
	public int getSuccessStatus(){
		return 201;
	}
	
	@Override
	public String getLocationHeader(){
		return DNS53Constants.LOCATION_VALUE;
	}
	
	@Override
	public String marshall(MarshallStruct<CreateHostedZoneResult> input,
			HttpServletResponse resp) throws Exception {
		CreateHostedZoneResult result = input.getMainObject();
		XMLNode response = new XMLNode(DNS53Constants.CREATEHOSTEDZONERESPONSE);
		
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
		if(result.getHostedZone() != null){
			DNS53QueryUtil.marshallHostedZone(result.getHostedZone(), response);
		}
		if(result.getChangeInfo() != null){
			DNS53QueryUtil.marshallChangeInfo(result.getChangeInfo(), response);
		}
		if(result.getDelegationSet() != null){
			DNS53QueryUtil.marshallDelegationSet(result.getDelegationSet(), response);
		}
		return response.toString();
	}

	public CreateHostedZoneRequest unmarshall(HttpServletRequest req) throws IOException, ParserConfigurationException, SAXException{
		final CreateHostedZoneRequest request = new CreateHostedZoneRequest();

		StringBuilder stringBuilder = new StringBuilder(1000);
		Scanner scanner = new Scanner(req.getInputStream());
		while (scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine());
		}
		String body = stringBuilder.toString();
		logger.debug("XML Body Content: " + body);

		DefaultHandler handler = new DefaultHandler(){
			private boolean createHostedZoneRequest = false;
			private boolean hostedZoneConfig = false;
			private CharArrayWriter contents = new CharArrayWriter();

			public void startElement(String uri, String localName,String nodeName, 
					Attributes attributes) throws SAXException {
				contents.reset();
				if(!uri.equals(DNS53Constants.XMLNS_VALUE)){
					throw DNS53Faults.InvalidInput("The XML you provided did not have the correct namespace.");
				}
				if(nodeName.equals( DNS53Constants.CREATEHOSTEDZONEREQUEST )){
					createHostedZoneRequest = true;
				}
				if(nodeName.equals( DNS53Constants.HOSTEDZONECONFIG )){
					hostedZoneConfig = true;
				}
			}
			public void endElement(String uri, String localName,
					String nodeName) throws SAXException {
				if(createHostedZoneRequest && localName.equals( DNS53Constants.CREATEHOSTEDZONEREQUEST )){
					createHostedZoneRequest = false;
				}
				if(createHostedZoneRequest && hostedZoneConfig && localName.equals( DNS53Constants.HOSTEDZONECONFIG )){
					hostedZoneConfig = false;
				}
				if(createHostedZoneRequest && localName.equals( DNS53Constants.NAME )){
					request.setName(contents.toString());
				}
				if(createHostedZoneRequest && localName.equals( DNS53Constants.CALLERREFERENCE )){
					request.setCallerReference(contents.toString());
				}
				if(createHostedZoneRequest && hostedZoneConfig && localName.equals( DNS53Constants.COMMENT )){
					HostedZoneConfig config = new HostedZoneConfig();
					config.setComment(contents.toString());
					request.setHostedZoneConfig(config);
				}
			}
			public void characters(char ch[], int start, int length) throws SAXException {
				this.contents.write( ch, start, length );
			}
		};

		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler( handler );
		xr.parse( new InputSource(new StringReader(body)));

		logger.debug("Unmarshalling complete: " + request.toString());
		return request;
	}

	@Override
	public CreateHostedZoneResult process0(Session session,
			HttpServletRequest req, HttpServletResponse resp,
			Map<String, String[]> map) throws Exception {
		CreateHostedZoneRequest request = null;
		try{
			request = unmarshall(req);	
		}catch(Exception e){
			e.printStackTrace();
			throw DNS53Faults.InvalidInput("XML is either malformed or some of its elements are invalid.");
		}
		
		return createHostedZone(session, request);
	}

	private CreateHostedZoneResult createHostedZone(Session session, CreateHostedZoneRequest request) throws ErrorResponse{
		CreateHostedZoneResult result = new CreateHostedZoneResult();
		Date submittedAt = new Date();
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		
		String name = request.getName();
		try {
			new java.net.URI("http://" + name);
		} catch(URISyntaxException e) {
			throw DNS53Faults.InvalidDomainName();
		}
		
		String comment = null;
		if(request.getHostedZoneConfig() != null){
			comment = request.getHostedZoneConfig().getComment();
		}
		List<String> zoneInfo = sqlaccess.createHostedZone(session, name, request.getCallerReference(), comment, this.getAccountId());
		String zoneId = zoneInfo.get(0);
		if(zoneId.equals("DUPLICATE_REFERENCE")){
			throw DNS53Faults.HostedZoneAlreadyExists();
		}
		else if(zoneId.equals("DUPLICATE_NAME")){
			throw DNS53Faults.DelegationSetNotAvailable();
		}
		else{
			String status = DNS53Constants.PENDING;
			String tableName = zoneInfo.get(1);
			String changeID = RequestHandler.writeChange(sqlaccess, status, submittedAt.toString(), tableName, "CREATE");
			
			HostedZone hz = new HostedZone(zoneId, request.getName(), request.getCallerReference());
			hz.setConfig(request.getHostedZoneConfig());
			result.setHostedZone(hz);
			
			ChangeInfo ci = new ChangeInfo(changeID, status, submittedAt);
			result.setChangeInfo(ci);
			
			List<String> nameServers = new LinkedList<String>();
			for(int i = 2; i < zoneInfo.size(); ++i){
				nameServers.add(zoneInfo.get(i));
			}
			DelegationSet ds = new DelegationSet(nameServers);
			result.setDelegationSet(ds);
		}
		return result;
	}
}
