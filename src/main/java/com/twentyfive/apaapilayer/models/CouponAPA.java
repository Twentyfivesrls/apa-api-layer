package com.twentyfive.apaapilayer.models;

import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;

@Document("coupons")
public class CouponAPA extends Coupon {
}
