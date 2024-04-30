package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<CustomerDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<CustomerDTO> customerDTOs = customerService.getAll(page, size).map(customerAPA -> new CustomerDTO(customerAPA));
        return ResponseEntity.ok(customerDTOs);
    }

    // Get a single customer by ID with added infos
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailsDTO> getById(@PathVariable String id) {
        CustomerDetailsDTO customerDetailsDTO = customerService.getById(id);
        if (customerDetailsDTO != null) {
            return ResponseEntity.ok(customerDetailsDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerAPA customerAPA) {
        try {
            CustomerAPA savedCustomer = customerService.saveCustomer(customerAPA);
            return ResponseEntity.ok(new CustomerDTO(savedCustomer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/changeStatus/{id}")
    public ResponseEntity<Boolean> changeStatusById(@PathVariable String id) {
        boolean changed = customerService.changeStatusById(id);
        if (changed) {
            return ResponseEntity.ok(true);  // Restituisce true se la cancellazione è avvenuta con successo
        } else {
            return ResponseEntity.notFound().build();  // Restituisce 404 se il cliente non è stato trovato
        }
    }



}
