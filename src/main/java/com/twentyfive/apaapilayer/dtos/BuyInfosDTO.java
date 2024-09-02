package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.CustomInfo;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyInfosDTO {
    private List<Integer> positions;
    private LocalDateTime selectedPickupDateTime;
    private String note;
    private CustomInfo customInfo;
    private String paymentId;
    private String captureId;
}
