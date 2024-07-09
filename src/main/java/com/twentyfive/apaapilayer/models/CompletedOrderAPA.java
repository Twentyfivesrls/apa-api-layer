package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CompletedOrder;


@Document("completedOrders")
public class CompletedOrderAPA extends CompletedOrder {
}
