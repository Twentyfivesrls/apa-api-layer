package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public Boolean isThisDayAvailable(LocalDate date) {
        List<LocalDate> inactivityDays = get().getInactivityDays();
        if (inactivityDays.contains(date)) {
            return false;
        }
        return true;
    }

    public LocalDate obtainDateIfTenDaysBefore(){
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(10);
       return get().getInactivityDays().stream().filter(date -> !date.isBefore(today) && !date.isAfter(maxDate)).min(Comparator.naturalOrder()).orElse(null);
    }
}
