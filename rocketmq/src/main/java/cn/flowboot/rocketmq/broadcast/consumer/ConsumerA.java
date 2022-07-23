package cn.flowboot.rocketmq.broadcast.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * <h1>广播消息消费者</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ConsumerA {
    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_producer_broadcasting_group");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 默认均衡轮询消费模式 改为广播模式
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // 3.订阅主题Topic和Tag
        consumer.subscribe("Topic_broadcasting_demo", "*");

        // 4.设置回调函数，处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            // 接受消息内容
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println(
                        "A----consumeThread=" + Thread.currentThread().getName() + "," + new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 5.启动消费者consumer
        consumer.start();

        System.out.println("消费者启动");
    }
}