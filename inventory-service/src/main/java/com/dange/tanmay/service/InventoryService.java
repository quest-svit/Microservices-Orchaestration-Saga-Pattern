package com.dange.tanmay.service;

import com.dange.tanmay.common.InventoryEvent;
import com.dange.tanmay.common.InventoryStatus;
import com.dange.tanmay.common.OrderEvent;
import com.dange.tanmay.entity.ProductEntity;
import com.dange.tanmay.entity.ProductInventory;
import com.dange.tanmay.entity.Transaction;
import com.dange.tanmay.repository.ProductEntityRepository;
import com.dange.tanmay.repository.ProductInventoryRepository;
import com.dange.tanmay.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductEntityRepository productEntityRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private QueueSender sender;

    public void processCreatedMessage(OrderEvent order){
        Transaction tr = getTransaction(order, order.getQuantity());

        InventoryEvent responseEvent = new InventoryEvent();
        responseEvent.setOrderId(order.getOrderId());

        ProductEntity productEntity = new ProductEntity();
        productEntity.setOrderId(order.getOrderId());

        //get the accountBalnce object from repository using accountID
        ProductInventory productInventory = productInventoryRepository.findById(tr.getProductId()).orElseThrow(() -> new RuntimeException("Product not Found"));
        // check if the orderAmount <= Account Balance
        if (order.getQuantity() <= productInventory.getQuantity()) {
            productInventory.setQuantity(productInventory.getQuantity() - order.getQuantity());
            transactionRepository.save(tr);
            productInventoryRepository.save(productInventory);

            productEntity.setStatus(InventoryStatus.SUCCESSFUL);
            responseEvent.setStatus(InventoryStatus.SUCCESSFUL);
            log.info("Inventory Successful");
        } else {
            System.out.println("Not enough product quantity in inventory. TransactionFailed");
            productEntity.setStatus(InventoryStatus.FAILED);
            responseEvent.setStatus(InventoryStatus.FAILED);
        }

        productEntityRepository.save(productEntity);
        //Publish the event again
        sender.send(responseEvent);

    }

    private Transaction getTransaction(OrderEvent order, Long quantity) {
        return Transaction.builder().productId(order.getProductId())
                .orderId(order.getOrderId())
                .quantity(quantity).build();
    }


    public void processRollback(OrderEvent order){
        System.out.println("Cancellation received for order="+ order.getOrderId());


        ProductEntity productEntity = productEntityRepository.findById(order.getProductId()).orElseThrow(() -> new RuntimeException("ProductEntity Not Found"));
        if (productEntity.getStatus() == InventoryStatus.SUCCESSFUL) {
            Transaction tr = getTransaction(order, -order.getQuantity());

            ProductInventory productInventory = productInventoryRepository.findById(tr.getProductId()).orElseThrow(()->new RuntimeException("Product not found in Inventory"));
            productInventory.setQuantity(productInventory.getQuantity() + order.getQuantity());

            //save transaction using repository
            transactionRepository.save(tr);
            productInventoryRepository.save(productInventory);

            productEntity.setStatus(InventoryStatus.ROLLEDBACK);
            productEntityRepository.save(productEntity);

            log.info("Inventory Rollback Successful");
        }

    }

    }
