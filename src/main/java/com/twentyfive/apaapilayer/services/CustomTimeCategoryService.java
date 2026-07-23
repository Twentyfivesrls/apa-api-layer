package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.CategoryCustomHoursDTO;
import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.mappers.CategoryMapperService;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeVariantAPA;
import com.twentyfive.apaapilayer.repositories.CustomTimeCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class CustomTimeCategoryService {
    private final CustomTimeCategoryRepository customTimeCategoryRepository;
    private final CategoryMapperService categoryMapperService;
    private final TimeSlotRefreshScheduling timeSlotRefreshScheduling;

    public CustomTimeCategoryService(CustomTimeCategoryRepository customTimeCategoryRepository, CategoryMapperService categoryMapperService, TimeSlotRefreshScheduling timeSlotRefreshScheduling) {
        this.customTimeCategoryRepository = customTimeCategoryRepository;
        this.categoryMapperService = categoryMapperService;
        this.timeSlotRefreshScheduling = timeSlotRefreshScheduling;
    }

    public CustomTimeCategoryAPA findByCategory(CategoryAPA category) {
        return customTimeCategoryRepository.findByCategory(category).orElseThrow(() -> new EntityNotFoundException("No custom time found for this category: "+category.getName()));
    }

    public CustomTimeCategoryAPA findByCategoryId(String categoryId) {
        return customTimeCategoryRepository.findByCategory_Id(categoryId).orElseThrow(() -> new EntityNotFoundException("No custom time found for this category ID: "+categoryId));
    }

    public CustomTimeCategoryAPA findByCategoryIdOrNull(String categoryId) {
        return customTimeCategoryRepository.findByCategory_Id(categoryId).orElse(null);
    }

    public List<CustomTimeCategoryAPA> findAll() {
        return customTimeCategoryRepository.findAll();
    }

    public boolean existsByCategory(CategoryAPA category) {
        return customTimeCategoryRepository.existsByCategory(category);
    }

    public void saveOrUpdate(CategoryAPA category, LocalTime start, LocalTime end,
                             Integer daysAhead, LocalTime cutoffHour, LocalTime cutoffResetHour,
                             LocalTime firstPickupAfterCutoff, boolean sameDayAllowed,
                             Integer maxMorningOrder, Integer maxAfternoonOrder,
                             CustomTimeVariantAPA variant){
        CustomTimeCategoryAPA customTimeCategory;
        Integer oldMaxMorning = null;
        Integer oldMaxAfternoon = null;

        if(existsByCategory(category)){
            customTimeCategory = findByCategory(category);
            oldMaxMorning = customTimeCategory.getMaxMorningOrder();
            oldMaxAfternoon = customTimeCategory.getMaxAfternoonOrder();
        } else {
            customTimeCategory = new CustomTimeCategoryAPA();
            customTimeCategory.setCategory(category);
        }
        customTimeCategory.setStart(start);
        customTimeCategory.setEnd(end);
        customTimeCategory.setDaysAhead(daysAhead);
        customTimeCategory.setCutoffHour(cutoffHour);
        customTimeCategory.setCutoffResetHour(cutoffResetHour);
        customTimeCategory.setFirstPickupAfterCutoff(firstPickupAfterCutoff);
        customTimeCategory.setSameDayAllowed(sameDayAllowed);
        customTimeCategory.setMaxMorningOrder(maxMorningOrder);
        customTimeCategory.setMaxAfternoonOrder(maxAfternoonOrder);
        customTimeCategory.setVariant(variant);
        customTimeCategoryRepository.save(customTimeCategory);

        // Se la capacità della categoria è cambiata, rigenera gli slot futuri col nuovo massimo
        if (!Objects.equals(oldMaxMorning, maxMorningOrder) || !Objects.equals(oldMaxAfternoon, maxAfternoonOrder)) {
            timeSlotRefreshScheduling.regenerateCategorySlots(category.getId());
        }
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