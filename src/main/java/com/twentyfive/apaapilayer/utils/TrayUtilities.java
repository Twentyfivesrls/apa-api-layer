package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.DTOs.TrayAPADTO;
import com.twentyfive.apaapilayer.DTOs.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.Tray;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Measure;

import java.util.List;

public class TrayUtilities {
    public static TrayDetailsAPADTO mapToTrayDetailsAPADTO(Tray tray) {
        TrayDetailsAPADTO dto = new TrayDetailsAPADTO();
        dto.setId(tray.getId());
        dto.setDescription(tray.getDescription());
        dto.setCustomized(tray.isCustomized());
        dto.setMeasures(concatMeasureString(tray.getMeasures()));
        return dto;
    }

    public static TrayAPADTO mapToTrayAPADTO(Tray tray){
        TrayAPADTO dto= new TrayAPADTO();
        dto.setId(tray.getId());
        dto.setName(tray.getName());
        dto.setCustomized(tray.isCustomized());
        dto.setDescription(tray.getDescription());
        dto.setMeasures(concatMeasureString(tray.getMeasures()));
        dto.setEnabled(tray.isEnabled());
        return dto;
    }

    private static String concatMeasureString(List<Measure> measures){
        if(measures==null || measures.size()<1){
            return "";
        }
        String measureDTO = "";
        for (Measure measure:measures){
            measureDTO+=(measure.getLabel()+" ("+measure.getWeight()+" kg); ");
        }
        return measureDTO;
    }
}
