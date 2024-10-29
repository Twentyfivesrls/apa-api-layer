package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryEmailDTO {
    private String id;
    private List<SummarySingleItemDTO> products;
    private String totalPrice;
    private String paymentID;
}
