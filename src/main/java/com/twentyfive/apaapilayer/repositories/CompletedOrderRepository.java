package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedOrderRepository extends MongoRepository<CompletedOrderAPA,String> {

    List<CompletedOrderAPA> findAllByOrderByCreatedDateDesc();
    List<CompletedOrderAPA> findByCustomerIdOrderByCreatedDateDesc(String customerId);
    Page<CompletedOrderAPA> findOrdersByCustomerIdOrderByCreatedDateDesc(String customerId, Pageable pageable);
}
