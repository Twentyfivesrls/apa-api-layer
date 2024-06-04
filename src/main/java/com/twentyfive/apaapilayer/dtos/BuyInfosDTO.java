package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyInfosDTO {
    private List<Integer> positions;
    private LocalDateTime selectedPickupDateTime;
    private String note;

}
