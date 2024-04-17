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
    public CategoryAPA getOrderById(@PathVariable String id) {
        return categoryService.getById(id);
    }

    @PostMapping("/save")
    public void saveOrder(@RequestBody CategoryAPA c) {
        categoryService.save(c);
    }


}
