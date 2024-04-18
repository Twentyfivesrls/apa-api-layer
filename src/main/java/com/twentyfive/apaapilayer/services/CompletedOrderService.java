package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.DTOs.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.exceptions.OrderRestoringNotAllowedException;
import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.repositories.ActiveOrderRepository;
import com.twentyfive.apaapilayer.repositories.CompletedOrderRepository;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CompletedOrderService {
    private final CompletedOrderRepository completedOrderRepository;
    private final ActiveOrderRepository activeOrderRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public CompletedOrderService(CompletedOrderRepository completedOrderRepository,CustomerRepository customerRepository,ActiveOrderRepository activeOrderRepository) {
        this.completedOrderRepository= completedOrderRepository;
        this.customerRepository=customerRepository;
        this.activeOrderRepository=activeOrderRepository;
    }

    public Page<OrderAPADTO> getAll(Pageable pageable) {
        // Fetching paginated orders from the database
        return completedOrderRepository.findAll(pageable)
                .map(this::convertToOrderAPADTO); // Convert Entity to DTO
    }

    private OrderAPADTO convertToOrderAPADTO(CompletedOrderAPA order) {
        CustomerAPA customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomerId()));

        OrderAPADTO dto = new OrderAPADTO();
        dto.setId(order.getId());
        dto.setFirstName(customer.getName());
        dto.setLastName(customer.getSurname());
        dto.setPickupDate(order.getPickupDate());
        dto.setPickupTime(order.getPickupTime());
        dto.setPrice(String.format("%.2f", order.getTotalPrice()));
        dto.setStatus(order.getStatus().name());
        return dto;
    }

    public OrderDetailsAPADTO getDetailsById(String id) {
        Optional<CompletedOrderAPA> orderOptional = completedOrderRepository.findById(id);
        // Se non viene trovato nessun ordine, ritorna null
        return orderOptional.map(this::convertToOrderDetailsAPADTO).orElse(null);
    }


    private OrderDetailsAPADTO convertToOrderDetailsAPADTO(CompletedOrderAPA order) {
        CustomerAPA customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomerId()));

        OrderDetailsAPADTO dto = new OrderDetailsAPADTO();
        dto.setId(order.getId());
        dto.setProducts(order.getProductsInPurchase()); // Assumi che esista un getter che restituisca i prodotti
        dto.setBundles(order.getBundlesInPurchase()); // Assumi che esista un getter che restituisca i bundle
        dto.setEmail(customer.getEmail()); // Assumi una relazione uno-a-uno con Customer
        dto.setPhoneNumber(customer.getPhoneNumber()); // Assumi che il telefono sia disponibile
        return dto;
    }

    public OrderAPADTO restoreOrder(String id) {
        CompletedOrderAPA completedOrder = completedOrderRepository.findById(id).orElse(null);

        if (completedOrder.getStatus().equals(OrderStatus.ANNULLATO) && completedOrder.getPickupDate().isAfter(LocalDate.now()) && completedOrder.getPickupTime().isAfter(LocalTime.now())) {
            completedOrderRepository.delete(completedOrder);
            completedOrder.setStatus(OrderStatus.RICEVUTO);

            OrderAPA restoredOrder= new OrderAPA();
            // copia i dati dell'ordine completato nell'ordine restorato
            restoreCompletedOrder(completedOrder,restoredOrder);

            activeOrderRepository.save(restoredOrder);
            // Converti l'ordine in un dto per restituirlo
            OrderAPADTO orderAPADTO = convertToOrderAPADTO(completedOrder);
            return orderAPADTO;
        } else {
            throw new OrderRestoringNotAllowedException("Non puoi restorare un ordine con una data di prelievo gia passata");
        }
    }

    private void restoreCompletedOrder (CompletedOrderAPA completedOrder, OrderAPA activeOrder) {
        // Copia l'ID se necessario, altrimenti generane uno nuovo se i sistemi devono essere indipendenti
        activeOrder.setId(completedOrder.getId());

        // Informazioni base dell'ordine
        activeOrder.setCustomerId(completedOrder.getCustomerId()); // Assumendo che ci sia un campo customerId
        activeOrder.setTotalPrice(completedOrder.getTotalPrice());
        activeOrder.setStatus(completedOrder.getStatus()); // Imposta lo stato a COMPLETO

        // Date e orari di ritiro
        activeOrder.setPickupDate(completedOrder.getPickupDate());
        activeOrder.setPickupTime(completedOrder.getPickupTime());

        // Dettagli aggiuntivi che possono essere copiati
        activeOrder.setNote(completedOrder.getNote()); // Copia le note se presenti
        activeOrder.setPickupDate(completedOrder.getPickupDate()); // Data di creazione dell'ordine originale
        activeOrder.setPickupTime(completedOrder.getPickupTime()); // Data di ultima modifica come data corrente

        // Copia dettagli specifici del prodotto, assicurati di approfondire la logica di clonazione o referenza
        if (completedOrder.getProductsInPurchase() != null) {
            activeOrder.setProductsInPurchase(new ArrayList<>(completedOrder.getProductsInPurchase()));
        }
        if (completedOrder.getBundlesInPurchase() != null) {
            activeOrder.setBundlesInPurchase(new ArrayList<>(completedOrder.getBundlesInPurchase()));
        }

        // Altri campi specifici dell'ordine possono essere aggiunti qui
    }
}




