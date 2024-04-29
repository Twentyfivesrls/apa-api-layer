package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayDetailsAPADTO {
    private String id;
    private boolean customized;
    private String measures;
    private String description;
}
