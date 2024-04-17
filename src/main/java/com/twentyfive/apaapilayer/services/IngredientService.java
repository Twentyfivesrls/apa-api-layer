package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.IngredientsAPADTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;
    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;


    private IngredientsAPADTO ingredientsToDTO(IngredientAPA ingredient){
        IngredientsAPADTO dto = new IngredientsAPADTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setActive(ingredient.isActive());
        dto.setNote(ingredient.getDescription());
        dto.setAlcoholic(ingredient.isAlcoholic());
        List<String> allergeni = ingredient.getAllergenNames();
        List<Allergen> realAllergeni = new ArrayList<>();
        for(String allergene : allergeni){
            Allergen realAllergene = allergenRepository.findByName(allergene).orElse(null);
            if(realAllergene!=null)
                realAllergeni.add(realAllergene);
        }
        dto.setAllergens(realAllergeni);
        return dto;
    }

    public Page<IngredientsAPADTO> findByIdCategory(String idCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<IngredientAPA> ingredients = ingredientRepository.findAllByCategoryId(idCategory);
        List<IngredientsAPADTO> realIngredients = new ArrayList<>();
        for(IngredientAPA ingredient : ingredients){
            if(ingredient!=null) {
                IngredientsAPADTO dto = ingredientsToDTO(ingredient);
                realIngredients.add(dto);
            }
        }
        return PageUtilities.convertListToPage(realIngredients, pageable);
    }


    public IngredientsAPADTO getById(String id) {
        IngredientAPA ingredientAPA = ingredientRepository.findById(id).orElse(null);
        if(ingredientAPA==null)
            return null;
        return ingredientsToDTO(ingredientAPA);
    }

    public IngredientAPA save(IngredientAPA i) {
        return ingredientRepository.save(i);
    }

    public boolean disableById(String id){
        IngredientAPA ingredientAPA = ingredientRepository.findById(id).orElse(null);
        if(ingredientAPA!=null){
            ingredientAPA.setActive(false);
            ingredientRepository.save(ingredientAPA);
            List<ProductKgAPA> prodottiAlKg = productKgRepository.findAllByIngredientIdsContaining(id);
            for(ProductKgAPA productkg : prodottiAlKg){
                productkg.setActive(false);
                productKgRepository.save(productkg);
            }
            List<ProductWeightedAPA> prodottiWeighted = productWeightedRepository.findAllByIngredientIdsContaining(id);
            for(ProductWeightedAPA productWeighted : prodottiWeighted){
                productWeighted.setActive(false);
                productWeightedRepository.save(productWeighted);
            }
            return true;
        }
        return false;
    }

    public boolean activateById(String id){
        IngredientAPA ingredientAPA = ingredientRepository.findById(id).orElse(null);
        if(ingredientAPA!=null){
            ingredientAPA.setActive(true);
            ingredientRepository.save(ingredientAPA);
            return true;
        }
        else return false;
    }
}
