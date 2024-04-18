package com.twentyfive.apaapilayer.models;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Cart;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;

@Document("customers")
public class CustomerAPA extends Customer {


}
