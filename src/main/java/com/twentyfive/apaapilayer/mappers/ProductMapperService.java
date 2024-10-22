package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;
import java.util.Set;

@Service
public class ProductMapperService {

    public ProductFixedAPADTO fixedAPAToDTO(ProductFixedAPA productFixed, String ingredientsName, Set<Allergen> allergens) {
        String realPrice = "€ " + productFixed.getPrice();
        return new ProductFixedAPADTO(
                productFixed.getId(),
                productFixed.getName(),
                allergens,
                ingredientsName,
                productFixed.getPrice(),
                realPrice,
                productFixed.isActive()
        );
    }

    public ProductFixedAPADetailsDTO fixedAPAToDetailsDTO(ProductFixedAPA productFixed,List<String> ingredientNames, Set<String> allergenNames) {
        String realPrice = "€ " + productFixed.getPrice();
        return new ProductFixedAPADetailsDTO(
                productFixed.getId(),
                allergenNames,
                ingredientNames,
                realPrice,
                productFixed.getImageUrl()
        );
    }
}
