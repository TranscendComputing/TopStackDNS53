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

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.msi.tough.core.Appctx;

/**
 * @author Daniel Kim (dkim@momentumsi.com)
 */

/**
 * DNS53Servlet: All the workloads are carried to DNS53Server class. This takes
 * care of the requests coming into /2010-10-01/hostedzone
 */
public class DNS53Servlet extends HttpServlet {
    private final static Logger logger = Appctx.getLogger(DNS53Servlet.class
            .getName());
    private static final long serialVersionUID = 1L;

    private void analyzeRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String queryString = req.getQueryString();
        logger.debug("Request URL chunked into pieces:\n" + "Scheme = "
                + scheme + "\n" + "ServerName = " + serverName + "\n"
                + "ServerPort = " + serverPort + "\n" + "ContextPath = "
                + contextPath + "\n" + "ServletPath = " + servletPath + "\n"
                + "PathInfo = " + pathInfo + "\n" + "QueryString = "
                + queryString);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            logger.debug("Request from " + req.getRemoteAddr());
            logger.debug("doGet() is called from DNS53Query...");

            analyzeRequest(req, resp);

            final DNS53Service service = Appctx.getBean("DNS53Query");
            service.process(req, resp, "GET");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            logger.debug("Request from " + req.getRemoteAddr());
            logger.debug("doPost() is called from DNS53Query...");

            analyzeRequest(req, resp);

            final DNS53Service service = Appctx.getBean("DNS53Query");
            service.process(req, resp, "POST");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            logger.debug("Request from " + req.getRemoteAddr());
            logger.debug("doDelete() is called from DNS53Query...");

            analyzeRequest(req, resp);

            final DNS53Service service = Appctx.getBean("DNS53Query");
            service.process(req, resp, "DELETE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
