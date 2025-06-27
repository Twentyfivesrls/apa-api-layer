package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<LocalDate> obtainConsecutiveDatesIfTenDaysBefore() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(10);

        List<LocalDate> sortedDates = get().getInactivityDays().stream()
                .filter(date -> !date.isBefore(today) && !date.isAfter(maxDate))
                .sorted()
                .collect(Collectors.toList());

        if (sortedDates.isEmpty()) {
            return Collections.emptyList();
        }

        List<LocalDate> consecutiveDates = new ArrayList<>();
        LocalDate firstDate = sortedDates.get(0);
        consecutiveDates.add(firstDate);

        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i).equals(consecutiveDates.get(consecutiveDates.size() - 1).plusDays(1))) {
                consecutiveDates.add(sortedDates.get(i));
            } else {
                break; // Interruzione se le date non sono consecutive
            }
        }

        return consecutiveDates;
    }

}
