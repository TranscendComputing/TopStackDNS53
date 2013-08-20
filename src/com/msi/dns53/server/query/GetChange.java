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

import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.GetChangeResult;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.util.DNS53QueryUtil;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;

public class GetChange extends AbstractHeaderAction<GetChangeResult>{

	@Override
	public String marshall(MarshallStruct<GetChangeResult> input,
			HttpServletResponse resp) throws Exception {
		GetChangeResult result = input.getMainObject();
		ChangeInfo ci = result.getChangeInfo();
		XMLNode response = new XMLNode(DNS53Constants.GETCHANGERESPONSE);
		response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
		if(ci != null){
			DNS53QueryUtil.marshallChangeInfo(ci, response);
		}
		return response.toString();
	}

	public GetChangeRequest unmarshall(HttpServletRequest req){
		GetChangeRequest request = new GetChangeRequest();
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
	public GetChangeResult process0(Session session, HttpServletRequest req,
			HttpServletResponse resp, Map<String, String[]> map)
			throws Exception {
		GetChangeRequest request = unmarshall(req);
		return getChange(request);
	}

	private GetChangeResult getChange(GetChangeRequest request) throws ErrorResponse{
		AccessMySQL sqlaccess = AccessMySQL.getInstance();
		GetChangeResult result = new GetChangeResult();
		
		String[] changeRecord = sqlaccess.getChange(request.getId());
		if(changeRecord[0] == null){	//result empty -> changeID not existing -> AccessDenied
			throw DNS53Faults.NoSuchChange(request.getId());
		}
		
		ChangeInfo ci = new ChangeInfo();
		ci.setId(changeRecord[0]);
		ci.setStatus(changeRecord[1]);
		String date = changeRecord[2];
		Date submittedAt = new Date(date);
		ci.setSubmittedAt(submittedAt);
		
		result.setChangeInfo(ci);
		
		return result;
	}
}
