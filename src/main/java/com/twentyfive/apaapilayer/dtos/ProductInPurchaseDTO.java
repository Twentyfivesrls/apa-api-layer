package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Customization;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.IngredientsWithCategory;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInPurchaseDTO extends ItemInPurchaseDTO{
    private String id;
    private String name;
    private double price;
    private double weight; // Il peso del prodotto
    private int quantity;
    private String shape; // La forma del prodotto, potrebbe essere meglio come Enum se le forme sono predefinite
    private List<Customization> customization = new ArrayList<>(); // Una mappa degli ingredienti personalizzati, dove la chiave è l'ID dell'ingrediente
    private List<IngredientsWithCategory> ingredients = new ArrayList<>(); // Una mappa degli ingredienti, dove la chiave è l'ID dell'ingrediente
    private List<Allergen> allergens;
    private ProductUpdateField productUpdateField;
    private String attachment; // Un allegato, presumibilmente un URL a un'immagine o a un documento
    private String location; // Luogo in cui è depositato il prodotto
    private LocalDate deliveryDate; // La data di consegna del prodotto
    private double totalPrice;
    private boolean toPrepare;
    private String counterNote; //Nota da bancone, se disponibile
    private boolean fixed;

    public ProductInPurchaseDTO(ProductInPurchase product, String name, double price,ProductUpdateField productUpdateField) {
        this.id = product.getId();
        this.name = name;
        this.price = price;
        this.weight = product.getWeight();
        this.quantity = product.getQuantity();
        this.shape = product.getShape();
        this.productUpdateField = productUpdateField;
        this.customization = product.getCustomization();
        this.ingredients = product.getIngredients();
        this.toPrepare = product.isToPrepare();
        this.location = product.getLocation();
        this.attachment = product.getAttachment();
        this.deliveryDate = product.getDeliveryDate();
        this.totalPrice = product.getTotalPrice();
        this.allergens=product.getAllergens();
        this.counterNote = product.getCounterNote();
        this.fixed = product.isFixed();
    }





}
