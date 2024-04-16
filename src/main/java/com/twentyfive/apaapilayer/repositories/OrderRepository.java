package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<OrderAPA,String> {
}
