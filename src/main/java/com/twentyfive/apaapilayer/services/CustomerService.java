package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.CartDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.InvalidCategoryException;
import com.twentyfive.apaapilayer.exceptions.InvalidCustomerIdException;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Cart;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ItemInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CompletedOrderRepository completedOrderRepository;

    private final ActiveOrderService orderService;
    private final EmailService emailService;
    private final KeycloakService keycloakService;

    private final SettingRepository settingRepository;

    private final ProductKgRepository productKgRepository;


    private final TimeSlotAPARepository timeSlotAPARepository;

    private final CategoryRepository categoryRepository;

    private final TrayRepository trayRepository;


    @Autowired
    public CustomerService(CustomerRepository customerRepository, ActiveOrderService activeOrderService, CompletedOrderRepository completedOrderRepository, EmailService emailService, KeycloakService keycloakService, SettingRepository settingRepository, ProductKgRepository productKgRepository, TimeSlotAPARepository timeSlotAPARepository, CategoryRepository categoryRepository, TrayRepository trayRepository) {
        this.customerRepository = customerRepository;
        this.orderService = activeOrderService;
        this.completedOrderRepository=completedOrderRepository;
        this.emailService = emailService;
        this.keycloakService = keycloakService;
        this.settingRepository=settingRepository;
        this.productKgRepository = productKgRepository;
        this.timeSlotAPARepository = timeSlotAPARepository;
        this.categoryRepository = categoryRepository;
        this.trayRepository = trayRepository;
    }

    public Page<CustomerAPA> getAll(int page, int size) {
        return customerRepository.findAll(PageRequest.of(page, size));
    }

    public CustomerDetailsDTO getById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());


        List<OrderAPA> orders = completedOrderRepository.findByCustomerId(customerId);

        // Calcola il totale speso e il numero di ordini
        String totalSpent = String.format("%.2f", orders.stream()
                .mapToDouble(OrderAPA::getTotalPrice)
                .sum());
        String orderCount = String.valueOf(orders.size());

        return new CustomerDetailsDTO(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                orderCount,
                totalSpent,
                customer.isEnabled(),
                customer.getNote()
            );


    }

    public CustomerAPA saveCustomer(CustomerAPA customer) {
        if(customer.getIdKeycloak()!=null){
            keycloakService.update(customer);
        } else {
            keycloakService.add(customer);
        }
        // Salva il nuovo cliente nel database o gli faccio update
        return customerRepository.save(customer);
    }

    public boolean changeStatusById(String id) {
        Optional<CustomerAPA> customerAPA = customerRepository.findById(id);
        // Verifica che il cliente esista prima di tentare di eliminarlo
        if (customerAPA.isPresent()) {
            customerAPA.get().setEnabled(!customerAPA.get().isEnabled());
        } else {
            // Se il cliente non esiste, restituisci false indicando che non c'era nulla da eliminare
            throw new InvalidCustomerIdException();
        }
        return true;
    }



    @Transactional
    public boolean buyItems(String customerId, List<Integer> positionIds, LocalDateTime selectedPickupDateTime) throws IOException {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());

        Cart cart = customer.getCart();
        List<ItemInPurchase> selectedItems = cart.getItemsAtPositions(positionIds);

        for(Integer positionId: positionIds)
            if (positionId < 0 || positionId >= selectedItems.size()) {
                throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
            }

        if (!selectedItems.isEmpty()) {
            OrderAPA order = createOrderFromItems(customer, selectedItems, selectedPickupDateTime);
            if(timeSlotAPARepository.findAll().get(0).reserveTimeSlots(selectedPickupDateTime,countSlotRequired(selectedItems))) {
                orderService.createOrder(order);
                cart.removeItemsAtPositions(positionIds); // Rimuovi gli articoli dal carrello
            }
            customerRepository.save(customer);
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
                ProductKgAPA product = productKgRepository.findById(pip.getItemId()).orElseThrow(InvalidItemException::new);
                if (product.isCustomized()) {
                    numSlotRequired += pip.getQuantity();

                }
                if (categoryRepository.findById(product.getId()).orElseThrow(InvalidCategoryException::new).getName().equals("Semifreddo")) {
                    double weight = pip.getWeight();
                }


            } else if (item instanceof BundleInPurchase) {
                BundleInPurchase pip = (BundleInPurchase) item;
                Tray tray = trayRepository.findById(pip.getItemId()).orElseThrow(InvalidItemException::new);
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
            } else if (item instanceof BundleInPurchase) {
                bundles.add((BundleInPurchase) item);
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
        if (customer.getCart()==null) customer.setCart(new Cart());

        return new CartDTO(customer);
    }


    @Transactional
    public CartDTO removeFromCart(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());
        else {
            Cart cart=customer.getCart();
            cart.removeItemsAtPositions(positions);
            customerRepository.save(customer);
            return new CartDTO(customer);
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
        return new CartDTO(customer);
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
        return new CartDTO(customer);
    }

    private LocalDateTime next(int hour){
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
    }



    public Map<LocalDate, List<LocalTime>> getAvailablePickupTimes(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();  // Assumi che Cart abbia un costruttore che inizializza le liste
            customer.setCart(cart);
            customerRepository.save(customer);
            return new TreeMap<>();
        }
        else{
            Integer minDelay= settingRepository.findAll().get(0).getMinOrderDelay();

            List<ItemInPurchase> items = cart.getItemsAtPositions(positions);
            int numSlotRequired=0;
            boolean somethingCustomized=false;
            boolean bigSemifreddo=false;


            for (ItemInPurchase item: items){

                if(item instanceof ProductInPurchase){
                    ProductInPurchase pip=(ProductInPurchase) item;
                    ProductKgAPA product= productKgRepository.findById(pip.getItemId()).orElseThrow(InvalidItemException::new);
                    if(product.isCustomized()){
                        numSlotRequired+=pip.getQuantity();
                        somethingCustomized=true;

                    }
                    if(categoryRepository.findById(product.getId()).orElseThrow(InvalidCategoryException::new).getName().equals("Semifreddo")){
                        double weight= pip.getWeight();
                        if(weight>=1.5)bigSemifreddo=true;
                    }


                }else if (item instanceof BundleInPurchase){
                    BundleInPurchase pip =(BundleInPurchase) item;
                    Tray tray= trayRepository.findById(pip.getItemId()).orElseThrow(InvalidItemException::new);
                    if(tray.isCustomized()){
                        numSlotRequired+=pip.getQuantity();
                        somethingCustomized=true;
                    }

                }

                LocalTime now=LocalTime.now();
                LocalTime startTime = settingRepository.findAll().get(0).getBusinessHours().getStartTime();;
                LocalTime endTime = settingRepository.findAll().get(0).getBusinessHours().getEndTime();
                LocalDateTime minStartingDate;

                if (bigSemifreddo)minDelay=48;

                if(!now.isBefore(startTime) && now.isBefore(endTime)){//la richiesta è fatta in orario lavorativo
                    if(!somethingCustomized)
                        minStartingDate= LocalDateTime.now().plusHours(minDelay);
                    else
                        minStartingDate=next(8).plusHours(minDelay);
                }
                else{
                    if(!somethingCustomized)
                        minStartingDate= next(8).plusHours(minDelay);
                    else
                        minStartingDate= next(12).plusHours(minDelay);

                }


                return timeSlotAPARepository.findAll().get(0).findTimeForNumSlots(minStartingDate,numSlotRequired);



            }
            return new TreeMap<>();


        }





    }









}
