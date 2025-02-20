package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    @Value("${setting.id}")
    private String settingId;

    private final SettingRepository settingRepository;


    public Boolean update(SettingAPA newSettings) {
        newSettings.setId(settingId);

        return settingRepository.save(newSettings) !=null;
    }

    public SettingAPA get() {
        return settingRepository.findById(settingId).orElse(null);
    }
}
