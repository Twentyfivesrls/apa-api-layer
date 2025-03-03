package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final TimeSlotRefreshScheduling timeSlotRefreshScheduling;

    @Value("${setting.id}")
    private String settingId;

    private final SettingRepository settingRepository;


    public Boolean update(SettingAPA newSettings) {
        newSettings.setId(settingId);

        timeSlotRefreshScheduling.updateTimeSlot(this.get(),newSettings);
        return settingRepository.save(newSettings) !=null;
    }

    public SettingAPA get() {
        return settingRepository.findById(settingId).orElse(null);
    }

    public Boolean isTodayAvailable() {
        List<LocalDate> inactivityDays = get().getInactivityDays();
        LocalDate today = LocalDate.now();
        if (inactivityDays.contains(today)) {
            return false;
        }
        return true;
    }
}
