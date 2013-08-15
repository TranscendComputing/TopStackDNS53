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
