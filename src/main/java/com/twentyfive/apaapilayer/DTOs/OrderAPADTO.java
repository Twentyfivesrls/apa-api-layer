package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAPADTO {

    private String id;
    private String firstName;
    private String lastName;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private String price;
    private String status;
    private List<ProductInPurchase> products;
    private List<BundleInPurchase> bundles;
    private String email;
    private String phoneNumber;

}