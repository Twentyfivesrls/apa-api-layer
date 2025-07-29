package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CategoryCustomHoursDTO;
import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import com.twentyfive.apaapilayer.services.SettingService;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.DateRange;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryMapperService {
    private final SettingService settingService;

    public CategoryMapperService(SettingService settingService) {
        this.settingService = settingService;
    }

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

    public List<CategoryCustomHoursDTO> ListCategoryCustomHoursDTO(List<CustomTimeCategoryAPA> customTimeCategories) {
        DateRange businessHours = settingService.get().getBusinessHours();

        List<CategoryCustomHoursDTO> dtos = new ArrayList<>();

        for (CustomTimeCategoryAPA customTimeCategory : customTimeCategories) {

            CategoryCustomHoursDTO categoryCustomHoursDto = new CategoryCustomHoursDTO();

            categoryCustomHoursDto.setId(customTimeCategory.getCategory().getId());
            categoryCustomHoursDto.setName(customTimeCategory.getCategory().getName());
            categoryCustomHoursDto.setStart(customTimeCategory.getStart());
            categoryCustomHoursDto.setEnd(customTimeCategory.getEnd());

            if (customTimeCategory.getStart().equals(businessHours.getStartTime()) && customTimeCategory.getEnd().equals(businessHours.getEndTime())) {
                categoryCustomHoursDto.setExactMatch(true);
            } else {
                categoryCustomHoursDto.setExactMatch(false);
            }

            dtos.add(categoryCustomHoursDto);
        }
        return dtos;

    }
}
