package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CustomizableIngredientDTO;
import com.twentyfive.apaapilayer.dtos.IngredientMinimalAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.CustomizableIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientMapperService {
    private final IngredientRepository ingredientRepository;

    public List<String> ingredientsIdToIngredientsNameList(List<IngredientAPA> ingredients) {
        return ingredients.stream()
                .map(IngredientAPA::getName)  // Supponendo che il nome dell'ingrediente sia ottenuto con getName()
                .collect(Collectors.toList());
    }

    public CustomizableIngredientDTO mapCustomizableIngredientToCustomizableIngredientDTO(CustomizableIngredient possibleCustomization, String name, List<IngredientMinimalAPADTO> customizableIngredientNames) {
        return new CustomizableIngredientDTO(
                possibleCustomization.getId(),
                name,
                customizableIngredientNames,
                possibleCustomization.getMaxCustomizable()
        );
    }

    public List<IngredientMinimalAPADTO> mapListIngredientToMinimalListIngredient(List<IngredientAPA> ingredients) {
        List<IngredientMinimalAPADTO> minimalIngredients = new ArrayList<>();
        for (IngredientAPA ingredient : ingredients) {
            IngredientMinimalAPADTO ingredientMinimalAPADTO = mapIngredientToMinimalIngredient(ingredient);
            minimalIngredients.add(ingredientMinimalAPADTO);
        }
        return minimalIngredients;
    }

    public IngredientMinimalAPADTO mapIngredientToMinimalIngredient(IngredientAPA ingredient){
        return new IngredientMinimalAPADTO(
                ingredient.getId(),
                ingredient.getName()
        );
    }

    public List<IngredientMinimalAPADTO> ingredientsIdToMinimalIngredientDTO(List<String> ingredientIds) {
        List<IngredientAPA> ingredients = ingredientRepository.findAllById(ingredientIds);
        return mapListIngredientToMinimalListIngredient(ingredients);
    }
}
