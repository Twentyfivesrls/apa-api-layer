package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.OrderSlotAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSlotRepository extends MongoRepository<OrderSlotAPA,String> {
}
