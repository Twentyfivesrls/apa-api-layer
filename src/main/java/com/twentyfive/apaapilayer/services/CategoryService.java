package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductKgRepository;
import com.twentyfive.apaapilayer.repositories.ProductWeightedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final ProductKgRepository productKgRepository;
    private final IngredientRepository ingredientRepository;


    public List<CategoryAPA> getEnabledCategories(List<String> types) {
        return categoryRepository.findAllByTypeInAndEnabledTrue(types);
    }

    public CategoryAPA getById(String id){
        return categoryRepository.findById(id).orElse(null);
    }

    public CategoryAPA save(CategoryAPA c){
        activateById(c.getName());
        return categoryRepository.save(c);
    }

    public boolean disableById(String id){
        CategoryAPA c = categoryRepository.findById(id).orElse(null);
        if(c!=null) {
            c.setEnabled(false);
            categoryRepository.save(c);
            List<ProductKgAPA> prodottiAlKg = productKgRepository.findAllByCategoryId(id);
            for (ProductKgAPA p : prodottiAlKg) {
                p.setActive(false);
                productKgRepository.save(p);
            }
            List<ProductWeightedAPA> prodottiWeighted = productWeightedRepository.findAllByCategoryId(id);
            for (ProductWeightedAPA p : prodottiWeighted) {
                p.setActive(false);
                productWeightedRepository.save(p);
            }
            List<IngredientAPA> ingredienti = ingredientRepository.findAllByCategoryId(id);
            for (IngredientAPA i : ingredienti) {
                i.setActive(false);
                ingredientRepository.save(i);
            }
            return true;
        }
        return false;
    }

    private void activateById(String cName){
        CategoryAPA category = categoryRepository.findByName(cName);
        if(category!=null){
            category.setEnabled(true);

        }
    }

}
