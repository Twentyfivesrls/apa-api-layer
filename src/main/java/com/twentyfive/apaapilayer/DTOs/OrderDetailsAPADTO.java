package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsAPADTO {
    private String id;
    private List<ProductInPurchaseDTO> products;
    private List<BundleInPurchaseDTO> bundles;
    private String email;
    private String note;
    private String phoneNumber;
    private String pickupDateTime;
    private double totalPrice;
    private double totalWeight;
}
