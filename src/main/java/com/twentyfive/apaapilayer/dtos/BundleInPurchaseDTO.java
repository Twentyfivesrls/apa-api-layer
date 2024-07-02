package com.twentyfive.apaapilayer.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Measure;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BundleInPurchaseDTO extends ItemInPurchaseDTO{
    private String id;

    private String name;

    private Measure measure;

    private int quantity;
    private double totalPrice;
    private double totalWeight;
    private List<Allergen> allergens;
    private List<PieceInPurchaseDTO> weightedProducts;

    public BundleInPurchaseDTO(BundleInPurchase bundle, String name,List<PieceInPurchaseDTO> weightedProducts) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.quantity = bundle.getQuantity();
        this.totalWeight = bundle.getTotalWeight();
        this.totalPrice = bundle.getTotalPrice();
        this.weightedProducts = weightedProducts;
        this.allergens= bundle.getAllergens();
    }
    public BundleInPurchaseDTO(BundleInPurchase bundle, String name) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.quantity = bundle.getQuantity();
        this.totalPrice = bundle.getTotalPrice();
        this.allergens = bundle.getAllergens();
    }

}
