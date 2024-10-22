package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductFixedAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFixedRepository extends MongoRepository<ProductFixedAPA, String> {

    List<ProductFixedAPA> findAllByCategoryIdAndSoftDeletedFalse(String categoryId);
}
