package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestControllers {


    private final CategoryRepository categoryRepository;



    @GetMapping("/catTest")
    public void categoryTesting (){
        CategoryAPA ctapa =new CategoryAPA();
        OrderAPA oApa = new OrderAPA();
        ctapa.setName("cazzi");
        ctapa.setEnabled(true);
        categoryRepository.save(ctapa);


    }
}
