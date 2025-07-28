package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.dtos.SaveCustomTimeReq;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

//    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryAPA>> getAllByTypeInAndEnable(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getEnabledCategories(types));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllMinimal")
    public ResponseEntity<List<CategoryMinimalDTO>> getAllMinimal(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getAllMinimal(types));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllDisabled")
    public ResponseEntity<List<CategoryAPA>> getAllByTypeInAndDisable(@RequestParam List<String> types) {
        return ResponseEntity.ok().body(categoryService.getDisabledCategories(types));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getById/{id}")
    public ResponseEntity<CategoryAPA> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getById(id));
    }

    @GetMapping("/getAllByIdSection/{id}")
    public ResponseEntity<List<CategoryAPA>> getAllByIdSectionEnabledTrueAndSoftDeletedFalse(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getAllActiveByIdSection(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllDisabledByIdSection/{id}")
    public ResponseEntity<List<CategoryAPA>> getAllDisabledByIdSectionEnabledTrueAndSoftDeletedFalse(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.getAllDisabledByIdSection(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<CategoryAPA> save(@RequestBody CategoryAPA c) {
        return ResponseEntity.ok().body(categoryService.save(c));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.disableById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.activateById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id) {
        return ResponseEntity.ok().body(categoryService.deleteById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/setOrderPriorities")
    public ResponseEntity<Boolean> setOrder(@RequestBody Map<String,Integer> priorities){
        return ResponseEntity.ok().body(categoryService.setOrderPriorities(priorities));

    }


    //CUSTOMTIMECATEGORY APIS

    //@PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save-custom-time")
    public ResponseEntity<Boolean> saveCustomTime(@RequestBody SaveCustomTimeReq req){
        return ResponseEntity.ok().body(categoryService.saveCustomTime(req));
    }
}
