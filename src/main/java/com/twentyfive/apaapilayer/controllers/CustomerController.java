package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.CustomerDTO;
import com.twentyfive.apaapilayer.dtos.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.CustomerService;
import com.twentyfive.apaapilayer.services.KeycloakService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private KeycloakService keycloakService;

    @Autowired
    public CustomerController(CustomerService customerService,KeycloakService keycloakService) {
        this.customerService = customerService;
        this.keycloakService= keycloakService;
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllCustomers")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(@RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "25") int size,
                                                             @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                             @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        Page<CustomerDTO> customerDTOs = customerService.getAllCustomers(page, size,sortColumn,sortDirection).map(customerAPA -> new CustomerDTO(customerAPA));
        return ResponseEntity.ok(customerDTOs);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/getAllEmployees")
    public ResponseEntity<Page<CustomerDTO>> getAllEmployees(@RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "25") int size,
                                                             @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                             @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        Page<CustomerDTO> customerDTOs = customerService.getAllEmployees(page, size,sortColumn,sortDirection).map(customerAPA -> new CustomerDTO(customerAPA));
        return ResponseEntity.ok(customerDTOs);
    }


    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailsDTO> getById(@PathVariable String id) {
        CustomerDetailsDTO customerDetailsDTO = customerService.getById(id);
        if (customerDetailsDTO != null) {
            return ResponseEntity.ok(customerDetailsDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerAPA customerAPA) {
        try {
            CustomerAPA savedCustomer = customerService.saveCustomer(customerAPA);
            return ResponseEntity.ok(new CustomerDTO(savedCustomer));
        } catch (IllegalStateException | IOException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save/client")
    public ResponseEntity<String> editCustomerClient(@RequestBody Map<String,String> newCustomerInfos) {
        String id = newCustomerInfos.get("id");
        String firstName = newCustomerInfos.get("firstName");
        String lastName = newCustomerInfos.get("lastName");
        String phoneNumber = newCustomerInfos.get("phoneNumber");

        //AGGIORNA LE INFO DELL'UTENTE SIA IN KEYCLOAK CHE SU MONGO
        customerService.modifyCustomerInfo(id,firstName,lastName,phoneNumber);

        return ResponseEntity.ok().body("\"modifiche salvate con successo\"");

    }

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> register(@RequestBody CustomerAPA customerAPA) {
        try {
            CustomerAPA savedCustomer = customerService.register(customerAPA);
            return ResponseEntity.ok(new CustomerDTO(savedCustomer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Potresti voler restituire un messaggio d'errore più specifico
        }
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @GetMapping("/details/byKeycloakId/{keycloakId}")
    public ResponseEntity<CustomerDetailsDTO> getCustomerDetailsByIdKeycloak(@PathVariable String keycloakId) {
        try {
            CustomerDetailsDTO customerDetailsDTO = customerService.getCustomerDetailsByIdKeycloak(keycloakId);
            return ResponseEntity.ok(customerDetailsDTO);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/changeStatus/{id}")
    public ResponseEntity<Boolean> changeStatusById(@PathVariable String id) {
        boolean changed = customerService.changeStatusById(id);
        if (changed) {
            return ResponseEntity.ok(true);  // Restituisce true se la cancellazione è avvenuta con successo
        } else {
            return ResponseEntity.notFound().build();  // Restituisce 404 se il cliente non è stato trovato
        }
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @PostMapping("/reset-password/{userId}")
    public ResponseEntity<String> resetPassword(@PathVariable String userId) {
        try {
            keycloakService.sendPasswordResetEmail(userId);
            return ResponseEntity.ok("Password reset email sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send password reset email: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @DeleteMapping("/delete-from-user/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable String id) {
        try {
            customerService.deleteUser(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete account.");
        }
        return ResponseEntity.ok("user cancellato con successo");
    }


}
