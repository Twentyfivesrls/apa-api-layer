package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.BuyInfosDTO;
import com.twentyfive.apaapilayer.DTOs.CartDTO;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    @PatchMapping("/modify-cart/{customerId}")
    public ResponseEntity<CartDTO> modifyProductInCart(@PathVariable String customerId, @RequestParam("index") int index, @RequestBody ItemInPurchase iIP) {
        try {
            return ResponseEntity.ok().body(customerService.modifyProductInCart(customerId,index,iIP));
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }


    @PostMapping("/remove-from-cart/{idCustomer}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable String idCustomer, @RequestBody List<Integer> positions) {
        try {
            CartDTO updatedCart = customerService.removeFromCart(idCustomer, positions);
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

    @PostMapping("/pickup-dateTimes/{id}")
    public ResponseEntity<Map<LocalDate, List<LocalTime>>> obtainMinimumPickupDateTime(
            @PathVariable String id, @RequestBody List<Integer> positions) {

        try {
            Map<LocalDate, List<LocalTime>> availablePickupTimes =
                    customerService.getAvailablePickupTimes(id, positions);

            if (!availablePickupTimes.isEmpty()) {
                return ResponseEntity.ok(availablePickupTimes);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/buy-from-cart/{id}")
    public ResponseEntity<Boolean> buyMultiple(@PathVariable String id, @RequestBody BuyInfosDTO buyInfos) {
        try {
            // Converti la lista di stringhe in interi
            boolean result = customerService.buyItems(id, buyInfos.getPositions(), buyInfos.getSelectedPickupDateTime(),buyInfos.getNote());
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(false); // In caso di formato non valido
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }



}
