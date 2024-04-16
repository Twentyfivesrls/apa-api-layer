package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;


    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<CustomerAPA>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<CustomerAPA> customers = customerService.getAll(page, size);
        return ResponseEntity.ok(customers);
    }

    // Get a single customer by ID with added infos
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getById(@PathVariable String id) {
        CustomerDTO customerDTO = customerService.getById(id);
        if (customerDTO != null) {
            return ResponseEntity.ok(customerDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Save or update a customer
    @PostMapping("save")
    public ResponseEntity<CustomerAPA> save(@RequestBody CustomerDTO customerDTO) {

        return ResponseEntity.ok().build();
    }

    // Delete a customer by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id) {
        // Dummy response for now
        return ResponseEntity.ok().build();
    }


    // Update a customer
    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerAPA> updateCustomer(@PathVariable String id, @RequestBody CustomerDTO customerDTO) {
        // Dummy response for now
        return ResponseEntity.ok().build();
    }

    // Buy a single item
    @PostMapping("/buy-single/{id}")
    public ResponseEntity<Boolean> buySingle(@PathVariable String id,@RequestBody String position) {
        // Dummy response for now
        return ResponseEntity.ok().build();
    }

    // Buy multiple items
    @PostMapping("/buy-multiple/{id}")
    public ResponseEntity<Boolean> buyMultiple(@PathVariable String id, @RequestBody List<String> positions) {
        // Dummy response for now
        return ResponseEntity.ok().build();
    }

    // Buy all items in the cart
    @PostMapping("/buy-all/{id}")
    public ResponseEntity<Boolean> buyAll(@PathVariable String id) {
        // Dummy response for now
        return ResponseEntity.ok().build();
    }


}
