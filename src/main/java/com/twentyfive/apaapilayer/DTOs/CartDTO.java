package com.twentyfive.apaapilayer.DTOs;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private String customerId;
    private List<ProductInPurchase> productsByWeight;
    private List<BundleInPurchase> bundles;


    public CartDTO (CustomerAPA capa){
        this.customerId=capa.getId();
        this.productsByWeight=capa.getCart().getProductsByWeight();
        this.bundles=capa.getCart().getBundles();
    }

}
