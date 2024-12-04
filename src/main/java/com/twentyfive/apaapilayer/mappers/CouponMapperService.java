package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.dtos.CouponDTO;
import com.twentyfive.apaapilayer.dtos.CouponDetailsDTO;
import com.twentyfive.apaapilayer.dtos.NumberRangeDTO;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.FixedAmountCoupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PercentageCoupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.LocalDateRange;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.NumberRange;

import java.util.ArrayList;
import java.util.List;

@Service
public class CouponMapperService {

    public List<CouponDTO> mapCouponsToDTO(List<Coupon> coupons) {
        List<CouponDTO> couponDTOs = new ArrayList<>();
        for (Coupon coupon : coupons) {
            CouponDTO couponDTO = mapCouponToDTO(coupon);
            couponDTOs.add(couponDTO);
        }
        return couponDTOs;
    }

    public CouponDTO mapCouponToDTO(Coupon coupon) {
        CouponDTO couponDTO = new CouponDTO();
        couponDTO.setId(coupon.getId());
        couponDTO.setActive(coupon.isActive());
        couponDTO.setName(coupon.getName());
        couponDTO.setCode(coupon.getCode());
        couponDTO.setValidationPeriod(validationPeriod(coupon.getDates()));
        couponDTO.setValue(valueFromChild(coupon));
        couponDTO.setPriceRange(priceRange(coupon.getPriceRange()));
        couponDTO.setMaxTotalUsage(validationMaxTotalUsage(coupon.getMaxTotalUsage()));
        return couponDTO;
    }

    public CouponDetailsDTO mapCouponToDetailsDTO(Coupon coupon, List<CategoryMinimalDTO> categories) {
        CouponDetailsDTO couponDTO = new CouponDetailsDTO();
        couponDTO.setId(coupon.getId());
        couponDTO.setActive(coupon.isActive());
        couponDTO.setName(coupon.getName());
        couponDTO.setCode(coupon.getCode());
        couponDTO.setDates(coupon.getDates());
        couponDTO.setPriceRange(mapNumberRangeToDTO(coupon.getPriceRange()));
        couponDTO.setUsageCount(coupon.getUsageCount());
        couponDTO.setMaxTotalUsage(coupon.getMaxTotalUsage());
        couponDTO.setMaxUsagePerCustomer(coupon.getMaxUsagePerCustomer());
        couponDTO.setType(typeFromChild(coupon));
        couponDTO.setValue(valueFromChild(coupon));
        couponDTO.setSpecificCategories(categories);
        couponDTO.setHome(coupon.getHome()!=null ? coupon.getHome() : null);
        return couponDTO;
    }

    private String priceRange(NumberRange numberRange) {
        String priceRange = "";
        if (numberRange == null) {
            priceRange = "-";
        } else {
            if (numberRange.getMin() == null){
                priceRange = "< "+numberRange.getMax();
            }
            else if (numberRange.getMax() == null){
                priceRange = "> "+numberRange.getMin();
            } else {
                priceRange = numberRange.getMin()+"€- "+numberRange.getMax()+" €";
            }
        }
        return priceRange;
    }

    private NumberRangeDTO mapNumberRangeToDTO(NumberRange numberRange) {
        NumberRangeDTO numberRangeDTO = new NumberRangeDTO();
        numberRangeDTO.setMin(numberRange.getMin() != null ? numberRange.getMin()+" €" : "-");
        numberRangeDTO.setMax(numberRange.getMax() != null ? numberRange.getMax()+" €" : "-");
        return numberRangeDTO;
    }

    private String validationPeriod(LocalDateRange dateRange){
        String validationPeriod = "";
        if(dateRange == null){
            validationPeriod = "∞";
        } else {
            if( dateRange.getStartDate() == null) {
                validationPeriod = "Fino al "+ dateRange.getEndDate();
            }
            else if (dateRange.getEndDate() == null) {
                validationPeriod = "Dal " + dateRange.getStartDate();
            } else {
                validationPeriod = dateRange.getStartDate() + " - " + dateRange.getEndDate();
            }
        }
        return validationPeriod;
    }

    private String validationMaxTotalUsage(Integer maxTotalUsage) {
        String validationMaxTotalUsage;
        if(maxTotalUsage == null){
            validationMaxTotalUsage = "-";
        } else {
            validationMaxTotalUsage = maxTotalUsage.toString();
        }
        return validationMaxTotalUsage;
    }

    private String valueFromChild(Coupon coupon){
        String value = "";
        if(coupon instanceof PercentageCoupon){
            PercentageCoupon percentageCoupon = (PercentageCoupon) coupon;
            value = percentageCoupon.getPercentage() + "%";
        } else if (coupon instanceof FixedAmountCoupon){
            FixedAmountCoupon amountCoupon = (FixedAmountCoupon) coupon;
            value = "€ " + amountCoupon.getFixedAmount();
        }
        return value;
    }

    private String typeFromChild(Coupon coupon){
        String value = "";
        if(coupon instanceof FixedAmountCoupon){
            value = "fixed";
        } else if (coupon instanceof PercentageCoupon){
            value = "percentage";
        }
        return value;
    }

}
