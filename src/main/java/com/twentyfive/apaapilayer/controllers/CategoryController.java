package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryAPA>> getAllByTypeInAndEnable(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getEnabledCategories(types));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<CategoryAPA> getOrderById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<CategoryAPA> saveOrder(@RequestBody CategoryAPA c) {
        return ResponseEntity.ok().body(categoryService.save(c));
    }


}
