package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.*;
import com.twentyfive.apaapilayer.configurations.ProducerPool;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.InvalidCategoryException;
import com.twentyfive.apaapilayer.exceptions.InvalidCustomerIdException;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.StompUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final String NOTIFICATION_TOPIC="twentyfive_internal_notifications";


    private final CustomerRepository customerRepository;

    private final CompletedOrderRepository completedOrderRepository;

    private final ActiveOrderService orderService;
    private final EmailService emailService;
    private final KeycloakService keycloakService;

    private final SettingRepository settingRepository;

    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;


    private final TimeSlotAPARepository timeSlotAPARepository;

    private final CategoryRepository categoryRepository;

    private final TrayRepository trayRepository;
    private final ProducerPool producerPool;
    private final ActiveOrderRepository activeOrdersRepository;

    public CustomerDetailsDTO getCustomerDetailsByIdKeycloak(String idKeycloak) {
        CustomerAPA customer = customerRepository.findByIdKeycloak(idKeycloak)
                .orElseThrow(() -> new RuntimeException("No customer found with idKeycloak: " + idKeycloak));

        List<OrderAPA> completedOrders = completedOrderRepository.findByCustomerId(customer.getId());

        // Calcola il totale speso e il numero di ordini
        String totalSpent = String.format("%.2f", completedOrders.stream()
                .mapToDouble(OrderAPA::getTotalPrice)
                .sum());
        String completedOrdersCount = String.valueOf(completedOrders.size());

        List<OrderAPA> activeOrders = activeOrdersRepository.findByCustomerId(customer.getId());

        String activeOrdersCount = String.valueOf(activeOrders.size());


        return new CustomerDetailsDTO(
                customer.getId(),
                customer.getLastName(),
                customer.getFirstName(),
                customer.getIdKeycloak(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                completedOrdersCount,
                activeOrdersCount,
                totalSpent,
                customer.isEnabled(),
                customer.getNote()
        );
    }

    @Transactional
    public void modifyCustomerInfo(String customerId, String firstName, String lastName, String phoneNumber){

        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhoneNumber(phoneNumber);

        customerRepository.save(customer);
        System.out.println("ok");
        keycloakService.update(customer);

    }


    @Autowired
    public CustomerService(ActiveOrderRepository activeOrderRepository, CustomerRepository customerRepository, ActiveOrderService activeOrderService, CompletedOrderRepository completedOrderRepository, EmailService emailService, KeycloakService keycloakService, SettingRepository settingRepository, ProductKgRepository productKgRepository, ProductWeightedRepository productWeightedRepository, TimeSlotAPARepository timeSlotAPARepository, CategoryRepository categoryRepository, TrayRepository trayRepository, ProducerPool producerPool) {
        this.customerRepository = customerRepository;
        this.orderService = activeOrderService;
        this.completedOrderRepository=completedOrderRepository;
        this.emailService = emailService;
        this.keycloakService = keycloakService;
        this.settingRepository=settingRepository;
        this.productKgRepository = productKgRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.timeSlotAPARepository = timeSlotAPARepository;
        this.categoryRepository = categoryRepository;
        this.trayRepository = trayRepository;
        this.producerPool = producerPool;
        this.activeOrdersRepository= activeOrderRepository;
    }

    public Page<CustomerAPA> getAll(int page, int size,String sortColumn,String sortDirection) {
        Pageable pageable;
        if(!(sortDirection.isBlank() || sortColumn.isBlank())) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            pageable=PageRequest.of(page,size,sort);
            return customerRepository.findAll(pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"lastName");
        pageable=PageRequest.of(page,size,sort);
        return customerRepository.findAll(pageable);
    }

    public CustomerDetailsDTO getById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());


        List<OrderAPA> comletedOrders = completedOrderRepository.findByCustomerId(customerId);

        // Calcola il totale speso e il numero di ordini
        String totalSpent = String.format("%.2f", comletedOrders.stream()
                .mapToDouble(OrderAPA::getTotalPrice)
                .sum());
        String completedOrdersCount = String.valueOf(comletedOrders.size());

        List<OrderAPA> activeOrders = activeOrdersRepository.findByCustomerId(customerId);

        String activeOrdersCount = String.valueOf(activeOrders.size());


        return new CustomerDetailsDTO(
                customer.getId(),
                customer.getLastName(),
                customer.getFirstName(),
                customer.getIdKeycloak(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                completedOrdersCount,
                activeOrdersCount,
                totalSpent,
                customer.isEnabled(),
                customer.getNote()
            );


    }

    public CustomerAPA register(CustomerAPA customerAPA) {
        return customerRepository.save(customerAPA);
    }
    public CustomerAPA saveCustomer(CustomerAPA customer) throws IOException {
        if(customer.getIdKeycloak()!=null){
            keycloakService.update(customer);
            return customerRepository.save(customer);
        } else {
            keycloakService.add(customer);
            CustomerAPA newCustomer = customerRepository.save(customer);
            keycloakService.sendPasswordResetEmail(newCustomer.getIdKeycloak());
            return newCustomer;
        }
        // Salva il nuovo cliente nel database o gli faccio update
    }

    public boolean changeStatusById(String id) {
        Optional<CustomerAPA> customerAPA = customerRepository.findById(id);
        // Verifica che il cliente esista prima di tentare di eliminarlo
        if (customerAPA.isPresent()) {
            customerAPA.get().setEnabled(!customerAPA.get().isEnabled());
            keycloakService.update(customerAPA.get());
            customerRepository.save(customerAPA.get());
        } else {
            // Se il cliente non esiste, restituisci false indicando che non c'era nulla da eliminare
            throw new InvalidCustomerIdException();
        }
        return true;
    }



    @Transactional
    public boolean buyItems(String customerId, List<Integer> positionIds, LocalDateTime selectedPickupDateTime) throws IOException {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
        if (customer.getCart()==null) customer.setCart(new Cart());

        Cart cart = customer.getCart();
        List<ItemInPurchase> selectedItems = cart.getItemsAtPositions(positionIds);

        for(Integer positionId: positionIds)
            if (positionId < 0 || positionId >= cart.getPurchases().size()) {
                throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
            }

        if (!selectedItems.isEmpty()) {
            OrderAPA order = createOrderFromItems(customer, selectedItems, selectedPickupDateTime);
            if(timeSlotAPA.reserveTimeSlots(selectedPickupDateTime,countSlotRequired(selectedItems))) {
                orderService.createOrder(order);
                cart.removeItemsAtPositions(positionIds); // Rimuovi gli articoli dal carrello
            }
            timeSlotAPARepository.save(timeSlotAPA);
            customerRepository.save(customer);
            String in= StompUtilities.sendNewOrderNotification();
            producerPool.send(in,1,NOTIFICATION_TOPIC);
            emailService.sendEmailReceived(customer.getEmail());
            return true;
        }
        return false;
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





    private OrderAPA createOrderFromItems(CustomerAPA customer, List<ItemInPurchase> items, LocalDateTime selectedPickupDateTime) {
        OrderAPA order = new OrderAPA();
        order.setCustomerId(customer.getId());
        order.setPickupDate(selectedPickupDateTime.toLocalDate());
        order.setPickupTime(selectedPickupDateTime.toLocalTime());
        order.setStatus(OrderStatus.RICEVUTO);

        List<ProductInPurchase> products = new ArrayList<>();
        List<BundleInPurchase> bundles = new ArrayList<>();

        for (ItemInPurchase item : items) {
            if (item instanceof ProductInPurchase) {
                products.add((ProductInPurchase) item);
                Optional<ProductKgAPA> productKGAPA =productKgRepository.findById(item.getId());
                if (productKGAPA.isPresent()){
                    productKGAPA.get().setBuyingCount(productKGAPA.get().getBuyingCount()+1);
                    productKgRepository.save(productKGAPA.get());
                }
            } else if (item instanceof BundleInPurchase) {
                bundles.add((BundleInPurchase) item);
                for (PieceInPurchase piece : ((BundleInPurchase) item).getWeightedProducts()){
                    Optional<ProductWeightedAPA> productWeightedAPA = productWeightedRepository.findById(piece.getId());
                    if (productWeightedAPA.isPresent()){
                        productWeightedAPA.get().setBuyingCount(productWeightedAPA.get().getBuyingCount()+1);
                        productWeightedRepository.save(productWeightedAPA.get());
                    }
                }
            }
        }

        order.setProductsInPurchase(products);
        order.setBundlesInPurchase(bundles);
        order.setTotalPrice(calculateTotalPrice(items));

        return order;
    }

    private double calculateTotalPrice(List<ItemInPurchase> items) {
        double total = 0;
        for (ItemInPurchase item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public CartDTO getCartById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) {
            customer.setCart(new Cart());
        }
        return convertCartToDTO(customer);
    }


    @Transactional
    public CartDTO removeFromCart(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());
        else {
            Cart cart=customer.getCart();
            cart.removeItemsAtPositions(positions);
            customerRepository.save(customer);
            return convertCartToDTO(customer);
        }
        throw new IllegalStateException("No cart available for this customer or invalid positions");
    }




    @Transactional
    public boolean clearCart(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart= customer.getCart();
        if(cart==null)
            customer.setCart(new Cart());
        else {
            customer.getCart().clearCart();
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    @Transactional
    public CartDTO addToCartProduct(String customerId, ProductInPurchase product) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();  // Assumi che Cart abbia un costruttore che inizializza le liste
            customer.setCart(cart);
        }
        System.out.println(cart);
        cart.getPurchases().add(product);
        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    @Transactional
    public CartDTO addToCartBundle(String customerId, BundleInPurchase bundle) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();  // Assumi che Cart abbia un costruttore che inizializza le liste
            customer.setCart(cart);
        }
        cart.getPurchases().add(bundle);
        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    /*private LocalDateTime next(int hour){
        LocalDateTime now= LocalDateTime.now();
        // Definisce il mezzogiorno
        LocalTime noon = LocalTime.of(hour, 0);

        // Se ora è già passato il mezzogiorno, passa al giorno successivo
        if (now.toLocalTime().isAfter(noon)) {
            return now.toLocalDate().plusDays(1).atTime(noon);
        } else {
            // Altrimenti, restituisce il mezzogiorno di oggi
            return now.toLocalDate().atTime(noon);
        }
    }*/



    public Map<LocalDate, List<LocalTime>> getAvailablePickupTimes(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();  // Assumi che Cart abbia un costruttore che inizializza le liste
            customer.setCart(cart);
            customerRepository.save(customer);
            return new TreeMap<>();
        }

        Integer minDelay = settingRepository.findAll().get(0).getMinOrderDelay();
        LocalTime startTime = settingRepository.findAll().get(0).getBusinessHours().getStartTime();
        LocalTime endTime = settingRepository.findAll().get(0).getBusinessHours().getEndTime();
        boolean bigSemifreddo = false;

        List<ItemInPurchase> items = cart.getItemsAtPositions(positions);
        int numSlotRequired = 0;
        boolean somethingCustomized = false;

        for (ItemInPurchase item : items) {
            if (item instanceof ProductInPurchase) {
                ProductInPurchase pip = (ProductInPurchase) item;
                ProductKgAPA product = productKgRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                if (product.isCustomized()) {
                    numSlotRequired += pip.getQuantity();
                    somethingCustomized = true;
                }
                if (categoryRepository.findById(product.getCategoryId()).orElseThrow(InvalidCategoryException::new).getName().equals("Semifreddo") && pip.getWeight() >= 1.5) {
                    bigSemifreddo = true;
                }
            } else if (item instanceof BundleInPurchase) {
                BundleInPurchase pip = (BundleInPurchase) item;
                if (trayRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new).isCustomized()) {
                    numSlotRequired += pip.getQuantity();
                    somethingCustomized = true;
                }
            }
        }

        if (bigSemifreddo) {
            minDelay = 48;
        }

        LocalTime now = LocalTime.now();
        LocalDateTime minStartingDate;

        if (!now.isBefore(startTime) && now.isBefore(endTime)) {
            minStartingDate = somethingCustomized ? next(8).plusHours(minDelay) : LocalDateTime.now().plusHours(minDelay);
        } else {
            minStartingDate = somethingCustomized ? next(12).plusHours(minDelay) : next(8).plusHours(minDelay);
        }

        Map<LocalDate, List<LocalTime>> availableTimes = timeSlotAPARepository.findAll().get(0).findTimeForNumSlots(minStartingDate, numSlotRequired);

        // Se non ci sono tempi di ritiro disponibili, restituisci una mappa vuota
        if (availableTimes.isEmpty()) {
            return new TreeMap<>();
        }

        // Restituisci una TreeMap ordinata per le chiavi (LocalDate)
        return new TreeMap<>(availableTimes);
    }


    // Metodo di utilità per ottenere il prossimo orario di inizio lavorativo
    private LocalDateTime next(int hour) {
        LocalDateTime next = LocalDateTime.now().withHour(hour).withMinute(0).withSecond(0).withNano(0);
        if (LocalDateTime.now().isAfter(next)) {
            next = next.plusDays(1);
        }
        return next;
    }


    private CartDTO convertCartToDTO(Customer customer){
        CartDTO cartDTO = new CartDTO();;
        cartDTO.setCustomerId(customer.getId());
        Double totalPrice=0.0;
        for (ItemInPurchase itemInPurchase : customer.getCart().getPurchases()){
            if (itemInPurchase instanceof BundleInPurchase){
                BundleInPurchaseDTO bundleInPurchaseDTO=convertBundlePurchaseToDTO((BundleInPurchase) itemInPurchase);
                cartDTO.getPurchases().add(bundleInPurchaseDTO);
                totalPrice+=bundleInPurchaseDTO.getTotalPrice();

            }
            if (itemInPurchase instanceof ProductInPurchase){
                ProductInPurchaseDTO productInPurchaseDTO =convertProductPurchaseToDTO((ProductInPurchase) itemInPurchase);
                cartDTO.getPurchases().add(productInPurchaseDTO);
                totalPrice+=productInPurchaseDTO.getTotalPrice();
            }
        }
        cartDTO.setTotalPrice(totalPrice);
        return cartDTO;
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




}
