package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductKgAPADTO {
    private String id;
    private String nome;
    private List<Allergen> allergens;
    private List<String> ingredients;
    private String pricePerKg;
    private String imageUrl;
    private boolean enable;
}
