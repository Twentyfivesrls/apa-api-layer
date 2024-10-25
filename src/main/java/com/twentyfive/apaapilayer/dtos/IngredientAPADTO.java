package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientAPADTO {
    private String id;
    private String name;
    private String idCategory;
    private List<Allergen> allergens;
    private String note;
    private boolean active;
    private boolean alcoholic;
    private String status;
    private String alcoholicString;
}
