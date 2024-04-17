package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.IngredientAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.Optional;

@Repository
public interface AllergenRepository extends MongoRepository<Allergen,String> {
    Optional<Allergen> findByName(String name);
}
