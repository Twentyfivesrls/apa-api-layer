package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveOrderRepository extends MongoRepository<OrderAPA,String> {
    List<OrderAPA> findAllByOrderByCreatedDateDesc();
    List<OrderAPA> findByCustomerId(String customerId);
    Page<OrderAPA> findOrdersByCustomerIdOrderByCreatedDateDesc(String customerId, Pageable pageable);
}
