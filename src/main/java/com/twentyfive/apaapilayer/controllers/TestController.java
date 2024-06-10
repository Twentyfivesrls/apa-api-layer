package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.job.TimeSlotRefreshScheduling;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {
    private final TimeSlotRefreshScheduling timeSlotRefreshScheduling;
    private final ProductKgRepository productKgRepository;
    private final TrayRepository trayRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final CategoryRepository categoryRepository;
    private final ProductStatRepository productStatRepository;
    @GetMapping("/test")
    public void test(){
        System.out.println("funziona!");



        timeSlotRefreshScheduling.createSlotsForNext30Days();
    }

    @GetMapping("/populateDb")
    public void populateDb(){
        List<CategoryAPA> categoryProductKg = categoryRepository.findAllByType("productKg");
        List<CategoryAPA> categoryProductWeighted = categoryRepository.findAllByType("productWeighted");
        List<CategoryAPA> categoryTray = categoryRepository.findAllByType("tray");
        List<ProductKgAPA> productKg = new ArrayList<>();
        List<ProductWeightedAPA> productWeighted = new ArrayList<>();
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
    }
}
