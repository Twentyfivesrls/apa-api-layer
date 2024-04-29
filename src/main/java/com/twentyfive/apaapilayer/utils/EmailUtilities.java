package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.services.KeycloakService;
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
    public static String readHtmlFile(ClassPathResource htmlFileResource) throws IOException {
        byte[] htmlBytes = htmlFileResource.getInputStream().readAllBytes();
        return new String(htmlBytes, StandardCharsets.UTF_8);
    }
    public EmailSendRequest toEmailSendRequest(String template, String subject, String email) throws IOException {
        ClassPathResource htmlFileResource = new ClassPathResource(template);
        String htmlContent = readHtmlFile(htmlFileResource);
        EmailSendRequest emailSendRequest = new EmailSendRequest();
        emailSendRequest.setTo(email);
        emailSendRequest.setText(htmlContent);
        emailSendRequest.setHtmlContent(htmlContent);
        emailSendRequest.setSubject(subject);
        emailSendRequest.setAttributes(keycloakService.getEmailSettings());
        return emailSendRequest;
    }
}
