<img src="https://opensearch.org/assets/img/opensearch-logo-themed.svg" height="64px">

# Welcome!

**OpenSearch** is [a community-driven, open source fork](https://aws.amazon.com/blogs/opensource/introducing-opensearch/) of [Elasticsearch](https://en.wikipedia.org/wiki/Elasticsearch) and [Kibana](https://en.wikipedia.org/wiki/Kibana) following the [license change](https://blog.opensource.org/the-sspl-is-not-an-open-source-license/) in early 2021. We're looking to sustain (and evolve!) a search and analytics suite for the multitude of businesses who are dependent on the rights granted by the original, [Apache v2.0 License](LICENSE.txt).

## 这个分支做了什么？

这个分支在原有的**OpenSearch**计基础上，集成了Cassandra了，v1.0 分支，Cassandra 作为一个子项目存在于分支里，Cassandra主要使用的分支 是 v4.1.1.6。

### 关于源码编译

##### 1、将源码克隆到本地。

* Open JDK 11
* Runtime OpenJDK 14

还需要 clone 子项目：

```shell
git clone --recurse-submodules 路径
```

##### 2、执行编译cassandra，因为cassandra是使用的ant编译的，本地ant 1.10版本测试没有问题。先编译Cassandra的目的也就是为了拉取ant相关插件的包，尝试过集成编译，发现依旧还是会失败，所以还是建议先执行下边的命令。

```shell
./gradlew cassandra-artifacts -Duse.jdk11=true
```

##### 3、编译本机系统版本，mac测试通过

```shell
./gradlew localDistro -Duse.jdk11=true --info
```

##### 4、编译打包 linux 系统版本

```shell
./gradlew :distribution:archives:linux-tar:assemble
```

##### 5、关于运行

```shell
./gradlew run
```

可以运行起来代码，但是只是单纯的的运行起来**OpenSearch**,还不能同时运行Cassandra。

##### 6、关于Debug，需要添加Remote JVM Degbu， 配置为Listen to Remote Debug 模式，其他配置默认即可。再运行如下命令即可。

```shell
./gradlew run --debug-jvm
```

### 关于使用

#### 启动

```
./bin/start
```

#### 停止

```
./bin/stop
```

### 关于配置

目前两个服务已经通用化配置，已经通用的配置项目如下：

![img.png](./assets/img.png)

所有配置，修改config文件夹下的**cassandra.yaml**文件即可。

### 集群配置

#### 集群啥样？

整个修改是在未破坏Cassndra特性和Opensearch的情况下修改的，Cassandra还是存储宽表，而Opensearch 基于lucene存储。只是做了在两个数据库下的结合。如下图：

![img_1.png](./assets/img_1.png)

##### 配置

举例 节点一的配置：

```
cluster_name: Test Cluster     ## 集群名字，集群内的所有节点要全部一样。
- seeds: "ip1:7000,ip2:7000,ip3:7000"  ## 集群内所有节点的通信地址和端口，默认的端口是7000
listen_address: ip1   ## 这个节点绑定的对外IP地址
rpc_address: ip1   ## 传输地址
```

ip1 就是节点1 的ip地址，其他两个节点同样配置这四个就可以了。当然已经通用化的配置，需要修改 的可以酌情修改。这四个是构成集群的基本配置。

##### 集群安装完成

所有的cassandra 的脚本工具，都放在安装路径下的 `cassandra/bin` 文件夹下。
检测cassandra 状态：

```shell
[elastic@elastic05 opensearch-3.0.0-SNAPSHOT]$ ./cassandra/bin/nodetool status
Datacenter: datacenter1
=======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address         Load       Tokens  Owns (effective)  Host ID                               Rack
UN  192.168.184.31  70.28 KiB  16      67.6%             cf50ce53-1b27-4292-9819-8d17362c4bdc  rack1
UN  192.168.184.33  70.28 KiB  16      69.1%             c0945ed1-9fa0-4b90-93b6-4ebe9aa80651  rack1
UN  192.168.184.32  70.28 KiB  16      63.3%             d65ee310-d761-4fc4-849f-9d40adacfaf5  rack1
```

检测 Opensearch 启动状态：

![img_2.png](./assets/img_2.png)

##### 集群启动后一些注意事项

1、因为Opensearch源码里，在启动Netty的时候，顺便用Cassandra的Java Driver ，这东西其实也是个Netty的客户端，所以启动Opensearch前，需要Cassandra 完全启动好，Java Driver 才能连接上去。当前这一切代码，都已经考虑到了。但是在检测Cassandra的状态的时候，使用了nodetool工具，如果程序一直没有获取到nodetool的正常状态，就会重复执行检查，这时候也许启动界面会有如下错误：

```shell
WARN  [GossipTasks:1] 2023-05-11 04:12:01,518 FailureDetector.java:335 - Not marking nodes down due to local pause of 9236573980ns > 5000000000ns
正在检测 Cassandra 服务状态。。。。。。
error: No nodes present in the cluster. Has this node finished starting up?
-- StackTrace --
java.lang.RuntimeException: No nodes present in the cluster. Has this node finished starting up?
        at org.apache.cassandra.dht.Murmur3Partitioner.describeOwnership(Murmur3Partitioner.java:294)
        at org.apache.cassandra.service.StorageService.getOwnershipWithPort(StorageService.java:5600)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at sun.reflect.misc.Trampoline.invoke(MethodUtil.java:71)
        at jdk.internal.reflect.GeneratedMethodAccessor3.invoke(Unknown Source)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at java.base/sun.reflect.misc.MethodUtil.invoke(MethodUtil.java:260)
        at java.management/com.sun.jmx.mbeanserver.StandardMBeanIntrospector.invokeM2(StandardMBeanIntrospector.java:112)
        at java.management/com.sun.jmx.mbeanserver.StandardMBeanIntrospector.invokeM2(StandardMBeanIntrospector.java:46)
        at java.management/com.sun.jmx.mbeanserver.MBeanIntrospector.invokeM(MBeanIntrospector.java:237)
        at java.management/com.sun.jmx.mbeanserver.PerInterface.getAttribute(PerInterface.java:83)
        at java.management/com.sun.jmx.mbeanserver.MBeanSupport.getAttribute(MBeanSupport.java:206)
        at java.management/com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.getAttribute(DefaultMBeanServerInterceptor.java:641)
        at java.management/com.sun.jmx.mbeanserver.JmxMBeanServer.getAttribute(JmxMBeanServer.java:678)
        at java.management.rmi/javax.management.remote.rmi.RMIConnectionImpl.doOperation(RMIConnectionImpl.java:1443)
        at java.management.rmi/javax.management.remote.rmi.RMIConnectionImpl$PrivilegedOperation.run(RMIConnectionImpl.java:1307)
        at java.management.rmi/javax.management.remote.rmi.RMIConnectionImpl.doPrivilegedOperation(RMIConnectionImpl.java:1399)
        at java.management.rmi/javax.management.remote.rmi.RMIConnectionImpl.getAttribute(RMIConnectionImpl.java:637)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at java.rmi/sun.rmi.server.UnicastServerRef.dispatch(UnicastServerRef.java:359)
        at java.rmi/sun.rmi.transport.Transport$1.run(Transport.java:200)
        at java.rmi/sun.rmi.transport.Transport$1.run(Transport.java:197)
        at java.base/java.security.AccessController.doPrivileged(Native Method)
        at java.rmi/sun.rmi.transport.Transport.serviceCall(Transport.java:196)
        at java.rmi/sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:562)
        at java.rmi/sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:796)
        at java.rmi/sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.lambda$run$0(TCPTransport.java:677)
        at java.base/java.security.AccessController.doPrivileged(Native Method)
        at java.rmi/sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:676)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:834)
```

easy! 不要紧张，这主要 nodetool 在检测时候，发现状态不对，返回的错误，接着会再次检查，正常了，就会返回 检测完毕的。

2、部署集群的时候，要注意。例如，我在测试的时候使用三节点的集群，启动需要一个一个的启动。这时候Cassandra会有如下错误：

```shell
INFO  [Messaging-EventLoop-3-1] 2023-05-11 04:11:34,209 NoSpamLogger.java:105 - /192.168.184.31:7000->/192.168.184.32:7000-URGENT_MESSAGES-[no-channel] failed to connect
io.netty.channel.AbstractChannel$AnnotatedConnectException: finishConnect(..) failed: Connection refused: /192.168.184.32:7000
Caused by: java.net.ConnectException: finishConnect(..) failed: Connection refused
        at io.netty.channel.unix.Errors.throwConnectException(Errors.java:124)
        at io.netty.channel.unix.Socket.finishConnect(Socket.java:251)
        at io.netty.channel.epoll.AbstractEpollChannel$AbstractEpollUnsafe.doFinishConnect(AbstractEpollChannel.java:673)
        at io.netty.channel.epoll.AbstractEpollChannel$AbstractEpollUnsafe.finishConnect(AbstractEpollChannel.java:650)
        at io.netty.channel.epoll.AbstractEpollChannel$AbstractEpollUnsafe.epollOutReady(AbstractEpollChannel.java:530)
        at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:470)
        at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:378)
        at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
        at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base/java.lang.Thread.run(Thread.java:834)
```

这是因为集群通信也使用的Netty，连不上另外一个集群了，所以Cassandra报了一个错误。等到另外一个集群启动成功后，就会消失。

### 关于同步

起初的想法是来一条数据，互相之间同步一份，作为大数据后端宽表分析的数据库，这种场景有可能不需要。所以在两端都做了个配置。

##### Cassandra 同步到 Opensearch

Cassandra 数据 同步到 Opensearch，例子如下:

```
CREATE TABLE users (
                 user_id varchar PRIMARY KEY,
                 first varchar,
                 last varchar,
                 age int
               ) WITH syncEs=true;
```

多了 一个 `WITH syncEs=true;` 这样就是启动了同步到 Opensearch。
但是这个东西 是默认为 false，如果不希望这张表同步到Opensearch，不加这个配置也可以。但是如果希望这张表同步到Opensearch的话，那就需要显示的增加`WITH syncEs=true;`。

```
CREATE TABLE users (
                 user_id varchar PRIMARY KEY,
                 first varchar,
                 last varchar,
                 age int
               ) WITH syncEs=false;
```


重点说明一下`.cassandra_metadata` 这个索引：
    因为cassandra的表配置是基于cql的，只是简单的存储在一个HashMap里边，所以将集群配置，放在了`Opensearch`里边了。

##### Opensearch 同步到 Cassandra

如果是Opensearch数据同步到 Cassandra，需要创建索引的时候，增加`"index.cdc.enabled":true,`这个配置。
例子：

```shell
PUT cassandra-test
{
  "settings":{
    "index.cdc.enabled":true,
    "number_of_shards":1,
    "number_of_replicas":0
  },
  "mappings": {
      "properties": {
        "name": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        }
      }
    }
}
```

这时候只是创建了索引，如果需要同步创建表，只需要写入一条数据即可。例如：

```
PUT cassandra-test/_doc/1
{
  "name":"1"
}
```

之后可以到Cassandra 查看，对应的表已经创建好了。

### Rack 冷热集群配置

需要在cassandra.yaml文件配置，如下项：

```shell
# You can use a custom Snitch by setting this to the full class name
# of the snitch, which will be assumed to be on your classpath.
endpoint_snitch: SimpleSnitch
```

修改为：

```shell
# You can use a custom Snitch by setting this to the full class name
# of the snitch, which will be assumed to be on your classpath.
endpoint_snitch: GossipingPropertyFileSnitch
```

### Cassandra keyspace的 Strategy 和 replactionFactory 配置

```shell
PUT cassandra1-test
{
  "settings":{
    "index.cdc.enabled":true,
    "index.cdc.cassandra.replaction.strategy":"NetworkTopologyStrategy",
    "index.cdc.cassandra.replaction.factory":"3",
    "number_of_shards":1,
    "number_of_replicas":0
  },
  "mappings": {
      "properties": {
        "name": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "age":{
          "type":"text"
        }
      }
    }
}
```

### 关于Cassandra的堆内存设置

Cassandra启动后，堆是自动计算的，可以在`config`目录下的`jvm-server.options`文件 进行设置，而且也有一个很明确的官方注释。

但是会频繁的GC，内存很容易被压榨的顶满，导致服务直接就挂了。

建议还是根据内存情况做一下设置：

![img_3.png](./assets/img_3.png)

到底是否参考官网的计算公示，根据情况选择吧！目前是两个服务，没有主动做这方面的计算。默认现在做了一个配置，如有需要可以修改。
