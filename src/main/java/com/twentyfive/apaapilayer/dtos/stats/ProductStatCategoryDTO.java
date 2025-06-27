package com.twentyfive.apaapilayer.dtos.stats;

import com.twentyfive.apaapilayer.dtos.IngredientMinimalAPADTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatCategoryDTO {
    private String idProduct;
    private String name;
    private String ingredientNames;
    private long quantity;
    private double totalWeight;
    private double totalRevenue;

}
