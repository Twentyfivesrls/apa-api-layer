package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationReq {
    private String orderId;
    private int position;
    private String location;
    private String counterNote;
}
