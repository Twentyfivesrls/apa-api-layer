package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.IngredientAPA;

import java.util.List;

public class StringUtilities {

    public static String ingredientsToString(List<String> ingredients) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
