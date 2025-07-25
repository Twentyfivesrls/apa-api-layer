package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.AutoCompleteProductWeighted;
import com.twentyfive.apaapilayer.dtos.ProductKgAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductWeightedAPADTO;
import com.twentyfive.apaapilayer.filters.ProductFilter;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
import com.twentyfive.apaapilayer.repositories.ProductWeightedRepository;
import com.twentyfive.apaapilayer.utils.FilterUtilities;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductWeightedService {

    private final ProductWeightedRepository productWeightedRepository;
    private final ProductStatRepository productStatRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;

    private final MongoTemplate mongoTemplate;

    private ProductWeightedAPADTO productsWeightedToDTO(ProductWeightedAPA product){
        ProductWeightedAPADTO dto = new ProductWeightedAPADTO();
        dto.setActive(product.isActive());
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setRealWeight(product.getWeight());
        dto.setWeight("Kg " +product.getWeight());
        dto.setDescription(product.getDescription());
        dto.setToPrepare(product.isToPrepare());
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

    public Page<ProductWeightedAPADTO> findByIdCategory(String idCategory, int page, int size, String sortColumn, String sortDirection, ProductFilter filters) {
        Query query = new Query();
        query = FilterUtilities.applyProductFilters(query,filters,idCategory,ingredientRepository,true,ProductWeightedAPA.class);
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else {
            sort = Sort.by(Sort.Direction.fromString(sortDirection),sortColumn);
        }
        Pageable pageable=PageRequest.of(page,size,sort);

        List<ProductWeightedAPA> productWeighted = mongoTemplate.find(query, ProductWeightedAPA.class);
        List<ProductWeightedAPADTO> realProductWeighted = productWeighted.stream()
                .map(this::productsWeightedToDTO)
                .collect(Collectors.toList());
        return PageUtilities.convertListToPageWithSorting(realProductWeighted,pageable);
    }


    public ProductWeightedAPADTO getById(String id) {
        ProductWeightedAPA productWeightedAPA = productWeightedRepository.findById(id).orElse(null);
        if(productWeightedAPA==null)
            return null;
        return productsWeightedToDTO(productWeightedAPA);
    }

    @Transactional
    public ProductWeightedAPA save(ProductWeightedAPA p) {
        if (p.getId()==null){
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
        List<ProductWeightedAPA> products = productWeightedRepository.findAllByCategoryIdAndSoftDeletedFalseAndNameContainsIgnoreCase("664361ed09aa3a0e1b249988",value);
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

    public boolean deleteById(String id) {
        Optional<ProductWeightedAPA> optProduct = productWeightedRepository.findById(id);
        if (optProduct.isPresent()){
            ProductWeightedAPA product = optProduct.get();
            product.setSoftDeleted(true);
            productWeightedRepository.save(product);
            return true;
        }
        return false;
    }
}
