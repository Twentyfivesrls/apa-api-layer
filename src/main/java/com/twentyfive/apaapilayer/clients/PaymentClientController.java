package com.twentyfive.apaapilayer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.PaypalCredentials;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.SimpleOrderRequest;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.util.Map;

@FeignClient(name = "PaymentClientController", url = "${twentyfive.groypal.daemon.url}/payments")
public interface PaymentClientController {

    @PostMapping("/outside")
    ResponseEntity<Map<String,Object>> pay(@RequestHeader("Authorization") String accessToken, @RequestBody SimpleOrderRequest simpleOrderRequest, @RequestHeader("Payment-App-Id") String paymentAppId);

    @PostMapping("/capture-outside/{orderId}")
    ResponseEntity<Map<String,Object>> capture(@RequestHeader("Authorization")String accessToken, @PathVariable("orderId") String orderId, @RequestBody PaypalCredentials paypalCredentials);
}