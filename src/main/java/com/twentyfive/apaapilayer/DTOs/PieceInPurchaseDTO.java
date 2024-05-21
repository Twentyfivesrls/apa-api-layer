package com.twentyfive.apaapilayer.DTOs;

import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieceInPurchaseDTO {
    private String id;
    private String name;
    private double weight;
    private int quantity;

    public PieceInPurchaseDTO(PieceInPurchase piece,String name, double weight){
        this.id=piece.getId();
        this.name=name;
        this.weight=weight;
        this.quantity=piece.getQuantity();
    }
}
