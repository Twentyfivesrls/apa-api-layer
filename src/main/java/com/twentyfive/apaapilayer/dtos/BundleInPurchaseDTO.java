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

//    private int quantity;
    private double totalPrice;
    private double totalWeight;
    private List<Allergen> allergens;
    private List<PieceInPurchaseDTO> weightedProducts;
    private String location; // Luogo in cui è depositato il prodotto
    private boolean toPrepare;

    public BundleInPurchaseDTO(BundleInPurchase bundle, String name,List<PieceInPurchaseDTO> weightedProducts) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.location = bundle.getLocation();
        this.totalWeight = bundle.getTotalWeight();
        this.totalPrice = bundle.getTotalPrice();
        this.weightedProducts = weightedProducts;
        this.allergens= bundle.getAllergens();
    }
    public BundleInPurchaseDTO(BundleInPurchase bundle, String name) {
        this.id = bundle.getId();
        this.name = name;
        this.measure = bundle.getMeasure();
        this.totalPrice = bundle.getTotalPrice();
        this.allergens = bundle.getAllergens();
        this.location = bundle.getLocation();
        this.toPrepare = bundle.isToPrepare();
    }

}
