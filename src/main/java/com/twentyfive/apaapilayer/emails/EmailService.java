package com.twentyfive.apaapilayer.emails;

import com.google.gson.Gson;
import com.twentyfive.apaapilayer.clients.EmailClientController;
import com.twentyfive.apaapilayer.configurations.ProducerPool;
import com.twentyfive.apaapilayer.dtos.SendCouponReq;
import com.twentyfive.apaapilayer.services.KeycloakService;
import com.twentyfive.apaapilayer.utils.EmailUtilities;
import com.twentyfive.subscription.model.DeleteUserRoleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;
import twentyfive.twentyfiveadapter.models.twentyfiveEmailModels.EmailSendRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailClientController emailClientController;
    private final KeycloakService keycloakService;
    private final EmailUtilities emailUtilities;
    private final TemplateEngine templateEngine;
    private final ProducerPool producerPool;

    private final String EMAIL_COUPON_TOPIC = "send_email_topic";

    private final String templateReceived = "orderReceived";
    private final String subjectReceived = " Abbiamo ricevuto il tuo ordine n.%s! ";

    private final String templateCanceled = "orderCanceled";
    private final String subjectCanceled = "Il tuo ordine n.%s è stato annullato!";

    private final String templatePreparation = "orderPreparation";
    private final String subjectPreparation = "Il tuo ordine n.%s è in preparazione!";

    private final String templateReady = "orderReady";
    private final String subjectReady = "Il tuo ordine n.%s è pronto!";

    private final String templateCoupon = "sendCoupon";

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

    public void sendCoupon(SendCouponReq sendCouponReq) throws IOException {
        String imgUrl = "http://80.211.123.141:8106/TwentyfiveMediaManager/twentyfiveserver/downloadkkk/apa/coupon/no_img_coupon.png";
        if(sendCouponReq.getMessage().getImgUrl()!= null){
            imgUrl = sendCouponReq.getMessage().getImgUrl();
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", sendCouponReq.getCode());
        variables.put("content", sendCouponReq.getMessage().getDescription());
        variables.put("imgUrl", imgUrl);
        String content = generateContent(templateCoupon, variables);
        String subject = sendCouponReq.getMessage().getTitle();
        for (String email : sendCouponReq.getEmails()) {
            try {
                String message = emailUtilities.toEmailSendKafka(content,subject,email);
                this.producerPool.send(message, 1, EMAIL_COUPON_TOPIC);
            } catch (Exception e) {

            }
        }

    }

    public String generateContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
