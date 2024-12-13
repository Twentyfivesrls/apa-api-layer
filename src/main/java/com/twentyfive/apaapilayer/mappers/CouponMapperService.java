package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.dtos.CouponDTO;
import com.twentyfive.apaapilayer.dtos.CouponDetailsDTO;
import com.twentyfive.apaapilayer.dtos.NumberRangeDTO;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.AppliedCoupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.FixedAmountCoupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PercentageCoupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.LocalDateRange;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.NumberRange;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public AppliedCoupon mapAppliedCouponFromCoupon(Coupon coupon,double discount,List<CategoryAPA> categories, boolean totalOrderDiscount){
        AppliedCoupon appliedCoupon = new AppliedCoupon();
        appliedCoupon.setCode(coupon.getCode());
        appliedCoupon.setValue(valueFromChild(coupon));
        appliedCoupon.setDiscountValue(discountFromCoupon(discount));
        appliedCoupon.setDescription(descriptionFromCoupon(categories));
        appliedCoupon.setTotalOrderDiscount(totalOrderDiscount);
        return appliedCoupon;
    }

    private String descriptionFromCoupon(List<CategoryAPA> categories) {
        if (categories.isEmpty()) {
            return "Totale";
        }

        StringBuilder description = new StringBuilder();
        for (CategoryAPA category : categories) {
            description.append(category.getName()).append(", ");
        }

        // Rimuove la virgola finale
        description.setLength(description.length() - 2);

        return description.toString();
    }

    private String discountFromCoupon(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(','); // Imposta la virgola come separatore decimale
        DecimalFormat decimalFormat = new DecimalFormat("0.00", symbols);
        return "€ " + decimalFormat.format(value);
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
        if (numberRange == null) {
            return null;
        }
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
    public double discountNumber(String discountApplied){
        return Double.parseDouble(discountApplied
                .replaceAll("[^\\d.,-]", "") // Rimuovi caratteri non numerici
                .replace(",", "."));        // Converti le virgole in punti
    }

}
