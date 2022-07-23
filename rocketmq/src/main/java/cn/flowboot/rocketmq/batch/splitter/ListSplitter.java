package cn.flowboot.rocketmq.batch.splitter;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <h1>拆分消息工具类</h1>
 *
 * @author hougq
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