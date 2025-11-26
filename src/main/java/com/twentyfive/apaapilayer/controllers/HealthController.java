package com.twentyfive.apaapilayer.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @RequestMapping("")
    public String healthCheck() {
        return "APA API Layer is running.";
    }
}
