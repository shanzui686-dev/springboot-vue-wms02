package com.wms.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseReturnVO extends PurchaseReturn {
    private String purchaseNo;
    private String creatorName;
    private String auditorName;
    private String goodsName;
    private BigDecimal returnAmount;
}
