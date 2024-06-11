package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.ProductStatAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.WeightRange;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductKgAPADTO {
    private String id;
    private String name;
    private List<Allergen> allergens = new ArrayList<>();
    private ProductStatAPA stats;
    private List<String> ingredients;
    private String pricePerKg;
    private String imageUrl;
    private boolean active;
    private String description;
    private WeightRange weightRange;
    private boolean customized;
}
