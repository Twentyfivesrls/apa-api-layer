package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatDTO {
    private long totalOrders;
    private double avgOrders;
    private double avgCustomer;
    private double avgPrice;
}
