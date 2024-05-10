package com.twentyfive.apaapilayer.emails;

import com.twentyfive.apaapilayer.clients.EmailClientController;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.apaapilayer.utils.EmailUtilities;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;
import java.util.Optional;

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

    private final String subjectReset ="Account creato, resetta la tua password!";
    //TODO creazione template Reset
    private final String templateReset="";
    public void sendEmailReceived(String id) throws IOException {
            Optional<CustomerAPA> customerAPA = customerRepository.findById(id);
            if(customerAPA.isPresent()){
                EmailSendRequest emailSendRequest = emailUtilities.toEmailSendRequest(templateReceived,subjectReceived,customerAPA.get().getEmail());
                String token = keycloakService.getAccessTokenTF();
                String authorizationHeader = "Bearer " + token;
                emailClientController.sendMail(authorizationHeader, emailSendRequest);
            }
    }
    public void sendEmailResetPassword(String email, String temporaryPassword) throws  IOException {
        EmailSendRequest emailSendRequest = emailUtilities.toEmailSendRequest(templateReset,templateReceived,email);
        String token = keycloakService.getAccessTokenTF();
        String authorizationHeader = "Bearer " + token;
        emailClientController.sendMail(authorizationHeader, emailSendRequest);
    }
}
