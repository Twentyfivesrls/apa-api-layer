package com.twentyfive.apaapilayer.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;
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

    private List<PieceInPurchaseDTO> pieces;

    public BundleInPurchaseDTO(BundleInPurchase bundle, String name,List<PieceInPurchaseDTO> pieces) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.quantity = bundle.getQuantity();
        this.totalPrice = bundle.getTotalPrice();
        this.pieces = pieces;
    }
    public BundleInPurchaseDTO(BundleInPurchase bundle, String name) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.quantity = bundle.getQuantity();
        this.totalPrice = bundle.getTotalPrice();
    }
}
