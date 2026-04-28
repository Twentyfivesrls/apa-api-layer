package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomTimeVariantAPA {
    private Integer daysAhead;
    private LocalTime cutoffHour;
    private LocalTime cutoffResetHour;
    private LocalTime firstPickupAfterCutoff;
    private boolean sameDayAllowed = false;
}
