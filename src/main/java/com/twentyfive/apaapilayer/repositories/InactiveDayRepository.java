package com.twentyfive.apaapilayer.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.InactiveDay;

@Repository
public interface InactiveDayRepository extends MongoRepository<InactiveDay,String> {
}
