package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.TimeSlotAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotAPARepository extends MongoRepository<TimeSlotAPA,String> {
}
