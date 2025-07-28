package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CustomTimeCategory;

@Document("customTimeCategories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomTimeCategoryAPA extends CustomTimeCategory {
    @DBRef
    @Indexed(unique = true)
    private CategoryAPA category;
}
