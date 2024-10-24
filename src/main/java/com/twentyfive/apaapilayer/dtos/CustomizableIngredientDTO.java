package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.IngredientAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomizableIngredientDTO {
    private String id;
    private String name;
    private List<String> ingredientNames;
    private int maxCustomizable;
}
