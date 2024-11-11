package com.twentyfive.apaapilayer.controllers;


import com.twentyfive.apaapilayer.dtos.AutoCompleteProductWeighted;
import com.twentyfive.apaapilayer.dtos.ProductWeightedAPADTO;
import com.twentyfive.apaapilayer.exceptions.ExistingFieldException;
import com.twentyfive.apaapilayer.filters.ProductFilter;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.services.ProductWeightedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/productsWeighted")
public class ProductWeightedController {

    private final ProductWeightedService productWeightedService;

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAll")
    public ResponseEntity<Page<ProductWeightedAPADTO>> findByIdCategory(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection,
            @RequestBody(required = false)ProductFilter filters) {
        return ResponseEntity.ok().body(productWeightedService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection,filters));
    }

    @GetMapping("/getAllForCustomizedTray")
    public ResponseEntity<List<AutoCompleteProductWeighted>> getAllForCustomizedTray(
            @RequestParam(value = "value", defaultValue = "") String value){
        return ResponseEntity.ok().body(productWeightedService.getAllForCustomizedTray(value));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductWeightedAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(productWeightedService.getById(id));
    }
    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<ProductWeightedAPA> save(@RequestBody ProductWeightedAPA p) {
        try {
            return ResponseEntity.ok().body(productWeightedService.save(p));
        } catch (Exception e){
            throw new ExistingFieldException();
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(productWeightedService.disableById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id,
                                                @RequestParam(value = "booleanModal", defaultValue = "false") boolean booleanModal) {
        return ResponseEntity.ok().body(productWeightedService.activateById(id, booleanModal));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id){
        return ResponseEntity.ok().body(productWeightedService.deleteById(id));
    }
    @GetMapping("/imageById/{id}")
    public ResponseEntity<String> imageUrlById(@PathVariable String id){
        return ResponseEntity.ok().body(productWeightedService.getImageUrl(id));
    }

}
