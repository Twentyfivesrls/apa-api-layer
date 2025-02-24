package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.OrderRestoringNotAllowedException;
import com.twentyfive.apaapilayer.filters.OrderFilter;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.FilterUtilities;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.TemplateUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.RedoOrderReq;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final EmailService emailService;

    private final ProductKgRepository productKgRepository;
    private final ProductFixedRepository productFixedRepository;
    private final TrayRepository trayRepository;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CompletedOrderService(CompletedOrderRepository completedOrderRepository, CustomerRepository customerRepository, ActiveOrderRepository activeOrderRepository, ProductWeightedRepository productWeightedRepository, EmailService emailService, ProductKgRepository productKgRepository, ProductFixedRepository productFixedRepository, TrayRepository trayRepository, MongoTemplate mongoTemplate) {
        this.completedOrderRepository= completedOrderRepository;
        this.customerRepository=customerRepository;
        this.activeOrderRepository=activeOrderRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.emailService = emailService;
        this.productKgRepository=productKgRepository;
        this.productFixedRepository = productFixedRepository;
        this.trayRepository=trayRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<OrderAPADTO> getAll(int page, int size, String sortColumn, String sortDirection, OrderFilter filters) {
        Query query = new Query();
        query = FilterUtilities.applyOrderFilters(query, filters, new ArrayList<>(), customerRepository);
        // Controllo del sorting di default su createdDate in ordine decrescente
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.DESC, "pickupDateTime");
        } else {
            String mappedColumn = switch (sortColumn) {
                case "formattedPickupDate" -> "pickupDateTime"; // Gestione sorting per pickupDateTime
                case "price" -> "realPrice"; // Gestione sorting per realPrice
                default -> sortColumn; // Utilizzo diretto per altre colonne
            };
            sort = Sort.by(Sort.Direction.fromString(sortDirection), mappedColumn);
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        List<CompletedOrderAPA> orderList = mongoTemplate.find(query, CompletedOrderAPA.class);
        List<OrderAPADTO> realOrder = orderList.stream()
                .map(this::convertToOrderAPADTO)
                .collect(Collectors.toList());

        return PageUtilities.convertListToPageWithSorting(realOrder, pageable);
    }

    private OrderAPADTO convertToOrderAPADTO(CompletedOrderAPA order) {
        OrderAPADTO dto = new OrderAPADTO();
        dto.setId(order.getId());
        dto.setPickupDateTime(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setPrice(String.format("%.2f", order.getTotalPrice()) + " €");
        dto.setRealPrice(order.getTotalPrice());
        if(order.getPaymentId()!=null){
            dto.setMethodPayment("Online");
        } else {
            dto.setMethodPayment("Al ritiro");
        }
        dto.setStatus(order.getStatus().getStatus());

        if(order.getCustomerId()!=null){
            Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
            if(optCustomer.isPresent()){
                CustomerAPA customer = optCustomer.get();
                dto.setFirstName(customer.getFirstName());
                dto.setLastName(customer.getLastName());
            }
        } else {
            dto.setFirstName(order.getCustomInfo().getFirstName());
            dto.setLastName(order.getCustomInfo().getLastName());
        }
        return dto;
    }

    public OrderDetailsAPADTO getDetailsById(String id) {
        Optional<CompletedOrderAPA> orderOptional = completedOrderRepository.findById(id);
        // Se non viene trovato nessun ordine, ritorna null
        return orderOptional.map(this::convertToOrderDetailsAPADTO).orElse(null);
    }


    private OrderDetailsAPADTO convertToOrderDetailsAPADTO(CompletedOrderAPA order) {
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
        // Assumi che esista un getter che restituisca i bundle

        dto.setOrderNote(order.getNote());
        dto.setPickupDateTime(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setTotalPrice(order.getTotalPrice());
        dto.setPaymentId(order.getPaymentId());
        dto.setPickupDateTime(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setStatus(order.getStatus().getStatus());

        if(order.getCustomerId()!=null){
            Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
            if(optCustomer.isPresent()){
                CustomerAPA customer = optCustomer.get();
                dto.setCustomerNote(customer.getNote());
                dto.setEmail(customer.getEmail()); // Assumi una relazione uno-a-uno con Customer
                dto.setPhoneNumber(customer.getPhoneNumber()); // Assumi che il telefono sia disponibile
            }
            } else {
                dto.setCustomerNote(order.getCustomInfo().getNote());
                dto.setEmail(order.getCustomInfo().getEmail()); // Assumi una relazione uno-a-uno con Customer
                dto.setPhoneNumber(order.getCustomInfo().getPhoneNumber()); // Assumi che il telefono sia disponibile
            }
        return dto;
    }

    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        String name ="";
        double price = 0;
        if(productInPurchase.isFixed()){
            Optional<ProductFixedAPA> optPFixed = productFixedRepository.findById(productInPurchase.getId());
            if(optPFixed.isPresent()){
                ProductFixedAPA productFixed = optPFixed.get();
                name = productFixed.getName();
                price = productFixed.getPrice();
            }
        } else {
            Optional<ProductKgAPA> optPkg = productKgRepository.findById(productInPurchase.getId());
            if(optPkg.isPresent()){
                ProductKgAPA productKg = optPkg.get();
                name = productKg.getName();
                price = productKg.getPricePerKg();
            }
        }
        return new ProductInPurchaseDTO(productInPurchase, name, price,ProductUpdateField.NONE);
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
        boolean toPrepare = false;
        // Informazioni base dell'ordine
        if(completedOrder.getCustomerId()!=null){
            activeOrder.setCustomerId(completedOrder.getCustomerId()); // Assumendo che ci sia un campo customerId
        } else {
            activeOrder.setCustomInfo(completedOrder.getCustomInfo());
        }
        activeOrder.setTotalPrice(completedOrder.getTotalPrice());

        // Date e orari di ritiro
        activeOrder.setPickupDate(completedOrder.getPickupDate());
        activeOrder.setPickupTime(completedOrder.getPickupTime());

        // Dettagli aggiuntivi che possono essere copiati
        activeOrder.setNote(completedOrder.getNote()); // Copia le note se presenti
        activeOrder.setPickupDate(completedOrder.getPickupDate()); // Data di creazione dell'ordine originale
        activeOrder.setPickupTime(completedOrder.getPickupTime()); // Data di ultima modifica come data corrente
        activeOrder.setCreatedDate(LocalDateTime.now());
        // Copia dettagli specifici del prodotto, assicurati di approfondire la logica di clonazione o referenza
        if (completedOrder.getProductsInPurchase() != null) {
            activeOrder.setProductsInPurchase(completedOrder.getProductsInPurchase());
            toPrepare=findCustomizedCake(completedOrder.getProductsInPurchase());
        }
        if (completedOrder.getBundlesInPurchase() != null) {
            activeOrder.setBundlesInPurchase(completedOrder.getBundlesInPurchase());
        }

        if (toPrepare){
            //StompUtilities.sendBakerNotification(false);
            activeOrder.setStatus(OrderStatus.IN_PASTICCERIA);
        } else {
            activeOrder.setStatus(OrderStatus.RICEVUTO);
        }
    }

    private boolean findCustomizedCake(List<ProductInPurchase> productsInPurchase) {
        for (ProductInPurchase productInPurchase : productsInPurchase) {
            Optional<ProductKgAPA> optProduct = productKgRepository.findById(productInPurchase.getId());
            if(optProduct.isPresent()){
                ProductKgAPA product = optProduct.get();
                if (product.isCustomized()){
                    return true;
                }
            }
        }
        return false;
    }

    public Page<OrderAPADTO> getByCustomerId(String customerId, Pageable pageable) {
        // Supponendo che il repository abbia il metodo findOrdersByCustomerId
        return completedOrderRepository.findOrdersByCustomerIdOrderByCreatedDateDesc(customerId, pageable)
                .map(this::convertToOrderAPADTO); // Converti ogni ordine in OrderAPADTO
    }

    private OrderAPA convertCompleteToActiveOrderWithoutId(CompletedOrderAPA completedOrder){
        OrderAPA activeOrder = new OrderAPA();
        activeOrder.setCreatedDate(completedOrder.getCreatedDate());
        activeOrder.setNote(completedOrder.getNote());
        if(completedOrder.getCustomerId()!=null){
            activeOrder.setCustomerId(completedOrder.getCustomerId());
        } else {
            activeOrder.setCustomInfo(completedOrder.getCustomInfo());
        }
        activeOrder.setTotalPrice(completedOrder.getTotalPrice());
        activeOrder.setBundlesInPurchase(completedOrder.getBundlesInPurchase());
        activeOrder.setProductsInPurchase(completedOrder.getProductsInPurchase());
        activeOrder.setPickupDate(completedOrder.getPickupDate());
        activeOrder.setPickupTime(completedOrder.getPickupTime());
        boolean toPrepare = findCustomizedCake(completedOrder.getProductsInPurchase());
        if (toPrepare) {
            //StompUtilities.sendBakerNotification(false);
            activeOrder.setStatus(OrderStatus.IN_PREPARAZIONE);
        } else {
            activeOrder.setStatus(OrderStatus.RICEVUTO);
        }
        return activeOrder;
    }
    private PieceInPurchaseDTO convertPiecePurchaseToDTO(PieceInPurchase piece) {
        Optional<ProductWeightedAPA> pWght = productWeightedRepository.findById(piece.getId());
        String name = pWght.map(ProductWeightedAPA::getName).orElse("No registered product");
        double weight = pWght.map(ProductWeightedAPA::getWeight).orElseThrow(() -> new IllegalArgumentException());
        return new PieceInPurchaseDTO(piece, name, weight);
    }

    public OrderAPA redoOrder(RedoOrderReq redoOrder) throws IOException {
        String email="";
        String firstName="";
        Optional<CompletedOrderAPA> optCompletedOrder=completedOrderRepository.findById(redoOrder.getId());
        if (optCompletedOrder.isPresent()){
            CompletedOrderAPA completedOrder = optCompletedOrder.get();
            completedOrder.setStatus(OrderStatus.RICEVUTO);
            completedOrder.setPickupDate(redoOrder.getPickupDate());
            completedOrder.setPickupTime(redoOrder.getPickupTime());
            completedOrder.setNote(redoOrder.getNote());
            completedOrder.setCreatedDate(LocalDateTime.now());
            OrderAPA orderAPA= convertCompleteToActiveOrderWithoutId(completedOrder);
            activeOrderRepository.save(orderAPA);
            if(orderAPA.getCustomerId()!=null){
                Optional<CustomerAPA> optCustomer = customerRepository.findById(orderAPA.getCustomerId());
                if(optCustomer.isPresent()){
                    CustomerAPA customer = optCustomer.get();
                    email = customer.getEmail();
                    firstName = customer.getFirstName();
                }
            } else {
                email = orderAPA.getCustomInfo().getEmail();
                firstName = orderAPA.getCustomInfo().getFirstName();
            }
            emailService.sendEmail(email, orderAPA.getStatus(), TemplateUtilities.populateEmail(firstName, orderAPA.getId()));
            return orderAPA;
        }
        return null;
    }
}




