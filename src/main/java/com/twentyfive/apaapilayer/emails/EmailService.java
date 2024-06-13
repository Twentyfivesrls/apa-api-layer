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
    private final String subjectReceived = " Abbiamo ricevuto il tuo ordine n.%s! ";

    private final String templateCanceled = "orderCanceled";
    private final String subjectCanceled = "Abbiamo annullato il tuo ordine n.%s!";

    private final String templatePreparation = "orderPreparation";
    private final String subjectPreparation = "Il tuo ordine n.%s è in preparazione!";

    private final String templateReady = "orderReady";
    private final String subjectReady = "Il tuo ordine n.%s è pronto!";

    public void sendEmail(String email, OrderStatus type, Map<String, Object> variables) throws IOException {
        String token = keycloakService.getAccessTokenTF();
        String authorizationHeader = "Bearer " + token;
        String content;
        String subject;
        String templateName;
        EmailSendRequest emailSendRequest;
        switch (type) {
            case ANNULLATO -> {
                subject = String.format(subjectCanceled,variables.get("orderId"));
                templateName=templateCanceled;
            }
            case RICEVUTO -> {
                subject = String.format(subjectReceived,variables.get("orderId"));
                templateName=templateReceived;
            }
            case IN_PREPARAZIONE -> {
                subject = String.format(subjectPreparation,variables.get("orderId"));
                templateName=templatePreparation;
            }
            case PRONTO -> {
                subject = String.format(subjectReady,variables.get("orderId"));
                templateName=templateReady;
            }
            default -> throw new IllegalArgumentException("Unknown order status: " + type);
        }
        content = generateContent(templateName, variables); //FIXME
        emailSendRequest = emailUtilities.toEmailSendRequest(content, subject, email);
        emailClientController.sendMail(authorizationHeader, emailSendRequest);
    }

    public String generateContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
