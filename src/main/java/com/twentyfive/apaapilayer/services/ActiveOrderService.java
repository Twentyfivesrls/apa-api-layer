package com.twentyfive.apaapilayer.services;

import com.itextpdf.text.DocumentException;
import com.twentyfive.apaapilayer.clients.PaymentClientController;
import com.twentyfive.apaapilayer.clients.StompClientController;
import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.configurations.ProducerPool;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.CancelThresholdPassedException;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.PaypalCredentials;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PieceInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
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

    private final EmailService emailService;

    private final ActiveOrderRepository activeOrderRepository;
    private final CustomerRepository customerRepository; // Aggiunto il CustomerRepository
    private final CompletedOrderRepository completedOrderRepository;
    private final ProducerPool producerPool;
    private final StompClientController stompClientController;
    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final PaymentClientController paymentClientController;
    private final KeycloakService keycloakService;

    private final TrayRepository trayRepository;

    private final SettingRepository settingRepository;

    private final TimeSlotAPARepository timeSlotAPARepository;

    @Autowired
    public ActiveOrderService(EmailService emailService, ActiveOrderRepository activeOrderRepository, CustomerRepository customerRepository, CompletedOrderRepository completedOrderRepository, ProducerPool producerPool, StompClientController stompClientController, ProductKgRepository productKgRepository, ProductWeightedRepository productWeightedRepository, PaymentClientController paymentClientController, KeycloakService keycloakService, TrayRepository trayRepository, SettingRepository settingRepository, TimeSlotAPARepository timeSlotAPARepository) {
        this.emailService = emailService;
        this.activeOrderRepository = activeOrderRepository;
        this.customerRepository = customerRepository; // Iniezione di CustomerRepository
        this.completedOrderRepository= completedOrderRepository;
        this.producerPool = producerPool;
        this.stompClientController = stompClientController;
        this.productKgRepository=productKgRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.paymentClientController = paymentClientController;
        this.keycloakService = keycloakService;
        this.trayRepository=trayRepository;
        this.settingRepository=settingRepository;
        this.timeSlotAPARepository=timeSlotAPARepository;
    }

    public OrderAPA createOrder(OrderAPA order) {
        double totalPrice=0;
        if(order.getBundlesInPurchase()!=null){
            if (order.getBundlesInPurchase().size()>0){
                for(BundleInPurchase bIP : order.getBundlesInPurchase()){
                    totalPrice+=bIP.getTotalPrice();
                }
            }
        }
        if(order.getProductsInPurchase()!=null){
            if (order.getProductsInPurchase().size()>0){
                for(ProductInPurchase pIP : order.getProductsInPurchase()){

                    totalPrice+=pIP.getTotalPrice();
                }
            }
        }
        order.setTotalPrice(totalPrice);
        order.setCreatedDate(LocalDateTime.now());
        return activeOrderRepository.save(order);
    }

    public Page<OrderAPADTO> getAll(int page, int size, String sortColumn, String sortDirection) throws IOException{
        List<String> roles = JwtUtilities.getRoles();
        List<OrderAPA> orderList = new ArrayList<>();
        List<OrderAPADTO> realOrder = new ArrayList<>();
        if(roles.contains("admin")){
            // Fetching paginated orders from the database
            orderList= activeOrderRepository.findAllByOrderByCreatedDateDesc();
        }
        else if (roles.contains("baker")){
            orderList = activeOrderRepository.findByProductsInPurchaseToPrepareTrueOrBundlesInPurchaseToPrepareTrueOrderByCreatedDateDesc();
        }
        for(OrderAPA order:orderList){
            OrderAPADTO orderAPA= convertToOrderAPADTO(order);
            realOrder.add(orderAPA);
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort;
            if (sortColumn.equals("price")) {
                sort = Sort.by(Sort.Direction.fromString(sortDirection), "realPrice");
            } else if (sortColumn.equals("formattedPickupDate")) {
                sort = Sort.by(Sort.Direction.fromString(sortDirection), "pickupDateTime");
            } else {
                sort = Sort.by(Sort.Direction.fromString(sortDirection),sortColumn);
            }
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realOrder,pageable);
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realOrder,pageable);
    }

    private OrderAPADTO convertToOrderAPADTO(OrderAPA order) {
        try {
            OrderAPADTO dto = new OrderAPADTO();
            dto.setId(order.getId());
            dto.setPickupDateTime((order.getPickupDate().atTime(order.getPickupTime())));
            dto.setRealPrice(order.getTotalPrice());
            if(order.getPaymentId()!=null){
                dto.setMethodPayment("Online");
            } else {
                dto.setMethodPayment("Al ritiro");
            }
            dto.setPrice(String.format("%.2f", order.getTotalPrice()) + " €");
            String status = maskModifiedFromBakerForCustomers(order.getStatus());
            dto.setStatus(status);
            dto.setUnread(order.isUnread());
            dto.setBakerUnread(order.isBakerUnread());
            dto.setCounterUnread(order.isCounterUnread());
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
            } else { //dovremmo mappare gli oggetti uguali nello stesso dto. (bIP e pIP) (customer)

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
        if (roles.contains("admin")){
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

    private OrderDetailsPrintAPADTO convertToOrderDetailsPrintAPADTO(OrderAPA order) {
        OrderDetailsPrintAPADTO dto = new OrderDetailsPrintAPADTO();
        dto.setId(order.getId());
        dto.setPickupDate(order.getPickupDate().atTime(order.getPickupTime()));
        dto.setStatus(order.getStatus().getStatus());
        dto.setNote(order.getNote());
        List<ProductInPurchaseDTO> productDTOs = order.getProductsInPurchase().stream()
                .map(this::convertProductPurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());
        dto.setProducts(productDTOs);

        List<BundleInPurchaseDTO> bundleDTOs = order.getBundlesInPurchase().stream()
                .map(this::convertBundlePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());
        dto.setBundles(bundleDTOs); // Assumi che esista un getter che restituisca i bundle

        if(order.getCustomerId()!=null){
            Optional<CustomerAPA> optCustomer = customerRepository.findById(order.getCustomerId());
            if(optCustomer.isPresent()){
                CustomerAPA customer = optCustomer.get();
                dto.setEmail(customer.getEmail()); // Assumi una relazione uno-a-uno con Customer
                dto.setPhoneNumber(customer.getPhoneNumber()); // Assumi che il telefono sia disponibile
                dto.setFullName(customer.getFirstName() + " " + customer.getLastName());
            }
        } else {
            dto.setEmail(order.getCustomInfo().getEmail()); // Assumi una relazione uno-a-uno con Customer
            dto.setPhoneNumber(order.getCustomInfo().getPhoneNumber()); // Assumi che il telefono sia disponibile
            dto.setFullName(order.getCustomInfo().getFirstName() + " " + order.getCustomInfo().getLastName());
        }
        return dto;
    }

    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        Optional<ProductKgAPA> pKg = productKgRepository.findById(productInPurchase.getId());
        String name = pKg.map(ProductKgAPA::getName).orElse("no registered product");
        return new ProductInPurchaseDTO(productInPurchase, name);
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
            if (roles.contains("admin")){
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
                } else if (location.equals("In pasticceria")) {
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
                } else if (location.equals("In pasticceria")) {
                    pIP.setToPrepare(true);
                    pIP.setLocation(location);
                    pIP.setCounterNote(counterNote);
                    order.setBakerUnread(true);
                    order.setStatus(OrderStatus.IN_PREPARAZIONE);
                } else {
                    pIP.setToPrepare(false);
                    pIP.setLocation(location);
                }
                if(!roles.contains("admin")){
                    TwentyfiveMessage twentyfiveMessage = StompUtilities.sendAdminMoveNotification(order.getId(),location);
                    stompClientController.sendObjectMessage(twentyfiveMessage);
                    order.setUnread(true);
                    order.setCreatedDate(LocalDateTime.now());
                }
                if (!roles.contains("baker")){
                    if(alreadySomeToPrepare){
                        if(location.equals("In pasticceria")){
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
            }
            boolean noMoreToPrepare = order.getProductsInPurchase().stream()
                    .allMatch(product -> product.getLocation() != null && !"In pasticceria".equals(product.getLocation())) &&
                    order.getBundlesInPurchase().stream()
                            .allMatch(bundle -> bundle.getLocation() != null && !"In pasticceria".equals(bundle.getLocation()));
            if (noMoreToPrepare){
                order.setStatus(OrderStatus.PRONTO);
                CustomerAPA customer = getCustomerFromOrder(order);
                emailService.sendEmail(customer.getEmail(),OrderStatus.PRONTO,TemplateUtilities.populateEmail(customer.getFirstName(),order.getId()));
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
        }
        // Se CustomInfo è null, prendi il customer usando l'idCustomer
        return customerRepository.findById(order.getCustomerId())
                .orElse(null);
    }

}

