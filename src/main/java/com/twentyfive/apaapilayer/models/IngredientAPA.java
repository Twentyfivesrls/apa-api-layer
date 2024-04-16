package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Ingredient;

@Document("ingredients")
public class IngredientAPA extends Ingredient {
}
