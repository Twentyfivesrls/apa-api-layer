package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.AutoCompleteRes;
import com.twentyfive.apaapilayer.dtos.IngredientAPADTO;
import com.twentyfive.apaapilayer.filters.IngredientFilter;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.FilterUtilities;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;
    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;


    private IngredientAPADTO ingredientsToDTO(IngredientAPA ingredient){
        IngredientAPADTO dto = new IngredientAPADTO();
        dto.setIdCategory(ingredient.getCategoryId());
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setActive(ingredient.isActive());
        dto.setNote(ingredient.getDescription());
        dto.setAlcoholic(ingredient.isAlcoholic());
        dto.setStatus(ingredient.isActive() ? "ATTIVO" : "DISATTIVO");
        dto.setAlcoholicString(ingredient.isAlcoholic() ? "SI" : "NO");
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

    public Page<IngredientAPADTO> findByIdCategory(String idCategory, int page, int size, String sortColumn, String sortDirection, IngredientFilter filters) {
        Query query = new Query();
        query = FilterUtilities.applyIngredientFilters(query,filters,idCategory);
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else {
            sort = Sort.by(Sort.Direction.fromString(sortDirection),sortColumn);
        }
        Pageable pageable=PageRequest.of(page,size,sort);
        List<IngredientAPA> ingredients = mongoTemplate.find(query, IngredientAPA.class);
        List<IngredientAPADTO> realIngredients = ingredients.stream()
                .map(this::ingredientsToDTO)
                .collect(Collectors.toList());
        return PageUtilities.convertListToPageWithSorting(realIngredients,pageable);
    }


    public IngredientAPADTO getById(String id) {
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
            List<ProductKgAPA> prodottiAlKg = productKgRepository.findAllByIngredientIdsContainingAndPricePerKgNotNull(id);
            for(ProductKgAPA productkg : prodottiAlKg){
                productkg.setActive(false);
                productKgRepository.save(productkg);
            }
            List<ProductWeightedAPA> prodottiWeighted = productWeightedRepository.findAllByIngredientIdsContainingAndWeightNotNull(id);
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
            List<ProductKgAPA> prodottiAlKg = productKgRepository.findAllByIngredientIdsContainingAndPricePerKgNotNull(id);
            List<ProductWeightedAPA> prodottiWeighted = productWeightedRepository.findAllByIngredientIdsContainingAndWeightNotNull(id);

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

    public List<IngredientAPA> getAllByTypeCategories(String type) {
        List<CategoryAPA> categories = categoryRepository.findAllByTypeAndEnabledTrueAndSoftDeletedFalse(type);
        List<IngredientAPA> ingredients= new ArrayList<>();
        for (CategoryAPA category: categories){
            ingredients.addAll(ingredientRepository.findAllByCategoryIdAndActiveTrueOrderByNameAsc(category.getId()));
        }
        return ingredients;
    }

    public IngredientAPA getByName(String name) {
        Optional<IngredientAPA> ingredient=ingredientRepository.findByName(name);
        return ingredient.orElse(null);
    }

    public List<IngredientAPA> getAllByNameCategories(String name, String type) {
        List<CategoryAPA> categories = categoryRepository.findAllByTypeAndNameAndEnabledTrue(type,name);
        List<IngredientAPA> ingredients= new ArrayList<>();
        for (CategoryAPA category: categories){
            ingredients.addAll(ingredientRepository.findAllByCategoryIdAndActiveTrueOrderByNameAsc(category.getId()));
        }
        return ingredients;
    }

    public List<AutoCompleteRes> getIngredientsAutocomplete(String name) {
        List<IngredientAPA> ingredients = ingredientRepository.findByNameContainsIgnoreCase(name);
        return ingredients.stream().map(ingredient -> new AutoCompleteRes(ingredient.getName())).toList();
    }

    public boolean deleteById(String id) {
        Optional<IngredientAPA> optIngredient = ingredientRepository.findById(id);
        if (optIngredient.isPresent()) {
            ingredientRepository.deleteById(id);
            Query query = new Query(Criteria.where("ingredientiIds").in(id));
            Update update = new Update().pull("ingredientiIds", id);
            mongoTemplate.updateMulti(query, update, ProductKgAPA.class);
            mongoTemplate.updateMulti(query, update, ProductWeightedAPA.class);
            return true;
        }
        return false;
    }
}
