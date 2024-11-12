package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<CustomerAPA,String> {
    Optional<CustomerAPA> findByIdKeycloak(String idKeycloak);

    @Query("{ '$and': [ " +
            "{ 'role': ?0 }, " +
            "{ 'idKeycloak': { '$ne': null } }, " +
            "{ '$or': [ " +
            "{ '$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$lastName'] }, 'regex': ?1, 'options': 'i' } } }, " +
            "{ '$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$middleName', ' ', '$lastName'] }, 'regex': ?1, 'options': 'i' } } }, " +
            "{ 'firstName': { '$regex': ?1, '$options': 'i' } }, " +
            "{ 'lastName': { '$regex': ?1, '$options': 'i' } } " +
            "] } " +
            "] }")
    Page<CustomerAPA> findByRoleAndIdKeycloakIsNotNullAndFullNameOrFirstNameOrLastName(String role,String regex,Pageable pageable);

    @Query("{ '$and': [ " +
            "{ 'role': { '$nin': ?0 } }, " +
            "{ 'idKeycloak': { '$ne': null } }, " +
            "{ '$or': [ " +
            "{ '$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$lastName'] }, 'regex': ?1, 'options': 'i' } } }, " +
            "{ '$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$middleName', ' ', '$lastName'] }, 'regex': ?1, 'options': 'i' } } }, " +
            "{ 'firstName': { '$regex': ?1, '$options': 'i' } }, " +
            "{ 'lastName': { '$regex': ?1, '$options': 'i' } } " +
            "] } " +
            "] }")
    Page<CustomerAPA> findAllByRoleNotInAndIdKeycloakIsNotNullAndFullNameOrFirstNameOrLastName(List<String> roles,String regex, Pageable pageable);



    @Query("{ '$or': [ " +
            "{'$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$lastName'] }, 'regex': ?0, 'options': 'i' } } }, " +
            "{'$expr': { '$regexMatch': { 'input': { '$concat': ['$firstName', ' ', '$middleName', ' ', '$lastName'] }, 'regex': ?0, 'options': 'i' } } }, " +
            "{'firstName': { $regex: ?0, $options: 'i' }}, " +
            "{'lastName': { $regex: ?0, $options: 'i' }}" +
            "] }")
    List<CustomerAPA> findByFullNameOrFirstNameOrLastName(String regex);
}
