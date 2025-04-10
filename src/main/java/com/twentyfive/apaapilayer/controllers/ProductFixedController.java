package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.dtos.ProductKgAPADTO;
import com.twentyfive.apaapilayer.filters.ProductFilter;
import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import com.twentyfive.apaapilayer.services.ProductFixedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productsFixed")
@RequiredArgsConstructor
public class ProductFixedController {

    private final ProductFixedService productFixedService;

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/getAll")
    public ResponseEntity<Page<ProductFixedAPADTO>> findByIdCategory(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection,
            @RequestBody(required = false) ProductFilter filters
            ){
        return ResponseEntity.ok().body(productFixedService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection,filters));
    }
    @GetMapping("/getAllActive")
    public ResponseEntity<Page<ProductFixedAPADTO>> getAllActive(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="9") int size){
        return ResponseEntity.ok().body(productFixedService.getAllActive(idCategory,page,size));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductFixedAPADetailsDTO> findById(@PathVariable("id") String id){
        return ResponseEntity.ok().body(productFixedService.getById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<ProductFixedAPA> save(@RequestBody ProductFixedAPA productFixedAPA){
        return ResponseEntity.ok().body(productFixedService.save(productFixedAPA));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable("id") String id){
        return ResponseEntity.ok().body(productFixedService.deleteById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/toggleById/{id}")
    public ResponseEntity<Boolean> toggleById(@PathVariable("id") String id){
        return ResponseEntity.ok().body(productFixedService.toggleById(id));
    }

    @GetMapping("/imageById/{id}")
    public ResponseEntity<String> imageUrlById(@PathVariable String id){
        return ResponseEntity.ok().body(productFixedService.getImageUrl(id));
    }

}
