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
import org.springframework.stereotype.Service;
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
        dto.setId(product.getId());
        dto.setNome(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setWeight(String.valueOf(product.getWeight()));
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
        dto.setAllergenList(allergeni);
        return dto;
    }


    public Page<ProductWeightedAPADTO> findByIdCategory(String idCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ProductWeightedAPA> productsWeighted = productWeightedRepository.findAllByCategoryId(idCategory);
        List<ProductWeightedAPADTO> realProductsWeighted = new ArrayList<>();
        for(ProductWeightedAPA p : productsWeighted){
            if(p!=null) {
                ProductWeightedAPADTO dto = productsWeightedToDTO(p);
                realProductsWeighted.add(dto);
            }
        }
        return PageUtilities.convertListToPage(realProductsWeighted, pageable);
    }


    public ProductWeightedAPADTO getById(String id) {
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA==null)
            return null;
        return productsWeightedToDTO(productWeightedAPA);
    }

    public ProductWeightedAPA save(ProductWeightedAPA p) {
        return productWeightedRepository.save(p);
    }

    public boolean disableById(String id){
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA!=null){
            productWeightedAPA.setActive(false);
            productWeightedRepository.save(productWeightedAPA);
            return true;
        }
        return false;
    }

    public boolean activateById(String id){
        //TODO
        return false;
    }


}
