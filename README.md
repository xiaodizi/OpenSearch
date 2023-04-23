<img src="https://opensearch.org/assets/img/opensearch-logo-themed.svg" height="64px">

# Welcome!

**OpenSearch** is [a community-driven, open source fork](https://aws.amazon.com/blogs/opensource/introducing-opensearch/) of [Elasticsearch](https://en.wikipedia.org/wiki/Elasticsearch) and [Kibana](https://en.wikipedia.org/wiki/Kibana) following the [license change](https://blog.opensource.org/the-sspl-is-not-an-open-source-license/) in early 2021. We're looking to sustain (and evolve!) a search and analytics suite for the multitude of businesses who are dependent on the rights granted by the original, [Apache v2.0 License](LICENSE.txt).

**OpenSearch** 是Elasticsearch和Kibana在2021年初更改许可证后的社区驱动的开源分支。我们正在寻求维持(和发展!)一个搜索和分析套件，以满足那些依赖于原始Apache v2.0许可证授权的大量企业。


## 这个分支做了什么？

这个分支在原有的**OpenSearch**计基础上，集成了Cassandra了，v1.0 分支，Cassandra 作为一个子项目存在于分支里，Cassandra主要使用的分支 是 v4.1.1.6。

### 关于源码编译

##### 1、将源码克隆到本地。
* Open JDK 11
* Runtime OpenJDK 14
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

| **opensearch**                        | **cassandra** | **说明**       |
| ------------------------------------- | ------------- | -------------- |
| network.host                          | rpc_address   | 服务地址       |
| node.name                             | rpc_address   | 节点名称       |
| discovery.seed_hosts                  | seeds         | 集群通信地址   |
| cluster.initial_cluster_manager_nodes | seeds         | 集群初始化节点 |
| cluster.name                          | cluster_name  | 集群名字       |
| path.data                             | data          | 数据存储路径   |

所有配置，修改config文件夹下的**cassandra.yaml**文件即可。




## Project Resources

* [Project Website](https://opensearch.org/)
* [Downloads](https://opensearch.org/downloads.html)
* [Documentation](https://opensearch.org/docs/)
* Need help? Try [Forums](https://discuss.opendistrocommunity.dev/)
* [Project Principles](https://opensearch.org/#principles)
* [Contributing to OpenSearch](CONTRIBUTING.md)
* [Maintainer Responsibilities](MAINTAINERS.md)
* [Release Management](RELEASING.md)
* [Admin Responsibilities](ADMINS.md)
* [Testing](TESTING.md)
* [Security](SECURITY.md)

## Code of Conduct

This project has adopted the [Amazon Open Source Code of Conduct](CODE_OF_CONDUCT.md). For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq), or contact [opensource-codeofconduct@amazon.com](mailto:opensource-codeofconduct@amazon.com) with any additional questions or comments.

## Security
If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/) or directly via email to aws-security@amazon.com. Please do **not** create a public GitHub issue.

## License

This project is licensed under the [Apache v2.0 License](LICENSE.txt).

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](NOTICE.txt) for details.

## Trademark

OpenSearch is a registered trademark of Amazon Web Services.

OpenSearch includes certain Apache-licensed Elasticsearch code from Elasticsearch B.V. and other source code. Elasticsearch B.V. is not the source of that other source code. ELASTICSEARCH is a registered trademark of Elasticsearch B.V.
