package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.ProductFixedAPADTO;
import com.twentyfive.apaapilayer.dtos.ProductFixedAPADetailsDTO;
import com.twentyfive.apaapilayer.services.ProductFixedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productsFixed")
@RequiredArgsConstructor
public class ProductFixedController {

    private final ProductFixedService productFixedService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<ProductFixedAPADTO>> findByIdCategory(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection
    ){
        return ResponseEntity.ok().body(productFixedService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductFixedAPADetailsDTO> findById(@PathVariable("id") String id){
        return ResponseEntity.ok().body(productFixedService.getById(id));
    }
}
