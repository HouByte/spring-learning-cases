[RocketMQ 官网](https://rocketmq.apache.org/) | 
[RocketMQ 快速开始&安装](https://rocketmq.apache.org/docs/quick-start/)

## 一、安装

基本步骤略

windows bat
```shell
start .\bin\mqnamesrv.cmd
timeout 5
start .\bin\mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true
```

## 二、控制台

```shell
git clone https://github.com/apache/rocketmq-externals
cd rocketmq-console
mvn clean package -Dmaven.test.skip=true
```
> 注意：打包前在rocketmq-console中配置namesrv集群地址：
> rocketmq.config.namesrvAddr=192.168.25.135:9876;192.168.25.138:9876

启动
```shell
java -jar rocketmq-console-ng-1.0.0.jar
```

> 浏览器访问http://localhost:8080





## 三、普通消息

### 3.1 基本步骤

#### 3.1.1 生产者

1. 创建消息生产者producer，并制定生产者组名
2. 指定Nameserver地址
3. 启动producer
4. 创建消息对象 Message，指定主题Topic、Tag和消息体
5. 发送消息
6. 关闭生产者producer

#### 3.1.2 消费者

1. 创建消费者Consumer，制定消费者组名
2. 指定Nameserver地址
3. 订阅主题Topic和Tag
4. 设置回调函数，处理消息
5. 启动消费者consumer

### 3.2 同步消息
#### 3.2.1 生产者

#### 3.2.2 消费者

### 3.3 同步消息
#### 3.3.1 生产者

#### 3.23.2 消费者

### 3.4 同步消息
#### 3.4.1 生产者

#### 3.4.2 消费者

### 3.5 小结
1. 同步消息、异步消息和单向消息的消费者实现方式是一样的。 
2. 同步消息、异步消息和单向消息的区别在于消息的发送方。 
3. 异步消息生产者没有返回值，需要使用 SendCallback 接收异步返回结果的回调。 
4.异步消息生产者，在关闭实例之前，建议进行休眠。 
4. 单向消息也是没有返回值的，并且它的消费者也是无序消费。 
5. 单向消息和异步消息的区别是单向消息不需要 SendCallback 来接收异步返回结果的回调。


## 四、顺序消息
### 4.1 顺序消息含义

> 顺序消息指的是可以按照消息的发送顺序来消费(FIFO)。RocketMQ可以严格的保证消息有序，可以分为分区有序或者全局有序。

在默认的情况下消息发送会采取Round Robin轮询方式把消息发送到不同的queue(分区队列)； 而消费消息的时候从多个queue上拉取消息，这种情况发送和消费是不能保证顺序。

如果控制发送的顺序消息只依次发送到同一个queue中，消费的时候只从这个queue上依次拉取，则就保证了顺序。

### 4.2 基本步骤

#### 4.2.1 生产者
1. 创建消息生产者producer并制定生产者组名 
2. 指定Nameserver地址 
3. 启动producer 
4. 创建消息对象，指定主题Topic、Tag和消息体 
5. 发送消息,选择的send方法有三个参数
  * 参数一：消息对象
  * 参数二：消息队列的选择器
  * 参数三：选择队列的业务标识 
6. 关闭生产者producer

#### 4.2.2 消费者
1. 创建消费者Consumer，制定消费者组名 
2. 指定Nameserver地址 
3. 订阅主题Topic和Tag 
4. 设置回调函数，处理消息：与普通消息的差别，这里用的是MessageListenerOrderly 
5. 启动消费者consumer

> 注意：消费者的 Topic 和 Tag 需要和生产者保持一致


## 五、延迟消息
### 5.1背景
比如一个交易的业务中，提交了一个订单就可以发送一个延时消息，1h后去检查这个订单的状态，如果还是未付款就取消订单释放库存。

它的实现和普通消息的生产者，消费者基本一样，多了一个设置延迟级别。

```java
message.setDelayTimeLevel()
```
现在RocketMq（开源的版本，白嫖的，知足吧）并不支持任意时间的延时，需要设置几个固定的延时等级，从1s到2h分别对应着等级1到18

> DelayLevel: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
> 
> 注：阿里云收费版本支持任意时间延时




### 5.2 基本步骤

#### 5.2.1 生产者
1. 创建消息生产者producer，并制定生产者组名 
2. 指定Nameserver地址 
3. 启动producer 
4. 创建消息对象，指定主题Topic、Tag和消息体,设置延时级别 
5. 发送消息 
6. 关闭生产者producer
#### 5.2.2 消费者
1.创建消费者Consumer，制定消费者组名
2. 指定Nameserver地址
3. 订阅主题Topic和Tag
4. 设置回调函数，处理消息
5. 启动消费者consumer

> 注意：消费者的 Topic 和 Tag 需要和生产者保持一致


## 六、批量消息
#### 6.1 介绍
批量发送消息能显著提高传递小消息的性能。

#### 限制
- 应该有相同的topic，相同的waitStoreMsgOK
- 不能是延时消息
- 这一批消息的总大小不应超过4MB（默认配置：DefaultMQProducer的maxMessageSize参数，可在broker*.properties配置文件中修改）。

### 6.2 基本步骤

#### 6.2.1 生产者
1. 创建消息生产者producer，并制定生产者组名
2. 指定Nameserver地址 
3. 启动producer 
4. 创建消息对象集合，指定主题Topic、Tag和消息体 
5. 发送集合消息 
6. 关闭生产者producer
#### 6.2.2 消费者
1. 创建消费者Consumer，制定消费者组名
2. 指定Nameserver地址 
3. 订阅主题Topic和Tag 
4. 设置回调函数，处理消息 
5. 启动消费者consumer

> 注意：消费者的 Topic 和 Tag 需要和生产者保持一致

### 6.3 小于4MB

### 6.3 大于4MB


## 七、广播消息
### 6.1 介绍
消费者采用广播的方式消费消息，每个消费者消费的消息都是相同的
#### 6.1.1 限制
不支持顺序消息
#### 6.1.2 消费者消费模式
1. 负载均衡模式：消费者采用负载均衡方式消费消息，多个消费者共同消费队列消息，每个消费者处理的消息不同
2. 广播模式：消费者采用广播的方式消费消息，每个消费者消费的消息都是相同的
### 6.2 基本步骤

#### 6.2.1 生产者
1. 创建消息生产者producer，并制定生产者组名
2. 指定Nameserver地址
3. 启动producer
4. 创建消息对象集合，指定主题Topic、Tag和消息体
5. 发送集合消息
6. 关闭生产者producer

> 注：这类例子与批量消息的生产者代码一模一样，也可以提供其他方式
#### 6.2.2 消费者
1. 创建消费者Consumer，制定消费者组名
2. 指定Nameserver地址
3. 默认均衡轮询消费模式 改为广播模式
4. 订阅主题Topic和Tag
5. 设置回调函数，处理消息
6. 启动消费者consumer

> 注意：消费者的 Topic 和 Tag 需要和生产者保持一致


## n、Spring Boot 集成
maven
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.rocketmq</groupId>
        <artifactId>rocketmq-spring-boot-starter</artifactId>
        <version>2.2.1</version>
    </dependency>
</dependencies>
```
> maven 继承spring boot 2.6.3


## 六、批量消息
### 5.2 基本步骤

#### 生产者
1. ss
2. 
#### 消费者

简略版本: [【Spring Cloud Alibaba】消息驱动 Kafka RocketMQ RabbitMQ Stream](https://blog.csdn.net/Vincent_Vic_/article/details/125198287)

