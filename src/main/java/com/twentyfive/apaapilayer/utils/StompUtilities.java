package com.twentyfive.apaapilayer.utils;

import com.google.gson.Gson;
import com.twentyfive.apaapilayer.dtos.StompMessage;
import lombok.AllArgsConstructor;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

@AllArgsConstructor
public class StompUtilities {


    private static final String ADMIN_CHANNEL ="/admin_apa";
    private static final String BAKER_CHANNEL ="/baker_apa";
    private static final String COUNTER_CHANNEL ="/counter_apa";
    private static final String CUSTOMER_CHANNEL ="/%s";

    private static final String DEL_ORDER_MESSAGE="L'ordine di %s con ID %s è stato cancellato!";
    private static final String NEW_ORDER_MESSAGE ="Un nuovo ordine è arrivato!";

    private static final String UPDATE_MESSAGE ="Un prodotto nell'ordine %s è stato spostato in %s!";
    private static final String UPDATE_BAKER_MESSAGE ="Un prodotto nell'ordine %s è da preparare!";
    private static final String UPDATE_COUNTER_MESSAGE ="Un prodotto nell'ordine %s è stato inserito in un luogo!";

    private static final String MOVE_BAKER_MESSAGE="Un prodotto in preparazione dell'ordine %s è stato spostato in %s!";
    private static final String CUSTOMER_ORDER_CHANNEL ="/%s";

    public static String sendChangedStatusNotification(OrderStatus status, String customerId){
        String customerMessage = String.format(CUSTOMER_ORDER_CHANNEL,customerId);
        TwentyfiveMessage twentyfiveMessage= new TwentyfiveMessage(customerMessage,"");
        switch(status){
            case ANNULLATO -> twentyfiveMessage.setContent("ANNULLATO");
            case RICEVUTO -> twentyfiveMessage.setContent("RICEVUTO");
            case IN_PREPARAZIONE -> twentyfiveMessage.setContent("IN_PREPARAZIONE");
            case IN_PASTICCERIA -> twentyfiveMessage.setContent("IN_PASTICCERIA");
            case PRONTO -> twentyfiveMessage.setContent("PRONTO");
            case COMPLETO -> twentyfiveMessage.setContent("COMPLETO");
        }
        Gson gson = new Gson();
        return gson.toJson(twentyfiveMessage);
    }

    public static TwentyfiveMessage sendAdminNewNotification(){
        StompMessage stompMessage = new StompMessage(NEW_ORDER_MESSAGE,true);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(ADMIN_CHANNEL,stompMessage);
        return twentyfiveMessage;
    }

    public static TwentyfiveMessage sendBakerNewNotification(){
        StompMessage stompMessage = new StompMessage(NEW_ORDER_MESSAGE,true);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(BAKER_CHANNEL,stompMessage);
        return twentyfiveMessage;
    }


    public static TwentyfiveMessage sendAdminDeleteNotification(String fullName,String orderId){
        String adminMessage = String.format(DEL_ORDER_MESSAGE,fullName,orderId);
        StompMessage stompMessage = new StompMessage(adminMessage,false);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(ADMIN_CHANNEL,stompMessage);
        return twentyfiveMessage;
    }

    public static TwentyfiveMessage sendBakerDeleteNotification(String fullName,String orderId){
        String bakerMessage = String.format(DEL_ORDER_MESSAGE,fullName,orderId);
        StompMessage stompMessage = new StompMessage(bakerMessage,false);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(BAKER_CHANNEL,stompMessage);
        return twentyfiveMessage;
    }

    public static TwentyfiveMessage sendAdminMoveNotification(String idOrder, String location){
        String adminMessage = String.format(UPDATE_MESSAGE,idOrder,location);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(ADMIN_CHANNEL,adminMessage);
        return twentyfiveMessage;
    }

    public static TwentyfiveMessage sendBakerMoveNotification(String idOrder, String location){
        String bakerMessage = String.format(MOVE_BAKER_MESSAGE,idOrder,location);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(BAKER_CHANNEL,bakerMessage);
        return twentyfiveMessage;
    }
    public static TwentyfiveMessage sendBakerUpdateNotification(String idOrder, String location){
        String bakerMessage = String.format(UPDATE_BAKER_MESSAGE,idOrder,location);
        StompMessage stompMessage = new StompMessage(bakerMessage,true);
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(BAKER_CHANNEL,stompMessage);
        return twentyfiveMessage;
    }

    public static TwentyfiveMessage sendCustomerNotification(String id) {
        String customerTopic = String.format(CUSTOMER_ORDER_CHANNEL,id);
        String message = "updateCart";
        TwentyfiveMessage twentyfiveMessage = new TwentyfiveMessage(customerTopic,message);
        return twentyfiveMessage;
    }
}
