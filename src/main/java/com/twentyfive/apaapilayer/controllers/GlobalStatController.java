package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.stats.GlobalStatDTO;
import com.twentyfive.apaapilayer.models.DateRange;
import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import com.twentyfive.apaapilayer.services.GlobalStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.GlobalStat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/global-stat")
@RequiredArgsConstructor
public class GlobalStatController {
    private final GlobalStatService globalStatService;

    @GetMapping("/get")
    public ResponseEntity<GlobalStat> getById(@RequestParam("id") LocalDate id) {
        return ResponseEntity.ok().body(globalStatService.getById(id));

    }
    @PostMapping("/add")
    public ResponseEntity<Boolean> add(@RequestBody GlobalStatAPA globalStat) {
        return ResponseEntity.ok().body(globalStatService.add(globalStat));
    }
    @PostMapping("/createByDate")
    public ResponseEntity<Boolean> createByDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok().body(globalStatService.createByDate(date));
    }

    @PostMapping("/getGlobalFilteredStats")
    public ResponseEntity<GlobalStatDTO> getGlobalFilteredStats(@RequestBody DateRange date){
        return ResponseEntity.ok().body(globalStatService.getGlobalFilteredStats(date));
    }

}
