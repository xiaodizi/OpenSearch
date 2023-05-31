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
        IndexEventListener.super.afterIndexCreated(indexService);
    }


    @Override
    public void beforeIndexCreated(Index index, Settings indexSettings) {
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
        if (cdcEnable == false) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                CassandraOperation.createKeyspace(index.getName(), replicationStrategy, replicationFactor, CqlConnect.getCqlSession());
            }
        }).start();
        IndexEventListener.super.beforeIndexAddedToCluster(index, indexSettings);
    }


    @Override
    public void afterIndexRemoved(Index index, IndexSettings indexSettings, IndicesClusterStateService.AllocatedIndices.IndexRemovalReason reason) {
        if (cdcEnable == false) {
            return;
        }

        CassandraOperation.dropTable(index.getName(), CqlConnect.getCqlSession());
        IndexEventListener.super.afterIndexRemoved(index, indexSettings, reason);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        String indexName = shardId.getIndex().getName();
        if (cdcEnable == false) {
            return;
        }
        String deleteId = delete.id();
        CassandraOperation.deleteById(indexName, deleteId, CqlConnect.getCqlSession());
    }


    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {
        String indexName = shardId.getIndex().getName();
        String utf8ToString = index.parsedDoc().source().utf8ToString();
        String id = index.id();
        if (cdcEnable == false) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                JSONObject jsonObject = JSONObject.parseObject(utf8ToString);
                Map<String, Object> map = (Map<String, Object>) jsonObject;
                if (result.isCreated()) {
                    CassandraOperation.createTables(indexName, map, id, CqlConnect.getCqlSession());
                } else {
                    CassandraOperation.updateTables(indexName, map, id, CqlConnect.getCqlSession());
                }
            }
        }).start();
    }
}
