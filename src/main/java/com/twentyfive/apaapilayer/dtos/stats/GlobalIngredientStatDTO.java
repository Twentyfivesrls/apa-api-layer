package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalIngredientStatDTO {
    private GeneralIngredientStatDTO generalStat;
    private List<GlobalCategoryStatDTO> globalCategoryStats;
}
