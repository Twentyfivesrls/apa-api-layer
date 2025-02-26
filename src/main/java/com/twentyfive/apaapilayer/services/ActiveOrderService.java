package com.twentyfive.apaapilayer.services;

import com.itextpdf.text.DocumentException;
import com.twentyfive.apaapilayer.clients.MediaManagerClientController;
import com.twentyfive.apaapilayer.clients.PaymentClientController;
import com.twentyfive.apaapilayer.clients.StompClientController;
import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.configurations.ProducerPool;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.CancelThresholdPassedException;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.exceptions.OrderNotFoundException;
import com.twentyfive.apaapilayer.filters.OrderFilter;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.PaypalCredentials;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus.ANNULLATO;
import static twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus.MODIFICATO_DA_PASTICCERIA;

@Service
public class ActiveOrderService {

    private final String NOTIFICATION_TOPIC="twentyfive_internal_notifications";

    private final String FTP_PATH="apa/orders/%s";

    private final EmailService emailService;

    private final ActiveOrderRepository activeOrderRepository;
    private final CustomerRepository customerRepository; // Aggiunto il CustomerRepository
    private final CompletedOrderRepository completedOrderRepository;
    private final ProducerPool producerPool;
    private final StompClientController stompClientController;
    private final ProductKgRepository productKgRepository;
    private final ProductFixedRepository productFixedRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final PaymentClientController paymentClientController;
    private final KeycloakService keycloakService;

    private final TrayRepository trayRepository;

    private final SettingRepository settingRepository;

    private final TimeSlotAPARepository timeSlotAPARepository;

    private final MongoTemplate mongoTemplate;
    private final MediaManagerClientController mediaManagerClientController;
    private final MediaManagerService mediaManagerService;
    private final CategoryService categoryService;

    @Autowired
    public ActiveOrderService(EmailService emailService, ActiveOrderRepository activeOrderRepository, CustomerRepository customerRepository, CompletedOrderRepository completedOrderRepository, ProducerPool producerPool, StompClientController stompClientController, ProductKgRepository productKgRepository, ProductFixedRepository productFixedRepository, ProductWeightedRepository productWeightedRepository, PaymentClientController paymentClientController, KeycloakService keycloakService, TrayRepository trayRepository, SettingRepository settingRepository, TimeSlotAPARepository timeSlotAPARepository, MongoTemplate mongoTemplate, MediaManagerClientController mediaManagerClientController, MediaManagerService mediaManagerService, CategoryService categoryService) {
        this.emailService = emailService;
        this.activeOrderRepository = activeOrderRepository;
        this.customerRepository = customerRepository; // Iniezione di CustomerRepository
        this.completedOrderRepository= completedOrderRepository;
        this.producerPool = producerPool;
        this.stompClientController = stompClientController;
        this.productKgRepository=productKgRepository;
        this.productFixedRepository = productFixedRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.paymentClientController = paymentClientController;
        this.keycloakService = keycloakService;
        this.trayRepository=trayRepository;
        this.settingRepository=settingRepository;
        this.timeSlotAPARepository=timeSlotAPARepository;
        this.mongoTemplate = mongoTemplate;
        this.mediaManagerClientController = mediaManagerClientController;
        this.mediaManagerService = mediaManagerService;
        this.categoryService = categoryService;
    }

    public OrderAPA createOrder(OrderAPA order) {
        return activeOrderRepository.save(order);
    }

    public Page<OrderAPADTO> getAll(int page, int size, String sortColumn, String sortDirection, OrderFilter filters) throws IOException{
        List<String> roles = JwtUtilities.getRoles();
        Query query = new Query();
        query = FilterUtilities.applyOrderFilters(query, filters, roles, customerRepository);

        // Controllo del sorting di default su createdDate in ordine decrescente
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.DESC, "createdDate");
        } else {
            String mappedColumn = switch (sortColumn) {
                case "formattedPickupDate" -> "pickupDateTime"; // Gestione sorting per pickupDateTime
                case "price" -> "realPrice"; // Gestione sorting per realPrice
                default -> sortColumn; // Utilizzo diretto per altre colonne
            };
            sort = Sort.by(Sort.Direction.fromString(sortDirection), mappedColumn);
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        List<OrderAPA> orderList = mongoTemplate.find(query, OrderAPA.class);
        List<OrderAPADTO> realOrder = orderList.stream()
                .map(this::convertToOrderAPADTO)
                .collect(Collectors.toList());

        return PageUtilities.convertListToPageWithSorting(realOrder, pageable);
    }

    private OrderAPADTO convertToOrderAPADTO(OrderAPA order) {
        try {
            OrderAPADTO dto = new OrderAPADTO();
            dto.setId(order.getId());
            dto.setPickupDateTime((order.getPickupDate().atTime(order.getPickupTime())));
            dto.setRealPrice(order.getTotalPrice());

            if(order.getPaymentId()!=null){
                if(order.getCounterUpdatedProducts()>0){
                    dto.setMethodPayment("Online (+"+order.getCounterUpdatedProducts()*5+"€)");
                } else {
                    dto.setMethodPayment("Online");
                }
            } else {
                dto.setMethodPayment("Al ritiro");
            }

            dto.setPrice("€ " +String.format("%.2f", order.getTotalPrice()));
            String status = maskModifiedFromBakerForCustomers(order.getStatus());
            dto.setStatus(status);
            dto.setUnread(order.isUnread());
            dto.setBakerUnread(order.isBakerUnread());
            dto.setCounterUnread(order.isCounterUnread());
            dto.setCreatedDate(order.getCreatedDate());
            if(order.getAppliedCoupon() != null){

                dto.setDiscountApplied(order.getAppliedCoupon().getDiscountValue());
            }
            if(order.getCustomerId()!= null){
                Optional<CustomerAPA> optCustomerId = customerRepository.findById(order.getCustomerId());
                if(optCustomerId.isPresent()){
                    CustomerAPA customer = optCustomerId.get();
                    dto.setFirstName(customer.getFirstName());
                    dto.setLastName(customer.getLastName());
                }
            } else {
                dto.setFirstName(order.getCustomInfo().getFirstName());
                dto.setLastName(order.getCustomInfo().getLastName());
            }
            someToPrepare(order);
            dto.setToPrepare(someToPrepare(order));
            return dto;
        } catch(Exception e){
            throw new RuntimeException("Error retrieving roles user!");
        }
    }

    private String maskModifiedFromBakerForCustomers(OrderStatus orderStatus) throws IOException{
        List<String> roles = JwtUtilities.getRoles();
        if(roles.contains("customer") && orderStatus.equals(MODIFICATO_DA_PASTICCERIA)){ //Mascheriamo modificato da pasticceria con "in preparazione"
            return OrderStatus.IN_PREPARAZIONE.getStatus();
        }
        return orderStatus.getStatus();
    }

    public OrderDetailsAPADTO getDetailsById(String id) throws IOException {
        Optional<OrderAPA> orderOptional = activeOrderRepository.findById(id);
        if (orderOptional.isPresent()) {
            OrderAPA orderAPA = orderOptional.get();
            List<String> roles =JwtUtilities.getRoles();
            if(roles.contains("admin")){
                orderAPA.setUnread(false);
                activeOrderRepository.save(orderAPA);
            } else if (roles.contains("baker")) {
                orderAPA.setBakerUnread(false);
                activeOrderRepository.save(orderAPA);
            } else if (roles.contains("counter")){ //dovremmo mappare gli oggetti uguali nello stesso dto. (bIP e pIP) (customer)
                orderAPA.setCounterUnread(false);
                activeOrderRepository.save(orderAPA);
            }
            return convertToOrderDetailsAPADTO(orderAPA);
        } else {
            return null; // Se non viene trovato nessun ordine, ritorna null
        }
    }

    private OrderDetailsAPADTO convertToOrderDetailsAPADTO(OrderAPA order) throws IOException {
        OrderDetailsAPADTO dto = new OrderDetailsAPADTO();
        dto.setId(order.getId());
        mapProductsForOrderDTOs(dto,order);
        dto.setTotalPrice(order.getTotalPrice());
        dto.setPaymentId(order.getPaymentId());
        dto.setPickupDateTime(order.getPickupDate().atTime(order.getPickupTime()));
        String status = maskModifiedFromBakerForCustomers(order.getStatus());
        dto.setStatus(status);
        dto.setOrderNote(order.getNote());
        dto.setUnread(order.isUnread());
        dto.setAppliedCoupon(order.getAppliedCoupon());
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

    private void mapProductsForOrderDTOs(OrderDetailsAPADTO dto,OrderAPA order) throws IOException {
        List<String> roles = JwtUtilities.getRoles();
        List<ProductInPurchaseDTO> productDTOs;
        List<BundleInPurchaseDTO> bundleDTOs;
        if (roles.contains("admin") || roles.contains("counter")){
            productDTOs = order.getProductsInPurchase().stream()
                    .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);

            bundleDTOs = order.getBundlesInPurchase().stream()
                    .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setBundles(bundleDTOs); // Assumi che esista un getter che restituisca i bundle
            return;
        }
        else if (roles.contains("baker")){
            // Filtra i prodotti con toPrepare = true
            productDTOs = order.getProductsInPurchase().stream()
                    .filter(ProductInPurchase::isToPrepare).map(this::convertProductPurchaseToDTO)
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);
            // Filtra i bundle con toPrepare = true
            bundleDTOs = order.getBundlesInPurchase().stream()
                    .filter(BundleInPurchase::isToPrepare).map(this::convertBundlePurchaseToDTO)
                    .collect(Collectors.toList());
            dto.setBundles(bundleDTOs);
            return;
        }
        else if (roles.contains("customer")){
            //TODO mapActiveOrderToSummaryEmail unire i prodotti uguali aumentato quantità, consiglio di creare un nuovo metodo o controllare con i ruoli
            productDTOs = order.getProductsInPurchase().stream()
                    .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);

            bundleDTOs = order.getBundlesInPurchase().stream()
                    .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setBundles(bundleDTOs); // Assumi che esista un getter che restituisca i bundle
        }
    }

    private OrderDetailsPrintAPADTO convertToOrderDetailsPrintAPADTO(OrderAPA order) throws IOException {
        OrderDetailsPrintAPADTO dto = new OrderDetailsPrintAPADTO();
        dto.setId(order.getId());
        dto.setPickupDate(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setStatus(order.getStatus().getStatus());
        dto.setNote(order.getNote());

        // Recupera i ruoli
        List<String> roles = JwtUtilities.getRoles();

        List<ProductInPurchaseDTO> productDTOs;
        List<BundleInPurchaseDTO> bundleDTOs;

        if (roles.contains("baker")) {
            // Filtra i prodotti con toPrepare = true
            productDTOs = order.getProductsInPurchase().stream()
                    .filter(ProductInPurchase::isToPrepare)
                    .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);

            // Filtra i bundle con toPrepare = true
            bundleDTOs = order.getBundlesInPurchase().stream()
                    .filter(BundleInPurchase::isToPrepare)
                    .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setBundles(bundleDTOs);
        } else {
            // Per gli altri ruoli, non si applica il filtro
            productDTOs = order.getProductsInPurchase().stream()
                    .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setProducts(productDTOs);

            bundleDTOs = order.getBundlesInPurchase().stream()
                    .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                    .collect(Collectors.toList());
            dto.setBundles(bundleDTOs);
        }

        // Gestione informazioni del cliente
        if (order.getCustomerId() != null) {
            Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
            if (optCustomer.isPresent()) {
                CustomerAPA customer = optCustomer.get();
                dto.setEmail(customer.getEmail());
                dto.setPhoneNumber(customer.getPhoneNumber());
                dto.setFullName(customer.getFirstName() + " " + customer.getLastName());
            }
        } else if (order.getCustomInfo() != null) {
            dto.setEmail(order.getCustomInfo().getEmail());
            dto.setPhoneNumber(order.getCustomInfo().getPhoneNumber());
            dto.setFullName(order.getCustomInfo().getFirstName() + " " + order.getCustomInfo().getLastName());
        }

        return dto;
    }
    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        ProductUpdateField productUpdateField = ProductUpdateField.NONE;
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

                CategoryAPA category = categoryService.getById(productKg.getCategoryId());
                switch(category.getName()){
                    case "Semifreddi" -> productUpdateField = ProductUpdateField.TEXT;
                    case "Le Nostre Torte" -> productUpdateField = ProductUpdateField.ALL;
                }

                name = productKg.getName();
                price = productKg.getPricePerKg();
            }
        }
        return new ProductInPurchaseDTO(productInPurchase, name, price, productUpdateField);
    }

    private PieceInPurchaseDTO convertPiecePurchaseToDTO(PieceInPurchase piece) {
        Optional<ProductWeightedAPA> pWght = productWeightedRepository.findById(piece.getId());
        String name = pWght.map(ProductWeightedAPA::getName).orElse("No registered product");
        double weight = pWght.map(ProductWeightedAPA::getWeight).orElseThrow(() -> new IllegalArgumentException());
        return new PieceInPurchaseDTO(piece, name, weight);
    }
    private BundleInPurchaseDTO convertBundlePurchaseToDTO(BundleInPurchase bundleInPurchase) {
        Optional<Tray> bun = trayRepository.findById(bundleInPurchase.getId());
        String name = bun.map(Tray::getName).orElse("no registered product");
        if(bun.isPresent()){
            if (!bun.get().isCustomized()) {
                return new BundleInPurchaseDTO(bundleInPurchase,name);
            }
        }
        List<PieceInPurchase> weightedProducts= bundleInPurchase.getWeightedProducts();
        List<PieceInPurchaseDTO> weightedProductsDTOs= weightedProducts.stream()
                .map(this::convertPiecePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());

        return new BundleInPurchaseDTO(bundleInPurchase, name,weightedProductsDTOs);
    }


    public boolean deleteById(String id) {
        if (activeOrderRepository.existsById(id)) {
            activeOrderRepository.deleteById(id);
            return true; // Restituisce true se l'ordine è stato trovato e cancellato
        } else {
            return false; // Restituisce false se l'ordine non è stato trovato
        }
    }

    public Page<OrderAPADTO> getByCustomerId(String customerId, Pageable pageable){
        // Supponendo che il repository abbia il metodo findOrdersByCustomerId
        return activeOrderRepository.findOrdersByCustomerIdOrderByCreatedDateDesc(customerId, pageable)
                .map(this::convertToOrderAPADTO); // Converti ogni ordine in OrderAPADTO
    }

    @Transactional
    public OrderAPADTO complete(String id) {
        OrderAPA order = activeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));



        return convertToOrderAPADTO(order); // Ritorna l'ordine completato come DTO
    }

    private void createCompletedOrder(OrderAPA order, CompletedOrderAPA completedOrder) {
        // Copia l'ID se necessario, altrimenti generane uno nuovo se i sistemi devono essere indipendenti
        completedOrder.setId(order.getId());

        // Informazioni base dell'ordine
        if(order.getCustomerId()!=null){
            completedOrder.setCustomerId(order.getCustomerId()); // Assumendo che ci sia un campo customerId
        } else {
            completedOrder.setCustomInfo(order.getCustomInfo());
        }
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
        if (order.getPaymentId()!=null) {
            completedOrder.setPaymentId(order.getPaymentId());
        }

        completedOrder.setCreatedDate(LocalDateTime.now());
        // Altri campi specifici dell'ordine possono essere aggiunti qui
    }
    @Transactional
    public boolean cancel(String id) {
        OrderAPA order = activeOrderRepository.findById(id)
                .orElse(null); // Trova l'ordine o restituisce null se non esiste

        LocalDate pickupDate= order.getPickupDate();


        // Calcola la data di "oggi più due giorni"
        LocalDate cancelThreshold = pickupDate.minusDays(settingRepository.findAll().get(0).getMinCancelOrder());

        // Verifica se la data fornita è dopo "due giorni da oggi"
        if(LocalDate.now().isAfter(cancelThreshold)){
            throw new CancelThresholdPassedException();
        }



        if (order != null) {
            String fullName ="";
            TimeSlotAPA timeSlotAPA=timeSlotAPARepository.findAll().get(0);
            order.setStatus(ANNULLATO); // Imposta lo stato a ANNULLATO

            CompletedOrderAPA completedOrder = new CompletedOrderAPA();
            createCompletedOrder(order, completedOrder); // Utilizza un metodo simile a createCompletedOrder per copiare i dettagli
            ArrayList<ItemInPurchase> items= new ArrayList<>();
            items.addAll(order.getBundlesInPurchase());
            items.addAll(order.getProductsInPurchase());
            if(timeSlotAPA.freeNumSlot(LocalDateTime.of(pickupDate,order.getPickupTime()),countSlotRequired(items),getStandardHourSlotMap())) {
                timeSlotAPARepository.save(timeSlotAPA);
            }
            if (order.getPaymentId() != null){
                PaypalCredentials paypalCredentials = settingRepository.findAll().get(0).getPaypalCredentials();
                String authorization=keycloakService.getAccessTokenTF();
                paymentClientController.refundPaymentOutside(authorization, order.getPaymentId(), paypalCredentials);
            }
            activeOrderRepository.delete(order); // Rimuove l'ordine dalla repository degli ordini attivi
            completedOrderRepository.save(completedOrder); // Salva l'ordine nella repository degli ordini completati/annullati
            if(order.getCustomerId()!=null){
                Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
                if(optCustomer.isPresent()){
                    CustomerAPA customer = optCustomer.get();
                    fullName= customer.getLastName() +" "+ customer.getFirstName();
                } else {
                    fullName = order.getCustomInfo().getLastName() +" "+ order.getCustomInfo().getFirstName();
                }
            }
            if(someToPrepare(order)){
                TwentyfiveMessage twentyfiveMessage = StompUtilities.sendBakerDeleteNotification(fullName, order.getId());
                stompClientController.sendObjectMessage(twentyfiveMessage);
            }
            TwentyfiveMessage message= StompUtilities.sendAdminDeleteNotification(fullName,order.getId());
            stompClientController.sendObjectMessage(message);
            return true;
        }
        return false;
    }

    private Map<LocalTime, Integer> getStandardHourSlotMap() {
        Map<LocalTime,Integer> slotsPerH= new TreeMap<>();
        slotsPerH.put(LocalTime.of(8,0,0),4);
        slotsPerH.put(LocalTime.of(9,0,0),4);
        slotsPerH.put(LocalTime.of(10,0,0),4);
        slotsPerH.put(LocalTime.of(11,0,0),4);
        slotsPerH.put(LocalTime.of(12,0,0),4);
        slotsPerH.put(LocalTime.of(13,0,0),4);


        slotsPerH.put(LocalTime.of(14,0,0),10);
        slotsPerH.put(LocalTime.of(15,0,0),10);
        slotsPerH.put(LocalTime.of(16,0,0),10);
        slotsPerH.put(LocalTime.of(17,0,0),10);
        slotsPerH.put(LocalTime.of(18,0,0),10);
        slotsPerH.put(LocalTime.of(19,0,0),10);
        return slotsPerH;

    }

    private int countSlotRequired(List<ItemInPurchase>items) {
        int numSlotRequired = 0;


        for (ItemInPurchase item : items) {

            if (item instanceof ProductInPurchase) {
                ProductInPurchase pip = (ProductInPurchase) item;
                ProductKgAPA product = productKgRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                if (product.isCustomized()) {
                    numSlotRequired += pip.getQuantity();

                }


            } else if (item instanceof BundleInPurchase) {
                BundleInPurchase pip = (BundleInPurchase) item;
                Tray tray = trayRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                if (tray.isCustomized()) {
                    numSlotRequired += pip.getQuantity();
                }

            }
        }
        return numSlotRequired;
    }

    public ByteArrayOutputStream print(String id) throws DocumentException, IOException {
        Optional<OrderAPA> orderOptional = activeOrderRepository.findById(id);
        if (orderOptional.isPresent()) {
            OrderDetailsPrintAPADTO orderDetails = convertToOrderDetailsPrintAPADTO(orderOptional.get());
            return PdfUtilities.generatePdfStream(orderDetails);
        }
        return null;
    }

    public OrderStatus[] getAllStatuses() {
        return OrderStatus.getStatusEditable();
    }

    @Transactional
    public Boolean changeOrderStatus(String id, String status) throws IOException {
        Optional<OrderAPA> optOrder = activeOrderRepository.findById(id);
        String email ="";
        String firstName ="";
        if (optOrder.isPresent()){
            OrderAPA order = optOrder.get();
            order.setCreatedDate(LocalDateTime.now());
            if(order.getCustomerId()!=null) {
                Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
                if (optCustomer.isPresent()) {
                    CustomerAPA customer = optCustomer.get();
                    firstName= customer.getFirstName();
                    email = customer.getEmail();
                    if(!(status.toUpperCase().equals(MODIFICATO_DA_PASTICCERIA))){
                    String customerNotification = StompUtilities.sendChangedStatusNotification(OrderStatus.valueOf(status.toUpperCase()), customer.getId());
                    producerPool.send(customerNotification,1,NOTIFICATION_TOPIC);
                }
                }
            } else {
                firstName= order.getCustomInfo().getFirstName();
                email = order.getCustomInfo().getEmail();
            }
            /*if(order.getStatus() == OrderStatus.IN_PREPARAZIONE && !(status.toUpperCase().equals("IN_PREPARAZIONE") || status.toUpperCase().equals("MODIFICATO_DA_PASTICCERIA"))){
                String bakerNotification =StompUtilities.sendBakerNotification("changed");
                producerPool.send(bakerNotification,1,NOTIFICATION_TOPIC);
            }
             */
            switch(OrderStatus.valueOf(status.toUpperCase())) {
                case ANNULLATO -> {
                    String fullName ="";
                    LocalDate pickupDate = order.getPickupDate();
                    TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
                    order.setStatus(ANNULLATO); // Imposta lo stato a ANNULLATO
                    CompletedOrderAPA completedOrder = new CompletedOrderAPA();
                    createCompletedOrder(order, completedOrder); // Utilizza un metodo simile a createCompletedOrder per copiare i dettagli
                    ArrayList<ItemInPurchase> items = new ArrayList<>();
                    items.addAll(optOrder.get().getBundlesInPurchase());
                    items.addAll(optOrder.get().getProductsInPurchase());
                    if (timeSlotAPA.freeNumSlot(LocalDateTime.of(pickupDate, optOrder.get().getPickupTime()), countSlotRequired(items), getStandardHourSlotMap())) {
                        timeSlotAPARepository.save(timeSlotAPA);
                    }
                    if (order.getPaymentId() != null){
                        PaypalCredentials paypalCredentials = settingRepository.findAll().get(0).getPaypalCredentials();
                        String authorization=keycloakService.getAccessTokenTF();
                        paymentClientController.refundPaymentOutside(authorization, order.getPaymentId(), paypalCredentials);
                    }
                    activeOrderRepository.delete(optOrder.get()); // Rimuove l'ordine dalla repository degli ordini attivi
                    completedOrderRepository.save(completedOrder); // Salva l'ordine nella repository degli ordini completati/annullati
                    emailService.sendEmail(email, OrderStatus.valueOf(status.toUpperCase()), TemplateUtilities.populateEmail(firstName,order.getId()));
                    if (someToPrepare(order)) {
                        if (order.getCustomerId() != null) {
                            Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
                            if (optCustomer.isPresent()) {
                                fullName = optCustomer.get().getFirstName() + optCustomer.get().getLastName();
                            } else {
                                fullName = order.getCustomInfo().getFirstName() + order.getCustomInfo().getLastName();
                            }
                            TwentyfiveMessage twentyfiveMessage = StompUtilities.sendBakerDeleteNotification(fullName, order.getId());
                            stompClientController.sendObjectMessage(twentyfiveMessage);
                        }
                    }
                }
                case IN_PASTICCERIA -> {
                    order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                    activeOrderRepository.save(order);
                }
                case IN_PREPARAZIONE -> {
                    //String bakerNotification =StompUtilities.sendBakerNotification("new");
                    //producerPool.send(bakerNotification,1,NOTIFICATION_TOPIC);
                    //emailService.sendEmail(email, OrderStatus.valueOf(status.toUpperCase()),TemplateUtilities.populateEmail(firstName,order.getId()));
                    order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                    activeOrderRepository.save(order);
                }
                case PRONTO -> {
                    emailService.sendEmail(email, OrderStatus.valueOf(status.toUpperCase()),TemplateUtilities.populateEmail(firstName,order.getId()));
                    order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                    activeOrderRepository.save(order);
                }
                case MODIFICATO_DA_PASTICCERIA -> {
                    //String adminNotification = StompUtilities.sendAdminNotification();
                    //producerPool.send(adminNotification,1,NOTIFICATION_TOPIC);
                    orderIsPrepared(order);
                    order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                    order.setUnread(true);
                    activeOrderRepository.save(order);
                }
                case RICEVUTO -> {
                    optOrder.get().setStatus(OrderStatus.valueOf(status.toUpperCase()));
                    activeOrderRepository.save(optOrder.get());
                }
                case COMPLETO -> {
                    optOrder.get().setStatus(OrderStatus.COMPLETO); // Assumendo che OrderStatus sia un enum
                    CompletedOrderAPA completedOrder = new CompletedOrderAPA();
                    createCompletedOrder(optOrder.get(), completedOrder); // Metodo helper per copiare i dettagli
                    activeOrderRepository.delete(optOrder.get()); // Rimuove l'ordine dalla repository degli ordini attivi
                    completedOrderRepository.save(completedOrder); // Salva l'ordine nella repository degli ordini completati
                }
            }
            return true;
        }
        return false;
    }

    public boolean setLocation(LocationReq locationReq) throws IOException {
        List<String> roles = JwtUtilities.getRoles();
        Optional<OrderAPA> optOrder = activeOrderRepository.findById(locationReq.getOrderId());
        int position = locationReq.getPosition();
        String location = locationReq.getLocation();
        String counterNote = locationReq.getCounterNote();
        List<ProductInPurchase> products = new ArrayList<>();
        List<BundleInPurchase> bundles = new ArrayList<>();
        if(optOrder.isPresent()){
            OrderAPA order = optOrder.get();
            boolean alreadySomeToPrepare = order.getProductsInPurchase().stream().anyMatch(ProductInPurchase::isToPrepare) || order.getBundlesInPurchase().stream().anyMatch(BundleInPurchase::isToPrepare);
            if (roles.contains("admin") || roles.contains("counter")){
                products = order.getProductsInPurchase();
                bundles = order.getBundlesInPurchase();
            } else if (roles.contains("baker")){

                // Filtra i prodotti con toPrepare = true
                products = order.getProductsInPurchase().stream()
                        .filter(ProductInPurchase::isToPrepare)
                        .collect(Collectors.toList());

                // Filtra i bundle con toPrepare = true
                bundles = order.getBundlesInPurchase().stream()
                        .filter(BundleInPurchase::isToPrepare)
                        .collect(Collectors.toList());
            }
            if (position >= products.size()) {//è un bundle
                position = position - products.size();
                BundleInPurchase bIP = bundles.get(position);
                if (location.equals("Nessun Luogo")) {
                    bIP.setLocation(null);
                } else if (location.equals("In preparazione")) {
                    bIP.setToPrepare(true);
                    bIP.setLocation(location);
                    bIP.setCounterNote(counterNote);
                    order.setBakerUnread(true);
                    order.setStatus(OrderStatus.IN_PREPARAZIONE);
                } else {
                    bIP.setToPrepare(false);
                    bIP.setLocation(location);
                }
            } else {// è un product
                ProductInPurchase pIP = products.get(position);
                if (location.equals("Nessun Luogo")) {
                    pIP.setLocation(null);
                } else if (location.equals("In preparazione")) {
                    pIP.setToPrepare(true);
                    pIP.setLocation(location);
                    pIP.setCounterNote(counterNote);
                    order.setBakerUnread(true);
                    order.setStatus(OrderStatus.IN_PREPARAZIONE);
                } else {
                    pIP.setToPrepare(false);
                    pIP.setLocation(location);
                }
            }
            boolean noMoreToPrepare = order.getProductsInPurchase().stream()
                    .allMatch(product -> product.getLocation() != null
                            && !"In pasticceria".equals(product.getLocation())
                            && !"In preparazione".equals(product.getLocation())) &&
                    order.getBundlesInPurchase().stream()
                            .allMatch(bundle -> bundle.getLocation() != null
                                    && !"In pasticceria".equals(bundle.getLocation())
                                    && !"In preparazione".equals(bundle.getLocation()));
            if (noMoreToPrepare){
                order.setStatus(OrderStatus.PRONTO);
                CustomerAPA customer = getCustomerFromOrder(order);
                emailService.sendEmail(customer.getEmail(),OrderStatus.PRONTO,TemplateUtilities.populateEmail(customer.getFirstName(),order.getId()));
            }
            if(!roles.contains("admin")){
                TwentyfiveMessage twentyfiveMessage = StompUtilities.sendAdminMoveNotification(order.getId(),location);
                stompClientController.sendObjectMessage(twentyfiveMessage);
                order.setUnread(true);
                order.setCreatedDate(LocalDateTime.now());
            }
            if (!roles.contains("baker")){
                if(alreadySomeToPrepare){
                    if(location.equals("In preparazione")){
                        TwentyfiveMessage twentyfiveMessage = StompUtilities.sendBakerUpdateNotification(order.getId(), location);
                        stompClientController.sendObjectMessage(twentyfiveMessage);
                    } else {
                        TwentyfiveMessage twentyfiveMessage = StompUtilities.sendBakerMoveNotification(order.getId(), location);
                        stompClientController.sendObjectMessage(twentyfiveMessage);
                    }
                } else {
                    TwentyfiveMessage twentyfiveMessage = StompUtilities.sendBakerNewNotification();
                    stompClientController.sendObjectMessage(twentyfiveMessage);
                }
            }
            activeOrderRepository.save(order);
            return true;
        }
        return false;
    }

    private void orderIsPrepared(OrderAPA order) {
        // Imposta toPrepare = false per tutti i prodotti
        order.getProductsInPurchase().forEach(product -> product.setToPrepare(false));

        // Imposta toPrepare = false per tutti i bundle
        order.getBundlesInPurchase().forEach(bundle -> bundle.setToPrepare(false));
    }

    private boolean someToPrepare(OrderAPA order){
        return order.getProductsInPurchase().stream().anyMatch(ProductInPurchase::isToPrepare) || order.getBundlesInPurchase().stream().anyMatch(BundleInPurchase::isToPrepare);
    }

    private CustomerAPA getCustomerFromOrder(OrderAPA order) {
        CustomerAPA customer = new CustomerAPA();
        // Se CustomInfo non è null, prendi il customer da lì
        if (order.getCustomInfo() != null) {
            customer.setFirstName(order.getCustomInfo().getFirstName());
            customer.setEmail(order.getCustomInfo().getEmail());
        } else {
            // Se CustomInfo è null, prendi il customer usando l'idCustomer
            customer = customerRepository.findById(order.getCustomerId())
                    .orElse(null);
        }
        return customer;
    }

    public OrderAPA getById(String id) {
        return activeOrderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("nessun ordine attivo trovato con questo id = " +id));
    }

    @Transactional
    public Boolean updateOrder(String id, UpdateOrderReq updateOrderReq, MultipartFile file) {
        OrderAPA order = this.getById(id);
        List<ProductInPurchase> productsInPurchase = order.getProductsInPurchase();

        if(!(updateOrderReq.getPosition()>=productsInPurchase.size()) && order.getProductsInPurchase() != null){

            ProductInPurchase productInPurchase = productsInPurchase.get(updateOrderReq.getPosition());

            if(updateOrderReq.getCustomizations()!=null) {
                List<Customization> newCustomizations = updateOrderReq.getCustomizations();

                if (productInPurchase.getCustomization() != null) {
                    List<Customization> oldCustomizations = productInPurchase.getCustomization();

                    for (Customization newCustomization : newCustomizations) {

                        Optional<Customization> optCustomization = oldCustomizations.stream()
                                .filter(c -> c.getName().equals(newCustomization.getName()))
                                .findFirst();

                        if (optCustomization.isPresent()) {
                            Customization customization = optCustomization.get();
                            customization.setValue(newCustomization.getValue());
                        } else {
                            oldCustomizations.add(newCustomization);
                        }

                    }

                } else {
                    productInPurchase.setCustomization(newCustomizations);
                }
            }
            String pathToDelete = productInPurchase.getAttachment()!=null ? productInPurchase.getAttachment() : "";

            if (file != null) {
                if (pathToDelete != null && !pathToDelete.isBlank()) {
                    mediaManagerClientController.deleteMedia(pathToDelete.substring(1));
                } else if (!productInPurchase.isUpdated()){
                    // Aumenta il prezzo degli ordini se il file è presente ma il path da eliminare non è valido
                    order.setCounterUpdatedProducts(order.getCounterUpdatedProducts() + 1);
                    order.setTotalPrice(order.getTotalPrice() + 5);
                }

                // Aggiornamento del prodotto e caricamento della nuova media
                productInPurchase.setUpdated(true);
                String path = String.format(FTP_PATH, order.getId());
                String realName = mediaManagerClientController.uploadMedia(file, path);
                productInPurchase.setAttachment("/" + path + "/" + realName);
            }


            return activeOrderRepository.save(order) != null;
        }
        throw new IndexOutOfBoundsException("Non c'è alcun prodotto con questa posizione nel carrelo " +updateOrderReq.getPosition());
    }
}

