package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.dtos.TrayAPADTO;
import com.twentyfive.apaapilayer.dtos.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.Tray;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Measure;

import java.util.List;

public class TrayUtilities {
    public static TrayDetailsAPADTO mapToTrayDetailsAPADTO(Tray tray, List<Allergen> realAllergens) {
        TrayDetailsAPADTO dto = new TrayDetailsAPADTO();
        dto.setId(tray.getId());
        dto.setName(tray.getName());
        dto.setDescription(tray.getDescription());
        dto.setCustomized(tray.isCustomized());
        dto.setPersonalized(tray.isCustomized() ? "Personalizzato" : "Standard");
        dto.setStats(tray.getStats());
        dto.setMeasures(concatMeasureString(tray.getMeasures()));
        dto.setMeasuresList(tray.getMeasures());
        dto.setPricePerKg(tray.getPricePerKg());
        dto.setImageUrl(tray.getImageUrl());
        dto.setAllergens(realAllergens);
        return dto;
    }

    public static TrayAPADTO mapToTrayAPADTO(Tray tray, List<Allergen> realAllergens){
        TrayAPADTO dto= new TrayAPADTO();
        dto.setId(tray.getId());
        dto.setName(tray.getName());
        dto.setImageUrl(tray.getImageUrl());
        dto.setCustomized(tray.isCustomized() ? "Personalizzato" : "Standard");
        dto.setDescription(tray.getDescription());
        dto.setMeasures(concatMeasureString(tray.getMeasures()));
        dto.setActive(tray.isActive());
        dto.setPricePerKg(tray.getPricePerKg());
        dto.setAllergens(realAllergens);
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
