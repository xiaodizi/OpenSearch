/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.apache.ruitu;


public class Cassandra {


    public Cassandra() {

    }

    public static void active() {

        System.setProperty("log4j2.debug", "true");
        String cassandraHome = System.getProperty("opensearch.path.home");
        String cassandraConfig=System.getProperty("opensearch.path.conf");
        System.setProperty("cassandra.config", "file://"+cassandraConfig+"/cassandra.yaml");
        System.setProperty("cassandra.storagedir", System.getProperty("opensearch.data.path"));
        System.setProperty("cassandra.home",cassandraHome);
        System.setProperty("cassandra.logdir",System.getProperty("opensearch.logs.base_path"));

        try {
            org.apache.cassandra.service.CassandraDaemon daemon = new org.apache.cassandra.service.CassandraDaemon();
            daemon.activate();
        } catch (Exception e) {
            System.out.println("打印个错误吧！");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            active();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
