package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponDTO {
    private String id;
    private boolean active;
    private String name;
    private String code;
    private String validationPeriod; //String buildata tra il range delle date del coupon
    private String value; //Valore che può essere % o €
    private String priceRange; //String buildata sul range di prezzo
    private int maxTotalUsage;
}
