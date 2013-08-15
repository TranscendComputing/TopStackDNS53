package com.msi.dns53.test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.sqs.AmazonSQSClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-dns-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })

public class AbstractBaseDnsTest {
	@Autowired
	private AWSCredentials creds;
	
	@Autowired
	private AmazonRoute53Client client;

	@Autowired
	private String defaultAvailabilityZone;
	
	public AmazonRoute53Client getRoute53Client(){
		return this.client;
	}
	public void setRoute53Client(AmazonRoute53Client client){
		this.client = client;
	}
	public AWSCredentials getCreds() {
		return this.creds;
	}
	public void setCreds(AWSCredentials creds) {
		this.creds = creds;
	}
	public String getDefaultAvailabilityZone() {
        return this.defaultAvailabilityZone;
    }
    public void setDefaultAvailabilityZone(String defaultAvailabilityZone) {
        this.defaultAvailabilityZone = defaultAvailabilityZone;
    }
}
