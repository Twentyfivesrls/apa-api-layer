package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.SimpleOrderRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReq {
    private SimpleOrderRequest simpleOrderRequest;
    private BuyInfosDTO buyInfos;
}
