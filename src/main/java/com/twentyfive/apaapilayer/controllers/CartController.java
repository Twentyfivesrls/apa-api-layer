package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.CartDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

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

    @GetMapping("/{idCustomer}")
    public ResponseEntity<CartDTO> getById(@PathVariable String idCustomer) {
        CartDTO cart = customerService.getCartById(idCustomer);
        if (cart != null) {
            return ResponseEntity.ok(cart);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/add-to-cart/product/{idCustomer}")
    public ResponseEntity<CartDTO> addToCartProduct(@PathVariable String idCustomer, @RequestBody ProductInPurchase product) {
        try {

            CartDTO updatedCart = customerService.addToCartProduct(idCustomer, product);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }


    @PostMapping("/add-to-cart/bundle/{idCustomer}")
    public ResponseEntity<CartDTO> addToCartBundle(@PathVariable String idCustomer, @RequestBody BundleInPurchase bundle) {
        try {
            CartDTO updatedCart = customerService.addToCartBundle(idCustomer, bundle);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }



    @PostMapping("/remove-from-cart/{idCustomer}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable String idCustomer, @RequestBody Integer position) {
        try {
            CartDTO updatedCart = customerService.removeFromCart(idCustomer, position);
            return ResponseEntity.ok(updatedCart);
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.badRequest().body(null); // Elemento non trovato all'indice specificato
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @DeleteMapping("/clear/{id}")
    public ResponseEntity<Boolean> clearById(@PathVariable String id) {
        boolean result = customerService.clearCart(id);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/buy-single/{id}")
    public ResponseEntity<Boolean> buySingle(@PathVariable String id, @RequestBody Integer position) {
        try {
            int positionIndex = position; // Assumi che 'position' sia un indice passato come stringa
            boolean result = customerService.buySingleItem(id, positionIndex);
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(false); // In caso di formato non valido
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }

    @PostMapping("/buy-multiple/{id}")
    public ResponseEntity<Boolean> buyMultiple(@PathVariable String id, @RequestBody List<Integer> positions) {
        try {
            // Converti la lista di stringhe in interi
            boolean result = customerService.buyMultipleItems(id, positions);
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
