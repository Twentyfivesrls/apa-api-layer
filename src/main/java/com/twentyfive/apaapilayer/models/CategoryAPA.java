package com.twentyfive.apaapilayer.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Category;

@Document(value = "categories")
@Data
public class CategoryAPA extends Category {
    private boolean aggregateQuantities;

}
