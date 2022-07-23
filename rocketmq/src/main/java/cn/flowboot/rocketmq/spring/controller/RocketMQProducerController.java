package cn.flowboot.rocketmq.spring.controller;

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
 * <h1></h1>
 *
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
