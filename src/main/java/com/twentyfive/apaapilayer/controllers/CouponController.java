package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.filters.CouponFilter;
import com.twentyfive.apaapilayer.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

import java.io.IOException;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ROLE_admin')")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/getAll")
    public ResponseEntity<Page<CouponDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "25") int size,
                                                  @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                  @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection,
                                                  @RequestBody(required = false) CouponFilter filters) {
        return ResponseEntity.ok().body(couponService.getAll(page,size,sortColumn,sortDirection,filters));

    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<CouponDetailsDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok().body(couponService.getById(id));
    }
    @PostMapping("/save")
    public ResponseEntity<Coupon> save(@RequestBody Coupon coupon) {
        return ResponseEntity.ok().body(couponService.save(coupon));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable String id) {
        return ResponseEntity.ok().body(couponService.deleteById(id));
    }

    @GetMapping("/changeStatus/{id}")
    public ResponseEntity<Boolean> changeStatus(@PathVariable String id) {
        return ResponseEntity.ok().body(couponService.changeStatus(id));
    }

    @PreAuthorize("hasRole('ROLE_customer') or hasRole('ROLE_admin')")
    @PostMapping("/checkCoupon")
    public ResponseEntity<SummaryOrderDTO> checkCoupon(@RequestBody CouponValidationReq couponValidationReq) throws IOException {
        return ResponseEntity.ok().body(couponService.checkCoupon(couponValidationReq));
    }

    @PostMapping("/sendCoupon")
    public ResponseEntity<Boolean> sendCoupon(@RequestBody SendCouponReq sendCouponReq) throws IOException {
        return ResponseEntity.ok().body(couponService.sendCoupon(sendCouponReq));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/homeCoupon")
    public ResponseEntity<HomeCouponDTO> homeCoupon() {
        return ResponseEntity.ok().body(couponService.randomCouponWithHome());
    }
}
