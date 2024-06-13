package com.twentyfive.apaapilayer.utils;

import java.util.HashMap;
import java.util.Map;

public class TemplateUtilities {

    public static Map<String,Object> populateEmail(String customerName,String orderId){
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerName",customerName);
        variables.put("orderId",orderId);
        return variables;
    }
}
