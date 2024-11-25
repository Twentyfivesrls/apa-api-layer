package com.twentyfive.apaapilayer.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.util.List;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    List<Coupon> findAllByExpired(boolean expired);
}
