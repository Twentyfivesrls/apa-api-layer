package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.ProductWeightedAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductWeightedRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductWeightedService {

    private final ProductWeightedRepository productWeightedRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;

    private ProductWeightedAPADTO productsWeightedToDTO(ProductWeightedAPA product){
        ProductWeightedAPADTO dto = new ProductWeightedAPADTO();
        dto.setActive(product.isActive());
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setWeight("Kg " +product.getWeight());
        List<String> idingredienti = product.getIngredientIds();
        List<String> nomeIngredienti = new ArrayList<>();
        List<Allergen> allergeni = new ArrayList<>();
        for(String id : idingredienti){
            IngredientAPA ingrediente = ingredientRepository.findById(id).orElse(null);
            if(ingrediente!=null) {
                nomeIngredienti.add(ingrediente.getName());
                List<String> nomiAllergeni = ingrediente.getAllergenNames();
                for(String nomeAllergene: nomiAllergeni){
                    Allergen allergene = allergenRepository.findByName(nomeAllergene).orElse(null);
                    if(allergene!=null && !allergeni.contains(allergene))
                        allergeni.add(allergene);
                }
            }
        }
        dto.setIngredients(nomeIngredienti);
        dto.setAllergens(allergeni);
        return dto;
    }


    public Page<ProductWeightedAPADTO> findByIdCategory(String idCategory, int page, int size,String sortColumn,String sortDirection) {
        List<ProductWeightedAPA> productsWeighted = productWeightedRepository.findAllByCategoryId(idCategory);
        List<ProductWeightedAPADTO> realProductsWeighted = new ArrayList<>();
        for(ProductWeightedAPA p : productsWeighted){
            if(p!=null) {
                ProductWeightedAPADTO dto = productsWeightedToDTO(p);
                realProductsWeighted.add(dto);
            }
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realProductsWeighted,pageable);
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realProductsWeighted,pageable);
    }


    public ProductWeightedAPADTO getById(String id) {
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA==null)
            return null;
        return productsWeightedToDTO(productWeightedAPA);
    }

    @Transactional
    public ProductWeightedAPA save(ProductWeightedAPA p) {
        return productWeightedRepository.save(p);
    }

    @Transactional
    public boolean disableById(String id){
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA!=null){
            productWeightedAPA.setActive(false);
            productWeightedRepository.save(productWeightedAPA);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean activateById(String id){
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA!=null){
            List<String> idIngredienti = productWeightedAPA.getIngredientIds();
            List<IngredientAPA> ingredientiDisattivati = new ArrayList<>();
            for(String idIngrediente: idIngredienti){
                IngredientAPA ingrediente = ingredientRepository.findById(idIngrediente).orElse(null);
                if(ingrediente!=null && !ingrediente.isActive())
                    ingredientiDisattivati.add(ingrediente);
            }
            if(ingredientiDisattivati.isEmpty()) {
                productWeightedAPA.setActive(true);
                productWeightedRepository.save(productWeightedAPA);
                return true;
            }
            else
                return false;
        }
        return false;
    }


}
