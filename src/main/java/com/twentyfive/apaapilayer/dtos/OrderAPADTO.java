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
    private String methodPayment;
    private LocalDateTime pickupDateTime;
    private double realPrice;
    private String price;
    private String status;
    private boolean toPrepare; //if atleast 1 of the products are in toPrepare
    private boolean unread; //status unread per admin
    private boolean bakerUnread; //status unread per pasticceria
    private boolean counterUnread; //status unread per bancone

    public String getFormattedPickupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.ITALIAN);
        return pickupDateTime.format(formatter);
    }

}