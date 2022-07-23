package cn.flowboot.rocketmq.orderly.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * <h1></h1>
 *
 * @author hougq
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