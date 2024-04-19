package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.BundleInPurchaseDTO;
import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.DTOs.OrderDetailsAPADTO;
import com.twentyfive.apaapilayer.DTOs.ProductInPurchaseDTO;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Product;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.ProductKg;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActiveOrderService {

    private final ActiveOrderRepository activeOrderRepository;
    private final CustomerRepository customerRepository; // Aggiunto il CustomerRepository
    private final CompletedOrderRepository completedOrderRepository;

    private final ProductKgRepository productKgRepository;

    private final TrayRepository trayRepository;

    @Autowired
    public ActiveOrderService(ActiveOrderRepository activeOrderRepository, CustomerRepository customerRepository, CompletedOrderRepository completedOrderRepository,ProductKgRepository productKgRepository,TrayRepository trayRepository) {
        this.activeOrderRepository = activeOrderRepository;
        this.customerRepository = customerRepository; // Iniezione di CustomerRepository
        this.completedOrderRepository= completedOrderRepository;
        this.productKgRepository=productKgRepository;
        this.trayRepository=trayRepository;
    }

    public OrderAPA createOrder(OrderAPA order) {
        return activeOrderRepository.save(order);
    }

    public Page<OrderAPADTO> getAll(Pageable pageable) {
        // Fetching paginated orders from the database
        return activeOrderRepository.findAll(pageable)
                .map(this::convertToOrderAPADTO); // Convert Entity to DTO
    }

    private OrderAPADTO convertToOrderAPADTO(OrderAPA order) {
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
        Optional<OrderAPA> orderOptional = activeOrderRepository.findById(id);
        if (orderOptional.isPresent()) {
            return convertToOrderDetailsAPADTO(orderOptional.get());
        } else {
            return null; // Se non viene trovato nessun ordine, ritorna null
        }
    }

    private OrderDetailsAPADTO convertToOrderDetailsAPADTO(OrderAPA order) {
        CustomerAPA customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomerId()));



        OrderDetailsAPADTO dto = new OrderDetailsAPADTO();
        dto.setId(order.getId());

        List<ProductInPurchaseDTO> productDTOs = order.getProductsInPurchase().stream()
                .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());


        dto.setProducts(productDTOs);



        List<BundleInPurchaseDTO> bundleDTOs = order.getBundlesInPurchase().stream()
                .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());
        dto.setBundles(bundleDTOs); // Assumi che esista un getter che restituisca i bundle

        dto.setEmail(customer.getEmail()); // Assumi una relazione uno-a-uno con Customer
        dto.setPhoneNumber(customer.getPhoneNumber()); // Assumi che il telefono sia disponibile
        return dto;
    }

    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        Optional<ProductKgAPA> pKg = productKgRepository.findById(productInPurchase.getProductId());
        String name = pKg.map(ProductKgAPA::getName).orElse("no registered product");
        return new ProductInPurchaseDTO(productInPurchase, name);
    }

    private BundleInPurchaseDTO convertBundlePurchaseToDTO(BundleInPurchase bundleInPurchase) {
        Optional<Tray> bun = trayRepository.findById(bundleInPurchase.getBundleId());
        String name = bun.map(Tray::getName).orElse("no registered product");

        List<ProductInPurchase> pieces= bundleInPurchase.getWeightedProducts();
        List<ProductInPurchaseDTO> piecesDTOs= pieces.stream()
                .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());

        return new BundleInPurchaseDTO(bundleInPurchase, name,piecesDTOs);
    }


    public boolean deleteById(String id) {
        if (activeOrderRepository.existsById(id)) {
            activeOrderRepository.deleteById(id);
            return true; // Restituisce true se l'ordine è stato trovato e cancellato
        } else {
            return false; // Restituisce false se l'ordine non è stato trovato
        }
    }

    public Page<OrderAPADTO> getByCustomerId(String customerId, Pageable pageable) {
        // Supponendo che il repository abbia il metodo findOrdersByCustomerId
        return activeOrderRepository.findOrdersByCustomerId(customerId, pageable)
                .map(this::convertToOrderAPADTO); // Converti ogni ordine in OrderAPADTO
    }

    @Transactional
    public OrderAPADTO complete(String id) {
        OrderAPA order = activeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(OrderStatus.COMPLETO); // Assumendo che OrderStatus sia un enum

        CompletedOrderAPA completedOrder = new CompletedOrderAPA();
        createCompletedOrder(order, completedOrder); // Metodo helper per copiare i dettagli

        activeOrderRepository.delete(order); // Rimuove l'ordine dalla repository degli ordini attivi
        completedOrderRepository.save(completedOrder); // Salva l'ordine nella repository degli ordini completati

        return convertToOrderAPADTO(order); // Ritorna l'ordine completato come DTO
    }

    private void createCompletedOrder(OrderAPA order, CompletedOrderAPA completedOrder) {
        // Copia l'ID se necessario, altrimenti generane uno nuovo se i sistemi devono essere indipendenti
        completedOrder.setId(order.getId());

        // Informazioni base dell'ordine
        completedOrder.setCustomerId(order.getCustomerId()); // Assumendo che ci sia un campo customerId
        completedOrder.setTotalPrice(order.getTotalPrice());
        completedOrder.setStatus(order.getStatus()); // Imposta lo stato a COMPLETO

        // Date e orari di ritiro
        completedOrder.setPickupDate(order.getPickupDate());
        completedOrder.setPickupTime(order.getPickupTime());

        // Dettagli aggiuntivi che possono essere copiati
        completedOrder.setNote(order.getNote()); // Copia le note se presenti
        completedOrder.setPickupDate(order.getPickupDate()); // Data di creazione dell'ordine originale
        completedOrder.setPickupTime(order.getPickupTime()); // Data di ultima modifica come data corrente

        // Copia dettagli specifici del prodotto, assicurati di approfondire la logica di clonazione o referenza
        if (order.getProductsInPurchase() != null) {
            completedOrder.setProductsInPurchase(new ArrayList<>(order.getProductsInPurchase()));
        }
        if (order.getBundlesInPurchase() != null) {
            completedOrder.setBundlesInPurchase(new ArrayList<>(order.getBundlesInPurchase()));
        }

        // Altri campi specifici dell'ordine possono essere aggiunti qui
    }

    @Transactional
    public boolean cancel(String id) {
        OrderAPA order = activeOrderRepository.findById(id)
                .orElse(null); // Trova l'ordine o restituisce null se non esiste

        if (order != null) {
            order.setStatus(OrderStatus.ANNULLATO); // Imposta lo stato a ANNULLATO

            CompletedOrderAPA completedOrder = new CompletedOrderAPA();
            createCompletedOrder(order, completedOrder); // Utilizza un metodo simile a createCompletedOrder per copiare i dettagli

            activeOrderRepository.delete(order); // Rimuove l'ordine dalla repository degli ordini attivi
            completedOrderRepository.save(completedOrder); // Salva l'ordine nella repository degli ordini completati/anullati

            return true;
        }
        return false;
    }

}

