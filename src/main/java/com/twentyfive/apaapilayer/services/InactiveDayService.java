package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.repositories.InactiveDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.InactiveDay;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InactiveDayService {

    private final InactiveDayRepository inactiveDayRepository;

    public List<InactiveDay> get() {
        List<InactiveDay> days = inactiveDayRepository.findAll();

        if (days.isEmpty()) {
            return Collections.emptyList();
        }
        return days;
    }

    public List<InactiveDay> getAllByFullDay(boolean fullDay) {
        List<InactiveDay> days = inactiveDayRepository.findAllByFullDay(fullDay);

        if (days.isEmpty()) {
            return Collections.emptyList();
        }
        return days;
    }
    
    public List<InactiveDay> update(List<InactiveDay> newInactiveDays) {
        List<InactiveDay> existingDays = inactiveDayRepository.findAll();

        Set<String> existingDaysIds = existingDays.stream()
                .map(InactiveDay::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if(newInactiveDays.isEmpty()) {
            if(!(existingDaysIds.isEmpty())) {
                inactiveDayRepository.deleteAll(existingDays);
            }
            return Collections.emptyList();
        }

        Set<String> seenIds = new HashSet<>();
        for (InactiveDay day : newInactiveDays) {
            String dayId = day.getId();

            if(dayId == null) {
                InactiveDay saved = inactiveDayRepository.save(day);
                if (saved.getId() != null) {
                    seenIds.add(saved.getId());
                }
            } else {
                inactiveDayRepository.save(day);
                seenIds.add(dayId);
            }
        }
        existingDaysIds.removeAll(seenIds);
        if(!existingDaysIds.isEmpty()) {
            inactiveDayRepository.deleteAllById(existingDaysIds);
        }

        return inactiveDayRepository.findAll();
    }

    public List<LocalDate> obtainConsecutiveDatesIfTenDaysBefore() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(10);

        List<LocalDate> inactivityDays = getAllByFullDay(true)
            .stream()
            .map(InactiveDay::getDate)
            .collect(Collectors.toList());

        List<LocalDate> sortedDates = inactivityDays.stream()
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
