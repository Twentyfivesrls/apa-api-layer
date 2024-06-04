package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayAPADTO {
    private String id;
    private String name;
    private String imageUrl;
    private String customized;
    private String measures;
    private String description;
    private boolean active;
    private double pricePerKg;
}
