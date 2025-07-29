package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import com.twentyfive.apaapilayer.repositories.CustomTimeCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CustomTimeCategoryService {
    private final CustomTimeCategoryRepository customTimeCategoryRepository;

    public CustomTimeCategoryService(CustomTimeCategoryRepository customTimeCategoryRepository) {
        this.customTimeCategoryRepository = customTimeCategoryRepository;
    }

    public CustomTimeCategoryAPA findByCategory(CategoryAPA category) {
        return customTimeCategoryRepository.findByCategory(category).orElseThrow(() -> new EntityNotFoundException("No custom time found for this category: "+category.getName()));
    }

    public List<CustomTimeCategoryAPA> findAll() {
        return customTimeCategoryRepository.findAll();
    }

    public void saveOrUpdate(CategoryAPA category, LocalTime start, LocalTime end){
        CustomTimeCategoryAPA customTimeCategory;

        //SE ESISTE è UNA PUT
        if(customTimeCategoryRepository.existsByCategory(category)){
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
}
