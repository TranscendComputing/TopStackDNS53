package com.msi.dns53.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rrSet")
public class DNS53ResourceRecord {	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rrSet_id")
	private long id;
	private String sid;
	private String zoneId;
	private String zoneName;
	private String name;
	private long ttl;
	private String rdtype;
	private String rdata;
	private long weight = -1;

	public DNS53ResourceRecord(){}
	
	public DNS53ResourceRecord(String name, long ttl, String rdtype, String rdata, long weight){
		this.name = name;
		this.ttl = ttl;
		this.rdtype = rdtype;
		this.rdata = rdata;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public String getRdtype() {
		return rdtype;
	}

	public void setRdtype(String rdtype) {
		this.rdtype = rdtype;
	}

	public String getRdata() {
		return rdata;
	}

	public void setRdata(String rdata) {
		this.rdata = rdata;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
}
