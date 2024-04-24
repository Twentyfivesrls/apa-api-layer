package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.CartDTO;
import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.exceptions.InvalidCustomerIdException;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.repositories.CompletedOrderRepository;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.repositories.ActiveOrderRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ActiveOrderRepository activeOrderRepository;

    private final CompletedOrderRepository completedOrderRepository;

    private final ActiveOrderService orderService;


    @Autowired
    public CustomerService(CustomerRepository customerRepository, ActiveOrderRepository activeOrderRepository, ActiveOrderService activeOrderService,CompletedOrderRepository completedOrderRepository) {
        this.customerRepository = customerRepository;
        this.activeOrderRepository = activeOrderRepository;
        this.orderService = activeOrderService;
        this.completedOrderRepository=completedOrderRepository;
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
        // Verifica se il cliente già esiste
        if (customer.getId() != null && customerRepository.existsById(customer.getId())) {
            throw new IllegalStateException("Customer already exists with id: " + customer.getId());
        }
        // Salva il nuovo cliente nel database
        return customerRepository.save(customer);
    }

    public CustomerAPA updateCustomer(CustomerAPA customer) {
        // Verifica se il cliente esiste
        if (customer.getId() == null || !customerRepository.existsById(customer.getId())) {
            throw new IllegalStateException("Cannot update non-existing customer with id: " + customer.getId());
        }
        // Aggiorna il cliente esistente nel database
        return customerRepository.save(customer);
    }

    public boolean deleteCustomerById(String id) {
        // Verifica che il cliente esista prima di tentare di eliminarlo
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            // Dopo la cancellazione, verifica che il cliente sia stato effettivamente rimosso
            boolean stillExists = customerRepository.existsById(id);
            return !stillExists;
        } else {
            // Se il cliente non esiste, restituisci false indicando che non c'era nulla da eliminare
            throw new InvalidCustomerIdException();
        }
    }

    @Transactional
    public boolean buySingleItem(String customerId, int positionId, LocalDateTime selectedPickupDateTime) {

        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());

        Cart cart = customer.getCart();
        List<ItemInPurchase> items = cart.getPurchases();

        if (positionId < 0 || positionId >= items.size()) {
            throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
        }

        ItemInPurchase item = items.remove(positionId);
        if (item == null) {
            return false;
        }

        OrderAPA order = createOrderFromItems(customer, Collections.singletonList(item), selectedPickupDateTime);

        orderService.createOrder(order);
        customerRepository.save(customer);//salvo il nuovo carrello, tolto l'elemento comprato
        return true;
    }

    @Transactional
    public boolean buyMultipleItems(String customerId, List<Integer> positionIds, LocalDateTime selectedPickupDateTime) {
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
            orderService.createOrder(order);
            cart.removeItemsAtPositions(positionIds); // Rimuovi gli articoli dal carrello
            customerRepository.save(customer);
            return true;
        }
        return false;
    }


    @Transactional
    public boolean buyAllItems(String customerId, LocalDateTime selectedPickupDateTime) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());


        Cart cart = customer.getCart();

        if (cart == null || cart.getPurchases().isEmpty()) {
            return false;
        }

        List<ItemInPurchase> allItems = new ArrayList<>(cart.getPurchases());
        cart.clearCart(); // Rimuove tutti gli articoli dal carrello

        OrderAPA order = createOrderFromItems(customer, allItems, selectedPickupDateTime);
        orderService.createOrder(order);
        customerRepository.save(customer);
        return true;
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



    public Map<LocalDate, List<LocalTime>> getAvailablePickupTimes(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        if (cart == null) {
            throw new IllegalStateException("Customer does not have a cart.");
        }

        List<ItemInPurchase> items = cart.getItemsAtPositions(positions);
        List<Map<LocalDate, List<LocalTime>>> allItemsTimes = new ArrayList<>();

        // Map phase: Retrieve available times for each item
        for (ItemInPurchase item : items) {
            Map<LocalDate, List<LocalTime>> availableTimes = calculateAvailableTimes(item);
            allItemsTimes.add(availableTimes);
        }

        // Reduce phase: Intersect all times
        Map<LocalDate, List<LocalTime>> commonAvailableTimes = new HashMap<>();
        if (!allItemsTimes.isEmpty()) {
            commonAvailableTimes.putAll(allItemsTimes.get(0)); // Start with the first item's times

            // Intersect with the rest
            for (Map<LocalDate, List<LocalTime>> itemTimes : allItemsTimes.subList(1, allItemsTimes.size())) {
                commonAvailableTimes.keySet().retainAll(itemTimes.keySet()); // Keep only dates present in both maps
                for (LocalDate date : commonAvailableTimes.keySet()) {
                    commonAvailableTimes.get(date).retainAll(itemTimes.getOrDefault(date, Collections.emptyList())); // Intersect the times
                }
            }
        }

        return commonAvailableTimes;
    }

    private Map<LocalDate, List<LocalTime>> calculateAvailableTimes(ItemInPurchase item) {
        Map<LocalDate, List<LocalTime>> availableTimes = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalTime[] possibleTimes = { LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(15, 0), LocalTime.of(18, 0) }; // Possible pickup times

        for (int i = 0; i < 30; i++) { // Simulate availability for the next 30 days
            LocalDate date = today.plusDays(i);
            availableTimes.put(date, new ArrayList<>(List.of(possibleTimes))); // Assume all times are available
        }

        return availableTimes;
    }









}
