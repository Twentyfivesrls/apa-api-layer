package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.ProductKgAPADTO;
import com.twentyfive.apaapilayer.exceptions.ExistingFieldException;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.services.ProductKgService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/productsKg")
public class ProductKgController {

    private final ProductKgService productkgService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<ProductKgAPADTO>> findByIdCategory(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="5") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(productkgService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }
    @GetMapping("/getAllActive")
    public ResponseEntity<Page<ProductKgAPADTO>> getAllActive(
            @RequestParam("idCategory")String idCategory,
            @RequestParam(value = "page", defaultValue ="0") int page,
            @RequestParam(value = "size", defaultValue ="9") int size){
        return ResponseEntity.ok().body(productkgService.getAllActive(idCategory,page,size));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductKgAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(productkgService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<ProductKgAPA> save(@RequestBody ProductKgAPA p) {
        try {
            return ResponseEntity.ok().body(productkgService.save(p));
        } catch (Exception e){
            throw new ExistingFieldException();
        }
    }

    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
        return ResponseEntity.ok().body(productkgService.disableById(id));
    }

    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id,
                                                @RequestParam(value = "booleanModal", defaultValue = "false") boolean booleanModal) {
        return ResponseEntity.ok().body(productkgService.activateById(id, booleanModal));
    }

    @GetMapping("/imageById/{id}")
    public ResponseEntity<String> imageUrlById(@PathVariable String id){
        return ResponseEntity.ok().body(productkgService.getImageUrl(id));
    }

}
