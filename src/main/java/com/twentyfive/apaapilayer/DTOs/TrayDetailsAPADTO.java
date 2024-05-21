package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Measure;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayDetailsAPADTO {
    private String id;
    private String name;
    private boolean customized;
    private String personalized;
    private String measures;
    private List<Measure> measuresList;
    private String description;
    private String imageUrl;
    private double pricePerKg;
}
