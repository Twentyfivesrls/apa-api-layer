package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInPurchaseDTO {
    private String id;
    private String name;
    private double weight; // Il peso del prodotto
    private int quantity; // La quantità acquistata del prodotto
    private String shape; // La forma del prodotto, potrebbe essere meglio come Enum se le forme sono predefinite
    private Map<String, String> customization; // Una mappa degli ingredienti personalizzati, dove la chiave è l'ID dell'ingrediente
    private boolean chocolateChips; // Un booleano per indicare se ci sono gocce di cioccolato o meno; sostituire con il nome corretto se necessario
    private String notes; // Testo aggiuntivo, forse per istruzioni o note speciali
    private String attachment; // Un allegato, presumibilmente un URL a un'immagine o a un documento
    private LocalDate deliveryDate; // La data di consegna del prodotto
    private double totalPrice;


    public ProductInPurchaseDTO(ProductInPurchase product, String name) {
        this.id = product.getId();
        this.name = name;
        this.weight = product.getWeight();
        this.quantity = product.getQuantity();
        this.shape = product.getShape();
        this.customization = product.getCustomization();
        this.chocolateChips = product.isChocolateChips();
        this.notes = product.getNotes();
        this.attachment = product.getAttachment();
        this.deliveryDate = product.getDeliveryDate();
        this.totalPrice = product.getTotalPrice();
    }





}
