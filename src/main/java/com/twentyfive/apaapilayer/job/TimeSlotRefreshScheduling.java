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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TimeSlotRefreshScheduling {
    private final SettingRepository settingRepository;
    private final TimeSlotAPARepository timeSlotAPARepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void timeSlotRefreshDaily(){
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        Map<LocalDate, Map<LocalTime, Integer>> availableSlots = timeSlotAPA.getNumSlotsMap();

        // Identifica il giorno massimo presente nella mappa
        LocalDate maxDay = availableSlots.keySet().stream().max(LocalDate::compareTo).orElse(null);

        // Aggiungi un giorno dopo il giorno massimo presente nella mappa
        LocalDate giornoDopoMax;
        if (maxDay != null) {
            giornoDopoMax = maxDay.plusDays(1);
        } else {
            giornoDopoMax = LocalDate.now(); // Se la mappa è vuota, aggiungi un giorno dopo l'odierno
        }
        DateRange dateRange =settingAPA.getBusinessHours();
        LocalTime slots =dateRange.getStartTime();
        Map<LocalTime,Integer> timeSlots = new HashMap<>();
        while(slots.isBefore(dateRange.getEndTime())){
            if(slots.isBefore(LocalTime.of(14,0))){
                timeSlots.put(slots, settingAPA.getMaxMorningOrder());
            } else {
                timeSlots.put(slots, settingAPA.getMaxAfternoonOrder());
            }
            slots = slots.plusHours(1);
        }
        timeSlotAPA.initializeDay(giornoDopoMax,timeSlots);
        // Rimuovi tutti i giorni antecedenti all'odierno
        LocalDate minDay = availableSlots.keySet().stream().min(LocalDate::compareTo).orElse(null).minusDays(1);
        if (minDay != null) {
            LocalDate oggi = LocalDate.now();
            Iterator<LocalDate> iterator = availableSlots.keySet().iterator();
            while (iterator.hasNext()) {
                LocalDate giorno = iterator.next();
                if (giorno.isAfter(minDay) && giorno.isBefore(oggi)) {
                    iterator.remove();
                }
            }
        }
        timeSlotAPARepository.save(timeSlotAPA);
    }
    /*public void createSlotsForNext15Days() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        Map<LocalDate, Map<LocalTime, Integer>> slotsMap = new HashMap<>();

        // Ottieni la data odierna
        LocalDate currentDate = LocalDate.now();

        // Crea gli slot per i prossimi 15 giorni
        for (int i = 0; i < 15; i++) {
            // Calcola la data per il giorno successivo
            LocalDate nextDate = currentDate.plusDays(i);

            // Crea la mappa degli slot per il giorno successivo
            DateRange dateRange =settingAPA.getBusinessHours();
            LocalTime slots =dateRange.getStartTime();
            Map<LocalTime,Integer> timeSlots = new HashMap<>();
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
     */
}
