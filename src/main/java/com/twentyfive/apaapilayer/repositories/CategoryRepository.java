package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.OrderAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryAPA,String> {

    @Query("{'type': {$in: ?0}, 'enabled': true}")
    List<CategoryAPA> findByTypeInAndEnabledTrue(List<String> types);




}
