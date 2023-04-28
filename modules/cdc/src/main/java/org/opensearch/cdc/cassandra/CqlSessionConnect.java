///*
// * SPDX-License-Identifier: Apache-2.0
// *
// * The OpenSearch Contributors require contributions made to
// * this file be licensed under the Apache-2.0 license or a
// * compatible open source license.
// */
//
//package org.opensearch.cdc.cassandra;
//
//import com.datastax.oss.driver.api.core.CqlSession;
//import com.datastax.oss.driver.api.core.cql.ResultSet;
//import com.datastax.oss.driver.api.core.cql.Row;
//
//import java.net.InetSocketAddress;
//
//public class CqlSessionConnect {
//
//    public static void main(String[] args) {
//
//        CqlSessionConnect client = new CqlSessionConnect();
//
//        try {
//            client.connect();
//            client.createSchema();
//            client.loadData();
//            client.querySchema();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            client.close();
//        }
//    }
//
//    private CqlSession session;
//
//    /** Initiates a connection to the session specified by the application.conf. */
//    public void connect() {
//
//        session = CqlSession.builder().addContactPoint(InetSocketAddress.createUnresolved("127.0.0.1",9042)).build();
//
//        System.out.printf("Connected session: %s%n", session.getName());
//    }
//
//    /** Creates the schema (keyspace) and tables for this example. */
//    public void createSchema() {
//
//        session.execute(
//            "CREATE KEYSPACE IF NOT EXISTS simplex WITH replication "
//                + "= {'class':'SimpleStrategy', 'replication_factor':1};");
//
//        session.execute(
//            "CREATE TABLE IF NOT EXISTS simplex.songs ("
//                + "id uuid PRIMARY KEY,"
//                + "title text,"
//                + "album text,"
//                + "artist text,"
//                + "tags set<text>,"
//                + "data blob"
//                + ");");
//
//        session.execute(
//            "CREATE TABLE IF NOT EXISTS simplex.playlists ("
//                + "id uuid,"
//                + "title text,"
//                + "album text, "
//                + "artist text,"
//                + "song_id uuid,"
//                + "PRIMARY KEY (id, title, album, artist)"
//                + ");");
//    }
//
//    /** Inserts data into the tables. */
//    public void loadData() {
//
//        session.execute(
//            "INSERT INTO simplex.songs (id, title, album, artist, tags) "
//                + "VALUES ("
//                + "756716f7-2e54-4715-9f00-91dcbea6cf50,"
//                + "'La Petite Tonkinoise',"
//                + "'Bye Bye Blackbird',"
//                + "'Joséphine Baker',"
//                + "{'jazz', '2013'})"
//                + ";");
//
//        session.execute(
//            "INSERT INTO simplex.playlists (id, song_id, title, album, artist) "
//                + "VALUES ("
//                + "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d,"
//                + "756716f7-2e54-4715-9f00-91dcbea6cf50,"
//                + "'La Petite Tonkinoise',"
//                + "'Bye Bye Blackbird',"
//                + "'Joséphine Baker'"
//                + ");");
//    }
//
//    /** Queries and displays data. */
//    public void querySchema() {
//
//        ResultSet results =
//            session.execute(
//                "SELECT * FROM simplex.playlists "
//                    + "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;");
//
//        System.out.printf("%-30s\t%-20s\t%-20s%n", "title", "album", "artist");
//        System.out.println(
//            "-------------------------------+-----------------------+--------------------");
//
//        for (Row row : results) {
//
//            System.out.printf(
//                "%-30s\t%-20s\t%-20s%n",
//                row.getString("title"), row.getString("album"), row.getString("artist"));
//        }
//    }
//
//    /** Closes the session. */
//    public void close() {
//        if (session != null) {
//            session.close();
//        }
//    }
//
//}
