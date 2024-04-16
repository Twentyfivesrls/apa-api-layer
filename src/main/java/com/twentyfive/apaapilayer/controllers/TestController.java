package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {


    private final CategoryRepository categoryRepository;



    @GetMapping("/catTest")
    public void categoryTesting (){
        CategoryAPA ctapa =new CategoryAPA();
        ctapa.setName("cazzi");
        categoryRepository.save(ctapa);


    }
}
