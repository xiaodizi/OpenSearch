/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.cassandra;

import org.apache.cassandra.service.CassandraDaemon;

public class NewCassandra {

    public static void main(String[] args) {
        System.setProperty("cassandra.config", "file:///Users/lei.fu/java/mca/gradle_demo/gradle_demo/src/main/resources/cassandra.yaml");
        System.setProperty("cassandra.storagedir", "/Users/lei.fu/data/");
        CassandraDaemon daemon=new CassandraDaemon();
        daemon.activate();
    }
}
