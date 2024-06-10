package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductKg;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.WeightRange;

@Document("products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductKgAPA extends ProductKg {
    @DBRef
    private ProductStatAPA stats;
}
