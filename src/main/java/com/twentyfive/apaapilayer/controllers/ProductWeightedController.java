package com.twentyfive.apaapilayer.controllers;


import com.twentyfive.apaapilayer.DTOs.ProductWeightedAPADTO;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.services.ProductWeightedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/productsWeighted")
public class ProductWeightedController {

    private final ProductWeightedService productWeightedService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<ProductWeightedAPADTO>> findByIdCategory(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(productWeightedService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }

    @GetMapping("/getAllForCustomizedTray")
    public ResponseEntity<Page<ProductWeightedAPADTO>> getAllForCustomizedTray(
            @RequestParam("idCategory") String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="10") int size){
        return ResponseEntity.ok().body(productWeightedService.getAllForCustomizedTray(idCategory, page, size));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductWeightedAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(productWeightedService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<ProductWeightedAPA> save(@RequestBody ProductWeightedAPA p) {
        return ResponseEntity.ok().body(productWeightedService.save(p));
    }

    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(productWeightedService.disableById(id));
    }

    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(productWeightedService.activateById(id));
    }

    @GetMapping("/imageById/{id}")
    public ResponseEntity<String> imageUrlById(@PathVariable String id){
        return ResponseEntity.ok().body(productWeightedService.getImageUrl(id));
    }

}
