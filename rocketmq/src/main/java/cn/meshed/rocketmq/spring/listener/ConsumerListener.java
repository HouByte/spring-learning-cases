package cn.meshed.rocketmq.spring.listener;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>生产者</h1>
 * demo 通过浏览器测试方便
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
