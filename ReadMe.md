# Orchestration Saga 

In Orchestration-based Saga, a central orchestrator coordinates the entire transaction. The orchestrator is responsible for determining the order of operations and instructing each service to perform its actions.

In this Project there are 3 services:
1) Order Service : Orchestrator
2) Account Service : Participant
3) Inventory Service : Participant


## Working
- The Order Service acts as a orchestrator.
- It receives the order which includes accountId, ProductId , Quantity of the product.
- It then sends OrderEvent on Topic (Exchange) - order-exchange .
- OrderExchange is bound to two queues -product-queue and account-queue.
- Thus OrderEvent is published to both the queues product-queue and account-queue
- The inventory-service subscribes to product-queue and account-service subscribles to account-queue.
- Both process the OrderEvent independently and sends response to account-response queue.
- order-service subscribes to the account-response queue.
- If any of the inventory-service or account-service reports FAILED transaction, it re-sends OrderEvent with ORDER_CANCELLED status.
- On receiving the OrderEvent with ORDER_CANCELLED both the microservices initates rollback of the order.


## Steps:
- Maven Build all the 3 services.
- Run Maven Build of Order Service with -DskipTests=true or start Product Service and Account Service before running the maven build of Order Service.
- Start all the 3 services:
- Open Browser and create order

## Pre-requisites
1) RabbitMQ running locally or inside the container.
2) Create an exchange order-exchange
![](./images/1.png)
3) Create 3 queues
   1) product-queue 
   2) account-queue 
   3) order-queue

![](./images/2.png)
![](./images/3.png)

- Bind the exchange to product-queue and account-queue.
## Tests

There are 4 scenarios:

1) Happy Scenario : Both Inventory Service and Account Service were able to complete the transation.
`http://localhost:8082/createOrder?accountId=1&productId=1&quantity=5`

![](./images/4.png)
- The order is completed
![](./images/5.png)
- The inventory service transaction is SUCCESSFUL.
![](./images/6.png)
- The account service transaction is SUCCESSFUL.
![](./images/7.png)
- Final order status
![](./images/8.png)



2) Failure Scenario 1 : Inventory Service reported less product in inventory than ordered value. Account Service has sufficient balance in the account.
`http://localhost:8082/createOrder?accountId=2&productId=2&quantity=8`

- The order is CANCELLED
![](./images/9.png)
- The inventory service transaction is FAILED.
![](./images/10.png)
- The account service transaction is PAYMENT_ROLLEDBACK.
![](./images/11.png)
- Final order status is CANCELLED.
![](./images/12.png)


3) Failure Scenario 2 : Inventory Service reported sufficient product quantity in inventory than ordered value. Account Service reported in-sufficient balance in the account.
`http://localhost:8082/createOrder?accountId=3&productId=3&quantity=5`

- The order is CANCELLED
![](./images/13.png)
- The inventory service transaction is ROLLEDBACK.
![](./images/14.png)
- The account service transaction is FAILED.
![](./images/15.png)
- Final order status is CANCELLED
![](./images/16.png)


4) Failure Scenario 3 : Inventory Service reported in-sufficient product quantity in inventory than ordered value. Account Service reported in-sufficient balance in the account.
`http://localhost:8082/createOrder?accountId=4&productId=4&quantity=8`

- The order is CANCELLED.
![](./images/17.png)
- The inventory service transaction is FAILED.
![](./images/18.png)
- The account service transaction is FAILED.
![](./images/20.png)
- Final order status is CANCELLED.



