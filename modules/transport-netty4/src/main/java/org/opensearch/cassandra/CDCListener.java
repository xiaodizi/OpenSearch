/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.cassandra;

import com.alibaba.fastjson2.JSONObject;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.index.IndexModule;
import org.opensearch.index.IndexService;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.engine.Engine;
import org.opensearch.index.mapper.ParseContext;
import org.opensearch.index.shard.IndexEventListener;
import org.opensearch.index.shard.IndexingOperationListener;
import org.opensearch.index.shard.ShardId;
import org.opensearch.indices.cluster.IndicesClusterStateService;

import java.util.List;
import java.util.Map;

public class CDCListener implements IndexingOperationListener, IndexEventListener {

    private static final Logger logger = LogManager.getLogger(CDCListener.class);


    private volatile boolean cdcEnable = false;


    private volatile String replicationStrategy = "SimpleStrategy";

    private volatile int replicationFactor = 1;

    private final IndexModule indexModule;


    public CDCListener(IndexModule indexModule) {
        cdcEnable = Boolean.parseBoolean(indexModule.getSettings().get("index.cdc.enabled"));
        String strategy = indexModule.getSettings().get("index.cdc.cassandra.replaction.strategy");
        if (!StringUtil.isNullOrEmpty(strategy)) {
            replicationStrategy = strategy;
        }
        String factoryInt = indexModule.getSettings().get("index.cdc.cassandra.replaction.factory");
        if (!StringUtil.isNullOrEmpty(factoryInt)) {
            replicationFactor = Integer.parseInt(factoryInt);
        }
        this.indexModule = indexModule;
    }

    @Override
    public void afterIndexCreated(IndexService indexService) {

        logger.info("-----------LEI TEST 索引创建之后-----------");
        logger.info("索引名称:" + indexService.getIndexSettings().getIndex().getName());
        IndexEventListener.super.afterIndexCreated(indexService);
    }


    @Override
    public void beforeIndexCreated(Index index, Settings indexSettings) {
        logger.info("-----------LEI TEST 索引创建之前 ----------");
        logger.info("索引名称:" + index.getName());

        if (cdcEnable == false) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                CassandraOperation.createKeyspace(index.getName(), replicationStrategy, replicationFactor, CqlConnect.getCqlSession());
            }
        }).start();

        IndexEventListener.super.beforeIndexCreated(index, indexSettings);
    }

    @Override
    public void beforeIndexAddedToCluster(Index index, Settings indexSettings) {
        logger.info("-----------LEI TEST 索引添加到集群之前-----------");
        logger.info("索引名称:" + index.getName());

        if (cdcEnable == false) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                CassandraOperation.createKeyspace(index.getName(), replicationStrategy, replicationFactor, CqlConnect.getCqlSession());
            }
        }).start();
        logger.info("------------------------------");
        IndexEventListener.super.beforeIndexAddedToCluster(index, indexSettings);
    }


    @Override
    public void afterIndexRemoved(Index index, IndexSettings indexSettings, IndicesClusterStateService.AllocatedIndices.IndexRemovalReason reason) {
        logger.info("-----------LEI TEST 索引删除之后-----------");
        logger.info("afterIndexRemoved 索引名称:" + index.getName());

        if (cdcEnable == false) {
            return;
        }

        CassandraOperation.dropTable(index.getName(), CqlConnect.getCqlSession());

        System.out.println("------------------------------");

        IndexEventListener.super.afterIndexRemoved(index, indexSettings, reason);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        logger.info("----------------------索引删除操作开始------------------------");
        String indexName = shardId.getIndex().getName();


        if (cdcEnable == false) {
            return;
        }
        String deleteId = delete.id();
        logger.info("----------Delete start------------");
        logger.info("删除索引名称:" + indexName + "；删除id:" + deleteId);
        CassandraOperation.deleteById(indexName, deleteId, CqlConnect.getCqlSession());
        logger.info("----------Delete end------------");
    }


    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {
        logger.info("----------------------索引操作开始------------------------");
        String indexName = shardId.getIndex().getName();
        logger.info("索引名称:" + indexName);
        List<ParseContext.Document> docs = index.docs();
        logger.info("得到的docs：" + docs);
        String id = index.id();
        if (cdcEnable == false) {
            return;
        }
        logger.info("--------------------------Index start----------------------------");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String utf8ToString = index.parsedDoc().source().utf8ToString();
                JSONObject jsonObject = JSONObject.parseObject(utf8ToString);
                Map<String, Object> map = (Map<String, Object>) jsonObject;
                if (result.isCreated()) {
                    CassandraOperation.createTables(indexName, map, id, CqlConnect.getCqlSession());
                } else {
                    CassandraOperation.updateTables(indexName, map, id, CqlConnect.getCqlSession());
                }
            }
        }).start();
        logger.info("--------------------------Index end----------------------------");

    }
}
