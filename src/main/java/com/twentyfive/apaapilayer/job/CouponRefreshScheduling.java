package com.twentyfive.apaapilayer.job;

import com.twentyfive.apaapilayer.repositories.CouponRepository;
import com.twentyfive.apaapilayer.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.util.List;

@Component
@RequiredArgsConstructor

public class CouponRefreshScheduling {
    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 30 0 * * *")
    public void checkExpiredCoupons(){
        List<Coupon> coupons = couponRepository.findAllByExpired(false);
        for (Coupon coupon : coupons) {
            if(coupon.checkExpiredCoupon()){
                coupon.setExpired(true);
                couponRepository.save(coupon);
            }
        }
    }
}
