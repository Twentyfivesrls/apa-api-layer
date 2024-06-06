package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStatRepository extends MongoRepository<ProductStatAPA,String> {
}
