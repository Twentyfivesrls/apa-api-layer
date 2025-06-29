package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralTraySingleStatDTO {
    private String id;
    private String name;
    private long quantity;
    private double price;
    private double weight;

}
