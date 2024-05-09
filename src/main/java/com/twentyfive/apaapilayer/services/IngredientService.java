package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.DropdownRes;
import com.twentyfive.apaapilayer.DTOs.IngredientsAPADTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.*;

@RequiredArgsConstructor
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;
    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final CategoryRepository categoryRepository;


    private IngredientsAPADTO ingredientsToDTO(IngredientAPA ingredient){
        IngredientsAPADTO dto = new IngredientsAPADTO();
        dto.setIdCategory(ingredient.getCategoryId());
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

    public Page<IngredientsAPADTO> findByIdCategory(String idCategory, int page, int size, String sortColumn,String sortDirection) {
        List<IngredientAPA> ingredients = ingredientRepository.findAllByCategoryId(idCategory);
        List<IngredientsAPADTO> realIngredients = new ArrayList<>();
        for(IngredientAPA ingredient : ingredients){
            if(ingredient!=null) {
                IngredientsAPADTO dto = ingredientsToDTO(ingredient);
                realIngredients.add(dto);
            }
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realIngredients,pageable);
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realIngredients,pageable);    }


    public IngredientsAPADTO getById(String id) {
        IngredientAPA ingredientAPA = ingredientRepository.findById(id).orElse(null);
        if(ingredientAPA==null)
            return null;
        return ingredientsToDTO(ingredientAPA);
    }

    @Transactional
    public IngredientAPA save(IngredientAPA i) {
        return ingredientRepository.save(i);
    }

    @Transactional
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

    @Transactional
    public boolean activateById(String id){
        IngredientAPA ingredientAPA = ingredientRepository.findById(id).orElse(null);
        if(ingredientAPA != null){

            // Trova tutti i prodotti che contengono esclusivamente l'ingrediente in questione
            List<ProductKgAPA> prodottiAlKg = productKgRepository.findAllByIngredientIdsContaining(id);
            List<ProductWeightedAPA> prodottiWeighted = productWeightedRepository.findAllByIngredientIdsContaining(id);

            // Verifica e aggiorna lo stato dei prodotti
            for(ProductKgAPA productKg : prodottiAlKg){
                List<String> idIngredientiPerProdottoKg = productKg.getIngredientIds();
                List<IngredientAPA> ingredientiDisattivati = new ArrayList<>();
                for(String idIngrediente : idIngredientiPerProdottoKg){
                    IngredientAPA ingrediente = ingredientRepository.findById(idIngrediente).orElse(null);
                    if(ingrediente != null && !ingrediente.isActive())
                        ingredientiDisattivati.add(ingrediente);
                }
                if(ingredientiDisattivati.size()==1 && ingredientiDisattivati.contains(ingredientAPA)) { // Verifica se l'ingrediente è l'unico presente
                    productKg.setActive(true);
                    productKgRepository.save(productKg);
                }
            }

            for(ProductWeightedAPA productWeighted : prodottiWeighted){
                List<String> idIngredientiPerProdottoWeighted = productWeighted.getIngredientIds();
                List<IngredientAPA> ingredientiDisattivati = new ArrayList<>();
                for(String idIngrediente : idIngredientiPerProdottoWeighted){
                    IngredientAPA ingrediente = ingredientRepository.findById(idIngrediente).orElse(null);
                    if(ingrediente != null && !ingrediente.isActive())
                        ingredientiDisattivati.add(ingrediente);
                }
                if(ingredientiDisattivati.size()==1 && ingredientiDisattivati.contains(ingredientAPA)) { // Verifica se l'ingrediente è l'unico presente
                    productWeighted.setActive(true);
                    productWeightedRepository.save(productWeighted);
                }
            }

            ingredientAPA.setActive(true);
            ingredientRepository.save(ingredientAPA);
            return true;
        }
        return false;
    }

    public List<DropdownRes> getAllByTypeCategories(List<String> types) {
        List<CategoryAPA> categories = categoryRepository.findAllByTypeInAndEnabledTrue(types);
        List<String> idCategories = new ArrayList<>();
        for (CategoryAPA category: categories){
            idCategories.add(category.getId());
        }
        List<IngredientAPA> ingredients= ingredientRepository.findAllByCategoryIdInAndActiveTrue(idCategories);
        List<DropdownRes> ingredientNames = new ArrayList<>();
        for (IngredientAPA ingredient: ingredients){
            DropdownRes dropdownRes = new DropdownRes();
            dropdownRes.setActionName(ingredient.getName());
            dropdownRes.setId(ingredient.getId());
            ingredientNames.add(dropdownRes);
        }
        Collections.sort(ingredientNames, Comparator.comparing(DropdownRes::getActionName));
        return ingredientNames;
    }

    public IngredientAPA getByName(String name) {
        Optional<IngredientAPA> ingredient=ingredientRepository.findByName(name);
        return ingredient.orElse(null);
    }
}
