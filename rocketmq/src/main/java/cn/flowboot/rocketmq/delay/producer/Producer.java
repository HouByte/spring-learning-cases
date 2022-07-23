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
 * @author hougq
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
