package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedOrderRepository extends MongoRepository<CompletedOrderAPA,String> {

    Page<OrderAPA> findOrdersByCustomerId(String customerId, Pageable pageable);
}
