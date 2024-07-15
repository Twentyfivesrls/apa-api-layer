package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.MenuItemDTO;
import com.twentyfive.apaapilayer.models.MenuItemAPA;
import com.twentyfive.apaapilayer.services.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        return ResponseEntity.ok().body(menuItemService.getAllByIdCategoryAndActiveTrue(id));
    }
    @GetMapping("/getAllByIdCategoryPaginated")
    public ResponseEntity<Page<MenuItemDTO>> getAllByIdCategoryPaginated(@RequestParam("idCategory")String idCategory,
                                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                                         @RequestParam(value = "size", defaultValue = "25") int size,
                                                                         @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                                         @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection){
        return ResponseEntity.ok().body(menuItemService.getAllByIdCategoryPaginated(idCategory,page,size,sortColumn,sortDirection));
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
    @GetMapping("/activateOrDisable/{id}")
    public ResponseEntity<Boolean> activeOrDisableById(@PathVariable String id) {
        return ResponseEntity.ok().body(menuItemService.activateOrDisableById(id));
    }
}
