package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalCategoryStatDTO {
    private String idCategory;
    private String name;
    private long usedIngredients;
    private long totalUsedIngredients;
}
