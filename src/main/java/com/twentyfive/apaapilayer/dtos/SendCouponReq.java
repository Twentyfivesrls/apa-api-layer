package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Message;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendCouponReq {
    private String code;
    private List<String> emails;
    private Message message;
}
