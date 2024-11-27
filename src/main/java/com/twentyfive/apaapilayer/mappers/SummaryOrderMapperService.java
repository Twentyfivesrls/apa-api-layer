package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.SummaryOrderDTO;
import com.twentyfive.apaapilayer.dtos.SummarySingleItemDTO;
import com.twentyfive.apaapilayer.models.CouponValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryOrderMapperService {

    public SummaryOrderDTO mapBuyInfosToSummaryOrder(List<SummarySingleItemDTO> summaryItems, CouponValidation validation,double discountApplied, double totalPrice) {
        SummaryOrderDTO summaryOrderDTO = new SummaryOrderDTO();
        summaryOrderDTO.setCouponValidation(validation);
        summaryOrderDTO.setSummaryItems(summaryItems);
        summaryOrderDTO.setDiscountApplied(discountApplied);
        summaryOrderDTO.setTotalPrice(totalPrice);
        return summaryOrderDTO;
    }

}
