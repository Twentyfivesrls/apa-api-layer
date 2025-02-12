package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatCategoryDTO {
    private String idProduct;
    private String name;
    private long quantity;
    private double totalWeight;
    private double totalRevenue;

}
