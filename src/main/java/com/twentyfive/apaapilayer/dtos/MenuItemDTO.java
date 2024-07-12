package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.MenuItemAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDTO {

    private String id;
    private String name;
    private String categoryId;
    private String description;
    private String price;
    private List<Allergen> allergens;
    private String imageUrl;

    public MenuItemDTO(MenuItemAPA menuItemAPA, List<Allergen> allergens){
        this.id=menuItemAPA.getId();
        this.name=menuItemAPA.getName();
        this.categoryId=menuItemAPA.getCategoryId();
        this.description=menuItemAPA.getDescription();
        this.price="€ "+menuItemAPA.getPrice();
        this.allergens=allergens;
        this.imageUrl=menuItemAPA.getImageUrl();
    }
}
