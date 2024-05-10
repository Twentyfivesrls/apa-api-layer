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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        dto.setActive(product.isActive());
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setPricePerKg("€ "+ product.getPricePerKg());
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
    /*private ProductKgDetailsAPADTO productsKgToDetailsDTO(ProductKgAPA product){
        ProductKgDetailsAPADTO dto = new ProductKgDetailsAPADTO();
        dto.setId(product.getId());
        dto.setImageUrl(product.getImageUrl());
        dto.setPricePerKg("€ "+ product.getPricePerKg());
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


     */

    public Page<ProductKgAPADTO> findByIdCategory(String idCategory, int page, int size,String sortColumn,String sortDirection) {
        List<ProductKgAPA> productsKg = productKgRepository.findAllByCategoryId(idCategory);
        List<ProductKgAPADTO> realProductsKg = new ArrayList<>();
        for(ProductKgAPA p : productsKg){
            if(p!=null) {
                ProductKgAPADTO dto = productsKgToDTO(p);
                realProductsKg.add(dto);
            }
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realProductsKg,pageable);
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realProductsKg,pageable);    }


    public ProductKgAPADTO getById(String id) {
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA==null)
            return null;
        return productsKgToDTO(productKgAPA);
    }

    @Transactional
    public ProductKgAPA save(ProductKgAPA p) {
        return productKgRepository.save(p);
    }

    @Transactional
    public boolean disableById(String id){
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA!=null){
            productKgAPA.setActive(false);
            productKgRepository.save(productKgAPA);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean activateById(String id){
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA!=null){
            List<String> idIngredienti = productKgAPA.getIngredientIds();
            List<IngredientAPA> ingredientiDisattivati = new ArrayList<>();
            for(String idIngrediente: idIngredienti){
                IngredientAPA ingrediente = ingredientRepository.findById(idIngrediente).orElse(null);
                if(ingrediente!=null && !ingrediente.isActive())
                    ingredientiDisattivati.add(ingrediente);
            }
            if(ingredientiDisattivati.isEmpty()) {
                productKgAPA.setActive(true);
                productKgRepository.save(productKgAPA);
                return true;
            }
            else
                return false;
        }
        return false;
    }




}
