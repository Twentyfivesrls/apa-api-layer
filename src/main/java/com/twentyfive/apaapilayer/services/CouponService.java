package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.CategoryMinimalDTO;
import com.twentyfive.apaapilayer.dtos.CouponDTO;
import com.twentyfive.apaapilayer.dtos.CouponDetailsDTO;
import com.twentyfive.apaapilayer.exceptions.InvalidCouponException;
import com.twentyfive.apaapilayer.mappers.CouponMapperService;
import com.twentyfive.apaapilayer.repositories.CouponRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.ReflectionUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapperService couponMapperService;
    private final CouponRepository couponRepository;

    private final CategoryService categoryService;

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

    public CouponDetailsDTO getById(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        List<CategoryMinimalDTO> categories = new ArrayList<>();
        if(coupon.getSpecificCategoriesId() != null) {
            categories = categoryService.getAllMinimalByListId(coupon.getSpecificCategoriesId());
        }
        return couponMapperService.mapCouponToDetailsDTO(coupon,categories);
    }
    public Coupon save(Coupon coupon) {
        if (coupon.getId() != null) {
            Optional<Coupon> optCouponToUpdate = couponRepository.findById(coupon.getId());

            if (optCouponToUpdate.isPresent()) {
                Coupon couponToUpdate = optCouponToUpdate.get();
                //TODO da vedere se passare da fixedAmount a percentage si spacca (e viceversa)
                ReflectionUtilities.updateNonNullFields(coupon, couponToUpdate);
                return couponRepository.save(couponToUpdate);
            }
        }
        return couponRepository.save(coupon);
    }


    public Boolean deleteById(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        coupon.setSoftDeleted(true);
        couponRepository.save(coupon);
        return true;
    }

    public Boolean changeStatus(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
        return true;
    }

}
