package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.AutoCompleteRes;
import com.twentyfive.apaapilayer.dtos.IngredientAPADTO;
import com.twentyfive.apaapilayer.exceptions.ExistingFieldException;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import com.twentyfive.apaapilayer.services.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;


    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAll")
    public ResponseEntity<Page<IngredientAPADTO>> findByIdCategory(@RequestParam("idCategory")String idCategory,
                                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "25") int size,
                                                                   @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                                   @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(ingredientService.findByIdCategory(idCategory,page,size,sortColumn,sortDirection));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllByTypeCategories")
    public ResponseEntity<List<IngredientAPA>> getAllByTypeCategories(@RequestParam String type){
        return ResponseEntity.ok().body(ingredientService.getAllByTypeCategories(type));
    }
    @GetMapping("/getAllByNameCategories")
    public ResponseEntity<List<IngredientAPA>> getAllByNameCategories(@RequestParam("name") String name,@RequestParam("type") String type){
        return ResponseEntity.ok().body(ingredientService.getAllByNameCategories(name, type));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getByName")
    public ResponseEntity<IngredientAPA> getByName(@RequestParam("name") String name){
        return ResponseEntity.ok().body(ingredientService.getByName(name));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getById/{id}")
    public ResponseEntity<IngredientAPADTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.getById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<IngredientAPA> save(@RequestBody IngredientAPA i) {
        try {
            System.out.println("prova");
            return ResponseEntity.ok().body(ingredientService.save(i));
        } catch (Exception e){
            throw new ExistingFieldException();
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/disableById/{id}")
    public ResponseEntity<Boolean> disableById(@PathVariable String id) {
       return ResponseEntity.ok().body(ingredientService.disableById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id){
        return ResponseEntity.ok(ingredientService.deleteById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/activateById/{id}")
    public ResponseEntity<Boolean> activateById(@PathVariable String id) {
        return ResponseEntity.ok().body(ingredientService.activateById(id));
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/get/autocomplete")
    public ResponseEntity<List<AutoCompleteRes>> getIngredientsAutocomplete(@RequestParam("value") String value) {
        return new ResponseEntity<>(ingredientService.getIngredientsAutocomplete(value), HttpStatus.OK);
    }
}
