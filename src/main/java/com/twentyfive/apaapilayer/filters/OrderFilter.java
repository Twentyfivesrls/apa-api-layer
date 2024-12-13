package com.twentyfive.apaapilayer.filters;

import com.twentyfive.apaapilayer.models.DateRange;
import com.twentyfive.apaapilayer.models.ValueRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFilter extends Filter {
    private DateRange dates;
    private String status;
    private ValueRange values;
}
