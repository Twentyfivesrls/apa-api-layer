package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.ProductKgAPADTO;
import com.twentyfive.apaapilayer.DTOs.TrayAPADTO;
import com.twentyfive.apaapilayer.DTOs.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.Tray;
import com.twentyfive.apaapilayer.services.TrayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trays")
public class TrayController {
    private final TrayService trayService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<TrayAPADTO>> getAll(
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(trayService.getAll(page,size,sortColumn,sortDirection));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<TrayDetailsAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(trayService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Tray> save(@RequestBody Tray tray) {
        return ResponseEntity.ok().body(trayService.save(tray));
    }

    @PutMapping("/activateOrDisable/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(trayService.activateOrDisableById(id));
    }

}
