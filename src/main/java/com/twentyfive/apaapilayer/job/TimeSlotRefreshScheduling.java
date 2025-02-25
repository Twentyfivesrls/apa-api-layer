package com.twentyfive.apaapilayer.job;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.models.TimeSlotAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import com.twentyfive.apaapilayer.repositories.TimeSlotAPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.DateRange;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TimeSlotRefreshScheduling {
    private final SettingRepository settingRepository;
    private final TimeSlotAPARepository timeSlotAPARepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void timeSlotRefreshDaily() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        Map<LocalDate, Map<LocalTime, Integer>> availableSlots = timeSlotAPA.getNumSlotsMap();

        // Ottieni la data più distante nel futuro (ultima data presente nel TreeMap)
        LocalDate maxDay = availableSlots.isEmpty() ? LocalDate.now() : Collections.max(availableSlots.keySet());

        // Determina il giorno successivo da inizializzare
        LocalDate giornoSuccessivo = maxDay.plusDays(1);

        // Crea la mappa degli slot temporali per il nuovo giorno
        Map<LocalTime, Integer> timeSlots = createTimeSlotsMap(settingAPA);

        // Inizializza il nuovo giorno con gli slot temporali creati
        timeSlotAPA.initializeDay(giornoSuccessivo, timeSlots);

        // Rimuovi tutti i giorni antecedenti all'odierno
        LocalDate giornoCorrente = LocalDate.now();
        availableSlots.keySet().removeIf(day -> day.isBefore(giornoCorrente));

        timeSlotAPARepository.save(timeSlotAPA);
    }

    private Map<LocalTime, Integer> createTimeSlotsMap(SettingAPA settingAPA) {
        Map<LocalTime, Integer> timeSlots = new TreeMap<>();
        DateRange dateRange = settingAPA.getBusinessHours();
        LocalTime slots = dateRange.getStartTime();
        while (slots.isBefore(dateRange.getEndTime())) {
            int maxOrder = slots.isBefore(LocalTime.of(14, 0)) ? settingAPA.getMaxMorningOrder() : settingAPA.getMaxAfternoonOrder();
            timeSlots.put(slots, maxOrder);
            slots = slots.plusHours(1);
        }
        return timeSlots;
    }

    public void createSlotsForNext90Days() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        timeSlotAPA.getNumSlotsMap().clear();
        Map<LocalDate, Map<LocalTime, Integer>> slotsMap = new TreeMap<>();

        // Ottieni la data odierna
        LocalDate currentDate = LocalDate.now();

        // Crea gli slot per i prossimi 90 giorni
        for (int i = 0; i < 90; i++) {
            // Calcola la data per il giorno successivo
            LocalDate nextDate = currentDate.plusDays(i);

            // Crea la mappa degli slot per il giorno successivo
            DateRange dateRange =settingAPA.getBusinessHours();
            LocalTime slots =dateRange.getStartTime();
            Map<LocalTime,Integer> timeSlots = new TreeMap<>();
            while(!slots.isAfter(dateRange.getEndTime())) {
                if(slots.isBefore(LocalTime.of(14,0))){
                    timeSlots.put(slots, settingAPA.getMaxMorningOrder());
                } else {
                    timeSlots.put(slots, settingAPA.getMaxAfternoonOrder());
                }
                slots = slots.plusHours(1);
            }
            // Aggiungi altri slot per altre ore, se necessario

            // Aggiungi la mappa degli slot alla mappa principale utilizzando la data come chiave
            slotsMap.put(nextDate, timeSlots);
        }
        timeSlotAPA.setNumSlotsMap(slotsMap);
        timeSlotAPARepository.save(timeSlotAPA);
    }

    public void updateTimeSlot(SettingAPA oldSettings, SettingAPA newSettings) {
        TimeSlotAPA timeSlot = timeSlotAPARepository.findById("66e951df4682b089c80a879b").get();
        Map<LocalDate, Map<LocalTime, Integer>> numSlotsMap = timeSlot.getNumSlotsMap();

        DateRange oldBusinessHours = oldSettings.getBusinessHours();
        DateRange newBusinessHours = newSettings.getBusinessHours();

        LocalTime oldStart = oldBusinessHours.getStartTime();
        LocalTime oldEnd = oldBusinessHours.getEndTime();
        LocalTime newStart = newBusinessHours.getStartTime();
        LocalTime newEnd = newBusinessHours.getEndTime();

        int oldMaxMorning = oldSettings.getMaxMorningOrder();
        int oldMaxAfternoon = oldSettings.getMaxAfternoonOrder();
        int newMaxMorning = newSettings.getMaxMorningOrder();
        int newMaxAfternoon = newSettings.getMaxAfternoonOrder();

        for (Map.Entry<LocalDate, Map<LocalTime, Integer>> dayEntry : numSlotsMap.entrySet()) {
            Map<LocalTime, Integer> oldDaySlots = dayEntry.getValue();
            Map<LocalTime, Integer> updatedDaySlots = new TreeMap<>();

            LocalTime current = newStart;
            while (!current.isAfter(newEnd)) {
                int newMax = current.isBefore(LocalTime.of(14, 0)) ? newMaxMorning : newMaxAfternoon;
                int oldMax = current.isBefore(LocalTime.of(14, 0)) ? oldMaxMorning : oldMaxAfternoon;
                int newAvailable;

                if (current.compareTo(oldStart) >= 0 && current.compareTo(oldEnd) <= 0 && oldDaySlots.containsKey(current)) {
                    int oldAvailable = oldDaySlots.get(current);
                    if (newMax < oldMax) {
                        // Se il nuovo max è inferiore, impostiamo a newMax
                        newAvailable = newMax;
                    } else if (newMax > oldMax) {
                        // Se il nuovo max è superiore, aggiungiamo la differenza all'available esistente
                        newAvailable = oldAvailable + (newMax - oldMax);
                        // Clamparlo a newMax se superato
                        if (newAvailable > newMax) {
                            newAvailable = newMax;
                        }
                    } else {
                        // Se i max sono uguali, manteniamo il valore attuale
                        newAvailable = oldAvailable;
                    }
                } else {
                    // Se lo slot non esisteva precedentemente, lo inizializziamo a newMax
                    newAvailable = newMax;
                }

                updatedDaySlots.put(current, newAvailable);
                current = current.plusHours(1);
            }
            numSlotsMap.put(dayEntry.getKey(), updatedDaySlots);
        }
        timeSlotAPARepository.save(timeSlot);
    }



}
