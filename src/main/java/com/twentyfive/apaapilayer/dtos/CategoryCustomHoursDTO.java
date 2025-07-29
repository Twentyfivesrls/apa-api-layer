package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCustomHoursDTO extends CategoryMinimalDTO{
    private boolean exactMatch;

    private LocalTime start;
    private LocalTime end;
}
