package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {
    private final TimeSlotRefreshScheduling timeSlotRefreshScheduling;
    @GetMapping("/test")
    public void test(){
        System.out.println("funziona!");



        //timeSlotRefreshScheduling.createSlotsForNext15Days();
    }
}
