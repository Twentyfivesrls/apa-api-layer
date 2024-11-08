package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilter {
    private List<String> allergenNames;
    private List<String> ingredientIds;
    private ValueRange values;
    private String name;
}
