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

import com.msi.dns53.model.DNS53ResourceRecord;

public class ResourceRecordEntity {
	private static String thisClass = "com.msi.dns53.util.ResourceRecordEntity";
	final static Logger logger = LoggerFactory.getLogger(thisClass);
	
	@SuppressWarnings("unchecked")
	public static List<DNS53ResourceRecord> selectResourceRecords(final Session sess,
			final String zoneId, final String name, final String value, final String type,
			final String sid, final Long ttl, final Long weight) {
		String sql = "FROM DNS53ResourceRecord WHERE zoneId = '" + zoneId + "'";
		if(name != null){
			sql += " AND name = '" + name + "'";
		}
		if(value != null){
			sql += " AND rdata = '" + value + "'";
		}
		if(type != null){
			sql += " AND rdtype = '" + type + "'";
		}
		if(sid != null){
			sql += " AND sid = '" + sid + "'";
		}
		if(ttl != null){
			if(ttl < 0){
				sql += " AND ttl = '" + ttl + "'";	
			}
		}
		if(weight != null){
			if(weight < 0){
				sql += " AND weight = '" + weight + "'";	
			}
		}
		//sql += " ORDER BY rrSet_id";
		logger.debug("SelectResourceRecords query: " + sql);
		final Query query = sess.createQuery(sql);
		final List<DNS53ResourceRecord> l = query.list();
		if (l == null || l.size() == 0) {
			return null;
		}
		return l;
	}
}
