package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientStatDTO {
    private String ingredientId;
    private String name;
    private long totalTimeUsed;

}
