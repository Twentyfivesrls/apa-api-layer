package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedOrderRepository extends MongoRepository<CompletedOrderAPA,String> {
    List<OrderAPA> findByCustomerId(String customerId);
    Page<OrderAPA> findOrdersByCustomerId(String customerId, Pageable pageable);
}
