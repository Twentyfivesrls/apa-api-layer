package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
import com.twentyfive.apaapilayer.services.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
@PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_baker') or hasRole('ROLE_counter')")
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

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllRoles")
    public ResponseEntity<List<String>> getAllRoles(){
        return ResponseEntity.ok().body(settingService.get().getRoles());
    }



}
