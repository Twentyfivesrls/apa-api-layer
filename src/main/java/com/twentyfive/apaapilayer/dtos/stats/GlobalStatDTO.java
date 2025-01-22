package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.GlobalIngredientStat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatDTO {
    GlobalProductStatDTO globalProductStat;
    GlobalIngredientStatDTO globalIngredientStat;
}
