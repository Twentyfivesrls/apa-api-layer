package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.Tray;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrayRepository extends MongoRepository<Tray, String> {
    List<Tray> findAllByCategoryIdAndCustomizedFalse(String idCategory);

    List<Tray> findAllByCategoryIdAndCustomizedFalseAndActiveTrue(String idCategory);
}
