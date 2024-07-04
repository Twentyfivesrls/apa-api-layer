package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<CustomerAPA,String> {
    Optional<CustomerAPA> findByIdKeycloak(String idKeycloak);
    Page<CustomerAPA> findAllByRoleAndIdKeycloakIsNotNull(String role,Pageable pageable);
    Page<CustomerAPA> findAllByRoleNotAndIdKeycloakIsNotNull(String role, Pageable pageable);


}
