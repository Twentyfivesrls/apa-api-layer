package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.twentyfive.apaapilayer.models.CustomTimeVariantAPA;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCustomHoursDTO extends CategoryMinimalDTO{
    private boolean exactMatch;

    private LocalTime start;
    private LocalTime end;
    private Integer daysAhead;
    private LocalTime cutoffHour;
    private LocalTime cutoffResetHour;
    private LocalTime firstPickupAfterCutoff;
    private boolean sameDayAllowed;
    private CustomTimeVariantAPA variant;
}
