package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.CouponValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryOrderDTO {

    private List<SummarySingleItemDTO> summaryItems;
    private double discountApplied;
    private double totalPrice;
    private CouponValidation couponValidation;
}
