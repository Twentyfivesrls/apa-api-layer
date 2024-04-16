package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductWeightedRepository extends MongoRepository<ProductWeightedAPA,String> {
}
