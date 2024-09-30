package com.twentyfive.apaapilayer.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;

@Document("customers")
@Data
public class CustomerAPA extends Customer {


}
