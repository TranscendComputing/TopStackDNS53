package com.msi.dns53.queryutil;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

import com.msi.tough.core.Appctx;

public class DigUtil {
	//private final static Logger logger =  Appctx.getLogger(DigUtil.class.getName());
	
	public static Record[] dig(String name) throws TextParseException, UnknownHostException{
		//logger.debug("Requesting DNS query for " + name + " against localhost.");
		Lookup l = new Lookup(name);
		l.setResolver(new SimpleResolver("localhost"));
		Record[] records = l.run();
		return records;
	}
	
	public static Record[] dig(String name, String resolver) throws TextParseException, UnknownHostException{
		//logger.debug("Requesting DNS query for " + name + " against " + resolver + ".");
		Lookup l = new Lookup(name);
		l.setResolver(new SimpleResolver(resolver));
		Record[] records = l.run();
		return records;
	}
}
