package com.msi.dns53.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "zones")
@org.hibernate.annotations.Table(appliesTo = "zones", indexes = { @Index(name = "idIndex", columnNames = { "id" }) })
public class DNS53HostedZone {
	@Id
	private String id;
	
	private String name;
	
	private String callerReference;
	
	private String comment;
	
	private long accountId;
	
	private String tableName;
	
	public DNS53HostedZone(){
		
	}
	
	public DNS53HostedZone(String id, String name, String callerReference, String comment){
		this.id = id;
		this.name = name;
		this.callerReference = callerReference;
		this.comment = comment;
	}
	
	public DNS53HostedZone(String id, String name, String callerReference, String comment, long acid){
		this.id = id;
		this.name = name;
		this.callerReference = callerReference;
		this.comment = comment;
		this.accountId = acid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCallerRefernce() {
		return callerReference;
	}

	public void setCallerRefernce(String callerRefernce) {
		this.callerReference = callerRefernce;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public long getAccount() {
		return accountId;
	}

	public void setAccount(long acId) {
		this.accountId = acId;
	}

	public String getCallerReference() {
		return callerReference;
	}

	public void setCallerReference(String callerReference) {
		this.callerReference = callerReference;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
}
