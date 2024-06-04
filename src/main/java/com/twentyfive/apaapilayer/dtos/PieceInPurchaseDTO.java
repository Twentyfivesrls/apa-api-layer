package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieceInPurchaseDTO extends ItemInPurchaseDTO{
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
