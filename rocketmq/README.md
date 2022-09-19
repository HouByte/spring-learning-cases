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
```java
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * <h1>同步消息 - 生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ProducerSync {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        System.out.println("生产者启动");
        producer.start();

        for (int i = 0; i < 3; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("Topic_demo_sync", "Tag_demo_sync",
                ("Hello，" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 5.发送同步消息
            SendResult sendResult = producer.send(msg);
            System.out.println("发送结果：" + sendResult);
        }

        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }
}
```
#### 3.2.2 消费者
```java
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <h1>同步消息 - 消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ConsumerSync {

    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_group");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 消息拉取最大条数
        consumer.setConsumeMessageBatchMaxSize(2);
        // 3.订阅主题Topic和Tag
        consumer.subscribe("Topic_demo_sync", "*");

        // 4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            // 接受消息内容
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {

                    try {
                        // 获取主题
                        String topic = msg.getTopic();
                        // 获取标签
                        String tags = msg.getTags();
                        // 获取信息
                        byte[] body = msg.getBody();
                        String result = new String(body, RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("Consumer消费信息：topic:" + topic + ",tags:" + tags + ",result：" + result);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 5.启动消费者consumer
        consumer.start();
    }
}
```
### 3.3 异步消息
#### 3.3.1 生产者
```java
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * <h1>异步消息 - 生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ProducerASync {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        System.out.println("生产者启动");
        producer.start();

        for (int i = 0; i < 3; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("Topic_demo_async", "Tag_demo_async",
                ("Hello，" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 发送消息到一个Broker，异步发送没有返回值，需要使用 SendCallback 接收异步返回结果的回调
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("发送成功：" + sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    System.out.println("发送异常：" + throwable.getMessage());
                }
            });

        }
        Thread.sleep(2000);
        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }
}

```
#### 3.23.2 消费者
```java
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <h1>异步消息 - 消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ConsumerASync {

    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_group");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 消息拉取最大条数
        consumer.setConsumeMessageBatchMaxSize(2);
        // 3.订阅主题Topic和Tag
        consumer.subscribe("Topic_demo_async", "*");

        // 4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            // 接受消息内容
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {

                    try {
                        // 获取主题
                        String topic = msg.getTopic();
                        // 获取标签
                        String tags = msg.getTags();
                        // 获取信息
                        byte[] body = msg.getBody();
                        String result = new String(body, RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("Consumer消费信息：topic:" + topic + ",tags:" + tags + ",result：" + result);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 5.启动消费者consumer
        consumer.start();
    }
}

```
### 3.4 单向消息
#### 3.4.1 生产者
```java
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * <h1>单向消息 - 生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Producer {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        producer.start();
        System.out.println("生产者启动");
        for (int i = 0; i < 20; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("Topic_demo", "Tag_demo",
                ("Hello，" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 5.发送单向消息
            producer.sendOneway(msg);
            System.out.println("发送结果：" + msg);
            // 线程睡1秒
        }

        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }
}
```
#### 3.4.2 消费者
```java
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <h1>单向消息 - 消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Consumer {

    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_group");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 消息拉取最大条数
        consumer.setConsumeMessageBatchMaxSize(2);
        // 3.订阅主题Topic和Tag
        consumer.subscribe("Topic_demo", "*");

        // 4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            // 接受消息内容
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {

                    try {
                        // 获取主题
                        String topic = msg.getTopic();
                        // 获取标签
                        String tags = msg.getTags();
                        // 获取信息
                        byte[] body = msg.getBody();
                        String result = new String(body, RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("Consumer消费信息：topic:" + topic + ",tags:" + tags + ",result:" + result);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 5.启动消费者consumer
        consumer.start();
    }
}

```
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

### 4.3 代码实现
#### 4.3.1 生产者
```java
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.List;

/**
 * <h1>顺序消息 - 生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Producer {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_order_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        producer.start();
        System.out.println("生产者启动");
        for (int i = 0; i < 20; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体

            Message msg = new Message("Topic_order_demo", "Tag_order_demo",
                ("Hello，这是顺序消息" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 5.发送消息
            /**
             * 参数一：消息对象
             * 参数二：消息队列的选择器
             * 参数三：选择队列的业务标识
             */
            SendResult result = producer.send(msg, new MessageQueueSelector() {
                /**
                 *
                 * @param mqs：队列集合
                 * @param msg：消息对象
                 * @param arg：业务标识的参数
                 * @return
                 */
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer index = (Integer) arg;
                    return mqs.get(index);
                }
            }, 1);
            System.out.println("发送结果：" + msg.toString());
        }

        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }
}

```
#### 4.3.2 消费者
```java
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * <h1>顺序消息 - 消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Consumer {

    public static void main(String[] args) throws Exception {
        //1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_order_group");
        //2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        //消息拉取最大条数
        consumer.setConsumeMessageBatchMaxSize(2);
        //3.订阅主题Topic和Tag
        consumer.subscribe("Topic_order_demo", "*");

        //4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerOrderly() {
            //接受消息内容
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext consumeOrderlyContext) {
                for (MessageExt msg : msgs) {

                    try {
                        //获取主题
                        String topic = msg.getTopic();
                        //获取标签
                        String tags = msg.getTags();
                        //获取信息
                        byte[] body =  msg.getBody();
                        String result = new String(body, RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("Consumer消费信息：topic:" + topic + ",tags:"+tags+
                            ",result"+ result);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        //重试
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        //5.启动消费者consumer
        consumer.start();
    }
}
```
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

### 5.3 代码实现
#### 5.3.1 生产者
```java
package cn.flowboot.rocketmq.delay.producer;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.concurrent.TimeUnit;

/**
 * <h1>延迟消息生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Producer {

    public static void main(String[] args)
        throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_delay_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        producer.start();
        System.out.println("生产者启动");

        for (int i = 0; i < 10; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("DelayTopic", "Tag1", ("Hello" + i).getBytes());
            // DelayLevel: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            // 设定延迟时间 1~10个等级
            msg.setDelayTimeLevel(i);
            // 5.发送消息
            SendResult result = producer.send(msg);
            // 发送状态
            SendStatus status = result.getSendStatus();

            System.out.println("发送结果:" + result);

            // 线程睡1秒
//            TimeUnit.SECONDS.sleep(10);
        }

        // 6.关闭生产者producer
        producer.shutdown();
    }

}

```
#### 5.3.2 消费者
```java
package cn.flowboot.rocketmq.delay.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * <h1>延迟消息消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Consumer {

    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_producer_delay_group");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        // 3.订阅主题Topic和Tag
        consumer.subscribe("DelayTopic", "*");

        // 4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            // 接受消息内容
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println("消息ID：【" + msg.getMsgId() + "】,消息内容：" + new String(msg.getBody()) + ",延迟时间："
                        + (System.currentTimeMillis() - msg.getBornTimestamp()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 5.启动消费者consumer
        consumer.start();

        System.out.println("消费者启动");
    }
}
```

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
#### 6.3.1 生产者
```java
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <h1></h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Producer {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_batch_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        producer.start();
        System.out.println("生产者启动");
        List<Message> msgs = new ArrayList<Message>();

        // 4.创建消息对象，指定主题Topic、Tag和消息体
        /**
         * 参数一：消息主题Topic
         * 参数二：消息Tag
         * 参数三：消息内容
         */
        for (int i = 0; i < 20; i++) {
            Message msg = new Message("Topic_batch_demo", "Tag_batch_demo", ("Hello，这是批量消息" + i).getBytes());
            msgs.add(msg);
        }

        // 5.发送消息
        SendResult result = producer.send(msgs);
        // 发送状态
        SendStatus status = result.getSendStatus();

        System.out.println("发送结果:" + result);

        // 线程睡1秒
        TimeUnit.SECONDS.sleep(1);

        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }

}
```
#### 6.3.2 消费者
```java
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <h1>批量消息消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class Consumer {

    public static void main(String[] args) throws Exception {
        //1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_order_group");
        //2.指定Nameserver地址
        consumer.setNamesrvAddr("192.168.88.131:9876");

        //消息拉取最大条数
        consumer.setConsumeMessageBatchMaxSize(2);
        //3.订阅主题Topic和Tag
        consumer.subscribe("Topic_batch_demo", "*");

        //4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerOrderly() {
            //接受消息内容
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext consumeOrderlyContext) {
                for (MessageExt msg : msgs) {

                    try {
                        //获取主题
                        String topic = msg.getTopic();
                        //获取标签
                        String tags = msg.getTags();
                        //获取信息
                        byte[] body =  msg.getBody();
                        String result = new String(body, RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("Consumer消费信息：topic:" + topic + ",tags:"+tags+
                            ",result"+ result);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        //重试
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        //5.启动消费者consumer
        consumer.start();
    }
}
```
### 6.3 大于4MB
#### 6.3.1 拆分消息工具
```java
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <h1>拆分消息工具类</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ListSplitter implements Iterator<List<Message>> {
    private int SIZE_LIMIT = 1024 * 1024 * 4;
    private final List<Message> messages;
    private int currIndex;

    public ListSplitter(List<Message> messages) {
        this.messages = messages;
    }

    public ListSplitter(List<Message> messages, DefaultMQProducer mqProducer) {
        this.messages = messages;
        this.SIZE_LIMIT = mqProducer.getMaxMessageSize();
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            Message message = messages.get(nextIndex);
            int tmpSize = message.getTopic().length() + message.getBody().length;
            Map<String, String> properties = message.getProperties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                tmpSize += entry.getKey().length() + entry.getValue().length();
            }
            // 增加日志的开销20字节
            tmpSize = tmpSize + 20;

            if (tmpSize > SIZE_LIMIT) {
                // 单个消息超过了最大的限制
                // 忽略,否则会阻塞分裂的进程
                if (nextIndex - currIndex == 0) {
                    // 假如下一个子列表没有元素,则添加这个子列表然后退出循环,否则只是退出循环
                    nextIndex++;
                }
                break;
            }
            if (tmpSize + totalSize > SIZE_LIMIT) {
                break;
            } else {
                totalSize += tmpSize;
            }

        }
        List<Message> subList = messages.subList(currIndex, nextIndex);
        currIndex = nextIndex;
        return subList;
    }

    @Override
    public void remove() {

    }

}
```
#### 6.3.1 生产者

```java
package cn.flowboot.rocketmq.batch.splitter.producer;

import ListSplitter;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <h1>6.3 大于4MB 生产者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ProducerMoreThan4M {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_batch_group");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 3.启动producer
        producer.start();
        System.out.println("生产者启动");
        List<Message> msgs = new ArrayList<Message>();

        // 4.创建消息对象，指定主题Topic、Tag和消息体
        /**
         * 参数一：消息主题Topic
         * 参数二：消息Tag
         * 参数三：消息内容
         */
        for (int i = 0; i < 20; i++) {
            Message msg = new Message("Topic_batch_demo", "Tag_batch_demo", ("Hello，这是批量消息" + i).getBytes());
            msgs.add(msg);
        }
        // 5.发送消息
        // 发送批量消息：把大的消息分裂成若干个小的消息
        ListSplitter splitter = new ListSplitter(msgs, producer);
        while (splitter.hasNext()) {
            try {
                List<Message> listItem = splitter.next();
                SendResult result = producer.send(listItem);
                System.out.println("发送结果:" + result);
            } catch (Exception e) {
                e.printStackTrace();
                // 处理error
            }
        }

        // 线程睡1秒
        TimeUnit.SECONDS.sleep(1);

        // 6.关闭生产者producer
        producer.shutdown();
        System.out.println("生产者关闭");
    }

}
```
#### 6.3.3 消费者
同6.3.2一致

## 七、广播消息
### 7.1 介绍
消费者默认使用负载均衡模式，同一个组内，两个消费者争抢消息
消费者同一个组中采用广播的方式消费消息，每个消费者消费的消息都是相同的
> 广播设置主要针对组内设置，如果是不同组，都会消息同一个消息
#### 7.1.1 限制
不支持顺序消息
#### 7.1.2 消费者消费模式
1. 负载均衡模式：消费者采用负载均衡方式消费消息，多个消费者共同消费队列消息，每个消费者处理的消息不同
2. 广播模式：消费者采用广播的方式消费消息，每个消费者消费的消息都是相同的
### 7.2 基本步骤

#### 7.2.1 生产者
1. 创建消息生产者producer，并制定生产者组名
2. 指定Nameserver地址
3. 启动producer
4. 创建消息对象集合，指定主题Topic、Tag和消息体
5. 发送集合消息
6. 关闭生产者producer

> 注：这类例子与批量消息的生产者代码一模一样，也可以提供其他方式
#### 7.2.2 消费者
1. 创建消费者Consumer，制定消费者组名
2. 指定Nameserver地址
3. 默认均衡轮询消费模式 改为广播模式
4. 订阅主题Topic和Tag
5. 设置回调函数，处理消息
6. 启动消费者consumer

> 注意：消费者的 Topic 和 Tag 需要和生产者保持一致
### 7.3 代码实现
#### 7.3.1 生产者
可以在上文例子挑选一个修改
#### 7.3.2 消费者

编写两个消费者，添加设置为广播模式
```java
// 默认均衡轮询消费模式 改为广播模式
consumer.setMessageModel(MessageModel.BROADCASTING);
```
## 七、Spring Boot 集成

### 6.1 基本步骤
方式一: 原生方式
1. 添加依赖
2. 创建生产者/消费者bean
3. 具体和上文一致
4. 注册销毁
5. 使用注入

方式二：使用Spring的方式
1. 添加依赖
2. 配置yaml
3. 使用注入RocketMQTemplate
4. 消费者实现RocketMQListener接口
5. 使用@RocketMQMessageListener

### 6.2 添加maven
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

### 6.3 添加配置
application.yml
```yaml
server:
  port: 8080

rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: rocket-mq-group
    send-message-timeout: 3000 # 发送消息超时时间，单位：毫秒。默认为 3000 。
    compress-message-body-threshold: 4096 # 消息压缩阀值，当消息体的大小超过该阀值后，进行消息压缩。默认为 4 * 1024B
    max-message-size: 4194304 # 消息体的最大允许大小。。默认为 4 * 1024 * 1024B
    retry-times-when-send-failed: 2 # 同步发送消息时，失败重试次数。默认为 2 次。
    retry-times-when-send-async-failed: 2 # 异步发送消息时，失败重试次数。默认为 2 次。
    retry-next-server: false # 发送消息给 Broker 时，如果发送失败，是否重试另外一台 Broker 。默认为 false
    # 自己的配置
    topics: topic_demo_spring
    tags: tag_demo_spring

```
### 6.4 编写生产者和消费者
### 6.4.1 生产者
```java
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <h1>生产者</h1>
 * demo 通过浏览器测试方便
 * @author Vincent Vic
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("mq")
public class RocketMQProducerController {

    /**
     * rocketmq模板注入
     */
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.topics}")
    private String topics;
    @Value("${rocketmq.producer.tags}")
    private String tags;

    /**
     * RocketMQTemplate 测试
     * @return
     */
    @RequestMapping("/send")
    public Map send() throws InterruptedException, RemotingException,
        MQClientException, MQBrokerException {
        Map<String,String> map = new HashMap<>();
        map.put("datetime", LocalDateTime.now().toString());
        log.info("发送MQ消息内容：" + JSON.toJSONString(map));
        //使用Spring 提供的接口不能使用 org.apache.rocketmq.common.message.Message
        // 应该使用org.springframework.messaging.Message
        //消息需要通过，MessageBuilder构建
        Message sendMsg = MessageBuilder.withPayload(JSON.toJSONString(map)).build();
        //如果直接填写topic 默认没有tag
        String dest = String.format("%s:%s",topics,tags);
        // 默认3秒超时
        rocketMQTemplate.send(dest,sendMsg);

        log.info("向 [{}] 消息发送完成",dest);
        return new HashMap();
    }

}

```
> 这类只写最简单的方式，看看模板的接口就知道了
### 6.4.2 消费者
```java
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>消费者监听</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "${rocketmq.producer.group}",
    topic = "${rocketmq.producer.topics}", selectorExpression = "${rocketmq.producer.tags}",
    consumeMode = ConsumeMode.ORDERLY)
public class ConsumerListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String s) {
        Map<String,String> map = JSONObject.parseObject(s, HashMap.class);
        log.info("接收到消息: {}",map);
    }
}

```
### 6.4 测试
> http://localhost:8080/mq/send



简略版本以及Stream方式集成: [【Spring Cloud Alibaba】消息驱动 Kafka RocketMQ RabbitMQ Stream](https://blog.csdn.net/Vincent_Vic_/article/details/125198287)

