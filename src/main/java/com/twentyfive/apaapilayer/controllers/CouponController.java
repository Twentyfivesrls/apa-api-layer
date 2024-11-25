package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.dtos.CouponDTO;
import com.twentyfive.apaapilayer.dtos.CouponDetailsDTO;
import com.twentyfive.apaapilayer.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<CouponDTO>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "25") int size,
                                                  @RequestParam(value = "sortColumn", defaultValue = "") String sortColumn,
                                                  @RequestParam(value = "sortDirection", defaultValue = "") String sortDirection) {
        return ResponseEntity.ok().body(couponService.getAll(page,size,sortColumn,sortDirection));

    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<CouponDetailsDTO> getById(@RequestParam(value = "id") String id) {
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
}
