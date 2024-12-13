package com.twentyfive.apaapilayer.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CouponUsage;

import java.util.Optional;

public interface CouponUsageRepository extends MongoRepository<CouponUsage, String> {

    Optional<CouponUsage> findByIdCouponAndIdCustomer(String idCoupon, String idCustomer);
}
