/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.cassandra;

import java.util.Iterator;
import java.util.Map;

public class CassandraUtil {

    /**
     * 拼接sql 语句
     */
    public static String getSql(String tableName, String operation, Map<?, ?> mapData) throws Exception {
        if (!(tableName != null && !tableName.equals("") && tableName.length() > 0)) {
            throw new Exception(" 参数 tableName 的值为空！");
        } else if (!(mapData != null && !mapData.equals("") && mapData.size() > 0)) {
            throw new Exception(" 参数 mapData 的值为空！");
        }
        // 操作标识 默认为 select
        String operations = "select";
        String condition = " a.* from " + tableName + " a where ";
        if (operation != null && !operation.equals("")) {
            if (operation.equals("update") || operation.equals("UPDATE")) {
                operations = "update";
                condition = " " + tableName + " a set ";
            } else if (operation.equals("delete") || operation.equals("DELETE")) {
                operations = "delete";
                condition = " from " + tableName + " a where ";
            } else if (operation.equals("insert") || operation.equals("INSERT")) {
                operations = "insert";
                condition = " into " + tableName + " (";
                String link = "";
                Iterator<?> iterator = mapData.keySet().iterator();
                while (iterator.hasNext()) {
                    String next = (String) iterator.next();
                    condition += link + next;
                    link = ",";
                }
                condition += ") values( ";
            }
        }
        String value = "";
        String link = "";
        String keyValueOperations = " where ";
        Iterator<? extends Map.Entry<?, ?>> iterator = mapData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> next = iterator.next();
            if (next.getValue() instanceof String) {
                value = "'" + next.getValue() + "'";
            } else {
                value = "" + next.getValue() + "";
            }
            if (next.getKey().toString().lastIndexOf("Key_") == -1) {
                if (!operations.equals("insert")) {
                    if (operations.equals("select") || operations.equals("delete")) {
                        condition += link + "a." + next.getKey();
                        condition += "=" + value;
                        link = " and ";
                    } else {
                        condition += link + "a." + next.getKey();
                        condition += "=" + value;
                        link = ",";
                    }
                } else {
                    condition += link + value;
                    link = ",";
                }
            } else {
                continue;
            }
        }

        // 组装 insert sql 的结尾
        if (operations.equals("insert")) {
            condition += ")";
        } else if (operations.equals("update")) { // 组装 update sql 的结尾
            condition += " where ";
            String and = "";
            Iterator<? extends Map.Entry<?, ?>> iterator1 = mapData.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<?, ?> next = iterator1.next();
                if (next.getValue() instanceof String) {
                    value = "'" + next.getValue() + "'";
                } else {
                    value = "" + next.getValue() + "";
                }
                String key = next.getKey().toString();
                if (key.lastIndexOf("Key_") != -1) {
                    key = key.substring(key.indexOf("Key_") + 4, key.length());
                    condition += and + "a." + key + "=" + value;
                    and = " and ";
                }
            }
        }

        return operations + condition;
    }
}
