package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductWeightedRepository extends MongoRepository<ProductWeightedAPA,String> {

    List<ProductWeightedAPA> findAllByIngredientIdsContaining(String ingredientId);
    List<ProductWeightedAPA> findAllByCategoryIdAndActiveTrue(String categoryId);

    List<ProductWeightedAPA> findAllByCategoryIdAndActiveTrue(String categoryId, Sort sort);

}
