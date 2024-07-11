package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.MenuSectionAPA;
import com.twentyfive.apaapilayer.services.MenuSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/menuSections")
public class MenuSectionController {
    private final MenuSectionService menuSectionService;
    @GetMapping("/getAll")
    public ResponseEntity<List<MenuSectionAPA>> getAll(){
        return ResponseEntity.ok().body(menuSectionService.getAll());
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<MenuSectionAPA> getById(@PathVariable("id")String id){
        return ResponseEntity.ok().body(menuSectionService.getById(id));
    }
    @PostMapping("/save")
    public ResponseEntity<MenuSectionAPA> save(@RequestBody MenuSectionAPA menuSectionAPA){
        return ResponseEntity.ok().body(menuSectionService.save(menuSectionAPA));
    }
    @PatchMapping("/updateById/{id}")
    public ResponseEntity<MenuSectionAPA> updateById(@PathVariable("id")String id,@RequestBody MenuSectionAPA menuSectionAPA){
        return ResponseEntity.ok().body(menuSectionService.updateById(id,menuSectionAPA));
    }
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable("id")String id){
        return ResponseEntity.ok().body(menuSectionService.deleteById(id));
    }
}
