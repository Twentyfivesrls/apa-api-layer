package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsAPADTO {
    private String id;
    private List<ProductInPurchaseDTO> products;
    private List<BundleInPurchaseDTO> bundles;
    private String email;
    private String orderNote;
    private String customerNote;
    private String phoneNumber;
    private LocalDateTime pickupDateTime;
    private String paymentId;
    private double totalPrice;
    private double totalWeight;
    private String status;
    private boolean unread;

    public String getFormattedPickupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.ITALIAN);
        return pickupDateTime.format(formatter);
    }
}
