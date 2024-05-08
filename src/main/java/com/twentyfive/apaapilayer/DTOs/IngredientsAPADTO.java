package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientsAPADTO {
    private String id;
    private String name;
    private String idCategory;
    private List<Allergen> allergens;
    private String note;
    private boolean active;
    private boolean alcoholic;
}
