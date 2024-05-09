package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.DropdownRes;
import com.twentyfive.apaapilayer.DTOs.IngredientsAPADTO;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.services.IngredientService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
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
                                                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                                                    @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                                    @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(ingredientService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }

    @GetMapping("/getAllByTypeCategories")
    public ResponseEntity<List<DropdownRes>> getAllByTypeCategories(@RequestParam List<String> types){
        return ResponseEntity.ok().body(ingredientService.getAllByTypeCategories(types));
    }
    @GetMapping("/getByName")
    public ResponseEntity<IngredientAPA> getByName(@RequestParam("name") String name){
        return ResponseEntity.ok().body(ingredientService.getByName(name));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<IngredientsAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.getById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<IngredientAPA> save(@RequestBody IngredientAPA i) {
        return ResponseEntity.ok().body(ingredientService.save(i));
    }

    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
       return ResponseEntity.ok().body(ingredientService.disableById(id));
    }

    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.activateById(id));
    }


}
