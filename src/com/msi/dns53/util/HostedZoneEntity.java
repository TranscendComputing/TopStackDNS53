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
package com.msi.dns53.util;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msi.dns53.model.DNS53HostedZone;

public class HostedZoneEntity {
	private static String thisClass = "com.msi.dns53.util.HostedZoneEntity";
	final static Logger logger = LoggerFactory.getLogger(thisClass);

	@SuppressWarnings("unchecked")
	public static DNS53HostedZone selectHostedZone(final Session sess,
			final String zoneId, final String zoneName) {
		if(zoneId == null && zoneName == null){
			return null;
		}
		String sql = "FROM DNS53HostedZone WHERE";
		boolean flag = false;
		if(zoneId != null){
			sql += " zoneId = '" + zoneId + "'";
			flag = true;
		}
		
		if(zoneName != null && !flag){
			sql += " name = '" + zoneName + "'";
		}else if(zoneName != null && flag){
			sql += " AND name = '" + zoneName + "'";
		}
		
		logger.debug("SelectHostedZone query: " + sql);
		
		final Query query = sess.createQuery(sql);
		final List<DNS53HostedZone> l = query.list();
		if (l == null || l.size() != 1) {
			return null;
		}
		return l.get(0);
	}
}
