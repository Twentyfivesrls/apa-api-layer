package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GlobalStatRepository extends MongoRepository<GlobalStatAPA, LocalDate> {
    @Query("{ '_id': { $gte: ?0, $lte: ?1 } }")
    List<GlobalStatAPA> findByIdBetweenInclusive(LocalDate startDate, LocalDate endDate);
}
