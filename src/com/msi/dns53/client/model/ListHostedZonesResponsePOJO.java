package com.msi.dns53.client.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;

import com.msi.dns53.client.model.CreateHostedZoneResponsePOJO.HostedZonePOJO;

@XmlRootElement(name = "ListHostedZonesResponse", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
public class ListHostedZonesResponsePOJO {
	private List<HostedZonePOJO> hostedZones;
	private String marker;
	private boolean isTruncated;
	private String nextMarker;
	private String maxItems;
	
	@XmlRegistry
	public class ObjectFactory {
	     public ListHostedZonesResponsePOJO createListHostedZonesResponsePOJO() { return new ListHostedZonesResponsePOJO(); }
	}

	@XmlElementWrapper(name="HostedZones", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	@XmlElement(name = "HostedZone", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public List<HostedZonePOJO> getHostedZones() {
		return hostedZones;
	}

	public void setHostedZones(List<HostedZonePOJO> hostedZones) {
		this.hostedZones = hostedZones;
	}

	@XmlElement(name = "Marker", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}

	@XmlElement(name = "IsTruncated", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	@XmlElement(name = "NextMarker", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public String getNextMarker() {
		return nextMarker;
	}

	public void setNextMarker(String nextMarker) {
		this.nextMarker = nextMarker;
	}

	@XmlElement(name = "MaxItems", namespace = "https://route53.amazonaws.com/doc/2012-02-29/")
	public String getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(String maxItems) {
		this.maxItems = maxItems;
	}
	
}
