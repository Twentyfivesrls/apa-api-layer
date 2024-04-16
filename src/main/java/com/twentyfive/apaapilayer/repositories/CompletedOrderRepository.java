package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CompletedOrderAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedOrderRepository extends MongoRepository<CompletedOrderAPA,String> {
}
