package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.AutoCompleteProductWeighted;
import com.twentyfive.apaapilayer.dtos.ProductWeightedAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
import com.twentyfive.apaapilayer.repositories.ProductWeightedRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Product;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RequiredArgsConstructor
@Service
public class ProductWeightedService {

    private final ProductWeightedRepository productWeightedRepository;
    private final ProductStatRepository productStatRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;

    private ProductWeightedAPADTO productsWeightedToDTO(ProductWeightedAPA product){
        ProductWeightedAPADTO dto = new ProductWeightedAPADTO();
        dto.setActive(product.isActive());
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setRealWeight(product.getWeight());
        dto.setWeight("Kg " +product.getWeight());
        dto.setDescription(product.getDescription());
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
        dto.setStats(product.getStats());
        dto.setIngredients(nomeIngredienti);
        dto.setAllergens(allergeni);
        return dto;
    }

    private AutoCompleteProductWeighted productsWeightedToAutoCompleteDTO(ProductWeightedAPA product){
        AutoCompleteProductWeighted dto = new AutoCompleteProductWeighted();
        dto.setActive(product.isActive());
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setRealWeight(product.getWeight());
        dto.setWeight("Kg " +product.getWeight());
        dto.setDescription(product.getDescription());
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
        dto.setValue(product.getName());
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
        Sort sort = Sort.by(Sort.Direction.ASC,"name");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(realProductsWeighted,pageable);
    }


    public ProductWeightedAPADTO getById(String id) {
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA==null)
            return null;
        return productsWeightedToDTO(productWeightedAPA);
    }

    @Transactional
    public ProductWeightedAPA save(ProductWeightedAPA p) {
        if(productWeightedRepository.findById(p.getId()).isEmpty()){
            ProductStatAPA pStat=new ProductStatAPA("productWeighted");
            p.setStats(pStat);
            productStatRepository.save(pStat);
        }
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
    public boolean activateById(String id,boolean modalResponse){
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
            else{
                if(modalResponse){
                    productWeightedAPA.setActive(true);
                    productWeightedRepository.save(productWeightedAPA);
                    return true;
                }
            }
        }
        return false;
    }


    public String getImageUrl(String id) {
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        return productWeightedAPA.getImageUrl();
    }

    public List<AutoCompleteProductWeighted> getAllForCustomizedTray(String value) {
        List<ProductWeightedAPA> products = productWeightedRepository.findAllByCategoryIdAndNameContainsIgnoreCase("664361ed09aa3a0e1b249988",value);
        // Ordinare prima per buyingCount e poi per name
        products = products.stream()
                .filter(product -> product.getStats() != null) // Filtra i prodotti con stats null
                .sorted((p1, p2) -> {
                    int cmp = Integer.compare(p2.getStats().getBuyingCount(), p1.getStats().getBuyingCount()); // Ordine desc
                    if (cmp == 0) {
                        cmp = p1.getName().compareTo(p2.getName()); // Ordine asc
                    }
                    return cmp;
                })
                .collect(Collectors.toList());

        List<AutoCompleteProductWeighted> autoCompleteProductWeighted = products.stream()
                .map(this::productsWeightedToAutoCompleteDTO) // Converti i risultati direttamente in DTO
                .collect(Collectors.toList());

        return autoCompleteProductWeighted;
    }
}
