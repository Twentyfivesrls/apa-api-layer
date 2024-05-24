package com.twentyfive.apaapilayer.DTOs;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private String customerId;
    private List<ItemInPurchaseDTO> purchases = new ArrayList<>();

    public CartDTO(CustomerAPA capa) {
        this.customerId = capa.getId();
        for(ItemInPurchase item : capa.getCart().getPurchases()){
            ItemInPurchaseDTO itemDTO= new ItemInPurchaseDTO(item);
            this.purchases.add(itemDTO);
        }
    }
}
