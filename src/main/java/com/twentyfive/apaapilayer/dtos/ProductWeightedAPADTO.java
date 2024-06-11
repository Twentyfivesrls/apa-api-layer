package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.ProductStatAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductWeightedAPADTO {
    private String id;
    private String name;
    private List<Allergen> allergens = new ArrayList<>();
    private List<String> ingredients;
    private ProductStatAPA stats;

    private double realWeight;
    private String weight;
    private String imageUrl;//immagine
    private boolean active;
    private String description;

}
