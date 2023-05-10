/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.opensearch.node.cassandra.NodeSetting;

import java.net.InetSocketAddress;
import java.time.Duration;

public class CqlConnect {

    private static CqlSession session;

    public static  CqlSession getCqlSession(String node, Integer port, String dataCenter) {
        if (session == null) {
            synchronized (CqlConnect.class) {
                if (session == null) {
                    CqlSessionBuilder builder = CqlSession.builder();
                    builder.addContactPoint(new InetSocketAddress(node, port));
                    builder.withLocalDatacenter(dataCenter);
                    builder.withConfigLoader(DriverConfigLoader.programmaticBuilder().withDuration(DefaultDriverOption.REQUEST_TIMEOUT,Duration.ofMillis(15000)).build());


                    session = builder.build();
                    return session;
                }
            }
        }
        return session;
    }


    public static CqlSession getCqlSession(){
        return session;
    }

    public void close() {
        session.close();
    }
}
