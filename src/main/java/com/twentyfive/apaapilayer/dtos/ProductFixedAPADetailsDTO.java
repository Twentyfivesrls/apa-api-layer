package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.ProductStatAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFixedAPADetailsDTO {
    private String id;
    private String name;
    private String description;
    private ProductStatAPA stats;
    private Set<Allergen> allergens;
    private List<String> ingredients;
    private String price;
    private String weight;
    private String imageUrl;
}
