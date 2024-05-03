package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.IngredientsAPADTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.services.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<IngredientsAPADTO>> findByIdCategory(@RequestParam("idCategory")String idCategory,
                                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok().body(ingredientService.findByIdCategory(idCategory,page,size));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<IngredientsAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<IngredientAPA> save(@RequestBody IngredientAPA i) {
        return ResponseEntity.ok().body(ingredientService.save(i));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
       return ResponseEntity.ok().body(ingredientService.disableById(id));
    }

    @PutMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.activateById(id));
    }


}
