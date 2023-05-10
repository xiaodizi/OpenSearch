/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opensearch.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.Map;

public class CassandraOperation {


    /**
     * 删除表
     */
    public static void dropTable(String tableName,CqlSession session) {
        long timeStart = System.currentTimeMillis();
        // 如果 "-" 在开头 或者 在结尾 ，或者 不存在 "-" 都不会向cassandra 创建表。
        // 需要 在"-" 两端都有，有效的字符
        if (tableName.indexOf("-") < 2 || tableName.indexOf("-") == (tableName.length() - 1)) {
            return;
        }
        String replace = tableName.replace("-", ".");
        tableName = replace;

        session.execute("drop table "+tableName+";");
        String keyspace=tableName.substring(0,tableName.indexOf("."));
        if (isNullKeyspace(tableName,keyspace,session)){
            session.execute("drop keyspace "+keyspace+";");
        }
        System.out.println("用时：" + (System.currentTimeMillis() - timeStart));
    }


    /**
     * 创建keyspace
     */
    public static void createKeyspace(
        String tableName, String replicationStrategy, int replicationFactor, CqlSession session) {
        long timeStart = System.currentTimeMillis();
        // 如果 "-" 在开头 或者 在结尾 ，或者 不存在 "-" 都不会向cassandra 创建表。
        // 需要 在"-" 两端都有，有效的字符
        if (tableName.indexOf("-") < 2 || tableName.indexOf("-") == (tableName.length() - 1)) {
            return;
        }
        String replace = tableName.replace("-", ".");
        tableName = replace;
        String keyspace = tableName.substring(0,tableName.indexOf("."));
        StringBuilder sb =
            new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                .append(keyspace).append(" WITH replication = {")
                .append("'class':'").append(replicationStrategy)
                .append("','replication_factor':").append(replicationFactor)
                .append("};");

        String query = sb.toString();
        System.out.println("创建 keyspace 语句："+query);
        session.execute(query);
        System.out.println("用时：" + (System.currentTimeMillis() - timeStart));
    }

    /**
     * 创建表 并写入数据
     */
    public static void createTables(String tableName, Map<String, Object> map, String dataId, CqlSession session) {
        long timeStart = System.currentTimeMillis();

        // 如果 "-" 在开头 或者 在结尾 ，或者 不存在 "-" 都不会向cassandra 创建表。
        // 需要 在"-" 两端都有，有效的字符
        if (tableName.indexOf("-") < 2 || tableName.indexOf("-") == (tableName.length() - 1)) {
            return;
        }
        String replace = tableName.replace("-", ".");
        tableName = replace;

        if (!isTableExits(tableName, session)) {
            StringBuilder createTableSb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            createTableSb.append("id text PRIMARY KEY,");
            int i = 0;
            for (Object key : map.keySet()) {
                String k = (String) key;
                createTableSb.append(k + " text");
                if (i != (map.size() - 1)) {
                    createTableSb.append(",");
                }
                i++;
            }
            createTableSb.append(");");
            String createTablecql = createTableSb.toString();
            session.execute(createTablecql);
        }

        try {
            map.put("id", dataId);
            String insertCql = CassandraUtil.getSql(tableName, "insert", map);
            session.execute(insertCql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("用时：" + (System.currentTimeMillis() - timeStart));
    }


    /**
     * 更新表数据
     */
    public static void updateTables(String tableName,Map<String,Object> map,String dataId,CqlSession session){
        long timeStart = System.currentTimeMillis();

        // 如果 "-" 在开头 或者 在结尾 ，或者 不存在 "-" 都不会向cassandra 创建表。
        // 需要 在"-" 两端都有，有效的字符
        if (tableName.indexOf("-") < 2 || tableName.indexOf("-") == (tableName.length() - 1)) {
            return;
        }
        String replace = tableName.replace("-", ".");
        tableName = replace;
        if (!isTableExits(tableName,session)){
            createTables(tableName,map,dataId,session);
        }
        try {
            String updateCql = CassandraUtil.getSql(tableName, "update", map);
            session.execute(updateCql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("用时：" + (System.currentTimeMillis() - timeStart));
    }


    /**
     * 删除表数据
     */
    public static void deleteById(String tableName, String dataId, CqlSession session) {
        // 如果 "-" 在开头 或者 在结尾 ，或者 不存在 "-" 都不会向cassandra 创建表。
        // 需要 在"-" 两端都有，有效的字符
        if (tableName.indexOf("-") < 2 || tableName.indexOf("-") == (tableName.length() - 1)) {
            return;
        }
        String replace = tableName.replace("-", ".");
        tableName = replace;
        session.execute("DELETE FROM " + tableName + " where id='" + dataId + "';");
    }


    /**
     * 判断表是否存在
     */
    private static boolean isTableExits(String tableName, CqlSession session) {
        String table = tableName.substring(tableName.indexOf(".") + 1);
        ResultSet resultSet = session.execute("select * from system_schema.columns where table_name='" + table + "' ALLOW FILTERING;");
        Row one = resultSet.one();
        if (one == null) {
            return false;
        }
        return true;
    }

    /**
     * 判断 keyspace 里的表是否全部删除了
     */
    private static boolean isNullKeyspace(String tableName,String keyspace,CqlSession session){
        ResultSet execute = session.execute("select * from "+tableName+" where keyspace_name='"+keyspace+"' ALLOW FILTERING;");
        Row one = execute.one();
        if (one != null){
            return false;
        }
        return true;
    }
}
