package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/allergens")
@PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_baker') or hasRole('ROLE_counter') or hasRole('ROLE_customer')")
public class AllergenController {
    private final AllergenRepository allergenRepository;

    @GetMapping("/getAll")
    public List<Allergen> getAll(){
        return allergenRepository.findAll();
    }
}
