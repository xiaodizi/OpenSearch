/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.cdc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.index.IndexModule;
import org.opensearch.index.IndexService;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.engine.Engine;
import org.opensearch.index.shard.IndexEventListener;
import org.opensearch.index.shard.IndexingOperationListener;
import org.opensearch.index.shard.ShardId;
import org.opensearch.indices.cluster.IndicesClusterStateService;

import java.util.function.Consumer;

public class CDCListener implements IndexingOperationListener, IndexEventListener,Consumer<Boolean> {

    private static final Logger logger = LogManager.getLogger(CDCListener.class);


    private volatile boolean needInit = true;

    private final IndexModule indexModule;


    private String kafkaNodes;
    private String kafkaTopic;


    public CDCListener(IndexModule indexModule) {
        kafkaNodes = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_NODES);
        kafkaTopic = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_TOPIC);
        this.indexModule = indexModule;
    }



    @Override
    public void beforeIndexAddedToCluster(Index index, Settings indexSettings) {
        System.out.println("-----------LEI TEST 索引添加到集群之前-----------");
        System.out.println("索引名称:"+index.getName());
        System.out.println("------------------------------");
        IndexEventListener.super.beforeIndexAddedToCluster(index, indexSettings);
    }


    @Override
    public void afterIndexCreated(IndexService indexService) {


        System.out.println("-----------LEI TEST 索引创建之后-----------");
        System.out.println("索引名称:"+indexService.getIndexSettings().getIndex().getName());
        System.out.println("------------------------------");
        IndexEventListener.super.afterIndexCreated(indexService);
    }


    @Override
    public void afterIndexRemoved(Index index, IndexSettings indexSettings, IndicesClusterStateService.AllocatedIndices.IndexRemovalReason reason) {
        System.out.println("-----------LEI TEST 索引删除之后-----------");
        System.out.println("索引名称:"+index.getName());
        System.out.println("------------------------------");

        IndexEventListener.super.afterIndexRemoved(index, indexSettings, reason);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        logger.info("----------------------索引删除操作开始------------------------");
        String indexName = shardId.getIndex().getName();
        logger.info("----------Delete start------------");
        logger.info("删除索引名称:"+indexName);
        String deleteId = delete.id();
        logger.info("删除ID:"+deleteId);
        boolean isFound = result.isFound();
        logger.info("是否找到了数据："+isFound);
        logger.info("----------Delete end------------");
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        logger.info("----------------------索引操作开始------------------------");
        String indexName=shardId.getIndex().getName();

        logger.info("索引名称:"+indexName);

        logger.info("--------------------------Index start----------------------------");
        String utf8ToString = index.parsedDoc().source().utf8ToString();
        boolean created = result.isCreated();

        String id = index.id();
        logger.info("数据ID："+id);

        logger.warn("数据:"+utf8ToString);


        logger.info("--------------------------Index end----------------------------");

    }


    @Override
    public void accept(Boolean cdcEnabled) {
        if (cdcEnabled) {
            needInit = true;
        }
    }
}
