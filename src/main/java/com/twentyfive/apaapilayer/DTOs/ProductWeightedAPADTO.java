package com.twentyfive.apaapilayer.DTOs;

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
    private String weight;
    private String imageUrl;//immagine
    private boolean enable;

}
