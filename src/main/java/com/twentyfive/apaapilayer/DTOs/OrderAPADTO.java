package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAPADTO {

    private String id;
    private String firstName; // Nome cambiato in firstName
    private String lastName; // Cognome cambiato in lastName
    private LocalDate pickupDate; // dataRitiro cambiato in pickupDate
    private LocalTime pickupTime; // orarioRitiro cambiato in pickupTime
    private String price; // prezzo cambiato in price
    private String status;
    private List<ProductInPurchase> products; // product cambiato in products
    private String email;
    private String phoneNumber; // telefoneNumber corretto a phoneNumber

}