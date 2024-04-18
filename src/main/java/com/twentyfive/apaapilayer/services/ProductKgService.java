package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.ProductKgAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductKgRepository;
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
public class ProductKgService {

    private final ProductKgRepository productKgRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;

    private ProductKgAPADTO productsKgToDTO(ProductKgAPA product){
        ProductKgAPADTO dto = new ProductKgAPADTO();
        dto.setId(product.getId());
        dto.setNome(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setPricePerKg(String.valueOf(product.getPricePerKg()));
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


    public Page<ProductKgAPADTO> findByIdCategory(String idCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ProductKgAPA> productsKg = productKgRepository.findAllByCategoryId(idCategory);
        List<ProductKgAPADTO> realProductsKg = new ArrayList<>();
        for(ProductKgAPA p : productsKg){
            if(p!=null) {
                ProductKgAPADTO dto = productsKgToDTO(p);
                realProductsKg.add(dto);
            }
        }
        return PageUtilities.convertListToPage(realProductsKg, pageable);
    }


    public ProductKgAPADTO getById(String id) {
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA==null)
            return null;
        return productsKgToDTO(productKgAPA);
    }

    public ProductKgAPA save(ProductKgAPA p) {
        return productKgRepository.save(p);
    }

    public boolean disableById(String id){
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA!=null){
            productKgAPA.setActive(false);
            productKgRepository.save(productKgAPA);
            return true;
        }
        return false;
    }

    public boolean activateById(String id){
       //TODO
        return false;
    }




}
