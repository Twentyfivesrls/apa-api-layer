package com.twentyfive.apaapilayer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

@FeignClient(name = "EmailClientController", url = "${twentyfive.email-daemon.url}/mail")
public interface EmailClientController {

    @PostMapping("/send-outside")
    void sendMail(@RequestHeader("Authorization") String accessToken,@RequestBody EmailSendRequest emailSendRequest);
}