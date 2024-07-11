package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryAPA,String> {

    List<CategoryAPA> findAllByTypeInAndEnabledTrueAndSoftDeletedFalseOrderByOrderPriorityAsc(List<String> types);
    List<CategoryAPA> findAllByTypeInAndEnabledFalseAndSoftDeletedFalseOrderByNameAsc(List<String> types);
    List<CategoryAPA> findAllByTypeAndEnabledTrueAndSoftDeletedFalse(String type);
    List<CategoryAPA> findAllByType(String type);

    Optional<CategoryAPA> findByName(String name);


    List<CategoryAPA> findAllByTypeAndNameAndEnabledTrue(String type, String name);

    List<CategoryAPA> findAllByIdSection(String id);
}
