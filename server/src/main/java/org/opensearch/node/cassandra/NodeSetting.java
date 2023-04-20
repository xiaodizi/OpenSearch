/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.node.cassandra;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import org.opensearch.common.settings.Settings;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class NodeSetting {

    public static void main(String[] args) {
        String seedsConfig = getSeedsConfig("/Users/lei.fu/java/mca/OpenSearch/build/testclusters/runTask-0/config/cassandra.yaml");
        if (seedsConfig.equals("127.0.0.1") || seedsConfig.equals("localhost")) {
            System.out.println(JSON.toJSONString(seedsConfig));
        }
    }

    /**
     * 获取seeds 配置 转换 opensearch 配置
     * @param filePath  配置问价路径
     * @return 转换好的 opensearch 配置
     */
    private static String getSeedsConfig(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String, List<Map<String, List<Map<String, Object>>>>> data = yaml.load(inputStream);
        String seedProvider = data.get("seed_provider").get(0).get("parameters").get(0).get("seeds").toString();
        String[] split = seedProvider.split(",");
        String[] ipArr=new String[split.length];
        for (int i = 0; i < split.length; i++) {
            String substring = split[i].substring(0, split[i].indexOf(":"));
            ipArr[i]=substring;
        }
        return JSON.toJSONString(ipArr).replace("[","").replace("]","").replace("\"","").trim();
    }


    public static String getCassandraYamlByKey(String key, String path) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        return data.get(key).toString();
    }

    public static Settings nodeSettings(Settings settings, String path) {
        if (getSeedsConfig(path).equals("127.0.0.1") || getSeedsConfig(path).equals("localhost")) {
            return Settings.builder()
                .put("network.host", getCassandraYamlByKey("rpc_address", path))
                .put("node.name", getCassandraYamlByKey("rpc_address", path))
                //.put("discovery.seed_hosts", getSeedsConfig(path))
                .put("cluster.name", getCassandraYamlByKey("cluster_name", path))
                .put("path.home", settings.get("path.home"))
                .put("path.data", settings.get("path.data"))
                .build();
        }
        return Settings.builder()
            .put("network.host", getCassandraYamlByKey("rpc_address", path))
            .put("node.name", getCassandraYamlByKey("rpc_address", path))
            .put("discovery.seed_hosts", getSeedsConfig(path))
            .put("cluster.initial_cluster_manager_nodes",getSeedsConfig(path))
            .put("cluster.name", getCassandraYamlByKey("cluster_name", path))
            .put("path.home", settings.get("path.home"))
            .put("path.data", settings.get("path.data"))
            .build();
    }

    private static String getElasticsearchDataDir() {
        String cassandra_storage = System.getProperty("OPENSEARCH_HOME", getHomeDir() + File.separator + "data");
        // 把elastic search 的数据存储到 cassandra 的数据路径下
        return cassandra_storage + File.separator + "elasticsearch.data";
    }


    public static String getHomeDir() {
        String cassandra_home = System.getenv("CASSANDRA_HOME");
        if (cassandra_home == null) {
            cassandra_home = System.getProperty("cassandra.home", System.getProperty("path.home"));
            if (cassandra_home == null)
                throw new IllegalStateException("Cannot start, environnement variable CASSANDRA_HOME and system properties cassandra.home or path.home are null. Please set one of these to start properly.");
        }
        return cassandra_home;
    }
}
