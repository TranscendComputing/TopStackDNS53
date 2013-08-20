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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.msi.dns53.client.DNS53Client;
import com.msi.tough.core.Appctx;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.utils.ConfigurationUtil;
import com.msi.tough.utils.ServiceMetadataUtil;

public class DNS53MetadataUtil implements ServiceMetadataUtil {
	private static final Logger logger = Appctx
            .getLogger(DNS53MetadataUtil.class.getName());

	public void populateServiceMetadata(
			final ServletConfig config, String serviceName) {
		logger.debug("init(): TXT record will be created for this service regarding its port and context path.");
		String contextPath = config.getServletContext().getContextPath();
		String port = Appctx.getBean("TOMCAT_PORT");
		String master_passwd = Appctx.getBean("DB_PASSWORD");

		final String fqdn = (String) ConfigurationUtil
				.getConfiguration(Arrays.asList(new String[] { "FQDN" }));
		final String domain = (String) ConfigurationUtil
				.getConfiguration(Arrays.asList(new String[] { "FQDN_DOMAIN" }));
		String txtRecordValue = ":" + port + contextPath;
		String baseDNSServerURL = "http://localhost:" + port + "/DNS53Server/2012-02-29/";

		logger.debug("Tomcat port = " + port + "; FQDN = " + fqdn + "; domain = " + domain + "; TXT Record Value = " + txtRecordValue + "; BaseDNSServerUrl = " + baseDNSServerURL);

		DNS53Client client = new DNS53Client(baseDNSServerURL + "hostedzone", baseDNSServerURL + "change",
				"admin", master_passwd);

		logger.debug("Service name = " + serviceName);
		String recordName = serviceName + "-" + fqdn;
		logger.debug("TXT Record Name: " + recordName);

		logger.debug("init(): Calling ListHostedZones to find the target zone!");
		ListHostedZonesRequest lhzReq = new ListHostedZonesRequest();
		lhzReq.setMaxItems("1");

		ListHostedZonesResult lhzResult = client.listHostedZones(lhzReq);

		HostedZone zone = null;
		List<HostedZone> zones = lhzResult.getHostedZones();
		if(zones != null && zones.size() > 0){
			for(HostedZone hz : zones){
				if(hz.getName().equals(domain + ".") || hz.getName().equals(domain)){
					zone = hz;
				}
			}
		} else{
			logger.error("BaseAsyncServlet encountered an error while it was trying to find the target hosted zone.");
			throw ErrorResponse.InternalFailure();
		}

		if(zone == null){
			logger.error("BaseAsyncServlet could not find any zone for this TopStackWeb instance.");
			throw ErrorResponse.InternalFailure();
		}

		// TODO (optional) check for the CNAME record for this service before proceeding

		logger.debug("init(): Creating a new TXT record for " + recordName + " with \"" + txtRecordValue + "\" as its value!");
		String zoneId = zone.getId();
		ChangeResourceRecordSetsRequest crrsReq = new ChangeResourceRecordSetsRequest();
		crrsReq.setHostedZoneId(zoneId);
		ChangeBatch cb = new ChangeBatch();
		cb.setComment("BaseAsyncServlet => init(): Registering " + serviceName + " service for Transcend TopStack.");
		Collection<Change> changes = new LinkedList<Change>();
		Change change = new Change();
		change.setAction(ChangeAction.CREATE);
		ResourceRecordSet rrSet = new ResourceRecordSet();
		rrSet.setName(recordName);
		rrSet.setTTL(900L);
		rrSet.setType(RRType.TXT);
		Collection<ResourceRecord> rr = new LinkedList<ResourceRecord>();
		ResourceRecord record = new ResourceRecord();
		record.setValue(txtRecordValue);
		rr.add(record);
		rrSet.setResourceRecords(rr);
		change.setResourceRecordSet(rrSet);
		changes.add(change);
		cb.setChanges(changes);
		crrsReq.setChangeBatch(cb);
		ChangeResourceRecordSetsResult result = client.changeResourceRecordSets(crrsReq);
		logger.debug("Result for the last ChangeResourceRecordSets request: " + result.toString());
	}
}
