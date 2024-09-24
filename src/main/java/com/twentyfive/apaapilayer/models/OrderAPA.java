package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Order;

@Data
@Document("orders")
@NoArgsConstructor
@AllArgsConstructor
public class OrderAPA extends Order {

    private boolean bakerUnread; //status unread per pasticceria
    private boolean counterUnread; //status unread per bancone

}
