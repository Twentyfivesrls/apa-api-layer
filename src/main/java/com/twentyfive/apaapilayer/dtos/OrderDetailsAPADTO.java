package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
