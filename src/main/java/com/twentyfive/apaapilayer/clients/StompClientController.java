package com.twentyfive.apaapilayer.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;

@FeignClient(name = "StompClientController", url="${twentyfive.stomp.url}")
public interface StompClientController {

    @PostMapping("/sendMessage")
    ResponseEntity sendObjectMessage(@RequestBody TwentyfiveMessage message);

}
