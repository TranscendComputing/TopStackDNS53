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
package com.msi.dns53.test;

import java.util.Collection;
import java.util.LinkedList;

import org.hibernate.Session;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.msi.tough.core.HibernateUtil;
import com.msi.tough.model.AccountBean;
import com.msi.tough.utils.AccountUtil;

/**
 * @author dkim
 * This test will run only if you set your localhost to "daniel" in /etc/hosts after importing self signed certificate to both
 * Tomcat 7 and JRE security cacerts.
 */
public class DNS53HostedZonesTest extends AbstractBaseDnsTest{
	@Autowired
	private String accessKey;

	@Autowired
	private String secretKey;

	private void setupUser(){
		Session s = HibernateUtil.getSession();
		s.beginTransaction();
		AccountBean ab = new AccountBean();
		ab.setName("DNS53_TESTER");
		ab.setAccessKey(accessKey);
		ab.setSecretKey(secretKey);
		ab.setDefSecurityGroups("default");
		ab.setDefZone("DNS53_TEST_DEFAULT_ZONE");
		s.save(ab);
		s.getTransaction().commit();
	}

	private void cleanupUser(){
		Session s = HibernateUtil.getSession();
		s.beginTransaction();
		AccountBean ab = AccountUtil.readAccount(s, accessKey);
		s.delete(ab);
		s.getTransaction().commit();
	}

    @Test
    public void testSetup(){
        setupUser();
    }

	@Test
	public void testCreateHostedZone0(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setName("example-meh.com");
		req.setCallerReference("thisShallBeUnique");
		req.setHostedZoneConfig(new HostedZoneConfig().withComment("Everything else matters..."));
		CreateHostedZoneResult result = this.getRoute53Client().createHostedZone(req);
		assertNotNull(result);
	}

	@Test
	public void testCreateHostedZone1(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setName("example-dkim.com");
		req.setCallerReference("thisIsUnique");
		req.setHostedZoneConfig(new HostedZoneConfig().withComment("Nothing else matters..."));
		CreateHostedZoneResult result = this.getRoute53Client().createHostedZone(req);
        assertNotNull(result);
	}

	@Test
	public void testGetHostedZone(){
		GetHostedZoneRequest req = new GetHostedZoneRequest();
		req.setId("Z9741F80626774E8D9B2C7B8AFACFDAE6");
		this.getRoute53Client().getHostedZone(req);
	}

	@Test
	public void testListHostedZones0(){
		this.getRoute53Client().listHostedZones();
	}

	@Test
	public void testListHostedZones1(){
		ListHostedZonesRequest req = new ListHostedZonesRequest();
		//req.setMaxItems("1");
		req.setMarker("ED3E987ACA7F41CB935273F45C7BBF10s");
		this.getRoute53Client().listHostedZones(req);
	}

	@Test
	public void testListResourceRecordSets(){
		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest();
		req.setHostedZoneId("ZB20159C9F447440CA75A34B7AAB1BFFA");
		//req.setStartRecordType("SOA");
		//req.setStartRecordName("example-dkim.com");
		//req.setMaxItems("1");
		this.getRoute53Client().listResourceRecordSets(req);
	}

	@Test
	public void testChangeResourceRecordSets0(){
		/*ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId("Z5A748ACB59134595AAF7FD013A0F4D72");
		ChangeBatch batch = new ChangeBatch();
		batch.setComment("I have no idea what I'm doing.");
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.1");
		ResourceRecord resourceRecord2 = new ResourceRecord().withValue("172.31.255.2");
		changes.add(new Change().withAction("CREATE").withResourceRecordSet(
				new ResourceRecordSet().withName("www.example-dkim2.com").withType("A").withTTL(300L).withResourceRecords(resourceRecord, resourceRecord2)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);*/

		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId("ZB20159C9F447440CA75A34B7AAB1BFFA");
		ChangeBatch batch = new ChangeBatch();
		batch.setComment("I have no idea what I'm doing.");
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.1");
		ResourceRecord resourceRecord2 = new ResourceRecord().withValue("172.31.255.2");
		changes.add(new Change().withAction("CREATE").withResourceRecordSet(
				new ResourceRecordSet().withName("www.dkim4.com")
				.withType("A").withTTL(300L).withResourceRecords(resourceRecord, resourceRecord2)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);


		this.getRoute53Client().changeResourceRecordSets(req);
	}

	@Test
	public void testChangeResourceRecordSets1(){
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId("ZDF69134B71A944C99CC46E2CEC45E1A0");
		ChangeBatch batch = new ChangeBatch();
		batch.setComment("I have no idea what I'm doing.");
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.1");
		changes.add(new Change().withAction("DELETE").withResourceRecordSet(
				new ResourceRecordSet().withName("www.example-meh.com.").withType("A").withSetIdentifier("sid0").withWeight(3L)
				.withTTL(300L).withResourceRecords(resourceRecord)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		this.getRoute53Client().changeResourceRecordSets(req);
	}

	@Test
	public void testGetChange(){
		GetChangeRequest req = new GetChangeRequest();
		req.setId("CFF3B807384CE4CFA966AE4B4EFE78861");
		this.getRoute53Client().getChange(req);
	}

	@Test
	public void testDeleteHostedZone(){
		DeleteHostedZoneRequest req = new DeleteHostedZoneRequest();
		req.setId("Z27926C4FC8F244F0A3AACF4658EDCE99");
		this.getRoute53Client().deleteHostedZone(req);
	}

	@Test
    public void testCleanup(){
        cleanupUser();
    }
}
