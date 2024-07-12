package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.MenuItemDTO;
import com.twentyfive.apaapilayer.models.MenuItemAPA;
import com.twentyfive.apaapilayer.models.MenuSectionAPA;
import com.twentyfive.apaapilayer.services.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menuItems")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping("/getAll")
    public ResponseEntity<List<MenuItemDTO>> getAll(){
        return ResponseEntity.ok().body(menuItemService.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<MenuItemDTO> getById(@PathVariable("id")String id){
        return ResponseEntity.ok().body(menuItemService.getById(id));
    }

    @GetMapping("/getAllByIdCategory/{id}")
    public ResponseEntity<List<MenuItemDTO>> getByIdCategory(@PathVariable("id")String id) {
        return ResponseEntity.ok().body(menuItemService.getAllByIdCategory(id));
    }

    @PostMapping("/save")
    public ResponseEntity<MenuItemAPA> save(@RequestBody MenuItemAPA menuItemAPA){
        return ResponseEntity.ok().body(menuItemService.save(menuItemAPA));
    }
    @PatchMapping("/updateById/{id}")
    public ResponseEntity<MenuItemAPA> updateById(@PathVariable("id")String id,@RequestBody MenuItemAPA menuItemAPA){
        return ResponseEntity.ok().body(menuItemService.updateById(id,menuItemAPA));
    }
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable("id")String id){
        return ResponseEntity.ok().body(menuItemService.deleteById(id));
    }
}
