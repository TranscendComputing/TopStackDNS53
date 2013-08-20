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

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CreateHostedZoneResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class CreateHostedZoneResponsePOJO {
	private HostedZonePOJO hostedZone;
	private ChangeInfoPOJO changeInfo;
	private DelegationSetPOJO delegationSet;

	@XmlRegistry
	public class ObjectFactory {
		public CreateHostedZoneResponsePOJO createCreateHostedZoneResultPOJO() { return new CreateHostedZoneResponsePOJO(); }
	}

	@XmlElement(name = "HostedZone", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public HostedZonePOJO getHostedZone() {
		return hostedZone;
	}

	public void setHostedZone(HostedZonePOJO hostedZone) {
		this.hostedZone = hostedZone;
	}

	@XmlElement(name = "ChangeInfo", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public ChangeInfoPOJO getChangeInfo() {
		return changeInfo;
	}

	public void setChangeInfo(ChangeInfoPOJO changeInfo) {
		this.changeInfo = changeInfo;
	}

	@XmlElement(name = "DelegationSet", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public DelegationSetPOJO getDelegationSet() {
		return delegationSet;
	}

	public void setDelegationSet(DelegationSetPOJO delegationSet) {
		this.delegationSet = delegationSet;
	}



	public static class HostedZonePOJO{
		private String id;
		private String name;
		private String callerReference;
		private ConfigPOJO config;
		private long resourceRecordSetCount;

		@XmlElement(name = "Id", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@XmlElement(name = "Name", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElement(name = "CallerReference", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getCallerReference() {
			return callerReference;
		}

		public void setCallerReference(String callerReference) {
			this.callerReference = callerReference;
		}

		@XmlElement(name = "Config", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public ConfigPOJO getConfig() {
			return config;
		}

		public void setConfig(ConfigPOJO config) {
			this.config = config;
		}

		@XmlElement(name = "ResourceRecordSetCount", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public long getResourceRecordSetCount() {
			return resourceRecordSetCount;
		}

		public void setResourceRecordSetCount(long resourceRecordSetCount) {
			this.resourceRecordSetCount = resourceRecordSetCount;
		}
	}

	public static class ConfigPOJO{
		private String comment;

		@XmlElement(name = "Comment", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

	public static class ChangeInfoPOJO{
		private String id;
		private String status;
		private Date submittedAt;
		private String comment;

		@XmlElement(name = "Id", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@XmlElement(name = "Status", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@XmlElement(name = "SubmittedAt", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public Date getSubmittedAt() {
			return submittedAt;
		}

		public void setSubmittedAt(Date submittedAt) {
			this.submittedAt = submittedAt;
		}

		@XmlElement(name = "Comment", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

	}

	public static class DelegationSetPOJO{
		private List<String> nameServers;

		@XmlElementWrapper(name="NameServers", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		@XmlElement(name = "NameServer", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public List<String> getNameServers() {
			return nameServers;
		}

		public void setNameServers(List<String> nameServers) {
			this.nameServers = nameServers;
		}
	}
}
