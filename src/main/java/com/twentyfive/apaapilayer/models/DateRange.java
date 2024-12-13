package com.twentyfive.apaapilayer.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
}
