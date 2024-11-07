package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.OrderAPADTO;
import com.twentyfive.apaapilayer.dtos.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.services.CompletedOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.RedoOrderReq;

import java.io.IOException;


@RestController
@RequestMapping("/completed_orders")
public class CompletedOrderController {

    private final CompletedOrderService completedOrderService; // Assumi che OrderService sia iniettato correttamente

    public CompletedOrderController(CompletedOrderService completedOrderService) {
        this.completedOrderService = completedOrderService;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_counter')")
    @GetMapping("/getAll")
    public ResponseEntity<Page<OrderAPADTO>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(completedOrderService.getAll(page,size,sortColumn,sortDirection));
    }


    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer') or hasRole('ROLE_counter')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsAPADTO> getDetailsById(@PathVariable String id) {
        OrderDetailsAPADTO odapa = completedOrderService.getDetailsById(id);
        if (odapa!=null)
            return new ResponseEntity<>(odapa,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/restore/{id}")
    public ResponseEntity<OrderAPADTO> restoreOrder(@PathVariable String id) {
        OrderAPADTO restoredOrder = completedOrderService.restoreOrder(id);
        return ResponseEntity.ok(restoredOrder);
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<Page<OrderAPADTO>> getByCustomerId(
            @PathVariable String customerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<OrderAPADTO> orders = completedOrderService.getByCustomerId(customerId, PageRequest.of(page, size));
        if (!orders.isEmpty()) {
            return ResponseEntity.ok(orders);
        } else {
            return ResponseEntity.noContent().build(); // Restituisce 204 No Content se non ci sono ordini
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/redo-order")
    public ResponseEntity<OrderAPA> redoOrder(@RequestBody RedoOrderReq redoOrder) throws IOException {
        return ResponseEntity.ok().body(completedOrderService.redoOrder(redoOrder));
    }
}
