package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductKgAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductKgRepository extends MongoRepository<ProductKgAPA,String> {
}
