package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralIngredientStatDTO {
    private List<String> distinctUsedIngredients; //Id ingredienti usati diversi
    private long totalIngredients; //Tutti gli ingredienti presenti al giorno in cui si è fatta la statistica
    private long totalUsedIngredients; //Totale ingredienti usati

    public long getUsedIngredients() {
        return distinctUsedIngredients !=null ? distinctUsedIngredients.size() : 0;
    }
}
