package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductWeightedRepository extends MongoRepository<ProductWeightedAPA,String> {

    List<ProductWeightedAPA> findAllByIngredientIdsContaining(String ingredientId);
    List<ProductWeightedAPA> findAllByCategoryId(String categoryId);
    List<ProductWeightedAPA> findAllByCategoryIdAndNameContainsIgnoreCase(String categoryId,String name);

}
