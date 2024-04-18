package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/allergens")
public class AllergenController {
    private final AllergenRepository allergenRepository;

    @GetMapping("/getall")
    public List<Allergen> getall(){
        return allergenRepository.findAll();
    }
}
