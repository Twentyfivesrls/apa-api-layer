package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.services.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.DateRange;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingController {
    private final SettingService settingService;


    @GetMapping("/get")
    public ResponseEntity<SettingAPA> get(){
        return ResponseEntity.ok().body(settingService.get());
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> updateSetting(@RequestBody SettingAPA newSettings) {
        return ResponseEntity.ok().body(settingService.update(newSettings));
    }

    @GetMapping("/getAllLocations")
    public ResponseEntity<List<String>> getAllLocations(){
        return ResponseEntity.ok().body(settingService.get().getLocations());
    }
    @GetMapping("/alert")
    public ResponseEntity<Boolean> isAlertOn(){
        return ResponseEntity.ok().body(settingService.get().isOrderReceivedAlert());
    }
    @GetMapping("/check-maintenance")
    public ResponseEntity<Boolean> checkMaintenance(){
        return ResponseEntity.ok().body(!(settingService.get().isOrdersEnabled()));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllRoles")
    public ResponseEntity<List<String>> getAllRoles(){
        return ResponseEntity.ok().body(settingService.get().getRoles());
    }

    @GetMapping("/getBusinessHours")
    public ResponseEntity<DateRange> getBusinessHours(){
        return ResponseEntity.ok().body(settingService.get().getBusinessHours());
    }

    @PutMapping("/updateBusinessHours")
    public ResponseEntity<DateRange> updateBusinessHours(@RequestBody DateRange newBusinessHours) {
        return ResponseEntity.ok().body(settingService.updateBusinessHours(newBusinessHours));
    }

    @GetMapping("/isTodayAvailable")
    public ResponseEntity<Boolean> isTodayAvailable(){
        return ResponseEntity.ok().body(settingService.isTodayAvailable());
    }

}
