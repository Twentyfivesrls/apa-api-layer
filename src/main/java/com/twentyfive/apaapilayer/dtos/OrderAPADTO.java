package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAPADTO {

    private String id;
    private String firstName;
    private String lastName;
    private LocalDateTime pickupDate;
    private double realPrice;
    private String price;
    private String status;

    public String getFormattedPickupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.ITALIAN);
        return pickupDate.format(formatter);
    }

}