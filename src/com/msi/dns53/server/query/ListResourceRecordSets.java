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
package com.msi.dns53.server.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.slf4j.Logger;

import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.generationjava.io.xml.XMLNode;
import com.msi.dns53.model.DNS53ResourceRecord;
import com.msi.dns53.server.AccessMySQL;
import com.msi.dns53.server.DNS53Constants;
import com.msi.dns53.server.DNS53Faults;
import com.msi.dns53.server.RequestHandler;
import com.msi.tough.core.Appctx;
import com.msi.tough.core.SlashObject;
import com.msi.tough.query.AbstractHeaderAction;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.query.MarshallStruct;
import com.msi.tough.query.QueryUtil;

public class ListResourceRecordSets extends
        AbstractHeaderAction<ListResourceRecordSetsResult> {
    private final static Logger logger = Appctx
            .getLogger(ListResourceRecordSets.class.getName());

    @Override
    public String marshall(MarshallStruct<ListResourceRecordSetsResult> input,
            HttpServletResponse resp) throws Exception {
        ListResourceRecordSetsResult result = input.getMainObject();

        XMLNode response = new XMLNode(
                DNS53Constants.LISTRESOURCERECORDSETSRESPONSE);
        response.addAttr(DNS53Constants.XMLNS, DNS53Constants.XMLNS_VALUE);
        if (result.getResourceRecordSets() != null) {
            XMLNode rrSet = QueryUtil.addNode(response,
                    DNS53Constants.RESOURCERECORDSETS);
            for (ResourceRecordSet rrs : result.getResourceRecordSets()) {
                XMLNode rr = QueryUtil.addNode(rrSet,
                        DNS53Constants.RESOURCERECORDSET);
                QueryUtil.addNode(rr, DNS53Constants.NAME, rrs.getName());
                QueryUtil.addNode(rr, DNS53Constants.TYPE, rrs.getType());
                QueryUtil.addNode(rr, DNS53Constants.TTL, rrs.getTTL());
                if (rrs.getSetIdentifier() != null) {
                    QueryUtil.addNode(rr, DNS53Constants.SETIDENTIFIER,
                            rrs.getSetIdentifier());
                }
                if (rrs.getWeight() != null && rrs.getWeight() != -1) {
                    QueryUtil.addNode(rr, DNS53Constants.WEIGHT,
                            rrs.getWeight());
                }
                if (rrs.getResourceRecords() != null
                        && rrs.getResourceRecords().size() > 0) {
                    XMLNode records = QueryUtil.addNode(rr,
                            DNS53Constants.RESOURCERECORDS);
                    for (ResourceRecord record : rrs.getResourceRecords()) {
                        XMLNode r = QueryUtil.addNode(records,
                                DNS53Constants.RESOURCERECORD);
                        QueryUtil.addNode(r, DNS53Constants.VALUE,
                                record.getValue());
                    }
                }
            }
        }
        QueryUtil.addNode(response, DNS53Constants.ISTRUNCATED,
                result.getIsTruncated());
        QueryUtil.addNode(response, DNS53Constants.MAXITEMS,
                result.getMaxItems());
        if (result.getIsTruncated() && result.getNextRecordName() != null) {
            QueryUtil.addNode(response, DNS53Constants.NEXTRECORDNAME,
                    result.getNextRecordName());
        }
        if (result.getIsTruncated() && result.getNextRecordType() != null) {
            QueryUtil.addNode(response, DNS53Constants.NEXTRECORDTYPE,
                    result.getNextRecordType());
        }
        if (result.getIsTruncated() && result.getNextRecordIdentifier() != null) {
            QueryUtil.addNode(response, DNS53Constants.NEXTRECORDIDENTIFIER,
                    result.getNextRecordIdentifier());
        }
        return response.toString();
    }

    public ListResourceRecordSetsRequest unmarshall(HttpServletRequest req) {
        ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();
        SlashObject so = new SlashObject(req.getPathInfo());
        List<String> paths = so.getList();
        if (paths.size() != 4) {
            throw DNS53Faults.InternalError();
        }
        String zoneId = paths.get(2);
        request.setHostedZoneId(zoneId);

        Map<String, String[]> map = req.getParameterMap();
        String name = QueryUtil.getString(map, "name");
        String type = QueryUtil.getString(map, "type");
        String identifier = QueryUtil.getString(map, "identifier");
        String maxItems = QueryUtil.getString(map, "maxitems");

        request.setStartRecordName(name);
        request.setStartRecordType(type);
        request.setStartRecordIdentifier(identifier);
        request.setMaxItems(maxItems);
        return request;
    }

    @Override
    public ListResourceRecordSetsResult process0(Session session,
            HttpServletRequest req, HttpServletResponse resp,
            Map<String, String[]> map) throws Exception {
        ListResourceRecordSetsRequest request = unmarshall(req);
        return listResourceRecordSets(session, request);
    }

    private ListResourceRecordSetsResult listResourceRecordSets(Session sess,
            ListResourceRecordSetsRequest request) throws ErrorResponse {
        ListResourceRecordSetsResult result = new ListResourceRecordSetsResult();
        AccessMySQL sqlaccess = AccessMySQL.getInstance();

        String tableName = sqlaccess.getTableName(request.getHostedZoneId(),
                this.getAccountId());
        if (tableName.equals("FAILED")) {
            throw DNS53Faults.NoSuchHostedZone(request.getHostedZoneId());
        }
        if (request.getStartRecordType() != null
                && request.getStartRecordName() == null) {
            throw DNS53Faults.InvalidInput();
        }
        logger.debug("Table " + tableName
                + " is found for hosted zone with id, "
                + request.getHostedZoneId() + ".");

        String zoneName = sqlaccess.getZoneName(request.getHostedZoneId());
        logger.debug("Target hosted zone: " + zoneName);

        RequestHandler.checkType(request.getStartRecordType());
        List<DNS53ResourceRecord> records = sqlaccess.listResourceRecords(sess,
                request.getHostedZoneId(), request.getStartRecordName(), null,
                request.getStartRecordType(), null, -1, -1);
        logger.debug("There are " + records.size()
                + " resource record sets in this hosted zone.");

        int maxItems = -1;
        if (request.getMaxItems() != null) {
            try {
                maxItems = Integer.valueOf(request.getMaxItems());
            } catch (NumberFormatException e) {
                throw DNS53Faults
                        .InvalidArgument("maxitems parameter must be an integer between 1 and 100.");
            }
        }
        if (maxItems == -1) {
            maxItems = 100; // default
        }
        boolean truncated = false;
        if (maxItems < records.size()) {
            truncated = true;
        }

        Collection<ResourceRecordSet> resourceRecordSets = new LinkedList<ResourceRecordSet>();
        /*
         * ResourceRecordSet ns = new ResourceRecordSet(); ns.setName(zoneName);
         * ns.setType("NS"); Collection<ResourceRecord> nsRecords = new
         * LinkedList<ResourceRecord>();
         */

        int i = 0;
        while (i < records.size() && i < maxItems) {
            DNS53ResourceRecord rec = records.get(i);
            ResourceRecordSet temp = new ResourceRecordSet();
            temp.setName(rec.getName());
            temp.setType(rec.getRdtype());
            temp.setTTL(Long.valueOf(rec.getTtl()));
            if (rec.getSid() != null) {
                temp.setSetIdentifier(rec.getSid());
            }
            if (rec.getWeight() != -1) {
                temp.setWeight(rec.getWeight());
            }
            Collection<ResourceRecord> resourceRecords = new LinkedList<ResourceRecord>();
            resourceRecords.add(new ResourceRecord().withValue(rec.getRdata()));
            temp.setResourceRecords(resourceRecords);
            resourceRecordSets.add(temp);

            ++i;
        }

        // now reduce the duplicates
        TreeMap<String, ResourceRecordSet> map = new TreeMap<String, ResourceRecordSet>();
        for (ResourceRecordSet rs : resourceRecordSets) {
            String key = rs.getName() + "::" + rs.getType();
            ResourceRecordSet temp = map.get(key);
            if (temp == null) {
                map.put(key, rs);
            } else {
                temp.getResourceRecords().add(
                        new ResourceRecord(rs.getResourceRecords().get(0)
                                .getValue()));
            }
        }

        resourceRecordSets = map.values();

        result.setResourceRecordSets(resourceRecordSets);
        result.setIsTruncated(truncated);
        if (truncated) {
            result.setNextRecordName("UNIMPLEMENTED");
            result.setNextRecordType("UNIMPLEMENTED");
        }
        result.setMaxItems(String.valueOf(maxItems));

        return result;
    }
}
