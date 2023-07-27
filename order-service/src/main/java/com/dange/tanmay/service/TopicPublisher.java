package com.dange.tanmay.service;

import com.dange.tanmay.common.OrderEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopicPublisher {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${topic.name}")
    private String topicExchangeName;


    public void send(OrderEvent order) {
        amqpTemplate.convertAndSend(topicExchangeName,"*", order);
    }

}
