package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryMapperService {

    public CategoryMinimalDTO CategoryAPAToMinimalDTO(CategoryAPA categoryAPA) {
        return new CategoryMinimalDTO(
                categoryAPA.getId(),
                categoryAPA.getName()
        );
    }

    public List<CategoryMinimalDTO> ListCategoryAPAToListMinimalDTO(List<CategoryAPA> allCategory) {
        List<CategoryMinimalDTO> dtos = new ArrayList<>();
        for (CategoryAPA categoryAPA : allCategory) {
            CategoryMinimalDTO categoryMinimalDTO = CategoryAPAToMinimalDTO(categoryAPA);
            dtos.add(categoryMinimalDTO);
        }
        return dtos;
    }
}
