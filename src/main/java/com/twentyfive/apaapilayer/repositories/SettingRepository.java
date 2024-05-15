package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.SettingAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends MongoRepository<SettingAPA,String> {
    boolean existsOrderReceivedAlertById(String id);
}
