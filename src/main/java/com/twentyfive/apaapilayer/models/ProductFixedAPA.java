package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductFixed;

@Document("products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFixedAPA extends ProductFixed {
    @DBRef
    private ProductStatAPA stats;
}
