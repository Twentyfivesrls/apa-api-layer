package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFixedAPADetailsDTO {
    private String id;
    private Set<String> allergenNames;
    private List<String> ingredientNames;
    private String price;
    private String imgUrl;
}
