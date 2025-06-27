package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralTrayStatDTO {
    private long quantity;
    private double totalWeight;
    private double totalRevenue;
    private long totalProductWeighted;
}
