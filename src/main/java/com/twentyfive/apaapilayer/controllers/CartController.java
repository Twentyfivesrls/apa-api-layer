package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CartDTO;
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
@RequestMapping("/cart")
public class CartController {

    private final CustomerService customerService;

    @Autowired
    public CartController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getById(@PathVariable String idCustomer) {
    }

    @PostMapping("/add-to-cart/{id}")
    public ResponseEntity<CartDTO> addToCart(@PathVariable String idCustomer,@RequestBody CartDTO cart) {
    }

    @PostMapping("/remove-from-cart/{id}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable String idCustomer,@RequestBody List<Integer> positions) {
    }

    @DeleteMapping("/clear/{id}")
    public ResponseEntity<Boolean> clearById(@PathVariable String id) {
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
