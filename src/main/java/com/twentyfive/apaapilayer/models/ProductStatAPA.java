package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductStat;


@Document("statistics")
public class ProductStatAPA extends ProductStat {
}
