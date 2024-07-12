package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryAPA>> getAllByTypeInAndEnable(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getEnabledCategories(types));
    }

    @GetMapping("/getAllDisabled")
    public ResponseEntity<List<CategoryAPA>> getAllByTypeInAndDisable(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getDisabledCategories(types));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<CategoryAPA> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getById(id));
    }
    @GetMapping("/getAllByIdSection/{id}")
    public ResponseEntity<List<CategoryAPA>> getAllByIdSection(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getAllActiveByIdSection(id));
    }
    @PostMapping("/save")
    public ResponseEntity<CategoryAPA> save(@RequestBody CategoryAPA c) {
        return ResponseEntity.ok().body(categoryService.save(c));
    }

    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.disableById(id));
    }
    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.activateById(id));
    }

    @GetMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> delateById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.deleteById(id));
    }

    @PostMapping("/setOrderPriorities")
    public ResponseEntity<Boolean> setOrder(@RequestBody Map<String,Integer> priorities){
        return ResponseEntity.ok().body(categoryService.setOrderPriorities(priorities));

    }

}
