package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Order;

@Document("testorders")
public class OrderAPA extends Order {
}
