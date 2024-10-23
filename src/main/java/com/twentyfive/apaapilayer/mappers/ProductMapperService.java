package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;
import java.util.Set;

@Service
public class ProductMapperService {

    public ProductFixedAPADTO fixedAPAToDTO(ProductFixedAPA productFixed, List<String> ingredients, Set<Allergen> allergens) {
        String realPrice = "€ " + productFixed.getPrice();
        return new ProductFixedAPADTO(
                productFixed.getId(),
                productFixed.getName(),
                productFixed.getDescription(),
                allergens,
                ingredients,
                productFixed.getPrice(),
                realPrice,
                productFixed.getImageUrl(),
                productFixed.isActive()
        );
    }

    public ProductFixedAPADetailsDTO fixedAPAToDetailsDTO(ProductFixedAPA productFixed,List<String> ingredientNames, Set<String> allergenNames) {
        String realPrice = "€ " + productFixed.getPrice();
        String realWeight = productFixed.getWeight() + " Kg";
        return new ProductFixedAPADetailsDTO(
                productFixed.getId(),
                allergenNames,
                ingredientNames,
                realPrice,
                realWeight,
                productFixed.getImageUrl()
        );
    }
}
