package com.twentyfive.apaapilayer.controllers;

import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.services.KeycloakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/emails")
@CrossOrigin(origins = "*")
public class EmailController {
    private final EmailService emailService;
    private final KeycloakService keycloakService;
    public EmailController(EmailService emailService, KeycloakService keycloakService){
        this.keycloakService=keycloakService;
        this.emailService=emailService;
    }

    @PostMapping("/send/{id}")
    public ResponseEntity<String> sendEmail(@PathVariable String id) throws IOException {
        emailService.sendEmailReceived(id);
        return ResponseEntity.ok().body("email inviata!");
    }
}
