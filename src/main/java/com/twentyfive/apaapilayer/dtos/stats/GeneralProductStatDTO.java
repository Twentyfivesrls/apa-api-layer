package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralProductStatDTO {

    private long totalProductsSold; // Numero totale di prodotti venduti
    private long totalOrders; // Numero totale di ordini effettuati
    private List<String> distinctCustomerServed;
    private double totalRevenue; // Totale guadagnato

    public long getTotalCustomerServed(){
        return distinctCustomerServed != null ? distinctCustomerServed.size() : 0;
    }
}
