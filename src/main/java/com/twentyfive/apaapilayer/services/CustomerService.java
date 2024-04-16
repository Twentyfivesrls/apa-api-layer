package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.CustomerDTO;
import com.twentyfive.apaapilayer.DTOs.OrderAPADTO;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;


    public CustomerService(CustomerRepository customerRepository,OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public Page<CustomerAPA> getAll(int page, int size) {
        return customerRepository.findAll(PageRequest.of(page, size));
    }

    public CustomerDTO getById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            List<OrderAPA> orders = orderRepository.findByCustomerId(customerId);

            // Converti ordini in OrderAPADTO includendo i dettagli del cliente
            List<OrderAPADTO> orderDTOs = orders.stream()
                    .map(order -> convertToOrderAPADTO(order, customer))
                    .collect(Collectors.toList());

            // Calcola il totale speso e il numero di ordini
            String totalSpent = String.format("%.2f", orders.stream()
                    .mapToDouble(OrderAPA::getTotalPrice)
                    .sum());
            String orderCount = String.valueOf(orders.size());

            return new CustomerDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.getSurname(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    orderCount,
                    totalSpent,
                    orderDTOs
            );
        }
        return null;
    }

    public OrderAPADTO convertToOrderAPADTO(OrderAPA orderAPA,CustomerAPA customerAPA) {
        if (orderAPA == null) {
            return null;
        }
        // Mappa i campi dal modello dell'ordine al DTO dell'ordine
        OrderAPADTO orderAPADTO = new OrderAPADTO();
        orderAPADTO.setId(orderAPA.getId());
        orderAPADTO.setFirstName(customerAPA.getName());
        orderAPADTO.setLastName(customerAPA.getSurname());
        orderAPADTO.setPickupDate(orderAPA.getPickupDate());
        orderAPADTO.setPickupTime(orderAPA.getPickupTime());
        orderAPADTO.setPrice(String.format("%.2f", orderAPA.getTotalPrice())); // Assumendo che totalPrice sia un double
        orderAPADTO.setStatus(orderAPA.getStatus().toString()); // Assumendo che status sia un enum o qualcosa che può essere convertito in stringa
        orderAPADTO.setProducts(orderAPA.getProductsInPurchase());
        orderAPADTO.setEmail(customerAPA.getEmail());
        orderAPADTO.setPhoneNumber(customerAPA.getPhoneNumber());

        return orderAPADTO;
    }

}
