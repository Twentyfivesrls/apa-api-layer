package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductKgAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductKgRepository extends MongoRepository<ProductKgAPA,String> {

    List<ProductKgAPA> findAllByIngredientIdsContainingAndPricePerKgNotNull(String ingredientId);

    List<ProductKgAPA> findAllByCategoryIdAndSoftDeletedFalse(String categoryId);
    List<ProductKgAPA> findAllByCategoryId(String categoryId);

    List<ProductKgAPA> findAllByCategoryIdAndActiveTrueAndCustomizedFalseAndSoftDeletedFalse(String categoryId);

}
