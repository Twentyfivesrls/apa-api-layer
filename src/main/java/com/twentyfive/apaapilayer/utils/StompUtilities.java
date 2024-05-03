package com.twentyfive.apaapilayer.utils;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import twentyfive.twentyfiveadapter.dto.stompDto.TwentyfiveMessage;

@AllArgsConstructor
public class StompUtilities {
    private static final String NEW_ORDER_MESSAGE="Un nuovo ordine è arrivato!";
    private static final String NEW_ORDER_CHANNEL="/apa_order";
    public static String sendNewOrderNotification(){
        TwentyfiveMessage twentyfiveMessage= new TwentyfiveMessage(NEW_ORDER_CHANNEL,NEW_ORDER_MESSAGE);
        Gson gson = new Gson();
        return gson.toJson(twentyfiveMessage);
    }
}
