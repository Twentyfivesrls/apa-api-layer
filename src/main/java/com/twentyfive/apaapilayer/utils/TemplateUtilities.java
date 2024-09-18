package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.dtos.SummaryEmailDTO;

import java.util.HashMap;
import java.util.Map;

public class TemplateUtilities {

    public static Map<String,Object> populateEmail(String customerName,String orderId){
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerName",customerName);
        variables.put("orderId",orderId);
        return variables;
    }

    public static Map<String,Object> populateEmailForNewOrder(String customerName, SummaryEmailDTO summaryEmailDTO){
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerName",customerName);
        variables.put("orderId",summaryEmailDTO.getId());
        variables.put("productList",summaryEmailDTO.getProducts());
        variables.put("totalPrice",summaryEmailDTO.getTotalPrice());
        return variables;
    }
}
