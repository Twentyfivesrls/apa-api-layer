package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardProductStatDTO {
    private String idCategory;
    private String name;
    private long totalProductSold;
    private double totalRevenue;
}
