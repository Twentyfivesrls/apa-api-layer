package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.twentyfive.apaapilayer.models.CustomTimeVariantAPA;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveCustomTimeReq {
    private String id;
    private LocalTime start;
    private LocalTime end;
    private Integer daysAhead;
    private LocalTime cutoffHour;
    private LocalTime cutoffResetHour;
    private LocalTime firstPickupAfterCutoff;
    private boolean sameDayAllowed;
    private Integer maxMorningOrder;
    private Integer maxAfternoonOrder;
    private CustomTimeVariantAPA variant;
}
