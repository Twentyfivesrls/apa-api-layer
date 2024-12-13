package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Message;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeCouponDTO {
    private String code;
    private Message home;
}
