package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customer) {
        try {
            CustomerAPA savedCustomer = customerService.saveCustomer(customer.toNewCustomerAPA());
            return ResponseEntity.ok(new CustomerDTO(savedCustomer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @PostMapping("/update")
    public ResponseEntity<CustomerDTO> updateCustomer(@RequestBody CustomerDTO customer) {
        try {
            CustomerAPA updatedCustomer = customerService.updateCustomer(customer.toNewCustomerAPA());
            return ResponseEntity.ok(new CustomerDTO(updatedCustomer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteCustomerById(@PathVariable String id) {
        boolean deleted = customerService.deleteCustomerById(id);
        if (deleted) {
            return ResponseEntity.ok(true);  // Restituisce true se la cancellazione è avvenuta con successo
        } else {
            return ResponseEntity.notFound().build();  // Restituisce 404 se il cliente non è stato trovato
        }
    }


    @PostMapping("/buy-single/{id}")
    public ResponseEntity<Boolean> buySingle(@PathVariable String id, @RequestBody String position) {
        try {
            int positionIndex = Integer.parseInt(position); // Assumi che 'position' sia un indice passato come stringa
            boolean result = customerService.buySingleItem(id, positionIndex);
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(false); // In caso di formato non valido
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }

    @PostMapping("/buy-multiple/{id}")
    public ResponseEntity<Boolean> buyMultiple(@PathVariable String id, @RequestBody List<String> positions) {
        try {
            List<Integer> positionIndexes = positions.stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()); // Converti la lista di stringhe in interi
            boolean result = customerService.buyMultipleItems(id, positionIndexes);
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(false); // In caso di formato non valido
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }

    @PostMapping("/buy-all/{id}")
    public ResponseEntity<Boolean> buyAll(@PathVariable String id) {
        try {
            boolean result = customerService.buyAllItems(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci eccezioni in modo generico
        }
    }

}
