package com.twentyfive.apaapilayer.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Ingredient;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document("ingredients")
public class IngredientAPA extends Ingredient {
    private List<String> containNames;
}
