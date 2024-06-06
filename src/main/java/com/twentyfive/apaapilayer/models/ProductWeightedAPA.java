package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductWeighted;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("products")
public class ProductWeightedAPA extends ProductWeighted {
    @DBRef
    private ProductStatAPA stats;
}
