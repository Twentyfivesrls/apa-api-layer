package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.repositories.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CouponUsage;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponUsageService {
    private final CouponUsageRepository couponUsageRepository;


    public CouponUsage save(String idCustomer, String idCoupon) {
        CouponUsage couponUsage;
        Optional<CouponUsage> optCouponUsage = couponUsageRepository.findByIdCouponAndIdCustomer(idCoupon,idCustomer);
        if(optCouponUsage.isPresent()) {
            couponUsage = optCouponUsage.get();
            couponUsage.setUsageCount(couponUsage.getUsageCount()+1);
        } else {
            couponUsage = new CouponUsage();
            couponUsage.setIdCoupon(idCoupon);
            couponUsage.setIdCustomer(idCustomer);
            couponUsage.setUsageCount(1);
        }
        return couponUsageRepository.save(couponUsage);
    }
}
