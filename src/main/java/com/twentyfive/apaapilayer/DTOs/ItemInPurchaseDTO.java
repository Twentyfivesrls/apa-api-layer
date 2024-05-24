package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInPurchaseDTO {
    private String id;
    private int quantity;
    private double totalPrice;
    private String notes;

    public ItemInPurchaseDTO(ItemInPurchase iIP){
        this.id = iIP.getId();
        this.quantity = iIP.getQuantity();
        this.totalPrice = iIP.getTotalPrice();
        this.notes = iIP.getNotes();
    }
}
