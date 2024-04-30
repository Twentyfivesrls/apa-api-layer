package com.twentyfive.apaapilayer.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Measure;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BundleInPurchaseDTO {
    private String id;

    private String name;

    private Measure measure;

    private int quantity;
    private double totalPrice;

    private List<ProductInPurchaseDTO> pices;

    public BundleInPurchaseDTO(BundleInPurchase bundle, String name,List<ProductInPurchaseDTO> pices) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.quantity = bundle.getQuantity();
        this.totalPrice = bundle.getTotalPrice();
        this.pices=pices;

    }

}
