package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.BuyInfosDTO;
import com.twentyfive.apaapilayer.dtos.CartDTO;
import com.twentyfive.apaapilayer.dtos.PaymentReq;
import com.twentyfive.apaapilayer.dtos.SummarySingleItemDTO;
import com.twentyfive.apaapilayer.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.SimpleOrderRequest;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")

@PreAuthorize("hasRole('ROLE_customer') or hasRole('ROLE_admin')")
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

    @GetMapping("/add-from-completed-order/{idCustomer}")
    public ResponseEntity<Boolean> addFromCompletedOrder(@PathVariable String idCustomer,@RequestParam("idOrder")String idOrder){
        return ResponseEntity.ok(customerService.addFromCompletedOrder(idCustomer, idOrder));
    }
    @PatchMapping("/modify-pip-cart/{customerId}")
    public ResponseEntity<CartDTO> modifyProductInCart(@PathVariable String customerId, @RequestParam("index") int index, @RequestBody ProductInPurchase pIP) {
        try {
            return ResponseEntity.ok().body(customerService.modifyProductInCart(customerId,index,pIP));
        }catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Gestisci altre eccezioni in modo generico
        }
    }

    @PatchMapping("/modify-bip-cart/{customerId}")
    public ResponseEntity<CartDTO> modifyProductInCart(@PathVariable String customerId, @RequestParam("index") int index, @RequestBody BundleInPurchase bIP) {
        try {
            return ResponseEntity.ok().body(customerService.modifyBundleInCart(customerId,index,bIP));
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

    @PostMapping("/prepare-buying")
    public ResponseEntity<Map<String,Object>> prepareBuying(@RequestHeader("Payment-App-Id") String paymentId,
                                                            @RequestBody PaymentReq paymentReq) throws IOException {
        return ResponseEntity.ok().body(customerService.prepareBuying(paymentId,paymentReq));
    }
    @GetMapping("/capture/{orderId}")
    public ResponseEntity<Map<String,Object>> capture(@PathVariable String orderId){
        return ResponseEntity.ok().body(customerService.capture(orderId));

    }
    @PostMapping("/buy-from-cart/{id}")
    public ResponseEntity<Boolean> buyMultiple(@PathVariable String id, @RequestBody BuyInfosDTO buyInfos) throws IOException {
        // Converti la lista di stringhe in interi
        boolean result = customerService.buyItems(id, buyInfos);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/summary/{id}")
    public ResponseEntity<List<SummarySingleItemDTO>> summary(@PathVariable String id, @RequestBody BuyInfosDTO buyInfos) {
        return ResponseEntity.ok().body(customerService.summary(id, buyInfos));
    }
    @GetMapping("/countCart/{id}")
    public ResponseEntity<Integer> countCart(@PathVariable String id) {
        return ResponseEntity.ok().body(customerService.countCart(id));
    }

    @GetMapping("/check-inactivity")
    public ResponseEntity<String> checkInactivity() {
        return ResponseEntity.ok().body(customerService.obtainDateIfTenDaysBefore());
    }
}
