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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;

import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.tough.core.Appctx;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;
import com.msi.tough.query.QueryUtil;

public class ListHostedZones extends AbstractHeaderAction<ListHostedZonesResult>{
	private final static Logger logger =  Appctx.getLogger(ListHostedZones.class.getName());
	
	@Override
	public String marshall(MarshallStruct<ListHostedZonesResult> input,
			HttpServletResponse resp) throws Exception {
		logger.debug("Marshalling the result into xml.");
		ListHostedZonesResult result = input.getMainObject();
		XMLNode response = new XMLNode(DNS53Constants.LISTHOSTEDZONESRESPONSE);
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
		if(result.getHostedZones() != null && result.getHostedZones().size() > 0){
			XMLNode hzs = QueryUtil.addNode(response, DNS53Constants.HOSTEDZONES);
			for(HostedZone hostedZone : result.getHostedZones()){
				DNS53QueryUtil.marshallHostedZone(hostedZone, hzs);
			}
		}
		QueryUtil.addNode(response, DNS53Constants.MARKER, result.getMarker());
		QueryUtil.addNode(response, DNS53Constants.ISTRUNCATED, result.getIsTruncated());
		QueryUtil.addNode(response, DNS53Constants.NEXTMARKER, result.getNextMarker());
		QueryUtil.addNode(response, DNS53Constants.MAXITEMS, result.getMaxItems());
		logger.debug("Returning the response xml to AbstractHeaderAction.");
		return response.toString();
	}

	public ListHostedZonesRequest unmarshall(HttpServletRequest req){
		logger.debug("Unmarshalling the ListHostedZones request.");
		ListHostedZonesRequest request = new ListHostedZonesRequest();
		Map<String, String[]> map = req.getParameterMap();
		String marker = QueryUtil.getString(map, "marker");
		String maxItems = QueryUtil.getString(map, "maxitems");
		request.setMarker(marker);
		request.setMaxItems(maxItems);
		return request;
	}

	@Override
	public ListHostedZonesResult process0(Session session,
			HttpServletRequest req, HttpServletResponse resp,
			Map<String, String[]> map) throws Exception {
		ListHostedZonesRequest request = unmarshall(req);
		return listHostedZones(request);
	}

	private ListHostedZonesResult listHostedZones(ListHostedZonesRequest request) throws ErrorResponse{
		String marker = request.getMarker();
		String maxI = request.getMaxItems();
		logger.debug("ListHostedZones request parameters { marker = " + marker + ", maxitems = " + maxI + " }");
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		
		if(marker != null && sqlaccess.getTableName(marker, this.getAccountId()).equals("FAILED")){
			logger.debug("User passed marker parameter, but no marker found in the server.");
			throw DNS53Faults.InvalidArgument("Marker parameter was passed, but it does not refer to any hosted zone.");
		}
		int maxItems = -1;
		try{
			if(maxI != null){
				maxItems = Integer.valueOf(request.getMaxItems());
			}
		}catch(NumberFormatException e){
			logger.debug("maxitems parameter is not parseable as an integer.");
			throw DNS53Faults.InvalidArgument("maxitems parameter must be an integer between 1 and 100.");
		}
		if(maxItems == -1){
			logger.debug("MaxItems is set to default value since user did not specify a value.");
			maxItems = 100;
		}
		
		ListHostedZonesResult result = sqlaccess.listHostedZones(request.getMarker(), maxItems, this.getAccountId());
		
		return result;
	}
}
