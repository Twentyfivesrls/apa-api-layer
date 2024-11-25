package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.LocalDateRange;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.NumberRange;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponDetailsDTO {
    private String id;
    private boolean active;
    private String name;
    private String code;
    private LocalDateRange dates;
    private NumberRangeDTO priceRange;
    private String type; //percentuale oppure fixed
    private String value; //percentuale oppure fixed
    private int usageCount; //Quanti coupon sono stati usati
    private Integer maxTotalUsage; //Numero massimo coupon consentiti, se null infiniti
    private Integer maxUsagePerCustomer; //Numero massimo per Customer consentiti, se null infiniti
    private List<CategoryMinimalDTO> specificCategories; //nome e id delle categorie impattate (se ci sono)
}
