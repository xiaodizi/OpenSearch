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
git clone --recurse-submodules git@github.com:xiaodizi/cassandra.git
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
但是这个东西 是默认为 true，如果希望这张表同步到Opensearch，不加这个配置也可以。但是如果希望这张表不同步到Opensearch的话，那就需要显示的增加`WITH syncEs=false;`。

```
CREATE TABLE users (
                 user_id varchar PRIMARY KEY,
                 first varchar,
                 last varchar,
                 age int
               ) WITH syncEs=false;
```

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
