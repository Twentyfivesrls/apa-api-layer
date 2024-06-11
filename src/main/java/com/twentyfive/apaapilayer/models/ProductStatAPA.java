package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductStat;


@Document("statistics")
@NoArgsConstructor
public class ProductStatAPA extends ProductStat {
    public ProductStatAPA(String type) {
        super(type); // Chiama il costruttore della classe padre con il parametro type
    }
}
