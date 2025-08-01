package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.CategoryCustomHoursDTO;
import com.twentyfive.apaapilayer.mappers.CategoryMapperService;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import com.twentyfive.apaapilayer.repositories.CustomTimeCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class CustomTimeCategoryService {
    private final CustomTimeCategoryRepository customTimeCategoryRepository;
    private final CategoryMapperService categoryMapperService;

    public CustomTimeCategoryService(CustomTimeCategoryRepository customTimeCategoryRepository, CategoryMapperService categoryMapperService) {
        this.customTimeCategoryRepository = customTimeCategoryRepository;
        this.categoryMapperService = categoryMapperService;
    }

    public CustomTimeCategoryAPA findByCategory(CategoryAPA category) {
        return customTimeCategoryRepository.findByCategory(category).orElseThrow(() -> new EntityNotFoundException("No custom time found for this category: "+category.getName()));
    }

    public CustomTimeCategoryAPA findByCategoryId(String categoryId) {
        return customTimeCategoryRepository.findByCategory_Id(categoryId).orElseThrow(() -> new EntityNotFoundException("No custom time found for this category ID: "+categoryId));
    }

    public List<CustomTimeCategoryAPA> findAll() {
        return customTimeCategoryRepository.findAll();
    }

    public boolean existsByCategory(CategoryAPA category) {
        return customTimeCategoryRepository.existsByCategory(category);
    }

    public void saveOrUpdate(CategoryAPA category, LocalTime start, LocalTime end){
        CustomTimeCategoryAPA customTimeCategory;

        //SE ESISTE è UNA PUT
        if(existsByCategory(category)){
            customTimeCategory = findByCategory(category);

            customTimeCategory.setStart(start);
            customTimeCategory.setEnd(end);

        } else {
            customTimeCategory = new CustomTimeCategoryAPA();
            customTimeCategory.setCategory(category);
            customTimeCategory.setStart(start);
            customTimeCategory.setEnd(end);
        }
        customTimeCategoryRepository.save(customTimeCategory);
    }

    public List<CategoryCustomHoursDTO> getAllCategoriesWithCustomHours(){
        List<CustomTimeCategoryAPA> customTimeCategories = findAll();

        return categoryMapperService.ListCategoryCustomHoursDTO(customTimeCategories);
    }

    public boolean deleteCustomTime(String categoryId) {
        CustomTimeCategoryAPA customTimeCategory = findByCategoryId(categoryId);
        
        customTimeCategoryRepository.delete(customTimeCategory);

        return true;
    }
}