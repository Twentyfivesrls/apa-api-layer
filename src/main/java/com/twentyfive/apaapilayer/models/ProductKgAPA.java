package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductKg;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.WeightRange;

@Document("products")
public class ProductKgAPA extends ProductKg {
}
