package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.ProductKgRepository;
import com.twentyfive.apaapilayer.repositories.ProductWeightedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final ProductKgRepository productKgRepository;
    private final IngredientRepository ingredientRepository;


    public List<CategoryAPA> getEnabledCategories(List<String> types) {
        return categoryRepository.findAllByTypeInAndEnabledTrueAndSoftDeletedFalseOrderByOrderPriorityAsc(types);
    }

    public List<CategoryAPA> getDisabledCategories(List<String> types) {
        return categoryRepository.findAllByTypeInAndEnabledFalseAndSoftDeletedFalseOrderByNameAsc(types);
    }
    public CategoryAPA getById(String id){
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public CategoryAPA save(CategoryAPA c){
        Optional<CategoryAPA> category = categoryRepository.findByName(c.getName());
        if (category.isPresent()){
            c.setId(category.get().getId());
        }
        return categoryRepository.save(c);
    }

    @Transactional
    public boolean disableById(String id){
        CategoryAPA c = categoryRepository.findById(id).orElse(null);
        if(c!=null) {
            c.setEnabled(false);
            categoryRepository.save(c);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean activateById(String id){
        CategoryAPA c = categoryRepository.findById(id).orElse(null);
        if(c!=null){
            c.setEnabled(true);
            categoryRepository.save(c);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteById(String id) {
        CategoryAPA c = categoryRepository.findById(id).orElse(null);
        if(c!=null) {
            c.setEnabled(false);
            c.setSoftDeleted(true);
            categoryRepository.save(c);
            return true;
        }
        return false;

    }

    @Transactional
    public boolean setOrderPriorities(Map<String, Integer> priorities) {
        for (Map.Entry<String, Integer> entry : priorities.entrySet()) {
            if(!categoryRepository.existsById(entry.getKey()))return false;
        }

        for (Map.Entry<String, Integer> entry : priorities.entrySet()) {
            CategoryAPA c = categoryRepository.findById(entry.getKey()).orElse(null);
            if(c!=null){
                c.setOrderPriority(entry.getValue());
                categoryRepository.save(c);
            }
        }
        return true;
    }

    public List<CategoryAPA> getAllActiveByIdSection(String id) {
        return categoryRepository.findAllByIdSectionAndEnabledTrueAndSoftDeletedFalseOrderByOrderPriorityAsc(id);
    }

    public List<CategoryAPA> getAllDisabledByIdSection(String id) {
        return categoryRepository.findAllByIdSectionAndEnabledFalseAndSoftDeletedFalse(id);

    }
}
