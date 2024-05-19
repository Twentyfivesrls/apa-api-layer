package com.twentyfive.apaapilayer.controllers;

import com.itextpdf.text.DocumentException;
import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.DTOs.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.services.ActiveOrderService;
import com.twentyfive.apaapilayer.utils.PdfUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class ActiveOrderController {

    private final ActiveOrderService activeOrderService; // Assumi che OrderService sia iniettato correttamente

    public ActiveOrderController(ActiveOrderService activeOrderService) {
        this.activeOrderService = activeOrderService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<OrderAPADTO>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
            @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(activeOrderService.getAll(page,size,sortColumn,sortDirection));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<OrderDetailsAPADTO> getDetailsById(@PathVariable String id) {

        OrderDetailsAPADTO orderDetails = activeOrderService.getDetailsById(id);
        if (orderDetails != null) {
            return ResponseEntity.ok(orderDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        boolean deleted = activeOrderService.deleteById(id);
        if (deleted) {
            return ResponseEntity.ok("l'ordine con id "+id+" è stato eliminato"); // Restituisce 200 OK se l'ordine è stato cancellato
        } else {
            return ResponseEntity.notFound().build(); // Restituisce 404 Not Found se l'ordine non esiste
        }
    }


    @PostMapping("/save")
    public void saveOrder(@RequestBody OrderAPA order){
        activeOrderService.createOrder(order);
    }


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

    @PostMapping("/cancel/{id}")//per annullare ordine lato cliente
    public ResponseEntity<Boolean> cancelOrder(@PathVariable String id) {
        try {
            boolean result = activeOrderService.cancel(id);
            if (result) {
                return ResponseEntity.ok(true); // Restituisce true se l'ordine è stato annullato correttamente
            } else {
                return ResponseEntity.ok(false); // Restituisce false se non c'è un ordine da annullare
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Restituisce un errore server interno in caso di problemi
        }
    }
    @PostMapping("/adminCancel/{id}")//per annullare ordine lato cliente
    public ResponseEntity<Boolean> AdminCancelOrder(@PathVariable String id) {
        try {
            boolean result = activeOrderService.adminCancel(id);
            if (result) {
                return ResponseEntity.ok(true); // Restituisce true se l'ordine è stato annullato correttamente
            } else {
                return ResponseEntity.ok(false); // Restituisce false se non c'è un ordine da annullare
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Restituisce un errore server interno in caso di problemi
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

}

