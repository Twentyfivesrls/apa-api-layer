package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.ProductStatAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.WeightRange;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductKgAPADetailsDTO {
    private String id;
    private String name;
    private String categoryName;
    private String description;
    private ProductStatAPA stats;
    private Set<Allergen> allergens;
    private List<String> ingredients;
    private List<CustomizableIngredientDTO> customizableIngredients;
    private String pricePerKg;
    private WeightRange weightRange;
    private String imageUrl;
}
