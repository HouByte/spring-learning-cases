package cn.flowboot.rocketmq.batch.splitter.producer;

import cn.flowboot.rocketmq.batch.splitter.ListSplitter;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <h1></h1>
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