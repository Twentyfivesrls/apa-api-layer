package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.TrayAPADTO;
import com.twentyfive.apaapilayer.dtos.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.exceptions.ExistingFieldException;
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
            @RequestParam(value = "idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(trayService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }

    @GetMapping("/getAllActive")
    public ResponseEntity<Page<TrayAPADTO>> getAllActive(@RequestParam("idCategory")String idCategory,
                                                         @RequestParam(value = "page", defaultValue ="0") int page,
                                                         @RequestParam(value = "size", defaultValue ="9") int size){
        return ResponseEntity.ok().body(trayService.getAllActive(idCategory,page,size));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<TrayDetailsAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(trayService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Tray> save(@RequestBody Tray t) {
        try {
            return ResponseEntity.ok().body(trayService.save(t));
        } catch (Exception e){
            throw new ExistingFieldException();
        }
    }

    @GetMapping("/activateOrDisable/{id}")
    public ResponseEntity<Boolean> activeOrDisableById(@PathVariable String id) {
        return ResponseEntity.ok().body(trayService.activateOrDisableById(id));
    }
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id) {
        return ResponseEntity.ok().body(trayService.deleteById(id));
    }

    @GetMapping("/imageById/{id}")
    public ResponseEntity<String> imageUrlById(@PathVariable String id){
        return ResponseEntity.ok().body(trayService.getImageUrl(id));
    }

}
