package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrayDTO {
    private String id;                // Unique identifier for the Tray
    private String name;              // Name of the Tray
    private String type;              // Typology or category of the Tray
    private List<String> measures;    // List of measurements or sizes available for the Tray
    private String note;              // Additional notes about the Tray
    private String pricePerKg;        // Price per kilogram for the Tray
    private String imageUrl;          // Image URL or path for the Tray
}
