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
package com.msi.dns53.util;

import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.DelegationSet;
import com.amazonaws.services.route53.model.HostedZone;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.server.DNS53Constants;
import com.msi.tough.query.QueryUtil;

public class DNS53QueryUtil {
	public static void marshallHostedZone(HostedZone hostedZone, XMLNode response) {
		XMLNode hz = QueryUtil.addNode(response, DNS53Constants.HOSTEDZONE);
		QueryUtil.addNode(hz, DNS53Constants.ID, hostedZone.getId());
		QueryUtil.addNode(hz, DNS53Constants.NAME, hostedZone.getName());
		QueryUtil.addNode(hz, DNS53Constants.CALLERREFERENCE, hostedZone.getCallerReference());
		if(hostedZone.getConfig() != null){
			XMLNode config = QueryUtil.addNode(hz, DNS53Constants.CONFIG);
			QueryUtil.addNode(config, DNS53Constants.COMMENT, hostedZone.getConfig().getComment());
		}
		QueryUtil.addNode(hz, DNS53Constants.RESOURCERECORDSETCOUNT, hostedZone.getResourceRecordSetCount());
	}
	
	public static void marshallChangeInfo(ChangeInfo ci, XMLNode response) {
		XMLNode changeInfo = QueryUtil.addNode(response, DNS53Constants.CHANGEINFO);
		QueryUtil.addNode(changeInfo, DNS53Constants.ID, ci.getId());
		QueryUtil.addNode(changeInfo, DNS53Constants.STATUS, ci.getStatus());
		QueryUtil.addNode(changeInfo, DNS53Constants.SUBMITTEDAT, ci.getSubmittedAt());
	}
	
	public static void marshallDelegationSet(DelegationSet result,
			XMLNode response) {
		XMLNode delegationSet = QueryUtil.addNode(response, DNS53Constants.DELEGATIONSET);
		if(result.getNameServers() != null && result.getNameServers().size() > 0){
			XMLNode nameservers = QueryUtil.addNode(delegationSet, DNS53Constants.NAMESERVERS);
			for(String nameserver : result.getNameServers()){
				QueryUtil.addNode(nameservers, DNS53Constants.NAMESERVER, nameserver);
			}
		}
	}
}
