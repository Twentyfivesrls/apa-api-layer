package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayAPADTO {
    private String id;
    private String name;
    private String type;
    private String measures;
    private String description;
    private boolean enabled;
}