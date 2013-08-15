package com.msi.dns53.test;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.HostedZoneAlreadyExistsException;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.msi.dns53.server.AccessMySQL;

public class Route53Test {
	public static void main(String args[]) throws SAXException, IOException{
		//aws msi
		String ac1 = "0RHVQECAGQXA4ESFRH02";
		String sc1 = "DetkdibfQwZ93QZMYg/kosovGym+dw0wQmx3R/0r";

		//aws dkim
		String ac2 = "AKIAJ5GBL5ZNDYTIJ2DA";
		String sc2 = "rT1zzxvGb732zrX8R3WcQF/CoKCSQK33ynrwXzNu";

		//euca
		String ac3 = "WN9OL5XUHV41VCOIE4G3C";
		String sc3 = "KYZBgtrhprNxwhvRp0tpwD4FNjr6jA8J4OmemhEgs";
		
		
		// euca3 buildbot
		String ac4 = "UZOIR7XKBG7OXF6O4XVWS";
		String sc4 = "XSEXgHz7Rmw3kc3oGfhG1gFSYRQqPamrZJmKntf5";
		
		
		//essex buildbot
		String ac5 = "45b0e430803c4714a142b5c33b7bfa0d";
		String sc5 = "e15ae5599f334f4198602ce4df386c40";
		
		
		AWSCredentials cred = new BasicAWSCredentials(ac5, sc5);
		AmazonRoute53Client client = new AmazonRoute53Client(cred);
		client.setEndpoint("https://daniel:8443/DNS53Server");
		//client.setEndpoint("localhost:12345");

		//ListResourceRecordSets(client);

		//CreateHostedZone(client);
		
		/*try{
			ListResourceRecordSets(client);
		}catch(Exception e){
			System.out.println(e.getClass().getCanonicalName());
		}*/
		
		//ListHostedZones(client);

		/*com.amazonaws.transform.StaxUnmarshallerContext context = new StaxUnmarshallerContext(null);
		context.getHeader("Location");*/

		//testScheduledExecution();

		//testTableExists();
		//testChangeResourceRecordSets(client);
		
		//createARecords(client);
		
		createCNAMERecords(client);
		
		
		//testReflection();
	}
	
	public static void testReflection(){
		Class<AmazonServiceException> clazz = null;
		Constructor<AmazonServiceException> c = null;
		AmazonServiceException exception = null;
		try {
			String clazzName = "HostedZoneAlreadyExists" + "Exception";
			clazz = (Class<AmazonServiceException>) Class.forName(clazzName);
			c = (Constructor<AmazonServiceException>) clazz.getConstructor(String.class);
			exception = (AmazonServiceException) c.newInstance(new String[]{"Whatever"});
		} catch (Exception e) {
			System.out.println("STOP!");
			e.printStackTrace();
		}
		System.out.println(exception.toString());
	}
	
	public static void createARecords(AmazonRoute53Client client){
		ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
		request.setHostedZoneId("Z6C671E6E1A044F6695AD50EE258D8BAE");
		ChangeBatch changeBatch = new ChangeBatch();
		Collection<Change> changes = new LinkedList<Change>();
		for(int i = 1; i < 151; ++i){
			Change change = new Change();
			change.setAction(ChangeAction.CREATE);
			ResourceRecordSet rrs  = new ResourceRecordSet();
			rrs.setType(RRType.A);
			rrs.setTTL(900L);
			rrs.setName("inst-" + i + ".msicluster.momentumsoftware.com");
			Collection<ResourceRecord> rr = new LinkedList<ResourceRecord>();
			ResourceRecord e = new ResourceRecord();
			e.setValue("172.31.253." + i);
			rr.add(e);
			rrs.setResourceRecords(rr);
			change.setResourceRecordSet(rrs);
			changes.add(change);
		}
		changeBatch.setChanges(changes);
		request.setChangeBatch(changeBatch);
		client.changeResourceRecordSets(request  );
		
	}
	
	public static void createCNAMERecords(AmazonRoute53Client client){
		ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
		request.setHostedZoneId("Z9366F3515BBA46B2AA8C86B1D6DF0311");
		ChangeBatch changeBatch = new ChangeBatch();
		Collection<Change> changes = new LinkedList<Change>();
			Change change = new Change();
			change.setAction(ChangeAction.CREATE);
			ResourceRecordSet rrs  = new ResourceRecordSet();
			rrs.setType(RRType.CNAME);
			rrs.setTTL(900L);
			rrs.setName("devessex.essex.momentumsoftware.com");
			
			Collection<ResourceRecord> rr = new LinkedList<ResourceRecord>();
			ResourceRecord e = new ResourceRecord();
			e.setValue("inst-5.essex.momentumsoftware.com");
			rr.add(e);
			rrs.setResourceRecords(rr);
			change.setResourceRecordSet(rrs);
			changes.add(change);
		changeBatch.setChanges(changes);
		request.setChangeBatch(changeBatch);
		client.changeResourceRecordSets(request  );
		
	}
	
	public static void testChangeResourceRecordSets(AmazonRoute53Client client){
		ChangeResourceRecordSetsRequest req = new ChangeResourceRecordSetsRequest();
		req.setHostedZoneId("Z3HKYAOP6P7EX7");
		ChangeBatch batch = new ChangeBatch();
		Collection<Change> changes = new LinkedList<Change>();
		Change change = new Change();
		change.setAction("DELETE");
		ResourceRecordSet rrSet = new ResourceRecordSet();
		rrSet.setName("www.examplethatshouldntbequeried.com.");
		rrSet.setType("A");
		rrSet.setTTL(300L);
		rrSet.setWeight(1L);
		Collection<ResourceRecord> rrs = new LinkedList<ResourceRecord>();
		ResourceRecord e = new ResourceRecord();
		e.setValue("172.31.255.2");
		rrs.add(e);
		rrSet.setResourceRecords(rrs);
		rrSet.setSetIdentifier("1");
		rrSet.setTTL(300L);
		change.setResourceRecordSet(rrSet);
		changes.add(change);
		batch.setChanges(changes);
		req.setChangeBatch(batch);
		client.changeResourceRecordSets(req);
	}
	
	public static void testTableExists(){
		AccessMySQL a = AccessMySQL.getInstance();
		System.out.println(a.tableExists("26thisShallBeUnique"));
	}

	public static void testScheduledExecution(){
		ScheduledExecutorService scheduler = 
				Executors.newScheduledThreadPool(1);
		final Runnable beeper = new Runnable() {
			public void run() {
				System.out.println("beep");
				try {
					Process process = new ProcessBuilder("/bin/bash", "-c", "ps ax | grep mysqld").start();
					InputStream is = process.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					
					String line;
					if ((line = br.readLine()) != null) {
						System.out.println(line);
					}
					if(line != null && !line.equals("")){
						String[] tokens = line.split("\\s+");
						System.out.println("First: " + tokens[0]);
						System.out.println("Second:" + tokens[1]);
						
						String restartCmd = "kill -SIGHUP " + tokens[1];
						System.out.println(restartCmd);
						
						//Process restart = new ProcessBuilder("/bin/bash", "-c", restartCmd).start();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		final ScheduledFuture<?> beeperHandle = 
				scheduler.scheduleAtFixedRate(beeper, 0, 10, SECONDS);
		
	}

	public static void ListResourceRecordSets(AmazonRoute53Client client){
		ListResourceRecordSetsRequest req = new ListResourceRecordSetsRequest();
		req.setHostedZoneId("Z6C671E6E1A044F6695AD50EE258D8BAE");
		//req.setMaxItems("1");
		//req.setStartRecordType("NS");
		System.out.println(client.listResourceRecordSets(req));
	}

	public static void CreateHostedZone(AmazonRoute53Client client){
		CreateHostedZoneRequest req = new CreateHostedZoneRequest();
		req.setName("iamdanielandthisisjustatest2.com.");
		req.setCallerReference("testCR5");
		HostedZoneConfig config = new HostedZoneConfig();
		config.setComment("Just some comment");
		req.setHostedZoneConfig(config );
		CreateHostedZoneResult result = client.createHostedZone(req);
		System.out.println(result.toString());
	}

	public static void GetHostedZone(AmazonRoute53Client client){
		GetHostedZoneRequest req = new GetHostedZoneRequest();
		req.setId("alrite");
		client.getHostedZone(req);
	}

	public static void ListHostedZones(AmazonRoute53Client client){
		ListHostedZonesRequest req = new ListHostedZonesRequest();
		req.setMaxItems("1");
		System.out.println(client.listHostedZones(req).toString());
	}

}
