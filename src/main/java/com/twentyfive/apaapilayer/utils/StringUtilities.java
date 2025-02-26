package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.IngredientAPA;

import java.util.*;

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

    public static long countUniqueStrings(List<String> list1, List<String> list2) {
        Set<String> uniqueStrings = new HashSet<>();
        if (list1 != null) uniqueStrings.addAll(list1);
        if (list2 != null) uniqueStrings.addAll(list2);
        return uniqueStrings.size();
    }

    public static List<String> mergeUniqueValues(List<String> list1, List<String> list2) {
        Set<String> set1 = new HashSet<>(Optional.ofNullable(list1).orElse(Collections.emptyList()));
        Set<String> set2 = new HashSet<>(Optional.ofNullable(list2).orElse(Collections.emptyList()));

        // Trova gli elementi unici in entrambe le liste
        Set<String> uniqueValues = new HashSet<>(set1);
        uniqueValues.addAll(set2); // Unione dei due set

        // Rimuove gli elementi comuni (quelli duplicati)
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2); // Mantiene solo gli elementi presenti in entrambi

        uniqueValues.removeAll(intersection); // Rimuove i duplicati

        return new ArrayList<>(uniqueValues);
    }
}
