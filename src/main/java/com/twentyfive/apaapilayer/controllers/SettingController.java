package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingController {
    private final SettingRepository settingRepository;
    @GetMapping
    public ResponseEntity<SettingAPA> get(){
        return ResponseEntity.ok().body(settingRepository.findById("6628cb0ee48d706a10f32bfa").get());
    }
    @GetMapping("/getAllLocations")
    public ResponseEntity<List<String>> getAllLocations(){
        return ResponseEntity.ok().body(settingRepository.findById("6628cb0ee48d706a10f32bfa").get().getLocations());
    }
    @GetMapping("/alert")
    public ResponseEntity<Boolean> isAlertOn(){
        return ResponseEntity.ok().body(settingRepository.existsOrderReceivedAlertById("6628cb0ee48d706a10f32bfa"));
    }
}
