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
package com.msi.dns53.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.DelegationSetPOJO;
import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.HostedZonePOJO;

@XmlRootElement(name = "GetHostedZoneResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class GetHostedZoneResponsePOJO {
	private HostedZonePOJO hostedZone;
	private DelegationSetPOJO delegationSet;
	
	@XmlRegistry
	public class ObjectFactory {
	     public GetHostedZoneResponsePOJO createGetHostedZoneResponsePOJO() { return new GetHostedZoneResponsePOJO(); }
	}
	
	@XmlElement(name = "HostedZone", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public HostedZonePOJO getHostedZone() {
		return hostedZone;
	}

	public void setHostedZone(HostedZonePOJO hostedZone) {
		this.hostedZone = hostedZone;
	}

	@XmlElement(name = "DelegationSet", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public DelegationSetPOJO getDelegationSet() {
		return delegationSet;
	}

	public void setDelegationSet(DelegationSetPOJO delegationSet) {
		this.delegationSet = delegationSet;
	}
	
	
}
