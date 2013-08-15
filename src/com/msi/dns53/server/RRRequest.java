package com.msi.dns53.server;


/**
 * @author Daniel Kim (dkim@momentumsi.com)
 *
 *	RRRequest is a Java object that represent each individual resource record modification request in the batch.
 */
public class RRRequest {
	private String action = "N/A";	//either "CREATE", "DELETE", or "LIST"
	private String name = "N/A"; //e.g., www.dns53test.com
	private String type = "N/A"; //A, AA, CNAME, PTR, etc.
	private int TTL = -1;
	private String value = "N/A";	//e.g., 123.1.2.3
	
	public RRRequest(){
		//default constructor
	}
	
	public RRRequest(String a, String n, String t, int ttl, String v){
		this.action = a;
		this.name = n;
		this.type = t;
		this.TTL = ttl;
		this.value = v;
	}
	
	public String getAction(){
		return this.action;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getType(){
		return this.type;
	}
	
	public int getTTL(){
		return this.TTL;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public void setAction(String a){
		this.action = a;
	}
	
	public void setName(String n){
		this.name = n;
	}
	
	public void setType(String t){
		this.type = t;
	}
	
	public void setTTL(int t){
		this.TTL = t;
	}
	
	public void setValue(String v){
		this.value = v;
	}
}
