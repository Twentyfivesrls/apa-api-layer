package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.SettingAPA;
import com.twentyfive.apaapilayer.repositories.SettingRepository;
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
//@PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_baker') or hasRole('ROLE_counter')")
public class SettingController {
    private final SettingRepository settingRepository;
    @GetMapping
    public ResponseEntity<SettingAPA> get(){
        return ResponseEntity.ok().body(settingRepository.findById("6628cb0ee48d706a10f32bfa").get());
    }

    @PutMapping
    public ResponseEntity<?> updateSetting(
            @Valid @RequestBody SettingAPA newSettings,
            BindingResult bindingResult) {

        // Verifica errori di validazione
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        // Controlla che l'oggetto contenga un ID
        if (newSettings.getId() == null) {
            return ResponseEntity.badRequest().body("ID non presente nell'oggetto settings");
        }

        // Verifica che l'entità esista già
        if (!settingRepository.existsById(newSettings.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Settings con ID " + newSettings.getId() + " non trovato");
        }

        try {
            // Aggiorna le impostazioni
            SettingAPA updatedSettings = settingRepository.save(newSettings);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore interno del server");
        }
    }

    @GetMapping("/getAllLocations")
    public ResponseEntity<List<String>> getAllLocations(){
        return ResponseEntity.ok().body(settingRepository.findById("6628cb0ee48d706a10f32bfa").get().getLocations());
    }
    @GetMapping("/alert")
    public ResponseEntity<Boolean> isAlertOn(){
        return ResponseEntity.ok().body(settingRepository.existsOrderReceivedAlertById("6628cb0ee48d706a10f32bfa"));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllRoles")
    public ResponseEntity<List<String>> getAllRoles(){
        return ResponseEntity.ok().body(settingRepository.findById("6628cb0ee48d706a10f32bfa").get().getRoles());
    }



}
