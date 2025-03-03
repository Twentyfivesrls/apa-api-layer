package com.twentyfive.apaapilayer.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRange {
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", example = "2024-01-01", format = "date")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", example = "2024-01-01", format = "date")
    private LocalDate endDate;

    // Metodo per calcolare il numero di giorni tra startDate e endDate
    public long getDaysBetween() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }
}

