package com.twentyfive.apaapilayer.filters;

import com.twentyfive.apaapilayer.models.DateRange;
import com.twentyfive.apaapilayer.models.ValueRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.CouponType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponFilter extends Filter {
    private DateRange dates;
    private String type;
    private ValueRange values;
    private Boolean expired;
}
