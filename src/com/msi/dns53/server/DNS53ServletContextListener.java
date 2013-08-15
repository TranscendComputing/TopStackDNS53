package com.msi.dns53.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;

import com.msi.dns53.server.bind.SignalBind;
import com.msi.tough.core.Appctx;

public class DNS53ServletContextListener implements ServletContextListener {
	private final static Logger logger =  Appctx.getLogger(DNS53ServletContextListener.class.getName());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Context is destroyed.");
		AccessMySQL.getInstance().closeConnection();
		SignalBind.getInstance().close();
		Appctx.instance().destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
	}
}
