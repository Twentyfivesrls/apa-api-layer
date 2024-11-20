package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.CouponDTO;
import com.twentyfive.apaapilayer.mappers.CouponMapperService;
import com.twentyfive.apaapilayer.repositories.CouponRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapperService couponMapperService;
    private final CouponRepository couponRepository;

    public Page<CouponDTO> getAll(int page, int size, String sortColumn, String sortDirection) {
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else {
            sort = Sort.by(Sort.Direction.fromString(sortDirection),sortColumn);
        }
        Pageable pageable= PageRequest.of(page,size,sort);
        List<Coupon> coupons = couponRepository.findAll();
        List<CouponDTO> dtos = couponMapperService.mapCouponsToDTO(coupons);
        return PageUtilities.convertListToPageWithSorting(dtos, pageable);
    }
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }


}
