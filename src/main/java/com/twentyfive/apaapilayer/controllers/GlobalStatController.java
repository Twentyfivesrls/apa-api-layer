package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.dtos.stats.GlobalStatDTO;
import com.twentyfive.apaapilayer.dtos.stats.IngredientStatDTO;
import com.twentyfive.apaapilayer.dtos.stats.OrderStatDTO;
import com.twentyfive.apaapilayer.dtos.stats.ProductStatCategoryDTO;
import com.twentyfive.apaapilayer.models.DateRange;
import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import com.twentyfive.apaapilayer.services.GlobalStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.GlobalStat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/global-stat")
@RequiredArgsConstructor
public class GlobalStatController {
    private final GlobalStatService globalStatService;

    @GetMapping("/get")
    public ResponseEntity<GlobalStat> getById(@RequestParam("id") LocalDate id) {
        return ResponseEntity.ok().body(globalStatService.getById(id));

    }
    @PostMapping("/add")
    public ResponseEntity<Boolean> add(@RequestBody GlobalStatAPA globalStat) {
        return ResponseEntity.ok().body(globalStatService.add(globalStat));
    }
    @PostMapping("/createByDate")
    public ResponseEntity<Boolean> createByDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok().body(globalStatService.createByDate(date));
    }

    @PostMapping("/getGlobalFilteredStats")
    public ResponseEntity<GlobalStatDTO> getGlobalFilteredStats(@RequestBody DateRange date){
        return ResponseEntity.ok().body(globalStatService.getGlobalFilteredStats(date));
    }

    @PostMapping("/getGlobalCategories")
    public ResponseEntity<List<CategoryMinimalDTO>> getGlobalCategories(@RequestBody DateRange date){
        return ResponseEntity.ok().body(globalStatService.getGlobalCategories(date));
    }

    @PostMapping("/getProductStatCategory")
    public ResponseEntity<Page<ProductStatCategoryDTO>> getProductStatCategory(@RequestBody DateRange date,
                                                                               @RequestParam(value = "page", defaultValue ="0") int page,
                                                                               @RequestParam(value = "size", defaultValue ="5") int size,
                                                                               @RequestParam(value = "sortColumn", defaultValue = "name") String sortColumn,
                                                                               @RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection,
                                                                               @RequestParam(value = "categoryId") String categoryId){
        return ResponseEntity.ok().body(globalStatService.getProductStatCategory(date,page,size,sortColumn,sortDirection,categoryId));
    }

    @PostMapping("/getIngredientStat")
    public ResponseEntity<Page<IngredientStatDTO>> getIngredientStat(@RequestBody DateRange date,
                                                                      @RequestParam(value = "page", defaultValue ="0") int page,
                                                                      @RequestParam(value = "size", defaultValue ="5") int size,
                                                                      @RequestParam(value = "sortColumn", defaultValue = "usedCount") String sortColumn,
                                                                      @RequestParam(value = "sortDirection", defaultValue = "DESC") String sortDirection){
        return ResponseEntity.ok().body(globalStatService.getIngredientStat(date,page,size,sortColumn,sortDirection));
    }

    @PostMapping("/getOrderStat")
    public ResponseEntity<OrderStatDTO> getOrderStat(@RequestBody DateRange date){
        return ResponseEntity.ok().body(globalStatService.getOrderStat(date));
    }

}
