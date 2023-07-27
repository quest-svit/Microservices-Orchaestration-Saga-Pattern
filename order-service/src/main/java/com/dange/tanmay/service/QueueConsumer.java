package com.dange.tanmay.service;

import com.dange.tanmay.common.*;
import com.dange.tanmay.entity.OrderEntity;
import com.dange.tanmay.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class QueueConsumer {

    @Autowired
    OrderService orderService;

    @RabbitListener(queues = {"${response.queue.name}"})
    public void receive(@Payload Event event) {
        if (event instanceof  AccountEvent){
           orderService.processAccountingEvent(event);
        } else if (event instanceof InventoryEvent){
            orderService.processInventoryEvent(event);
        }
    }
}
