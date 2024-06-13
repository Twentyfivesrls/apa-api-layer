package com.twentyfive.apaapilayer.emails;

import com.twentyfive.apaapilayer.clients.EmailClientController;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.apaapilayer.utils.EmailUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailClientController emailClientController;
    private final KeycloakService keycloakService;
    private final EmailUtilities emailUtilities;
    private final TemplateEngine templateEngine;

    private final String templateReceived = "orderReceived";
    private final String subjectReceived = "Il tuo ordine è arrivato!";

    private final String templateCanceled = "orderCanceled";
    private final String subjectCanceled = "Il tuo ordine è stato cancellato!";

    private final String templatePreparation = "orderPreparation";
    private final String subjectPreparation = "Il tuo ordine è in preparazione!";

    private final String templateReady = "orderReady";
    private final String subjectReady = "Il tuo ordine è pronto!";

    public void sendEmail(String email, OrderStatus type, Map<String, Object> variables) throws IOException {
        String token = keycloakService.getAccessTokenTF();
        String authorizationHeader = "Bearer " + token;
        String content;
        EmailSendRequest emailSendRequest;
        switch (type) {
            case ANNULLATO -> {
                content = generateContent(templateCanceled, variables);
                emailSendRequest = emailUtilities.toEmailSendRequest(content, subjectCanceled, email);
            }
            case RICEVUTO -> {
                content = generateContent(templateReceived, variables);
                emailSendRequest = emailUtilities.toEmailSendRequest(content, subjectReceived, email);
            }
            case IN_PREPARAZIONE -> {
                content = generateContent(templatePreparation, variables);
                emailSendRequest = emailUtilities.toEmailSendRequest(content, subjectPreparation, email);
            }
            case PRONTO -> {
                content = generateContent(templateReady, variables);
                emailSendRequest = emailUtilities.toEmailSendRequest(content, subjectReady, email);
            }
            default -> throw new IllegalArgumentException("Unknown order status: " + type);
        }
        emailClientController.sendMail(authorizationHeader, emailSendRequest);
    }

    public String generateContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
