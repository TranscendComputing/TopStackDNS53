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

import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.ChangeInfoPOJO;

@XmlRootElement(name = "DeleteHostedZoneResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class DeleteHostedZoneResponsePOJO {
	private ChangeInfoPOJO changeInfo;
	
	@XmlRegistry
	public class ObjectFactory {
		public DeleteHostedZoneResponsePOJO createDeleteHostedZoneResponsePOJO() { return new DeleteHostedZoneResponsePOJO(); }
	}
	
	@XmlElement(name = "ChangeInfo", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public ChangeInfoPOJO getChangeInfo() {
		return changeInfo;
	}

	public void setChangeInfo(ChangeInfoPOJO changeInfo) {
		this.changeInfo = changeInfo;
	}
}
