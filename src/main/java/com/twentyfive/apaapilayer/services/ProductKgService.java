package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.ProductKgAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductKgRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductKgService {

    private final ProductKgRepository productKgRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;
    private final ProductStatRepository productStatRepository;

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
        dto.setStats(product.getStats());
        dto.setAllergens(allergeni);
        dto.setDescription(product.getDescription());
        dto.setWeightRange(product.getWeightRange());
        dto.setCustomized(product.isCustomized());
        return dto;
    }

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
        Sort sort = Sort.by(Sort.Direction.ASC,"name");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(realProductsKg,pageable);
    }


    public ProductKgAPADTO getById(String id) {
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        if(productKgAPA==null)
            return null;
        return productsKgToDTO(productKgAPA);
    }

    @Transactional
    public ProductKgAPA save(ProductKgAPA p) {
        if(p.getId()==null){
                ProductStatAPA pStat=new ProductStatAPA("productKg");
                p.setStats(pStat);
                productStatRepository.save(pStat);
        }
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
    public boolean activateById(String id,boolean modalResponse){
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
            else{
                if(modalResponse){
                    productKgAPA.setActive(true);
                    productKgRepository.save(productKgAPA);
                    return true;
                }
            }
        }
        return false;
    }


    public Page<ProductKgAPADTO> getAllActive(String idCategory, int page, int size) {
        List<ProductKgAPA> productsKg = productKgRepository.findAllByCategoryIdAndActiveTrueAndCustomizedFalse(idCategory);
        // Ordinare prima per buyingCount e poi per name
        productsKg = productsKg.stream()
                .filter(product -> product.getStats() != null) // Filtra i prodotti con stats null
                .sorted((p1, p2) -> {
                    int cmp = Integer.compare(p2.getStats().getBuyingCount(), p1.getStats().getBuyingCount()); // Ordine desc
                    if (cmp == 0) {
                        cmp = p1.getName().compareTo(p2.getName()); // Ordine asc
                    }
                    return cmp;
                })
                .collect(Collectors.toList());
        List<ProductKgAPADTO> realProductsKg = new ArrayList<>();
        for(ProductKgAPA p : productsKg){
            if(p!=null) {
                ProductKgAPADTO dto = productsKgToDTO(p);
                realProductsKg.add(dto);
            }
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realProductsKg,pageable);
    }

    public String getImageUrl(String id) {
        ProductKgAPA productKgAPA = productKgRepository.findById(id).orElse(null);
        return productKgAPA.getImageUrl();

    }
}
