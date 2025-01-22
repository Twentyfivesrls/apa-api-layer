package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.GeneralIngredientStat;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalIngredientStatDTO {
    private GeneralIngredientStat generalStat;
    private List<GlobalCategoryStatDTO> globalCategoryStats;
}
