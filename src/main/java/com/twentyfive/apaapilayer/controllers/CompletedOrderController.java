package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.DTOs.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.services.CompletedOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/completed_orders")
public class CompletedOrderController {

    private final CompletedOrderService completedOrderService; // Assumi che OrderService sia iniettato correttamente

    public CompletedOrderController(CompletedOrderService completedOrderService) {
        this.completedOrderService = completedOrderService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<OrderAPADTO>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "column", defaultValue = "") String sortColumn,
            @RequestParam(value = "direction", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(completedOrderService.getAll(page,size,sortColumn,sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsAPADTO> getDetailsById(@PathVariable String id) {
        OrderDetailsAPADTO odapa = completedOrderService.getDetailsById(id);
        if (odapa!=null)
            return new ResponseEntity<>(odapa,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



    @PostMapping("/restore/{id}")
    public ResponseEntity<OrderAPADTO> restoreOrder(@PathVariable String id) {
        OrderAPADTO restoredOrder = completedOrderService.restoreOrder(id);
        return ResponseEntity.ok(restoredOrder);
    }

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
}
