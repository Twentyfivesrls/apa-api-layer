package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.util.List;

@Repository
public interface ActiveOrderRepository extends MongoRepository<OrderAPA,String> {
    List<OrderAPA> findAllByOrderByCreatedDateDesc();
    List<OrderAPA> findAllByStatusOrderByCreatedDateDesc(OrderStatus status);
    List<OrderAPA> findByCustomerId(String customerId);
    Page<OrderAPA> findOrdersByCustomerIdOrderByCreatedDateDesc(String customerId, Pageable pageable);

    // Trova tutti gli ordini che hanno almeno un prodotto o bundle con toPrepare = true
    @Query("{$or: [{'productsInPurchase.toPrepare': true}, {'bundlesInPurchase.toPrepare': true}]}")
    List<OrderAPA> findOrdersWithItemsToPrepareByOrderByCreatedDateDesc();
}
