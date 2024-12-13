package com.twentyfive.apaapilayer.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientFilter extends Filter {

    private List<String> allergenNames;
}
