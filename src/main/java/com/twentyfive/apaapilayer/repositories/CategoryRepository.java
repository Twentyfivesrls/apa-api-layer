package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryAPA,String> {

    List<CategoryAPA> findAllByTypeInAndEnabledTrue(List<String> types);
    List<CategoryAPA> findAllByTypeAndEnabledTrue(String type);


    Optional<CategoryAPA> findByName(String name);



}
