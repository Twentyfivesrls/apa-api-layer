package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.repositories.InactiveDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.InactiveDay;

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
}
