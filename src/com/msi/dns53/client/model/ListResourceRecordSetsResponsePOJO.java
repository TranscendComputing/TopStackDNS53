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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListResourceRecordSetsResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class ListResourceRecordSetsResponsePOJO {
	private List<ResourceRecordSetPOJO> resourceRecordSets;
	private boolean isTruncated;
	private String maxItems;
	
	@XmlRegistry
	public class ObjectFactory {
		public ListResourceRecordSetsResponsePOJO createListResourceRecordSetsResponsePOJO() { return new ListResourceRecordSetsResponsePOJO(); }
	}
	
	@XmlElementWrapper(name="ResourceRecordSets", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	@XmlElement(name = "ResourceRecordSet", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public List<ResourceRecordSetPOJO> getResourceRecordSets() {
		return resourceRecordSets;
	}

	public void setResourceRecordSets(List<ResourceRecordSetPOJO> resourceRecordSets) {
		this.resourceRecordSets = resourceRecordSets;
	}

	@XmlElement(name = "IsTruncated", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	@XmlElement(name = "MaxItems", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public String getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(String maxItems) {
		this.maxItems = maxItems;
	}

	
	
	
	
	public static class ResourceRecordSetPOJO{
		private String name;
		private String type;
		private Long TTL;
		private String setIdentifier;
		private Long weight;
		private List<ResourceRecordPOJO> resourceRecords;
		
		@XmlElement(name = "Name", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@XmlElement(name = "Type", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		@XmlElement(name = "TTL", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public Long getTTL() {
			return TTL;
		}
		public void setTTL(Long tTL) {
			TTL = tTL;
		}
		@XmlElementWrapper(name="ResourceRecords", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		@XmlElement(name = "ResourceRecord", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public List<ResourceRecordPOJO> getResourceRecords() {
			return resourceRecords;
		}
		public void setResourceRecords(List<ResourceRecordPOJO> resourceRecords) {
			this.resourceRecords = resourceRecords;
		}
		@XmlElement(name = "SetIdentifier", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getSetIdentifier() {
			return setIdentifier;
		}
		public void setSetIdentifier(String setIdentifier) {
			this.setIdentifier = setIdentifier;
		}
		@XmlElement(name = "Weight", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public Long getWeight() {
			return weight;
		}
		public void setWeight(Long weight) {
			this.weight = weight;
		}
		
	}
	
	public static class ResourceRecordPOJO{
		private String value;

		@XmlElement(name = "Value", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
