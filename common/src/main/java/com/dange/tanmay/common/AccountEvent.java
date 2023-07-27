package com.dange.tanmay.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class AccountEvent implements Event,Serializable {
    private static final long serialVersionUID = -6470090945414208496L;
    private Long id;
    private Long orderId;
    private AccountingStatus status;

}
