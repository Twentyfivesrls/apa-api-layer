package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.IngredientAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends MongoRepository<IngredientAPA,String> {

    List<IngredientAPA> findAllByCategoryId(String id);
    List<IngredientAPA> findAllByCategoryIdAndActiveTrueOrderByNameAsc(String idCategory);

    List<IngredientAPA> findByNameContainsIgnoreCase(String name);

    List<IngredientAPA> findByIdIn(List<String> ids);

    Optional<IngredientAPA> findByName(String name);

    List<IngredientAPA> findAllByNameIn(List<String> ingredientNames);

    @Query("{ 'categoryId': ?0, 'allergenNames': { $in: ?1 } }")
    List<IngredientAPA> findAllByCategoryIdAndAllergenNames(String categoryId, List<String> allergenNames);
}
