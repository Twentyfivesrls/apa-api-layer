package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.mappers.IngredientMapperService;
import com.twentyfive.apaapilayer.mappers.ProductMapperService;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductFixedRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductFixedService {

    private final ProductFixedRepository productFixedRepository;
    private final ProductStatRepository productStatRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;

    private final ProductMapperService productMapperService;
    private final IngredientMapperService ingredientMapperService;

    public Page<ProductFixedAPADTO> findByIdCategory(String idCategory, int page, int size, String sortColumn, String sortDirection) {
        List<ProductFixedAPA> productsFixed = productFixedRepository.findAllByCategoryIdAndSoftDeletedFalse(idCategory);
        List<ProductFixedAPADTO> dto = new ArrayList<>();
        for (ProductFixedAPA productFixedAPA : productsFixed) {
            List<IngredientAPA> ingredients = ingredientRepository.findByIdIn(productFixedAPA.getIngredientIds());
            String ingredientNames = ingredientMapperService.ingredientsIdToIngredientsName(ingredients);
            Set<Allergen> allergens = new HashSet<>();
            for (IngredientAPA ingredient : ingredients) {
                List<Allergen> allergensList = allergenRepository.findByNameIn(ingredient.getAllergenNames());
                allergens.addAll(allergensList);
            }
            ProductFixedAPADTO productFixedAPADTO = productMapperService.fixedAPAToDTO(productFixedAPA,ingredientNames,allergens);
            dto.add(productFixedAPADTO);
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(dto,pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"name");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(dto,pageable);
    }

    public ProductFixedAPADetailsDTO getById(String id) {
        Optional<ProductFixedAPA> optProductFixedAPA = productFixedRepository.findById(id);
        if(optProductFixedAPA.isPresent()){
            ProductFixedAPA product = optProductFixedAPA.get();
            List<IngredientAPA> ingredients = ingredientRepository.findByIdIn(product.getIngredientIds());
            List<String> ingredientNames = ingredientMapperService.ingredientsIdToIngredientsNameList(ingredients);
            Set<String> allergenNames = new HashSet<>();
            for (IngredientAPA ingredient : ingredients) {
                allergenNames.addAll(ingredient.getAllergenNames()); // Aggiunge solo se non presente
            }
            return productMapperService.fixedAPAToDetailsDTO(product,ingredientNames,allergenNames);
        }
        throw new InvalidItemException();
    }

    public ProductFixedAPA save(ProductFixedAPA p) {
        if (p.getId() == null){
            ProductStatAPA pStat=new ProductStatAPA("productFixed");
            p.setStats(pStat);
            productStatRepository.save(pStat);
        }
        return productFixedRepository.save(p);
    }

    public Boolean deleteById(String id) {
        Optional<ProductFixedAPA> optProduct = productFixedRepository.findById(id);
        if(optProduct.isPresent()){
            ProductFixedAPA product = optProduct.get();
            product.setSoftDeleted(true);
            productFixedRepository.save(product);
            return true;
        }
        return false;
    }

    public Boolean toggleById(String id) {
        Optional<ProductFixedAPA> optProduct = productFixedRepository.findById(id);
        if(optProduct.isPresent()){
            ProductFixedAPA product = optProduct.get();
            product.setActive(!product.isActive());
            productFixedRepository.save(product);
            return true;
        }
        return false;
    }

    public String getImageUrl(String id) {
        Optional<ProductFixedAPA> optProduct = productFixedRepository.findById(id);
        if(optProduct.isPresent()){
            return optProduct.get().getImageUrl();
        }
        return null;
    }
}
