package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.IngredientAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends MongoRepository<IngredientAPA,String> {

    List<IngredientAPA> findAllByCategoryId(String id);
    List<IngredientAPA> findAllByCategoryIdInAndActiveTrue(List<String> idCategories);


    Optional<IngredientAPA> findByName(String name);
}
