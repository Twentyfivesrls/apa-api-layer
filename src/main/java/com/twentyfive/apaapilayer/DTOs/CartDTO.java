package com.twentyfive.apaapilayer.DTOs;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private String customerId;
    private List<ItemInPurchase> purchases;

    public CartDTO(CustomerAPA capa) {
        this.customerId = capa.getId();
        this.purchases = capa.getCart().getPurchases();
    }
}
