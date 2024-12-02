package com.twentyfive.apaapilayer.utils;

import com.google.gson.Gson;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.subscription.model.DeleteUserRoleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
public class EmailUtilities {
    private final KeycloakService keycloakService;
    public EmailSendRequest toEmailSendRequest(String template, String subject, String email) throws IOException {
        EmailSendRequest emailSendRequest = new EmailSendRequest();
        emailSendRequest.setTo(email);
        emailSendRequest.setText(template); //da indagare
        emailSendRequest.setHtmlContent(template);
        emailSendRequest.setSubject(subject);
        emailSendRequest.setAttributes(keycloakService.getEmailSettings());
        return emailSendRequest;
    }

    public String toEmailSendKafka(String template, String subject, String email) throws IOException {
        Gson gson = new Gson();
        EmailSendRequest emailSendRequest = toEmailSendRequest(template, subject, email);
        return gson.toJson(emailSendRequest);
    }
}
