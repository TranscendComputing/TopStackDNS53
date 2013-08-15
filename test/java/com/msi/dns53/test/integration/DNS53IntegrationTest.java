package com.msi.dns53.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.LinkedList;

import org.hibernate.Session;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.DeleteHostedZoneRequest;
import com.amazonaws.services.route53.model.DeleteHostedZoneResult;
import com.amazonaws.services.route53.model.GetChangeRequest;
import com.amazonaws.services.route53.model.GetChangeResult;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.GetHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.msi.dns53.client.DNS53Client;
import com.msi.dns53.model.DNS53HostedZone;
import com.msi.dns53.test.AbstractBaseDnsTest;
import com.msi.dns53.util.HostedZoneEntity;
import com.msi.tough.core.HibernateUtil;

public class DNS53IntegrationTest extends AbstractBaseDnsTest{
	@Autowired
	private String accessKey;

	@Autowired
	private String secretKey;

	@Autowired
	private DNS53Client customDNS53Client;

	private String zoneName0 = "dns53-test.com.";
	private String zoneName1 = "dns53-test-v2.com.";
	private String callerRef = "u003923";
	private String comment = "DNS 53 is here!";

	/*private void setupUser(){
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
	}*/

	private String getZoneId(String zoneName){
		Session s = HibernateUtil.getSession();
		s.beginTransaction();
		DNS53HostedZone hz = HostedZoneEntity.selectHostedZone(s, null, zoneName);
		return hz.getId();
	}

	/*@Test
	public void setupTestUser(){
		setupUser();
	}*/

	@Test
	public void createHostedZone0(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setCallerReference(callerRef);
		req.setName(zoneName0);
		req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		CreateHostedZoneResult result = customDNS53Client.createHostedZone(req);
		
		assertNotNull(result);
		assertNotNull(result.getHostedZone());
		assertEquals(zoneName0, result.getHostedZone().getName());
		assertEquals(callerRef, result.getHostedZone().getCallerReference());
		assertNotNull(result.getHostedZone().getConfig());
		assertEquals(comment, result.getHostedZone().getConfig().getComment());
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());
		assertNotNull(result.getDelegationSet());
		assertNotNull(result.getDelegationSet().getNameServers());
		assertNotNull(result.getDelegationSet().getNameServers().get(0));

		System.out.println(result.toString());
	}
	
	@Test	//(expected = HostedZoneAlreadyExistsException.class)
	public void createHostedZone1(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setCallerReference(callerRef);
		req.setName(zoneName0);
		req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		CreateHostedZoneResult result = null;
		try{
			result = customDNS53Client.createHostedZone(req);	
		}catch(AmazonServiceException e){
			assertEquals("HostedZoneAlreadyExists", e.getErrorCode());
		}
	}
	
	@Test
	public void createHostedZone2(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setCallerReference(callerRef);
		req.setName(zoneName1);
		req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		CreateHostedZoneResult result = null;
		try{
			result = customDNS53Client.createHostedZone(req);	
		}catch(AmazonServiceException e){
			assertEquals("HostedZoneAlreadyExists", e.getErrorCode());
		}
	}
	
	@Test	//(expected = DelegationSetNotAvailableException.class)
	public void createHostedZone3(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setCallerReference(callerRef + "1");
		req.setName(zoneName0);
		req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		CreateHostedZoneResult result = null;
		try{
			result = customDNS53Client.createHostedZone(req);	
		}catch(AmazonServiceException e){
			assertEquals("DelegationSetNotAvailable", e.getErrorCode());
		}
	}
	
	@Test	//(expected = InvalidInputException.class)
	public void createHostedZone4(){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setCallerReference("&*#)!@735)@#53(@&%^)(&%#");
		req.setName(zoneName1);
		req.setHostedZoneConfig(new HostedZoneConfig().withComment(comment));
		CreateHostedZoneResult result = null;
		try{
			result = customDNS53Client.createHostedZone(req);	
		}catch(AmazonServiceException e){
			assertEquals("InvalidInput", e.getErrorCode());
		}
	}

	@Test
	public void getHostedZone0(){
		GetHostedZoneRequest req = new GetHostedZoneRequest(getZoneId(zoneName0));
		GetHostedZoneResult result = customDNS53Client.getHostedZone(req);

		assertNotNull(result);
		assertNotNull(result.getHostedZone());
		assertEquals(zoneName0, result.getHostedZone().getName());
		assertEquals(callerRef, result.getHostedZone().getCallerReference());
		assertNotNull(result.getHostedZone().getConfig());
		assertEquals(comment, result.getHostedZone().getConfig().getComment());
		assertNotNull(result.getDelegationSet());
		assertNotNull(result.getDelegationSet().getNameServers());
		assertNotNull(result.getDelegationSet().getNameServers().get(0));

		System.out.println(result);
	}

	@Test	//(expected = NoSuchHostedZoneException.class)
	public void getHostedZone1(){
		GetHostedZoneRequest req = new GetHostedZoneRequest("BOGUS_ZONE_ID");
		GetHostedZoneResult result = null;
		try{
			result = customDNS53Client.getHostedZone(req);
		}catch(AmazonServiceException e){
			assertEquals("NoSuchHostedZone", e.getErrorCode());
		}
	}
	
	@Test
	public void listHostZones0(){
		ListHostedZonesRequest req = new ListHostedZonesRequest();
		ListHostedZonesResult result = customDNS53Client.listHostedZones(req);

		assertNotNull(result);
		assertNotNull(result.getHostedZones());
		assertEquals(1, result.getHostedZones().size());
		assertEquals(zoneName0, result.getHostedZones().get(0).getName());
		assertEquals(callerRef, result.getHostedZones().get(0).getCallerReference());
		assertNotNull(result.getHostedZones().get(0).getConfig());
		assertEquals(comment, result.getHostedZones().get(0).getConfig().getComment());
		//assertEquals(false, result.getIsTruncated());
		assertEquals(null, result.getMarker());
		assertEquals(null, result.getNextMarker());
		assertEquals("100", result.getMaxItems());

		System.out.println(result);
	}
	
	@Test
	public void listHostZones1(){
		ListHostedZonesRequest req = new ListHostedZonesRequest();
		req.setMarker(this.getZoneId(this.zoneName0));
		ListHostedZonesResult result = customDNS53Client.listHostedZones(req);

		assertNotNull(result);
		assertNotNull(result.getHostedZones());
		assertEquals(1, result.getHostedZones().size());
		assertEquals(zoneName0, result.getHostedZones().get(0).getName());
		assertEquals(callerRef, result.getHostedZones().get(0).getCallerReference());
		assertNotNull(result.getHostedZones().get(0).getConfig());
		assertEquals(comment, result.getHostedZones().get(0).getConfig().getComment());
		//assertEquals(false, result.getIsTruncated());
		assertEquals(null, result.getMarker());
		assertEquals(null, result.getNextMarker());
		assertEquals("100", result.getMaxItems());

		System.out.println(result);
	}
	
	@Test	//(expected = NoSuchHostedZoneException.class)
	public void listHostZones2(){
		ListHostedZonesRequest req = new ListHostedZonesRequest();
		req.setMarker("BOGUS_ZONE_ID");
		ListHostedZonesResult result = null;
		try{
			result = customDNS53Client.listHostedZones(req);
		}catch(AmazonServiceException e){
			assertEquals("NoSuchHostedZone", e.getErrorCode());
		}
		System.out.println(result);
	}

	@Test
	public void changeResourceRecordSets0(){
		String batchComment = "I have no idea what I'm doing.";
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId(getZoneId(zoneName0));
		ChangeBatch batch = new ChangeBatch();
		batch.setComment(batchComment);
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.235");
		changes.add(new Change().withAction("CREATE").withResourceRecordSet(
				new ResourceRecordSet().withName("www." + zoneName0)
				.withType("A").withTTL(300L).withResourceRecords(resourceRecord)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		ChangeResourceRecordSetsResult result = customDNS53Client.changeResourceRecordSets(req);

		assertNotNull(result);
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());

		System.out.println(result);
	}
	
	@Test
	public void listResourceRecordSets0(){
		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest(this.getZoneId(zoneName0));
		ListResourceRecordSetsResult result = customDNS53Client.listResourceRecordSets(req);
		
		assertNotNull(result);
		assertNotNull(result.getResourceRecordSets());
		assertEquals(true, result.getResourceRecordSets().size() > 0);
		boolean found = false;
		for(ResourceRecordSet rrSet : result.getResourceRecordSets()){
			if(rrSet.getName().equals("www." + zoneName0) && rrSet.getTTL().equals(300L) 
					&& rrSet.getType().equals("A") && rrSet.getResourceRecords().get(0).getValue().equals("172.31.255.235")){
				found = true;
			}
		}
		assertEquals(true, found);
		assertEquals(false, result.getIsTruncated());
		assertEquals("100", result.getMaxItems());
		assertEquals(null, result.getNextRecordIdentifier());
		assertEquals(null, result.getNextRecordName());
		assertEquals(null, result.getNextRecordType());
		System.out.println(result);
	}
	
	@Test	//(expected = NoSuchHostedZoneException.class)
	public void listResourceRecordSets1(){
		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest("BOGUS_HOSTED_ZONE_ID");
		ListResourceRecordSetsResult result = null;
		try{
			result = customDNS53Client.listResourceRecordSets(req);
		}catch(AmazonServiceException e){
			assertEquals("NoSuchHostedZone", e.getErrorCode());
		}
		System.out.println(result);
	}
	
	@Test
	public void changeResourceRecordSets1(){
		String batchComment = "I have no idea what I'm doing.";
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId(getZoneId(zoneName0));
		ChangeBatch batch = new ChangeBatch();
		batch.setComment(batchComment);
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.235");
		ResourceRecord resourceRecord2 = new ResourceRecord().withValue("172.31.255.254");
		changes.add(new Change().withAction("CREATE").withResourceRecordSet(
				new ResourceRecordSet().withName("w2." + zoneName0)
				.withType("A").withTTL(300L).withResourceRecords(resourceRecord, resourceRecord2)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		ChangeResourceRecordSetsResult result = customDNS53Client.changeResourceRecordSets(req);

		assertNotNull(result);
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());

		System.out.println(result);
	}

	@Test
	public void listResourceRecordSets2(){
		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest(this.getZoneId(zoneName0));
		ListResourceRecordSetsResult result = customDNS53Client.listResourceRecordSets(req);
		
		assertNotNull(result);
		assertNotNull(result.getResourceRecordSets());
		assertEquals(true, result.getResourceRecordSets().size() > 0);
		/*boolean found = false;
		for(ResourceRecordSet rrSet : result.getResourceRecordSets()){
			if(rrSet.getName().equals("w2." + zoneName0) && rrSet.getTTL().equals(300L) 
					&& rrSet.getType().equals("A") && rrSet.getResourceRecords().get(0).getValue().equals("172.31.255.235")){
				if(rrSet.getResourceRecords().get(1).getValue().equals("172.31.255.234")){
					found = true;	
				}
			}
		}
		assertEquals(true, found);*/
		assertEquals(false, result.getIsTruncated());
		assertEquals("100", result.getMaxItems());
		assertEquals(null, result.getNextRecordIdentifier());
		assertEquals(null, result.getNextRecordName());
		assertEquals(null, result.getNextRecordType());
		System.out.println(result);
	}
	
	//@Test
	public void getChange(){
		GetChangeRequest req = new GetChangeRequest("CFF0D1C08442C4584B9D39B1311496393");
		GetChangeResult result = customDNS53Client.getChange(req);
		System.out.println(result);
	}

	@Test
	public void changeResourceRecordSets2(){
		String batchComment = "Now I know what I'm doing. Kind of...";
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId(getZoneId(zoneName0));
		ChangeBatch batch = new ChangeBatch();
		batch.setComment(batchComment);
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.235");
		ResourceRecord resourceRecord2 = new ResourceRecord().withValue("172.31.255.254");
		changes.add(new Change().withAction("DELETE").withResourceRecordSet(
				new ResourceRecordSet().withName("w2." + zoneName0)
				.withType("A").withTTL(300L).withResourceRecords(resourceRecord, resourceRecord2)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		ChangeResourceRecordSetsResult result = customDNS53Client.changeResourceRecordSets(req);

		assertNotNull(result);
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());

		System.out.println(result);
	}
	
	@Test
	public void changeResourceRecordSets3(){
		String batchComment = "I definitely know what I'm doing.";
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId(getZoneId(zoneName0));
		ChangeBatch batch = new ChangeBatch();
		batch.setComment(batchComment);
		Collection<Change> changes = new LinkedList<Change>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue("172.31.255.235");
		changes.add(new Change().withAction("DELETE").withResourceRecordSet(
				new ResourceRecordSet().withName("www." + zoneName0)
				.withType("A").withTTL(300L).withResourceRecords(resourceRecord)));
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		ChangeResourceRecordSetsResult result = customDNS53Client.changeResourceRecordSets(req);

		assertNotNull(result);
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());

		System.out.println(result);
	}

	@Test
	public void deleteHostedZone(){
		DeleteHostedZoneRequest req = new DeleteHostedZoneRequest(getZoneId(zoneName0));
		DeleteHostedZoneResult result = customDNS53Client.deleteHostedZone(req);

		assertNotNull(result);
		assertNotNull(result.getChangeInfo());
		assertNotNull(result.getChangeInfo().getId());
		assertNotNull(result.getChangeInfo().getStatus());
		assertNotNull(result.getChangeInfo().getSubmittedAt());

		System.out.println(result);
	}

	/*@Test
	public void cleanupTestUser(){
		cleanupUser();
	}*/
}
