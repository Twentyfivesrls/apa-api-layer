package com.twentyfive.apaapilayer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.twentyfive.apaapilayer.dtos.LocationReq;
import com.twentyfive.apaapilayer.dtos.OrderAPADTO;
import com.twentyfive.apaapilayer.dtos.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.dtos.UpdateOrderReq;
import com.twentyfive.apaapilayer.exceptions.CancelThresholdPassedException;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.filters.OrderFilter;
import com.twentyfive.apaapilayer.services.ActiveOrderService;
import com.twentyfive.authorizationflow.services.AuthenticationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/orders")
@PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_baker') or hasRole('ROLE_counter')")
public class ActiveOrderController {

    private final ActiveOrderService activeOrderService; // Assumi che OrderService sia iniettato correttamente

    public ActiveOrderController(ActiveOrderService activeOrderService) {
        this.activeOrderService = activeOrderService;
    }

    @PostMapping("/getAll")
    public ResponseEntity<Page<OrderAPADTO>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection,
            @RequestBody(required = false) OrderFilter filters)  throws IOException{
        return ResponseEntity.ok().body(activeOrderService.getAll(page,size,sortColumn,sortDirection,filters));
    }
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_baker') or hasRole('ROLE_counter') or hasRole('ROLE_customer')")
    @GetMapping("/details/{id}")
    public ResponseEntity<OrderDetailsAPADTO> getDetailsById(@PathVariable String id) throws IOException {
        OrderDetailsAPADTO orderDetails = activeOrderService.getDetailsById(id);
        if (orderDetails != null) {
            return ResponseEntity.ok(orderDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/save")
    public ResponseEntity<OrderAPA> save(@RequestBody OrderAPA orderAPA){
        return ResponseEntity.ok().body(activeOrderService.createOrder(orderAPA));
    }
    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        boolean deleted = activeOrderService.deleteById(id);
        if (deleted) {
            return ResponseEntity.ok("l'ordine con id "+id+" è stato eliminato"); // Restituisce 200 OK se l'ordine è stato cancellato
        } else {
            return ResponseEntity.notFound().build(); // Restituisce 404 Not Found se l'ordine non esiste
        }
    }

    @PutMapping(value ="/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Boolean> update(@PathVariable String id,
                                          @RequestParam(value ="updateOrderReq", required = false) String updateOrderReq,
                                          @RequestPart(value ="file", required = false) MultipartFile file) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        UpdateOrderReq updateOrderReqObj = objectMapper.readValue(updateOrderReq, UpdateOrderReq.class);

        return ResponseEntity.ok().body(activeOrderService.updateOrder(id,updateOrderReqObj,file));

    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_customer')")
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<Page<OrderAPADTO>> getByCustomerId(
            @PathVariable String customerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<OrderAPADTO> orders = activeOrderService.getByCustomerId(customerId, PageRequest.of(page, size));
        if (!orders.isEmpty()) {
            return ResponseEntity.ok(orders);
        } else {
            return ResponseEntity.noContent().build(); // Restituisce 204 No Content se non ci sono ordini
        }
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_counter')")
    @PostMapping("/complete/{id}")//per segnare ordine come completato
    public ResponseEntity<OrderAPADTO> completeOrder(@PathVariable String id) {
        try {
            OrderAPADTO updatedOrder = activeOrderService.complete(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.notFound().build(); // Non trovato se l'ordine non esiste
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Error interno se qualcosa va storto
        }
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_counter') or hasRole('ROLE_customer')")
    @PostMapping("/cancel/{id}") // per annullare ordine lato cliente
    public ResponseEntity<String> cancelOrder(@PathVariable String id) {
        try {
            boolean result = activeOrderService.cancel(id);
            if (result) {
                return ResponseEntity.status(HttpStatus.OK).body("\"Order cancelled successfully.\""); // Messaggio di successo
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("\"No order to cancel.\""); // Messaggio quando non c'è nessun ordine da annullare
            }
        } catch (CancelThresholdPassedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("\"Order cancellation failed. Order ID may be incorrect.\""); // Messaggio di errore dettagliato
        }
    }


    @GetMapping("/print/{id}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String id) throws IOException, DocumentException {
        ByteArrayOutputStream pdfStream = activeOrderService.print(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id + ".pdf");
        headers.setContentLength(pdfStream.size());
        return new ResponseEntity<>(pdfStream.toByteArray(), headers, HttpStatus.OK);
    }
    @GetMapping("/changeOrderStatus/{id}")
    public ResponseEntity<Boolean> changeOrderStatus(@PathVariable String id, @RequestParam("status") String status){
        try {
            Boolean changedStatus = activeOrderService.changeOrderStatus(id,status);
            if (changedStatus) {
                return ResponseEntity.ok(changedStatus);
            } else {
                return ResponseEntity.notFound().build(); // Non trovato se l'ordine non esiste
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Error interno se qualcosa va storto
        }
    }
    @GetMapping("/getAllStatuses")
    public ResponseEntity<OrderStatus[]> getAllStatuses() {
        return ResponseEntity.ok().body(activeOrderService.getAllStatuses());
    }

    @PutMapping("/setLocation")
    public ResponseEntity<Boolean> setLocation(@RequestBody LocationReq locationReq) throws IOException {
        return ResponseEntity.ok().body(activeOrderService.setLocation(locationReq));

    }
}

