package com.msi.dns53.server.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;

import com.amazonaws.services.route53.model.DelegationSet;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.GetHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.model.DNS53ResourceRecord;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.tough.core.Appctx;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;

public class GetHostedZone extends AbstractHeaderAction<GetHostedZoneResult>{
	private final static Logger logger =  Appctx.getLogger(CreateHostedZone.class.getName());

	@Override
	public String marshall(MarshallStruct<GetHostedZoneResult> input,
			HttpServletResponse resp) throws Exception {
		GetHostedZoneResult result = input.getMainObject();
		XMLNode response = new XMLNode(DNS53Constants.GETHOSTEDZONERESPONSE);
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);

		DNS53QueryUtil.marshallHostedZone(result.getHostedZone(), response);
		DNS53QueryUtil.marshallDelegationSet(result.getDelegationSet(), response);

		return response.toString();
	}

	public GetHostedZoneRequest unmarshall(HttpServletRequest req){
		GetHostedZoneRequest request = new GetHostedZoneRequest();
		SlashObject so = new SlashObject(req.getPathInfo());
		List<String> paths = so.getList();
		if(paths.size() != 3){
			throw DNS53Faults.InternalError();
		}
		String zoneId = paths.get(2);
		request.setId(zoneId);
		return request;
	}

	@Override
	public GetHostedZoneResult process0(Session session,
			HttpServletRequest req, HttpServletResponse resp,
			Map<String, String[]> map) throws Exception {
		GetHostedZoneRequest request = unmarshall(req);
		GetHostedZoneResult result = getHostedZone(session, request);
		return result;
	}

	private GetHostedZoneResult getHostedZone(Session sess, GetHostedZoneRequest request) throws ErrorResponse{
		String zoneId = request.getId();
		logger.debug("GetHosteZone target: " + zoneId);
		GetHostedZoneResult result = new GetHostedZoneResult();
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		String[] responseCont = sqlaccess.getHostedZone(zoneId);
		if(responseCont[1] == null){
			throw DNS53Faults.NoSuchHostedZone(zoneId);
		}
		HostedZone hz = new HostedZone();
		hz.setId(responseCont[0]);
		hz.setName(responseCont[1]);
		hz.setCallerReference(responseCont[2]);
		HostedZoneConfig config = new HostedZoneConfig();
		config.setComment(responseCont[3]);
		hz.setConfig(config);
		result.setHostedZone(hz);
		
		DelegationSet delegationSet = new DelegationSet();
		Collection<String> nameServers = new LinkedList<String>();
		List<DNS53ResourceRecord> nsRecords = sqlaccess.listResourceRecords(sess, request.getId(), 
				null, null, "NS",
				null, -1, -1);
		for(DNS53ResourceRecord ns : nsRecords){
			String nameserver = ns.getRdata();
			nameserver = nameserver.substring(0, nameserver.length() - 1);
			nameServers.add(nameserver);
		}
		delegationSet.setNameServers(nameServers);
		result.setDelegationSet(delegationSet);
		logger.debug("Returning the result: " + result.toString());
		return result;
	}
}
