package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.services.CustomerService;
import feign.Body;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<CustomerDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                                    @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                    @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        Page<CustomerDTO> customerDTOs = customerService.getAll(page, size,sortColumn,sortDirection).map(customerAPA -> new CustomerDTO(customerAPA));
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
        } catch (IllegalStateException | IOException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @PostMapping("/save/client")
    public ResponseEntity<String> editCustomerClient(@RequestBody Map<String,String> newCustomerInfos) {
        String id = newCustomerInfos.get("id");
        String firstName = newCustomerInfos.get("firstName");
        String lastName = newCustomerInfos.get("lastName");
        String phoneNumber = newCustomerInfos.get("phoneNumber");

        //AGGIORNA LE INFO DELL'UTENTE SIA IN KEYCLOAK CHE SU MONGO
        customerService.modifyCustomerInfo(id,firstName,lastName,phoneNumber);

        return ResponseEntity.ok().body("\"modifiche salvate\"");

    }

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> register(@RequestBody CustomerAPA customerAPA) {
        System.out.println("dehjufejhg!!!");
        try {
            CustomerAPA savedCustomer = customerService.register(customerAPA);
            return ResponseEntity.ok(new CustomerDTO(savedCustomer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @GetMapping("/details/byKeycloakId/{keycloakId}")
    public ResponseEntity<CustomerDetailsDTO> getCustomerDetailsByIdKeycloak(@PathVariable String keycloakId) {
        try {
            CustomerDetailsDTO customerDetailsDTO = customerService.getCustomerDetailsByIdKeycloak(keycloakId);
            return ResponseEntity.ok(customerDetailsDTO);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test")
    public void test(){System.out.println("ok");}
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
