package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsPrintAPADTO {
    private String id;
    private String fullName;
    private String email;
    private String note;
    private String phoneNumber;
    private String status;
    private LocalDateTime pickupDate;
    private List<ProductInPurchaseDTO> products;
    private List<BundleInPurchaseDTO> bundles;

    public String getFormattedPickupDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return pickupDate.format(formatter);
    }
}
