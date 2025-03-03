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
        Set<String> uniqueElements = new LinkedHashSet<>();
        Set<String> duplicates = new HashSet<>();

        // Aggiungi tutti gli elementi della prima lista
        for (String s : Optional.ofNullable(list1).orElse(Collections.emptyList())) {
            if (!uniqueElements.add(s)) { // Se l'elemento è già presente, è un duplicato
                duplicates.add(s);
            }
        }

        // Aggiungi tutti gli elementi della seconda lista
        for (String s : Optional.ofNullable(list2).orElse(Collections.emptyList())) {
            if (!uniqueElements.add(s)) { // Se l'elemento è già presente, è un duplicato
                duplicates.add(s);
            }
        }

        // Rimuoviamo tutti gli elementi duplicati tra le due liste
        uniqueElements.removeAll(duplicates);

        return new ArrayList<>(uniqueElements);
    }

}
