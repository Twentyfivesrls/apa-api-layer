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

    public void createSlotsForNext30Days() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        timeSlotAPA.getNumSlotsMap().clear();
        Map<LocalDate, Map<LocalTime, Integer>> slotsMap = new TreeMap<>();

        // Ottieni la data odierna
        LocalDate currentDate = LocalDate.now();

        // Crea gli slot per i prossimi 15 giorni
        for (int i = 0; i < 30; i++) {
            // Calcola la data per il giorno successivo
            LocalDate nextDate = currentDate.plusDays(i);

            // Crea la mappa degli slot per il giorno successivo
            DateRange dateRange =settingAPA.getBusinessHours();
            LocalTime slots =dateRange.getStartTime();
            Map<LocalTime,Integer> timeSlots = new TreeMap<>();
            while(slots.isBefore(dateRange.getEndTime())){
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
}
