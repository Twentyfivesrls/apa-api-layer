package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.*;
import com.twentyfive.apaapilayer.exceptions.OrderRestoringNotAllowedException;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompletedOrderService {
    private final CompletedOrderRepository completedOrderRepository;
    private final ActiveOrderRepository activeOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductWeightedRepository productWeightedRepository;

    private final ProductKgRepository productKgRepository;

    private final TrayRepository trayRepository;

    @Autowired
    public CompletedOrderService(CompletedOrderRepository completedOrderRepository, CustomerRepository customerRepository, ActiveOrderRepository activeOrderRepository, ProductWeightedRepository productWeightedRepository, ProductKgRepository productKgRepository, TrayRepository trayRepository) {
        this.completedOrderRepository= completedOrderRepository;
        this.customerRepository=customerRepository;
        this.activeOrderRepository=activeOrderRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.productKgRepository=productKgRepository;
        this.trayRepository=trayRepository;
    }

    public Page<OrderAPADTO> getAll(int page, int size, String sortColumn, String sortDirection) {
        // Fetching paginated orders from the database
        List<CompletedOrderAPA> orderList= completedOrderRepository.findAll();
        List<OrderAPADTO> realOrder= new ArrayList<>();
        for(CompletedOrderAPA order:orderList){
            OrderAPADTO orderAPA= convertToOrderAPADTO(order);
            realOrder.add(orderAPA);
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realOrder,pageable);
        }
        Sort sort = Sort.by(Sort.Direction.DESC,"formattedPickupDate");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(realOrder,pageable);
    }

    private OrderAPADTO convertToOrderAPADTO(CompletedOrderAPA order) {
        CustomerAPA customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomerId()));

        OrderAPADTO dto = new OrderAPADTO();
        dto.setId(order.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setPickupDate(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setPrice(String.format("%.2f", order.getTotalPrice()) + " €");
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
        List<ProductInPurchaseDTO> productDTOs = order.getProductsInPurchase().stream()
                .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());

        dto.setProducts(productDTOs);
        dto.setNote(order.getNote());

        dto.setPickupDateTime(order.getPickupTime().toString());

        List<BundleInPurchaseDTO> bundleDTOs = order.getBundlesInPurchase().stream()
                .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());
        dto.setBundles(bundleDTOs); // Assumi che esista un getter che restituisca i bundle
        // Assumi che esista un getter che restituisca i bundle
        dto.setEmail(customer.getEmail()); // Assumi una relazione uno-a-uno con Customer
        dto.setPhoneNumber(customer.getPhoneNumber()); // Assumi che il telefono sia disponibile
        dto.setPickupDateTime(order.getPickupDate().atTime(order.getPickupTime()).toString());
        dto.setTotalPrice(order.getTotalPrice());
        return dto;
    }

    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        Optional<ProductKgAPA> pKg = productKgRepository.findById(productInPurchase.getId());
        String name = pKg.map(ProductKgAPA::getName).orElse("no registered product");
        return new ProductInPurchaseDTO(productInPurchase, name);
    }

    private BundleInPurchaseDTO convertBundlePurchaseToDTO(BundleInPurchase bundleInPurchase) {
        Optional<Tray> bun = trayRepository.findById(bundleInPurchase.getId());
        String name = bun.map(Tray::getName).orElse("no registered product");
        if (bun.isPresent()){
            if (!bun.get().isCustomized()){
                return new BundleInPurchaseDTO(bundleInPurchase, name);
            }
        }
        List<PieceInPurchase> weightedProducts= bundleInPurchase.getWeightedProducts();
        List<PieceInPurchaseDTO> weightedProductsDTOs = weightedProducts.stream()
                .map(this::convertPiecePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());
        return new BundleInPurchaseDTO(bundleInPurchase, name, weightedProductsDTOs);

    }

    public OrderAPADTO restoreOrder(String id) {
        CompletedOrderAPA completedOrder = completedOrderRepository.findById(id).orElse(null);

        if (completedOrder.getPickupDate().isAfter(LocalDate.now()) || completedOrder.getPickupDate().isEqual(LocalDate.now())) {
            if(!(completedOrder.getPickupDate().isEqual(LocalDate.now()) && completedOrder.getPickupTime().isBefore(LocalTime.now()))) {
                completedOrderRepository.delete(completedOrder);
                completedOrder.setStatus(OrderStatus.RICEVUTO);

                OrderAPA restoredOrder = new OrderAPA();
                // copia i dati dell'ordine completato nell'ordine restorato
                restoreCompletedOrder(completedOrder, restoredOrder);

                activeOrderRepository.save(restoredOrder);
                // Converti l'ordine in un dto per restituirlo
                OrderAPADTO orderAPADTO = convertToOrderAPADTO(completedOrder);
                return orderAPADTO;
            }
        }
        throw new OrderRestoringNotAllowedException("Non è più possibile ripristinare l'ordine, il tempo di consegna è passato");
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

    public Page<OrderAPADTO> getByCustomerId(String customerId, Pageable pageable) {
        // Supponendo che il repository abbia il metodo findOrdersByCustomerId
        return completedOrderRepository.findOrdersByCustomerId(customerId, pageable)
                .map(this::convertToOrderAPADTO); // Converti ogni ordine in OrderAPADTO
    }

    private OrderAPADTO convertToOrderAPADTO(OrderAPA order) {
        CustomerAPA customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomerId()));

        OrderAPADTO dto = new OrderAPADTO();
        dto.setId(order.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setPickupDate(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setPrice(String.format("%.2f", order.getTotalPrice()) + " €");
        dto.setStatus(order.getStatus().name());
        return dto;
    }

    private PieceInPurchaseDTO convertPiecePurchaseToDTO(PieceInPurchase piece) {
        Optional<ProductWeightedAPA> pWght = productWeightedRepository.findById(piece.getId());
        String name = pWght.map(ProductWeightedAPA::getName).orElse("No registered product");
        double weight = pWght.map(ProductWeightedAPA::getWeight).orElseThrow(() -> new IllegalArgumentException());
        return new PieceInPurchaseDTO(piece, name, weight);
    }
}




