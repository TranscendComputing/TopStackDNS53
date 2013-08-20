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

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;

import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest;
import com.amazonaws.services.route53.model.DeleteHostedZoneResult;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.server.RequestHandler;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.tough.core.Appctx;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;

public class DeleteHostedZone extends AbstractHeaderAction<DeleteHostedZoneResult>{
	private final static Logger logger = Appctx.getLogger(DeleteHostedZone.class
			.getName());
	
	@Override
	public String marshall(MarshallStruct<DeleteHostedZoneResult> input,
			HttpServletResponse resp) throws Exception {
		DeleteHostedZoneResult result = input.getMainObject();
		ChangeInfo ci = result.getChangeInfo();
		XMLNode response = new XMLNode(DNS53Constants.DELETEHOSTEDZONERESPONSE);
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
		if(ci != null){
			DNS53QueryUtil.marshallChangeInfo(ci, response);
		}
		return response.toString();
	}	

	public DeleteHostedZoneRequest unmarshall(HttpServletRequest req){
		DeleteHostedZoneRequest request = new DeleteHostedZoneRequest();
		SlashObject so = new SlashObject(req.getPathInfo());
		List<String> paths = so.getList();
		if(paths.size() != 3){
			logger.debug("Request path is not set correctly. There are " + paths.size() + " path elements when there should 3.");
			throw DNS53Faults.InternalError();
		}
		String zoneId = paths.get(2);
		request.setId(zoneId);
		return request;
	}
	
	@Override
	public DeleteHostedZoneResult process0(Session session, HttpServletRequest req,
			HttpServletResponse resp, Map<String, String[]> map)
			throws Exception {
		DeleteHostedZoneRequest request = unmarshall(req);
		return deleteHostedZone(request);
	}
	
	private DeleteHostedZoneResult deleteHostedZone(DeleteHostedZoneRequest request) throws ErrorResponse{
		String zoneId = request.getId();
		logger.debug("Delete target: " + zoneId);
		Date submittedAt = new Date();
		DeleteHostedZoneResult result = new DeleteHostedZoneResult();
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		String tableName = sqlaccess.getTableName(zoneId, this.getAccountId());
		if(tableName.equals("FAILED")){
			throw DNS53Faults.NoSuchHostedZone(zoneId);
		}else{
			String callerReference = sqlaccess.getCallerReference(zoneId, this.getAccountId());
			sqlaccess.deleteHostedZone(zoneId, tableName, callerReference);
			String changeID = RequestHandler.writeChange(sqlaccess, "PENDING", submittedAt.toString(), tableName, "DELETE");	
			ChangeInfo changeInfo = new ChangeInfo();
			changeInfo.setId(changeID);
			changeInfo.setStatus("PENDING");
			changeInfo.setSubmittedAt(submittedAt);
			result.setChangeInfo(changeInfo);
		}
		return result;
	}
}
