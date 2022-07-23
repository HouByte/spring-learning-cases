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
 * @author hougq
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