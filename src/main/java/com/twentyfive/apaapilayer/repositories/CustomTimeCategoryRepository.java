package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.CustomTimeCategoryAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomTimeCategoryRepository extends MongoRepository<CustomTimeCategoryAPA, String> {
    boolean existsByCategory(CategoryAPA category);

    Optional<CustomTimeCategoryAPA> findByCategory(CategoryAPA category);
}
