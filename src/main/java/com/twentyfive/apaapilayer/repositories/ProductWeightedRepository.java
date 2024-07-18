package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductWeightedRepository extends MongoRepository<ProductWeightedAPA,String> {

    List<ProductWeightedAPA> findAllByIngredientIdsContainingAndWeightNotNull(String ingredientId);
    List<ProductWeightedAPA> findAllByCategoryId(String categoryId);
    List<ProductWeightedAPA> findAllByCategoryIdAndSoftDeletedFalse(String categoryId);
    List<ProductWeightedAPA> findAllByCategoryIdAndSoftDeletedFalseAndNameContainsIgnoreCase(String categoryId, String name);

}
