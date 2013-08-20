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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.msi.tough.core.Appctx;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.Action;
import com.msi.tough.query.QueryUtil;

public class DNS53Service {
	private final static Logger logger =  Appctx.getLogger(DNS53Service.class.getName());

	private Map<String, Action> actionMap;

	public DNS53Service(){}

	public DNS53Service(Map<String, Action> amap){
		this.actionMap = amap;
	}

	public void setActionMap(Map<String, Action> amap){
		this.actionMap = amap;
	}

	public Map<String, Action> getActionMap(){
		return this.actionMap;
	}

	public void process(HttpServletRequest req, HttpServletResponse resp, String httpMethod) throws Exception{
		logger.debug("HttpMethod: " + httpMethod);
		String actionName = QueryUtil.getString(req.getParameterMap(), "Action");
		if(actionName == null){
			actionName = analyzeRequest(req, httpMethod);	
		}
		if(actionName == null){
			logger.debug("Could not analyze the request.");
			throw DNS53Faults.InvalidHTTPRequest();
		}
		Action a = actionMap.get(actionName);
		logger.debug("Processing requested action, " + a);
		a.process(req, resp);
	}

	private String analyzeRequest(HttpServletRequest req, String httpMethod){
		if(httpMethod.equals("GET")){
			return analyzeGetRequest(req);
		}
		if(httpMethod.equals("POST")){
			return analyzePostRequest(req);
		}
		if(httpMethod.equals("DELETE")){
			return analyzeDeleteRequest(req);
		}
		return null;
	}

	private String analyzePostRequest(HttpServletRequest req){
		SlashObject so = new SlashObject(req.getPathInfo());
		List<String> paths = so.getList();
		if(paths == null || paths.size() < 2 || !paths.get(0).equals(DNS53Constants.DNS53VERSION)
				|| !paths.get(1).equals("hostedzone")){
			return null;
		}
		if(paths.size() == 2){
			return DNS53Constants.CREATEHOSTEDZONE;
		}
		if(paths.size() == 4){
			if(!paths.get(3).equals(DNS53Constants.RRSET)){
				//TODO throw request malformed? 
			}
			return DNS53Constants.CHANGERESOURCERECORDSETS;
		}
		return null;
	}
	
	private String analyzeDeleteRequest(HttpServletRequest req){
		return DNS53Constants.DELETEHOSTEDZONE;
	}
	
	private String analyzeGetRequest(HttpServletRequest req){
		SlashObject so = new SlashObject(req.getPathInfo());
		List<String> paths = so.getList();
		if(paths == null || paths.size() < 2 || !paths.get(0).equals(DNS53Constants.DNS53VERSION)){
			return null;
		}
		if(paths.get(1).equals("hostedzone")){
			if(paths.size() == 2){
				return DNS53Constants.LISTHOSTEDZONES;
			}
			if(paths.size() == 3){
				return DNS53Constants.GETHOSTEDZONE;
			}
			if(paths.size() == 4){
				return DNS53Constants.LISTRESOURCERECORDSETS;
			}
		}
		if(paths.get(1).equals("change")){
			return DNS53Constants.GETCHANGE;
		}
		return null;
	}
}
