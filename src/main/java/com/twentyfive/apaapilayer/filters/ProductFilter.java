package com.twentyfive.apaapilayer.filters;

import com.twentyfive.apaapilayer.models.ValueRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilter extends Filter {
    private List<String> ingredientNames;
    private ValueRange values;
}
