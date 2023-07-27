package com.dange.tanmay.service;

import com.dange.tanmay.common.*;
import com.dange.tanmay.entity.OrderEntity;
import com.dange.tanmay.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    TopicPublisher topicPublisher;


    public void processAccountingEvent(Event event){
        AccountEvent accountingEvent=(AccountEvent) event;
        log.info("Accounting Event Received"+ accountingEvent);
        OrderEntity order =  orderRepository.findById(accountingEvent.getOrderId()).orElseThrow(() -> new RuntimeException("Order Not Found"));
        order.setAccountingStatus(accountingEvent.getStatus());
        orderRepository.save(order);
        updateOrderStatus(order);
    }


    public void processInventoryEvent(Event event){
        InventoryEvent inventoryEvent=(InventoryEvent) event;
        log.info("Inventory Event Received"+ inventoryEvent);
        OrderEntity order =  orderRepository.findById(inventoryEvent.getOrderId()).orElseThrow(()->new RuntimeException("Order Not Found"));
        order.setInventoryStatus(inventoryEvent.getStatus());
        orderRepository.save(order);
        updateOrderStatus(order);

    }

    private void updateOrderStatus(OrderEntity order){

        if (order.getAccountingStatus() == null || order.getInventoryStatus() == null){
            log.info("Waiting for all the responses to be received");
            return;
        }
        if (order.getInventoryStatus() ==  InventoryStatus.SUCCESSFUL && order.getAccountingStatus() ==  AccountingStatus.PAYMENT_SUCCESSFUL){
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setStatus(OrderStatus.CANCELLED);

            OrderEvent event = OrderEvent.builder().orderId(order.getOrderId())
                    .accountId(order.getAccountId())
                    .productId(order.getProductId())
                    .quantity(order.getQuantity())
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .build();

            topicPublisher.send(event);
            log.info("Cancelling Order..");

        }
        orderRepository.save(order);
        log.info("Processing Complete for Order:+"+ order.getOrderId());
    }
}
