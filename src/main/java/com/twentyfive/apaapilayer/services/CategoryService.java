package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.mappers.CategoryMapperService;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapperService categoryMapperService;

    private final String[] PRODUCT_CATEGORY = {"productWeighted","ProductKg","ProductFixed","Tray"};



    public List<CategoryAPA> getEnabledCategories(List<String> types) {
        return categoryRepository.findAllByTypeInAndEnabledTrueAndSoftDeletedFalseOrderByOrderPriorityAsc(types);
    }

    public List<CategoryMinimalDTO> getAllMinimal(List<String> types) {
        List<CategoryAPA> allCategory = categoryRepository.findAllByTypeInAndEnabledTrueAndSoftDeletedFalseOrderByOrderPriorityAsc(types);
        List<CategoryMinimalDTO> dto = categoryMapperService.ListCategoryAPAToListMinimalDTO(allCategory);

        return dto;
    }

    public List<CategoryMinimalDTO> getAllMinimalByListId(List<String> ids){
        List<CategoryAPA> categories = categoryRepository.findAllByIdInOrderByOrderPriority(ids);
        List<CategoryMinimalDTO> dto = categoryMapperService.ListCategoryAPAToListMinimalDTO(categories);
        return dto;
    }
    public List<CategoryAPA> getDisabledCategories(List<String> types) {
        return categoryRepository.findAllByTypeInAndEnabledFalseAndSoftDeletedFalseOrderByNameAsc(types);
    }
    public CategoryAPA getById(String id){
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public CategoryAPA save(CategoryAPA c){
        //Andremo a calcolare l'orderPriority per la nuova categoria
        Integer max = null;

        if (c.getIdSection() != null) {
            max = getEnabledCategories(Arrays.stream(PRODUCT_CATEGORY).toList())
                    .stream()
                    .map(CategoryAPA::getOrderPriority)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder()).orElse(null);
        } else if(c.getId() != null) {
            max = (getAllActiveByIdSection(c.getIdSection())
                    .stream()
                    .map(CategoryAPA::getOrderPriority)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder()).orElse(null));
        }

        if (max != null && c.getId()== null){
            c.setOrderPriority(max+1);
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
