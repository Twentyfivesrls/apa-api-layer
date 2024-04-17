package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public List<CategoryAPA> getEnabledCategories(List<String> types) {
        return categoryRepository.findByTypeInAndEnabledTrue(types);
    }

    public CategoryAPA getById(String id){
        return categoryRepository.findById(id).orElse(null);
    }

    public CategoryAPA save(CategoryAPA c){
        return categoryRepository.save(c);
    }

    public boolean deleteById(String id){
        CategoryAPA c = categoryRepository.findById(id).orElse(null);
        c.setEnabled(false);
        if(c.getType().equals("Ingredienti")){
            //TODO
            //disabilita tutti gli ingredienti
        }
        //disabilita torte
        return false;
    }

}
