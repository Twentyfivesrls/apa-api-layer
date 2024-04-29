package com.twentyfive.apaapilayer.emails;

import com.twentyfive.apaapilayer.clients.EmailClientController;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.apaapilayer.utils.EmailUtilities;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;

@Service
@Data
@RequiredArgsConstructor
public class EmailService {
    private final CustomerRepository customerRepository;
    private final EmailClientController emailClientController;
    private final KeycloakService keycloakService;
    private final EmailUtilities emailUtilities;

    private final String templateReceived ="templates/orderSuccessful.html";
    private final String subjectReceived ="Il tuo ordine è arrivato!";
    public void sendEmailReceived(String email) throws IOException {
            EmailSendRequest emailSendRequest = emailUtilities.toEmailSendRequest(templateReceived,subjectReceived,email);
            String token = keycloakService.getAccessTokenTF();
            String authorizationHeader = "Bearer " + token;
            emailClientController.sendMail(authorizationHeader, emailSendRequest);
    }
}
