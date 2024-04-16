package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.IngredientAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends MongoRepository<IngredientAPA,String> {
}
