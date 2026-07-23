package com.twentyfive.apaapilayer.job;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.models.TimeSlotAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.CustomTimeCategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final CustomTimeCategoryRepository customTimeCategoryRepository;

    private static final LocalTime AFTERNOON_THRESHOLD = LocalTime.of(14, 0);
    private static final int HORIZON_DAYS = 90;

    @Scheduled(cron = "0 0 0 * * *")
    public void timeSlotRefreshDaily() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        LocalDate giornoCorrente = LocalDate.now();

        for (TimeSlotAPA timeSlotAPA : timeSlotAPARepository.findAll()) {
            Map<LocalDate, Map<LocalTime, Integer>> availableSlots = timeSlotAPA.getNumSlotsMap();

            // Aggiungi il giorno successivo all'ultimo presente, con la capacità della categoria
            LocalDate maxDay = availableSlots.isEmpty() ? giornoCorrente : Collections.max(availableSlots.keySet());
            LocalDate giornoSuccessivo = maxDay.plusDays(1);
            timeSlotAPA.initializeDay(giornoSuccessivo, createTimeSlotsMap(settingAPA, timeSlotAPA.getCategoryId()));

            // Rimuovi i giorni passati
            availableSlots.keySet().removeIf(day -> day.isBefore(giornoCorrente));

            timeSlotAPARepository.save(timeSlotAPA);
        }
    }

    /**
     * Capacità (ordini/ora) di un singolo giorno per una categoria:
     * usa i massimi della categoria se impostati, altrimenti i default globali di Setting.
     */
    private Map<LocalTime, Integer> createTimeSlotsMap(SettingAPA settingAPA, String categoryId) {
        CustomTimeCategoryAPA ct = (categoryId != null)
                ? customTimeCategoryRepository.findByCategory_Id(categoryId).orElse(null)
                : null;
        return createTimeSlotsMap(settingAPA, ct);
    }

    // Overload senza lookup DB: la categoria è già stata caricata (riuso su tutti i giorni)
    private Map<LocalTime, Integer> createTimeSlotsMap(SettingAPA settingAPA, CustomTimeCategoryAPA ct) {
        int maxMorning = (ct != null && ct.getMaxMorningOrder() != null)
                ? ct.getMaxMorningOrder() : settingAPA.getMaxMorningOrder();
        int maxAfternoon = (ct != null && ct.getMaxAfternoonOrder() != null)
                ? ct.getMaxAfternoonOrder() : settingAPA.getMaxAfternoonOrder();

        Map<LocalTime, Integer> timeSlots = new TreeMap<>();
        DateRange dateRange = settingAPA.getBusinessHours();
        LocalTime slots = dateRange.getStartTime();
        while (!slots.isAfter(dateRange.getEndTime())) {
            int maxOrder = slots.isBefore(AFTERNOON_THRESHOLD) ? maxMorning : maxAfternoon;
            timeSlots.put(slots, maxOrder);
            slots = slots.plusHours(1);
        }
        return timeSlots;
    }

    /**
     * Capacità (ordini/ora) di una categoria per un giorno tipo: usata come tetto quando si liberano slot.
     */
    public Map<LocalTime, Integer> getHourCapacityMap(String categoryId) {
        return createTimeSlotsMap(settingRepository.findAll().get(0), categoryId);
    }

    /**
     * Rigenera da zero gli slot per i prossimi HORIZON_DAYS giorni: un documento TimeSlotAPA per categoria.
     */
    public void createSlotsForNext90Days() {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        List<CategoryAPA> categories = categoryRepository.findAll();

        // Rimuove tutti i documenti esistenti (incluso l'eventuale legacy globale senza categoryId)
        timeSlotAPARepository.deleteAll();

        LocalDate currentDate = LocalDate.now();
        for (CategoryAPA category : categories) {
            timeSlotAPARepository.save(buildTimeSlotForCategory(settingAPA, category.getId(), currentDate));
        }
    }

    private TimeSlotAPA buildTimeSlotForCategory(SettingAPA settingAPA, String categoryId, LocalDate startDate) {
        // Una sola lookup della categoria, poi una mappa fresca (indipendente) per ogni giorno
        CustomTimeCategoryAPA ct = (categoryId != null)
                ? customTimeCategoryRepository.findByCategory_Id(categoryId).orElse(null)
                : null;
        TimeSlotAPA timeSlotAPA = new TimeSlotAPA();
        timeSlotAPA.setCategoryId(categoryId);
        Map<LocalDate, Map<LocalTime, Integer>> slotsMap = new TreeMap<>();
        for (int i = 0; i < HORIZON_DAYS; i++) {
            slotsMap.put(startDate.plusDays(i), createTimeSlotsMap(settingAPA, ct));
        }
        timeSlotAPA.setNumSlotsMap(slotsMap);
        return timeSlotAPA;
    }

    /**
     * Rigenera gli slot futuri della singola categoria (usato quando cambiano i massimi della categoria).
     * NB: reimposta la capacità residua dei giorni futuri al nuovo massimo.
     */
    public void regenerateCategorySlots(String categoryId) {
        SettingAPA settingAPA = settingRepository.findAll().get(0);
        TimeSlotAPA existing = timeSlotAPARepository.findByCategoryId(categoryId).orElse(null);
        LocalDate today = LocalDate.now();
        TimeSlotAPA rebuilt = buildTimeSlotForCategory(settingAPA, categoryId, today);
        if (existing != null) {
            rebuilt.setId(existing.getId());
        }
        timeSlotAPARepository.save(rebuilt);
    }

    /**
     * Ricalcola gli slot esistenti quando cambiano le impostazioni globali, preservando le prenotazioni.
     * Applicato per ogni categoria: le categorie con massimi propri non sono influenzate dal cambio globale.
     */
    public void updateTimeSlot(SettingAPA oldSettings, SettingAPA newSettings) {
        for (TimeSlotAPA timeSlot : timeSlotAPARepository.findAll()) {
            CustomTimeCategoryAPA ct = (timeSlot.getCategoryId() != null)
                    ? customTimeCategoryRepository.findByCategory_Id(timeSlot.getCategoryId()).orElse(null)
                    : null;

            DateRange oldBusinessHours = oldSettings.getBusinessHours();
            DateRange newBusinessHours = newSettings.getBusinessHours();

            LocalTime oldStart = oldBusinessHours.getStartTime();
            LocalTime oldEnd = oldBusinessHours.getEndTime();
            LocalTime newStart = newBusinessHours.getStartTime();
            LocalTime newEnd = newBusinessHours.getEndTime();

            int oldMaxMorning = (ct != null && ct.getMaxMorningOrder() != null) ? ct.getMaxMorningOrder() : oldSettings.getMaxMorningOrder();
            int oldMaxAfternoon = (ct != null && ct.getMaxAfternoonOrder() != null) ? ct.getMaxAfternoonOrder() : oldSettings.getMaxAfternoonOrder();
            int newMaxMorning = (ct != null && ct.getMaxMorningOrder() != null) ? ct.getMaxMorningOrder() : newSettings.getMaxMorningOrder();
            int newMaxAfternoon = (ct != null && ct.getMaxAfternoonOrder() != null) ? ct.getMaxAfternoonOrder() : newSettings.getMaxAfternoonOrder();

            Map<LocalDate, Map<LocalTime, Integer>> numSlotsMap = timeSlot.getNumSlotsMap();
            for (Map.Entry<LocalDate, Map<LocalTime, Integer>> dayEntry : numSlotsMap.entrySet()) {
                Map<LocalTime, Integer> oldDaySlots = dayEntry.getValue();
                Map<LocalTime, Integer> updatedDaySlots = new TreeMap<>();

                LocalTime current = newStart;
                while (!current.isAfter(newEnd)) {
                    int newMax = current.isBefore(AFTERNOON_THRESHOLD) ? newMaxMorning : newMaxAfternoon;
                    int oldMax = current.isBefore(AFTERNOON_THRESHOLD) ? oldMaxMorning : oldMaxAfternoon;
                    int newAvailable;

                    if (current.compareTo(oldStart) >= 0 && current.compareTo(oldEnd) <= 0 && oldDaySlots.containsKey(current)) {
                        int oldAvailable = oldDaySlots.get(current);
                        if (newMax < oldMax) {
                            newAvailable = newMax;
                        } else if (newMax > oldMax) {
                            newAvailable = oldAvailable + (newMax - oldMax);
                            if (newAvailable > newMax) {
                                newAvailable = newMax;
                            }
                        } else {
                            newAvailable = oldAvailable;
                        }
                    } else {
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
}
