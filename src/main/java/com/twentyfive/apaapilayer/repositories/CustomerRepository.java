package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<CustomerAPA,String> {
}
