package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.OrderSlot;

@Document("orderSlots")
public class OrderSlotAPA extends OrderSlot {
}
