package com.twentyfive.apaapilayer.dtos.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWeightedStatCategoryDTO {
    private String id;
    private String name;
    private String ingredients;
    private long quantity;

    private double totalWeight;

    public double getTotalWeight() {
        return Math.round(totalWeight * 100.0) / 100.0;
    }
}
