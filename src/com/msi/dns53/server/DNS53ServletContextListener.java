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
