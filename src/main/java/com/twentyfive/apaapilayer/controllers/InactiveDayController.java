package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.services.InactiveDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.InactiveDay;

import java.util.List;

@RestController
@RequestMapping("/inactive-day")
@RequiredArgsConstructor
public class InactiveDayController {
    private final InactiveDayService inactiveDayService;

    @GetMapping("/get")
    public ResponseEntity<List<InactiveDay>> get() {
        return ResponseEntity.ok().body(inactiveDayService.get());
    }

    @PutMapping("/update")
    public ResponseEntity<List<InactiveDay>> update(@RequestBody List<InactiveDay> newInactiveDays) {
        return ResponseEntity.ok().body(inactiveDayService.update(newInactiveDays));
    }


}
