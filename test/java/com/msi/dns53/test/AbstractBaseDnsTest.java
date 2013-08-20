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
