package com.twentyfive.apaapilayer.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    List<Coupon> findAllByExpiredAndSoftDeletedFalse(boolean expired);
    Optional<Coupon> findByCodeAndSoftDeletedFalse(String code);
    List<Coupon> findAllByExpiredFalseAndSoftDeletedFalseAndHomeIsNotNull();
    boolean existsByCodeAndSoftDeletedFalseAndIdNot(String code,String id);
}
