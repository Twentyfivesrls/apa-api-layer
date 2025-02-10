package com.twentyfive.apaapilayer.job;

import com.twentyfive.apaapilayer.services.GlobalStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GlobalStatScheduling {
    private final GlobalStatService globalStatService;

    @Scheduled(cron = "0 30 2 * * *")
    public void globalStat() {
        globalStatService.createByDate(LocalDate.now().minusDays(1));
    }
}
