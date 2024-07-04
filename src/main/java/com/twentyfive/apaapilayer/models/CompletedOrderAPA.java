package com.twentyfive.apaapilayer.models;

import com.twentyfive.apaapilayer.dtos.CustomInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CompletedOrder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("completedOrders")
public class CompletedOrderAPA extends CompletedOrder {
    private CustomInfo customInfo; // admin can choose customInfo for order and not himself from idCustomer
}
