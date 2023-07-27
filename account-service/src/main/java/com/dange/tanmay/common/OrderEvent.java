package com.dange.tanmay.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = -6470090944414208496L;
    private Long orderId;
    private Long accountId;
    private Long productId;
    private Long quantity;
    private Long amount;
    private OrderStatus status;

}
