package com.dange.tanmay.service;

import com.dange.tanmay.common.AccountEvent;
import com.dange.tanmay.common.AccountingStatus;
import com.dange.tanmay.common.OrderEvent;
import com.dange.tanmay.common.OrderStatus;
import com.dange.tanmay.entity.AccountBalance;
import com.dange.tanmay.entity.AccountEntity;
import com.dange.tanmay.entity.Transaction;
import com.dange.tanmay.repository.AccountRepository;
import com.dange.tanmay.repository.AccountEntityRepository;
import com.dange.tanmay.repository.TransactionRepository;
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
    private AccountService service;

    @RabbitListener(queues = {"${queue.name}"})
    public void receive(@Payload OrderEvent order) throws InterruptedException {
        log.info("Message Received"+ order);

        if (order.getStatus() == OrderStatus.CREATED) {
            service.processCreatedMessage(order);
        } else if (order.getStatus() == OrderStatus.CANCELLED){
            service.processRollback(order);
        }
        else{
            log.warn("Unknown Order Status");
        }

    }

}
