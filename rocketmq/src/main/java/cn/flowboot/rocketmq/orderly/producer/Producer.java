package cn.flowboot.rocketmq.orderly.producer;

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
