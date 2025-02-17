package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.services.GlobalStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {
    private final TimeSlotRefreshScheduling timeSlotRefreshScheduling;
    private final ProductKgRepository productKgRepository;
    private final ProductFixedRepository productFixedRepository;
    private final TrayRepository trayRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final CategoryRepository categoryRepository;
    private final ProductStatRepository productStatRepository;
    private final GlobalStatService globalStatService;

    @GetMapping("/populateJob")
    public void test(){
        timeSlotRefreshScheduling.createSlotsForNext90Days();
    }

    @GetMapping("/populateDb")
    public void populateDb(){
        List<CategoryAPA> categoryProductKg = categoryRepository.findAllByType("productKg");
        List<CategoryAPA> categoryProductWeighted = categoryRepository.findAllByType("productWeighted");
        List<CategoryAPA> categoryTray = categoryRepository.findAllByType("tray");
        List<CategoryAPA> categoryProductFixed = categoryRepository.findAllByType("productFixed");
        List<ProductKgAPA> productKg = new ArrayList<>();
        List<ProductWeightedAPA> productWeighted = new ArrayList<>();
        List<ProductFixedAPA> productFixed = new ArrayList<>();
        List<Tray> tray = new ArrayList<>();
        for (CategoryAPA category: categoryProductKg){
            productKg.addAll(productKgRepository.findAllByCategoryId(category.getId()));
        }
        for (ProductKgAPA product : productKg){
            ProductStatAPA productStatAPA=new ProductStatAPA("productKg");
            product.setStats(productStatAPA);
            productStatRepository.save(productStatAPA);
            productKgRepository.save(product);
        }

        for (CategoryAPA category: categoryProductWeighted){
            productWeighted.addAll(productWeightedRepository.findAllByCategoryId(category.getId()));
        }
        for (ProductWeightedAPA product : productWeighted){
            ProductStatAPA productStatAPA=new ProductStatAPA("productWeighted");
            product.setStats(productStatAPA);
            productStatRepository.save(productStatAPA);
            productWeightedRepository.save(product);
        }

        for (CategoryAPA category: categoryTray){
            tray.addAll(trayRepository.findAllByCategoryId(category.getId()));
        }
        for (Tray product : tray){
            ProductStatAPA productStatAPA=new ProductStatAPA("tray");
            product.setStats(productStatAPA);
            productStatRepository.save(productStatAPA);
            trayRepository.save(product);
        }

        for (CategoryAPA category: categoryProductFixed){
            productFixed.addAll(productFixedRepository.findAllByCategoryId(category.getId()));
        }
        for (ProductFixedAPA product : productFixed){
            ProductStatAPA productStatAPA=new ProductStatAPA("productFixed");
            product.setStats(productStatAPA);
            productStatRepository.save(productStatAPA);
            productFixedRepository.save(product);
        }
    }

    @GetMapping("/populateStats")
    public void populateStats(){
        LocalDate firstDay = LocalDate.of(LocalDate.now().getYear(), 1, 1);

        while (firstDay.isBefore(LocalDate.now())){
            globalStatService.createByDate(firstDay);
            firstDay = firstDay.plusDays(1);
        }
    }
}
