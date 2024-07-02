package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.ProductStatAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayAPADTO {
    private String id;
    private String name;
    private List<Allergen> allergens;
    private String imageUrl;
    private String customized;
    private String measures;
    private String description;
    private boolean active;
    private double pricePerKg;
}
