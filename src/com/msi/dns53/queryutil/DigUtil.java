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
package com.msi.dns53.queryutil;

import java.net.UnknownHostException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

public class DigUtil {
    // private final static Logger logger =
    // Appctx.getLogger(DigUtil.class.getName());

    public static Record[] dig(String name) throws TextParseException,
            UnknownHostException {
        // logger.debug("Requesting DNS query for " + name +
        // " against localhost.");
        Lookup l = new Lookup(name);
        l.setResolver(new SimpleResolver("localhost"));
        Record[] records = l.run();
        return records;
    }

    public static Record[] dig(String name, String resolver)
            throws TextParseException, UnknownHostException {
        // logger.debug("Requesting DNS query for " + name + " against " +
        // resolver + ".");
        Lookup l = new Lookup(name);
        l.setResolver(new SimpleResolver(resolver));
        Record[] records = l.run();
        return records;
    }
}
