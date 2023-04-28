/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.cdc;


public class PluginSettings {
    public static final String CDC_ENABLED = "index.cdc.enabled";
    public static final String CDC_PK_COL = "index.cdc.pk.column";
    public static final String CDC_EXCLUDE_COLS = "index.cdc.exclude.columns";
    // for support alias
    public static final String CDC_ALIAS = "index.cdc.alias";

    // node level settings constant
    public static final String CLUSTER_SETTING_PREFIX = "indices.cdc.";
    public static final String CDC_PRODUCER_NUMBER = "indices.cdc.producer.nums";


    public static final String KAFKA_CDC_TOPIC = "kafka.cdc.topic";
    public static final String KAFKA_CDC_NODES = "kafka.cdc.nodes";


}
