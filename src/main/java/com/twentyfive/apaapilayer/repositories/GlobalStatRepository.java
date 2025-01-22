package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GlobalStatRepository extends MongoRepository<GlobalStatAPA, LocalDate> {
    List<GlobalStatAPA> findByIdBetween(LocalDate startDate, LocalDate endDate);
}
