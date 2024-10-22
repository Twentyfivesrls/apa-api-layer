package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFixedAPADTO {
    private String id;
    private String name;
    private Set<Allergen> allergens;
    private String ingredientsName;
    private double realPrice;
    private String price;
    private boolean active;
}
