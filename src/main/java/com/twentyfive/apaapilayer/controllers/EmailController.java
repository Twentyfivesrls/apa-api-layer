package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.emails.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.IOException;

@RestController
@RequestMapping("/emails")
@CrossOrigin(origins = "*")
public class EmailController {
    private final EmailService emailService;
    public EmailController(EmailService emailService){
        this.emailService=emailService;
    }

    @PostMapping("/send/{email}")
    public ResponseEntity<String> sendEmail(@PathVariable String email) throws IOException {
        emailService.sendEmail(email, OrderStatus.RICEVUTO);
        return ResponseEntity.ok().body("email inviata!");
    }
}
