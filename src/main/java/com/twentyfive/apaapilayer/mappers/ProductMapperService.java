package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CustomizableIngredientDTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.dtos.ProductKgAPADetailsDTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
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

    public ProductFixedAPADetailsDTO fixedAPAToDetailsDTO(ProductFixedAPA productFixed,List<String> ingredients, Set<Allergen> allergens, List<CustomizableIngredientDTO> customizableIngredients) {
        String realPrice = "€ " + productFixed.getPrice();
        String realWeight ="Kg " + productFixed.getWeight();
        return new ProductFixedAPADetailsDTO(
                productFixed.getId(),
                productFixed.getName(),
                productFixed.getDescription(),
                productFixed.getStats(),
                allergens,
                ingredients,
                customizableIngredients,
                realPrice,
                realWeight,
                productFixed.getImageUrl()
        );
    }

    public ProductKgAPADetailsDTO kgAPAToDetailsDTO(ProductKgAPA product, List<String> ingredients, Set<Allergen> allergens, List<CustomizableIngredientDTO> customizableIngredients) {
        String realPrice = "€ " + product.getPricePerKg();
        return new ProductKgAPADetailsDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getStats(),
                allergens,
                ingredients,
                customizableIngredients,
                realPrice,
                product.getWeightRange(),
                product.getImageUrl()
        );
    }
}
