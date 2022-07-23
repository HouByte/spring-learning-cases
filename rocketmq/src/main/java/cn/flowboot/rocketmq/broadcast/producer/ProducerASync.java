package cn.flowboot.rocketmq.broadcast.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * <h1></h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
public class ProducerASync {

    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("demo_producer_broadcasting_group");
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
            Message msg = new Message("Topic_broadcasting_demo", "Tag_broadcasting_demo",
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
