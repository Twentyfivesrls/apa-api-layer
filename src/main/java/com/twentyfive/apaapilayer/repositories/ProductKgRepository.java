package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductKgAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductKgRepository extends MongoRepository<ProductKgAPA,String> {

    List<ProductKgAPA> findAllByIngredientIdsContaining(String ingredientId);

    List<ProductKgAPA> findAllByCategoryId(String categoryId);

    List<ProductKgAPA> findAllByCategoryIdAndActiveTrueAndCustomizedFalseOrderByNameAsc(String categoryId);
}
