package com.twentyfive.apaapilayer.models;


import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Category;

@Document(value = "category")
public class CategoryAPA extends Category {

}
