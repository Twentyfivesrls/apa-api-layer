package com.twentyfive.apaapilayer.emails;

import com.twentyfive.apaapilayer.clients.EmailClientController;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.apaapilayer.utils.EmailUtilities;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;

@Service
@Data
@RequiredArgsConstructor
public class EmailService {
    private final EmailClientController emailClientController;
    private final KeycloakService keycloakService;
    private final EmailUtilities emailUtilities;


    private final String templateReceived = "templates/orderReceived.html";
    private final String subjectReceived ="Il tuo ordine è arrivato!";

    private final String templateCanceled = "templates/orderCanceled.html";
    private final String subjectCanceled = "Il tuo è ordine stato cancellato!";

    private final String templatePreparation = "templates/orderPreparation.html";
    private final String subjectPreparation = "Il tuo ordine è in preparazione!";

    private final String templateReady = "templates/orderReady.html";
    private final String subjectReady = "Il tuo ordine è pronto!";

    public void sendEmail(String email, OrderStatus type) throws IOException {
        String token = keycloakService.getAccessTokenTF();
        String authorizationHeader = "Bearer " + token;
        EmailSendRequest emailSendRequest = new EmailSendRequest();
        switch(type){
            case ANNULLATO -> emailSendRequest = emailUtilities.toEmailSendRequest(templateCanceled,subjectCanceled,email);
            case RICEVUTO -> emailSendRequest = emailUtilities.toEmailSendRequest(templateReceived,subjectReceived,email);
            case IN_PREPARAZIONE -> emailSendRequest = emailUtilities.toEmailSendRequest(templatePreparation,subjectPreparation,email);
            case PRONTO -> emailSendRequest = emailUtilities.toEmailSendRequest(templateReady,subjectPreparation,email);
        }
        emailClientController.sendMail(authorizationHeader, emailSendRequest);
    }
}
