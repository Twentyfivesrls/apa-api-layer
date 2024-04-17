package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.CustomerDetailsDTO;
import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.BundleInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Cart;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.ProductInPurchase;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    private final OrderService orderService;


    @Autowired
    public CustomerService(CustomerRepository customerRepository,OrderRepository orderRepository,OrderService orderService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    public Page<CustomerAPA> getAll(int page, int size) {
        return customerRepository.findAll(PageRequest.of(page, size));
    }

    public CustomerDetailsDTO getById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            List<OrderAPA> orders = orderRepository.findByCustomerId(customerId);

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
                    totalSpent
            );
        }
        return null;
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
            return false;
        }
    }

    @Transactional
    public boolean buySingleItem(String customerId, int positionId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getCart() == null) {
            return false;
        }

        Cart cart = customer.getCart();
        List<ProductInPurchase> products = cart.getProductsByWeight();
        List<BundleInPurchase> bundles = cart.getBundles();

        if (positionId < products.size()) {
            // L'indice si riferisce a un ProductInPurchase
            ProductInPurchase product = products.remove(positionId);  // Rimuove il prodotto dal carrello
            if (product != null) {
                OrderAPA order = createOrderFromProducts(customer, List.of(product), new ArrayList<>());
                orderService.createOrder(order);
                customerRepository.save(customer);
                return true;
            }
        } else {
            // L'indice si riferisce a un BundleInPurchase
            int bundleIndex = positionId - products.size();
            if (bundleIndex < bundles.size()) {
                BundleInPurchase bundle = bundles.remove(bundleIndex);  // Rimuove il bundle dal carrello
                if (bundle != null) {

                    OrderAPA order = createOrderFromProducts(customer, new ArrayList<>(),List.of(bundle));
                    orderService.createOrder(order);
                    customerRepository.save(customer);
                    return true;
                }
            }
        }
        return false;
    }


    @Transactional
    public boolean buyMultipleItems(String customerId, List<Integer> positionIds) {
        CustomerAPA customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getCart() == null) {
            return false;
        }

        List<ProductInPurchase> selectedProducts = new ArrayList<>();
        List<BundleInPurchase> selectedBundles = new ArrayList<>();
        Cart cart = customer.getCart();

        for (int positionId : positionIds) {
            if (positionId < cart.getProductsByWeight().size()) {
                selectedProducts.add(cart.getProductsByWeight().remove(positionId));
            } else {
                int bundleIndex = positionId - cart.getProductsByWeight().size();
                if (bundleIndex < cart.getBundles().size()) {
                    selectedBundles.add(cart.getBundles().remove(bundleIndex));
                }
            }
        }

        if (!selectedProducts.isEmpty() || !selectedBundles.isEmpty()) {
            OrderAPA order = createOrderFromProducts(customer, selectedProducts, selectedBundles);
            orderService.createOrder(order);
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean buyAllItems(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getCart() == null) {
            return false;
        }

        List<ProductInPurchase> allProducts = new ArrayList<>(customer.getCart().getProductsByWeight());
        List<BundleInPurchase> allBundles = new ArrayList<>(customer.getCart().getBundles());

        customer.getCart().getProductsByWeight().clear();
        customer.getCart().getBundles().clear();

        if (!allProducts.isEmpty() || !allBundles.isEmpty()) {
            OrderAPA order = createOrderFromProducts(customer, allProducts, allBundles);
            orderService.createOrder(order);
            customerRepository.save(customer);
            return true;
        }
        return false;
    }


    private OrderAPA createOrderFromProducts(CustomerAPA customer, List<ProductInPurchase> products, List<BundleInPurchase> bundles) {
        OrderAPA order = new OrderAPA();
        order.setCustomerId(customer.getId());
        order.setProductsInPurchase(products);
        order.setBundlesInPurchase(bundles);
        //order.setPickupDate(LocalDate.now());
        //order.setPickupTime(LocalTime.now());
        order.setTotalPrice(calculateTotalPrice(products, bundles));
        order.setStatus(OrderStatus.RICEVUTO);
        return order;
    }

    private double calculateTotalPrice(List<ProductInPurchase> products, List<BundleInPurchase> bundles) {
        double total = 0;
        if (products != null) {
            total += products.stream().mapToDouble(ProductInPurchase::getTotalPrice).sum();
        }
        if (bundles != null) {
            total += bundles.stream().mapToDouble(BundleInPurchase::getTotalPrice).sum();
        }
        return total;
    }



}