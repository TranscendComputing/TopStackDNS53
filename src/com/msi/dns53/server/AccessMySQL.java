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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.hibernate.Session;
import org.slf4j.Logger;

import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.HostedZoneConfig;
import com.amazonaws.services.route53.model.HostedZoneNotEmptyException;
import com.amazonaws.services.route53.model.InvalidChangeBatchException;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.sns.model.InternalErrorException;
import com.msi.dns53.model.DNS53HostedZone;
import com.msi.dns53.model.DNS53ResourceRecord;
import com.msi.dns53.util.ResourceRecordEntity;
import com.msi.tough.core.Appctx;
import com.msi.tough.query.ErrorResponse;
import com.msi.tough.utils.ConfigurationUtil;

public class AccessMySQL implements AccessDB {
    private final static Logger logger = Appctx.getLogger(AccessMySQL.class
            .getName());
    private Connection sqlConnection;
    private static AccessMySQL access;

    /**
     * Constructor for AccessMySQL
     *
     * @param userName
     *            username used to access MySQL database
     * @param password
     *            password used to access MySQL database
     * @param jdbctarget
     * @throws InternalErrorException
     */
    private AccessMySQL(String userName, String password, String jdbctarget)
            throws ErrorResponse {
        this.loadDriver();
        this.sqlConnection = initConnection(userName, password, jdbctarget);
    }

    public static AccessMySQL getInstance() throws InternalErrorException {
        if (access == null) {
            String userName = (String) Appctx.getBean("DB_USERNAME");
            String password = (String) Appctx.getBean("DB_PASSWORD");
            String jdbctarget = (String) Appctx.getBean("BIND_DB_URL");
            logger.debug("Initializing a new connection with JDBC = "
                    + "{ Username : " + userName + ", Password : " + password
                    + ", URL : " + jdbctarget + " }");

            access = new AccessMySQL(userName, password, jdbctarget);
        }
        return access;
    }

    public static AccessMySQL initialize(String userName, String password,
            String jdbctarget) throws InternalErrorException {
        logger.debug("Initializing a new connection with JDBC = "
                + "{ Username : " + userName + ", Password : " + password
                + ", URL : " + jdbctarget + " }");
        access = new AccessMySQL(userName, password, jdbctarget);
        return access;
    }

    /**
     * @return Connection object used to establish the connection with mysql
     *         server
     */
    public Connection getConnection() {
        return this.sqlConnection;
    }

    /**
     * Load the jdbc driver for MySQL server
     *
     * @throws InternalErrorException
     */
    @Override
    public void loadDriver() throws ErrorResponse {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
    }

    /**
     * @param userName
     *            username used to access MySQL database
     * @param password
     *            password used to access MySQL database
     * @param jdbctarget
     * @return Connection object with established connection to MySQL server
     * @throws InternalErrorException
     */
    private Connection initConnection(String userName, String password,
            String jdbctarget) throws ErrorResponse {
        Connection conn = null;
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            if (password != null || password != "") {
                connectionProps.put("password", password);
            }
            conn = DriverManager.getConnection(jdbctarget, connectionProps); // TODO
                                                                             // make
                                                                             // this
                                                                             // installation
                                                                             // dependent
        } catch (SQLException e) {
            logger.debug("Failed to establish a connection.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return conn;
    }

    /**
     * Close the connection used by AccessMySQL object
     *
     * @throws InternalErrorException
     */
    @Override
    public void closeConnection() throws ErrorResponse {
        try {
            this.sqlConnection.close();
            // logger.debug("Connection closed succesfully.");
        } catch (SQLException e) {
            // logger.debug("Failed to close the connection.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
    }

    /**
     * Populate the new hosted zone data into MySQL database
     *
     * @param domainName
     *            name of the hosted zone
     * @param callerRef
     *            unique caller reference for this request
     * @param comment
     *            user's comment for this request
     * @return ID of the new hosted zone (automatically issued);
     *         "DUPLICATE_NAME" is returned if target name already exists
     * @throws InternalErrorException
     */
    @Override
    public List<String> createHostedZone(final Session session,
            String domainName, String callerRef, String comment, long acid)
            throws ErrorResponse {
        List<String> result = new LinkedList<String>();
        // String tableName = acid + callerRef;
        String tableName = "B"
                + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        if (callerReferenceIsUsed(callerRef, acid)) {
            result.add("DUPLICATE_REFERENCE");
            return result; // response: signal that the name is already in use
        }
        String domainNameDot = domainName;
        if (domainName.charAt(domainName.length() - 1) != '.') {
            domainNameDot += ".";
        } else {
            domainName = domainName.substring(0, domainName.length() - 1);
        }

        if (domainExists(domainNameDot, acid)) {
            result.add("DUPLICATE_NAME");
            return result;
        }
        String query = "CREATE TABLE `" + tableName + "` ("
                + "		  name varchar(255) default NULL,"
                + "		  ttl int(11) default NULL,"
                + "		  rdtype varchar(255) default NULL,"
                + "		  rdata varchar(255) default NULL"
                + "		) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err
                    .println("Failed to create a new schema in MySQL database.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        // insert this new ID-name mapping entry including caller reference and
        // comment
        String uniqueID = "Z"
                + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        final DNS53HostedZone zone = new DNS53HostedZone();
        zone.setId(uniqueID);
        zone.setName(domainNameDot);
        zone.setCallerRefernce(callerRef);
        zone.setComment(comment);
        zone.setAccount(acid);
        zone.setTableName(tableName);
        session.save(zone);

        addRecord(tableName, domainName, 259200, "SOA", domainName
                + ". hostmaster." + domainName
                + ". 200309181 28800 7200 86400 28800");
        DNS53ResourceRecord recordSOA = new DNS53ResourceRecord();
        recordSOA.setName(domainNameDot);
        recordSOA.setTtl(259200);
        recordSOA.setRdata(domainName + ". hostmaster." + domainName
                + ". 200309181 28800 7200 86400 28800");
        recordSOA.setRdtype("SOA");
        recordSOA.setZoneId(uniqueID);
        recordSOA.setZoneName(domainNameDot);
        session.save(recordSOA);

        addRecord(tableName, domainName, 259200, "NS", "ns0." + domainName
                + ".");
        DNS53ResourceRecord recordNS0 = new DNS53ResourceRecord();
        recordNS0.setName(domainNameDot);
        recordNS0.setTtl(259200);
        recordNS0.setRdata("ns0." + domainName + ".");
        recordNS0.setRdtype("NS");
        recordNS0.setZoneId(uniqueID);
        recordNS0.setZoneName(domainNameDot);
        session.save(recordNS0);

        addRecord(tableName, domainName, 259200, "NS", "ns1." + domainName
                + ".");
        DNS53ResourceRecord recordNS1 = new DNS53ResourceRecord();
        recordNS1.setName(domainNameDot);
        recordNS1.setTtl(259200);
        recordNS1.setRdata("ns1." + domainName + ".");
        recordNS1.setRdtype("NS");
        recordNS1.setZoneId(uniqueID);
        recordNS1.setZoneName(domainNameDot);
        session.save(recordNS1);

        final String nameserverIp = (String) ConfigurationUtil
                .getConfiguration(Arrays.asList(new String[] { "DNS_IP" }));
        String ns0name = "ns0." + domainName;
        String ns1name = "ns1." + domainName;
        String ns0nameDot = "ns0." + domainNameDot;
        String ns1nameDot = "ns1." + domainNameDot;
        addRecord(tableName, ns0name, 259200, "A", nameserverIp);
        DNS53ResourceRecord recordA0 = new DNS53ResourceRecord();
        recordA0.setName(ns0nameDot);
        recordA0.setTtl(259200);
        recordA0.setRdata(nameserverIp);
        recordA0.setRdtype("A");
        recordA0.setZoneId(uniqueID);
        recordA0.setZoneName(domainNameDot);
        session.save(recordA0);

        addRecord(tableName, ns1name, 259200, "A", nameserverIp);
        DNS53ResourceRecord recordA1 = new DNS53ResourceRecord();
        recordA1.setName(ns1nameDot);
        recordA1.setTtl(259200);
        recordA1.setRdata(nameserverIp);
        recordA1.setRdtype("A");
        recordA1.setZoneId(uniqueID);
        recordA1.setZoneName(domainNameDot);
        session.save(recordA1);

        result.add(uniqueID);
        result.add(tableName);
        result.add(ns0name);
        result.add(ns1name);

        session.save(zone);

        return result;
    }

    /**
     * Check if the target name is already being used for another hosted zone
     *
     * @param tableName
     *            name of the table to be checked
     * @return true if name exists; false otherwise
     * @throws InternalErrorException
     */
    private boolean callerReferenceIsUsed(String callerRef, long acId)
            throws ErrorResponse {
        String query = "SELECT name FROM msi.zones WHERE accountId = " + acId
                + " AND callerReference = \'" + callerRef + "\';";
        boolean result = false;
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            result = rs.next();
        } catch (SQLException e) {
            System.err
                    .println("Failed while checking if the target name already exists.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return result;
    }

    private boolean domainExists(String zoneName, long acId)
            throws ErrorResponse {
        String query = "SELECT name FROM msi.zones WHERE accountId = " + acId
                + " AND name = \'" + zoneName + "\';";
        boolean result = false;
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            result = rs.next();
        } catch (SQLException e) {
            System.err
                    .println("Failed while checking if the target name already exists.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return result;
    }

    /**
     * Returns matching table name for given zone ID
     *
     * @param zoneID
     *            ID of the target hosted zone
     * @return table name of the target hosted zone
     * @throws InternalErrorException
     */
    @Override
    public String getTableName(String zoneID, long accId) throws ErrorResponse {
        String tname = "FAILED";
        String query = "SELECT tableName FROM msi.zones WHERE ID = \'" + zoneID
                + "\' AND accountId = " + accId + ";";
        logger.debug("Retrieving the table name with the query: " + query);
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int counter = 0;
            while (rs.next()) {
                tname = rs.getString("tableName");
                ++counter;
            }
            logger.debug("Counter = " + counter
                    + "; the value should always be 1.");
            if (counter == 0) {
                throw DNS53Faults.NoSuchHostedZone(zoneID);
            }
            if (counter > 1) {
                throw DNS53Faults.InternalError();
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return tname;
    }

    public boolean tableExists(String tableName) {
        boolean result = false;
        String query = "SELECT * FROM `" + tableName + "`;";
        Statement stmt;
        try {
            stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            return false;
        }
        return result;
    }

    public String getCallerReference(String zoneID, long accId)
            throws ErrorResponse {
        String tname = "FAILED";
        String query = "SELECT callerReference FROM msi.zones WHERE ID = \'"
                + zoneID + "\' AND accountId = " + accId + ";";
        logger.debug("Retrieving the table name with the query: " + query);
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                tname = rs.getString("callerReference");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return tname;
    }

    @Override
    public String getZoneName(String zoneId) throws ErrorResponse {
        String zname = "FAILED";
        String query = "SELECT name FROM msi.zones WHERE ID = \'" + zoneId
                + "\';";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                zname = rs.getString("name");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return zname;
    }

    @Override
    public String getZoneId(String zoneName) throws ErrorResponse {
        String zid = "FAILED";
        String query = "SELECT id FROM msi.zones WHERE name = \'" + zoneName
                + "\';";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                zid = rs.getString("id");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return zid;
    }

    @Override
    public List<DNS53HostedZone> getAllHostedZones() {
        List<DNS53HostedZone> result = new LinkedList<DNS53HostedZone>();

        String query = "SELECT * FROM msi.zones;";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                DNS53HostedZone temp = new DNS53HostedZone();
                temp.setId(rs.getString("ID"));
                temp.setAccount(rs.getLong("accountId"));
                temp.setCallerRefernce(rs.getString("callerReference"));
                temp.setComment(rs.getString("comment"));
                temp.setName(rs.getString("name"));
                temp.setTableName(rs.getString("tableName"));
                result.add(temp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void addResourceRecord(String zoneID, String name, long ttl,
            String rdtype, String rdata, long accId) throws ErrorResponse {
        String tableName = getTableName(zoneID, accId);
        addRecord(tableName, name, ttl, rdtype, rdata);
    }

    @Override
    public void addRecord(String tableName, String name, long ttl,
            String rdtype, String rdata) throws ErrorResponse {
        name = "\'" + name + "\'";
        rdtype = "\'" + rdtype + "\'";
        rdata = "\'" + rdata + "\'";
        String query = "INSERT INTO `" + tableName + "` VALUES (" + name + ", "
                + ttl + ", " + rdtype + ", " + rdata + ");";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err
                    .println("Failed to insert a resource record into database.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
    }

    /**
     * @throws InternalErrorException
     *
     */
    @Override
    public void deleteResourceRecord(String zoneID, String name, long ttl,
            String rdtype, String rdata, long accId) throws ErrorResponse {
        String tableName = getTableName(zoneID, accId);
        deleteRecord(tableName, name, ttl, rdtype, rdata);
    }

    /**
     * @throws InvalidChangeBatchException
     *
     */
    @Override
    public void deleteRecord(String tableName, String name, long ttl,
            String rdtype, String rdata) throws ErrorResponse {
        name = "\'" + name + "\'";
        rdtype = "\'" + rdtype + "\'";
        rdata = "\'" + rdata + "\'";
        String query = "DELETE FROM `" + tableName + "` where name = " + name
                + " AND ttl = " + ttl + " AND rdtype = " + rdtype
                + " AND rdata = " + rdata + ";";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
    }

    @Override
    public List<DNS53ResourceRecord> listResourceRecords(Session sess,
            String zoneId, String name, String value, String type, String sid,
            long ttl, long weight) throws ErrorResponse {
        List<DNS53ResourceRecord> records = new LinkedList<DNS53ResourceRecord>();

        String zoneName = getZoneName(zoneId);
        if (zoneName.equals("FAILED")) {
            throw DNS53Faults.NoSuchHostedZone(zoneId);
        }

        String query = "SELECT * FROM `msi`.`rrSet` WHERE zoneName = \'"
                + zoneName + "\'";
        if (type != null && !type.equals("")) {
            type = "\'" + type + "\'";
            query += " AND rdtype = " + type;
        }
        if (name != null && !name.equals("")) {
            String nameDot = name;
            if (name.charAt(name.length() - 1) != '.') {
                nameDot += ".";
            } else {
                name = name.substring(0, name.length() - 1);
            }
            name = "\'" + nameDot + "\'";
            query += " AND name = " + name;
        }
        if (value != null && !value.equals("")) {
            value = "\'" + value + "\'";
            query += " AND rdata = " + value;
        }
        if (sid != null && !sid.equals("")) {
            sid = "\'" + sid + "\'";
            query += " AND sid = " + sid;
        }
        if (ttl != -1) {
            query += " AND ttl = " + ttl;
        }
        if (weight != -1) {
            query += " AND weight = " + weight;
        }
        query += ";";
        // now query is created
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                DNS53ResourceRecord temp = new DNS53ResourceRecord();
                temp.setName(rs.getString("name"));
                temp.setTtl(rs.getLong("ttl"));
                temp.setRdtype(rs.getString("rdtype"));
                temp.setRdata(rs.getString("rdata"));
                temp.setSid(rs.getString("sid"));
                temp.setWeight(rs.getLong("weight"));
                records.add(temp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw DNS53Faults.InternalError();
        }
        return records;
    }

    public List<DNS53ResourceRecord> listResourceRecords(String tableName,
            String type, String name, String rdata) throws ErrorResponse {
        List<DNS53ResourceRecord> records = new LinkedList<DNS53ResourceRecord>();
        boolean whereFlag = false;
        if (tableName == null) {
            // TODO throw InternalError
        }
        String query = "select * from `" + tableName + "`";
        if (type != null && !type.equals("")) {
            type = "\'" + type + "\'";
            if (whereFlag) {
                // probably never reached:
                query += " AND rdtype = " + type;
            } else {
                query += " WHERE rdtype = " + type;
                whereFlag = true;
            }
        }
        if (name != null && !name.equals("")) {
            name = "\'" + name + "\'";
            if (whereFlag) {
                query += " AND name = " + name;
            } else {
                query += " WHERE name = " + name;
                whereFlag = true;
            }
        }
        if (rdata != null && !rdata.equals("")) {
            rdata = "\'" + rdata + "\'";
            if (whereFlag) {
                query += " AND rdata = " + rdata;
            } else {
                query += " WHERE rdata = " + rdata;
                whereFlag = true;
            }
        }
        query += ";";
        // now query is created
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                DNS53ResourceRecord temp = new DNS53ResourceRecord();
                temp.setName(rs.getString("name"));
                temp.setTtl(rs.getLong("ttl"));
                temp.setRdtype(rs.getString("rdtype"));
                temp.setRdata(rs.getString("rdata"));
                records.add(temp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw DNS53Faults.InternalError();
        }
        return records;
    }

    /**
     * Returns ID, name, caller reference, and comment for the target hosted
     * zone
     *
     * @param zoneID
     *            ID of the target hosted zone
     * @return String[] with ID, name, caller reference, and comment of the
     *         target hosted zone
     * @throws InternalErrorException
     */
    @Override
    public String[] getHostedZone(String zoneID) throws ErrorResponse {
        String[] result = new String[4];
        result[0] = zoneID;
        try {
            String query = "SELECT * FROM msi.zones WHERE ID = \'" + zoneID
                    + "\';";
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                result[1] = rs.getString("name");
                result[2] = rs.getString("callerReference");
                result[3] = rs.getString("comment");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        return result;
    }

    /**
     * @throws HostedZoneNotEmptyException
     *
     */
    public void deleteHostedZone(String zoneId, String tableName,
            String callerRef) throws ErrorResponse {
        String zoneName = getZoneName(zoneId);

        // check to see if the table is empty or not
        boolean empty = true;
        String chkquery = "SELECT * FROM `" + tableName
                + "` WHERE NOT rdtype = \'SOA\' AND NOT rdtype = \'NS\';";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(chkquery);
            if (rs.next()) {
                // TODO handling special cases are poor and hardcoded since
                // there's not enough time; fix the snippet below later
                String name = rs.getString("name");
                String rdtype = rs.getString("rdtype");
                if (!rdtype.equals("A")) {
                    empty = false;
                } else if (!(name.equals("ns0." + zoneName) || name
                        .equals("ns1." + zoneName))) {
                    empty = false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete a hosted zone.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        if (!empty) {
            // TODO right now "empty" variable is not being calculated correctly
            // since A records are added for CreateHostedZone as well
            // TODO modify the snippet to verify whether zone is empty or not;
            // implement HostedZoneNotEmptyException in DNS53Faults and throw it
            // throw new
            // HostedZoneNotEmptyException(UUID.randomUUID().toString()); //just
            // put some bogus UUID unless RequestID becomes important this layer
        }
        // delete
        String query = "DROP TABLE `" + tableName + "`;";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Failed to delete a hosted zone.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        query = "DELETE FROM msi.zones WHERE callerReference = \'" + callerRef
                + "\';";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err
                    .println("Failed to delete the mapping information of a deleted hosted zone.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
        query = "DELETE FROM msi.rrSet WHERE zoneId = \'" + zoneId + "\';";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err
                    .println("Failed to delete the mapping information of a deleted hosted zone.");
            e.printStackTrace();
            throw DNS53Faults.InternalError(); // just put some bogus UUID
                                               // unless RequestID becomes
                                               // important this layer
        }
    }

    /**
     * Returns hashmap of <KEY: zoneID, VALUE: String[] of ID, name, caller
     * reference, and comment>
     *
     * @param marker_tableName
     *            table name of the marker
     * @param maxItems
     *            number of items returned when the actual number of list
     *            exceeds maxItems
     * @return Hashmap of <KEY: zoneID, VALUE: String[] of ID, name, caller
     *         reference, and comment>
     * @throws InternalErrorException
     */
    @Override
    public ListHostedZonesResult listHostedZones(String marker, int maxItems,
            long accId) throws ErrorResponse {
        ListHostedZonesResult result = new ListHostedZonesResult();
        Collection<HostedZone> hostedZones = new LinkedList<HostedZone>();
        int lim = maxItems;
        try {
            ResultSet rs = null;
            String query = null;
            Statement stmt = this.sqlConnection.createStatement();
            if (marker == null) {
                logger.debug("No marker is given.");
                query = "SELECT * FROM msi.zones WHERE accountId = " + accId
                        + ";";

            } else {
                logger.debug("Marker is assigned.");
                query = "SELECT * FROM msi.zones WHERE accountId = " + accId
                        + " AND ID >= \'" + marker + "\';";
            }

            rs = stmt.executeQuery(query);
            while (lim != 0 && rs.next()) {
                HostedZone hz = new HostedZone(rs.getString("ID"),
                        rs.getString("name"), rs.getString("callerReference"));
                HostedZoneConfig config = new HostedZoneConfig();
                config.setComment(rs.getString("comment"));
                hz.setConfig(config);
                --lim;
                hostedZones.add(hz);
            }

            if (marker != null && hostedZones.size() == 0) {
                // TODO throw an exception for marker not existing (test against
                // AWS to see which exception is being returned)
            }

            boolean truncated = rs.next();

            logger.debug("Relative Limit = " + lim + "; MaxItems = " + maxItems);
            logger.debug("Truncated = " + truncated);

            if (lim == 0 && truncated) {
                truncated = true;
            }

            result.setHostedZones(hostedZones);
            result.setMaxItems(String.valueOf(maxItems));
            result.setIsTruncated(truncated);
            if (truncated) {
                result.setNextMarker(rs.getString("ID"));
            }

        } catch (SQLException e) {
            System.err
                    .println("Failed to get zone informations for listHostedZone request.");
            e.printStackTrace();
            throw DNS53Faults.InternalError();
        }
        logger.debug("Returning " + hostedZones.size()
                + " hosted zones information.");
        return result;
    }

    @Override
    public String[] getChange(String changeID) throws ErrorResponse {
        String query = "SELECT * FROM msi.changes WHERE ID = \'" + changeID
                + "\';";
        String[] result = new String[4];
        try {
            Statement stmt = this.sqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) { // should be only one entry or none at all
                result[0] = rs.getString("ID");
                result[1] = rs.getString("status");
                result[2] = rs.getString("submit_time");
                result[3] = rs.getString("zone_table");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get domain name for the input ID.");
            e.printStackTrace();
            throw DNS53Faults.InternalError();
        }
        // logger.debug(result[0] + "; " + result[1] + "; " + result[2] + "; " +
        // result[3]);
        return result;
    }

    @Override
    public boolean pendingChangesExist() throws SQLException {
        boolean result = false;
        String query = "SELECT * FROM msi.changes WHERE status = 'PENDING';";
        Statement stmt = this.sqlConnection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
            result = true;
        }
        return result;
    }

    @Override
    public void updateChanges() throws SQLException {
        String query = "UPDATE msi.changes SET status = 'INSYNC';";
        Statement stmt = this.sqlConnection.createStatement();
        stmt.executeUpdate(query);
    }

    @Override
    public void addChangeRecord(String changeID, String status,
            String submitTime, String tableName, String request)
            throws ErrorResponse {
        changeID = "\'" + changeID + "\'";
        status = "\'" + status + "\'";
        submitTime = "\'" + submitTime + "\'";
        tableName = "\'" + tableName + "\'";
        request = "\'" + request + "\'";
        String query = "INSERT INTO msi.changes VALUES (" + changeID + ", "
                + status + ", " + submitTime + ", " + tableName + ", "
                + request + ");";
        try {
            Statement stmt = this.sqlConnection.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
        } catch (SQLException e) {
            System.err
                    .println("Failed to insert a resource record into the database.");
            e.printStackTrace();
            throw DNS53Faults.InternalError();
        }
    }

    /*
     * @Override public boolean recordExists(String tableName, String name,
     * String rdtype) throws ErrorResponse { boolean result = false;
     * if("NS".equals(rdtype)){ //allow duplicate NS records with different
     * values return result; } String query = "SELECT * from `" + tableName +
     * "`"; boolean where = false; if(name != null){ where = true; name = "\'" +
     * name +"\'"; query += " WHERE name = " + name; } if(rdtype != null){
     * rdtype = "\'" + rdtype +"\'"; if(where){ query += " AND rdtype = " +
     * rdtype; }else{ query += " WHERE rdtype = " + rdtype; } } query += ";";
     * try { Statement stmt = this.sqlConnection.createStatement(); ResultSet rs
     * = stmt.executeQuery(query); if(rs.next()) { result = true; } }
     * catch(SQLException e){
     * System.err.println("Failed while checking if a record exists.");
     * e.printStackTrace(); throw DNS53Faults.InternalError(); //put some bogus
     * UUID for now until Request ID actually matters } return result; }
     */

    @Override
    public boolean recordExists(Session session, String zoneId, String name,
            String rdtype, String value) throws ErrorResponse {
        List<DNS53ResourceRecord> list = ResourceRecordEntity
                .selectResourceRecords(session, zoneId, name, value, rdtype,
                        null, null, null);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

}
