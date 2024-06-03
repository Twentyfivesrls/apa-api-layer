package com.twentyfive.apaapilayer.utils;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;

@AllArgsConstructor
public class StompUtilities {
    private static final String NEW_ORDER_MESSAGE="Un nuovo ordine è arrivato!";
    private static final String CANCEL_ORDER_MESSAGE="l'ordine con ID %s è stato cancellato!";
    private static final String NEW_ORDER_CHANNEL ="/new_apa_order";
    private static final String CANCEL_ORDER_CHANNEL ="/cancel_apa_order";
    public static String sendNewOrderNotification(){
        TwentyfiveMessage twentyfiveMessage= new TwentyfiveMessage(NEW_ORDER_CHANNEL,NEW_ORDER_MESSAGE);
        Gson gson = new Gson();
        return gson.toJson(twentyfiveMessage);
    }

    public static String sendCancelOrderNotification(String id){
        String cancelOrderMessage = String.format(CANCEL_ORDER_MESSAGE, id);
        TwentyfiveMessage twentyfiveMessage= new TwentyfiveMessage(CANCEL_ORDER_CHANNEL,cancelOrderMessage);
        Gson gson = new Gson();
        return gson.toJson(twentyfiveMessage);
    }
}
